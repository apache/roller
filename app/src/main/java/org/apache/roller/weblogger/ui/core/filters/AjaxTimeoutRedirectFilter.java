package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Based on sample originally from DuyHai Doan:
 * https://doanduyhai.wordpress.com/2012/04/21/spring-security-part-vi-session-timeout-handling-for-ajax-calls/
 * Apache licensed in his Tatami fork: https://github.com/doanduyhai/tatami.
 */
public class AjaxTimeoutRedirectFilter extends GenericFilterBean {

    private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();
    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            Throwable[] causeChain = throwableAnalyzer.determineCauseChain(ex);

            // Trap AccessDeniedExceptions that *don't* have a corresponding AuthenticationException --
            // the latter will normally occur just on the non-Ajax login form, so no need to check further.
            RuntimeException ase = (AuthenticationException)
                    throwableAnalyzer.getFirstThrowableOfType(AuthenticationException.class, causeChain);

            if (ase != null) {
                throw ase;
            }

            ase = (AccessDeniedException) throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);

            if (ase != null && ase instanceof AccessDeniedException) {
                if (authenticationTrustResolver.isAnonymous(SecurityContextHolder.getContext().getAuthentication())) {
                    // User session expired or not logged in yet
                    String ajaxHeader = ((HttpServletRequest) request).getHeader("X-Requested-With");

                    if ("XMLHttpRequest".equals(ajaxHeader)) {
                        // Ajax call detected, return TIMEOUT (408).
                        HttpServletResponse resp = (HttpServletResponse) response;
                        resp.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
                    } else {
                        // non-Ajax call, just redirect to login as usual
                        throw ase;
                    }
                } else {
                    // user already authenticated but simply lacks access to the resource
                    throw ase;
                }
            } else {
                // some other exception
                throw ex;
            }

        }
    }

    private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {
        protected void initExtractorMap() {
            super.initExtractorMap();

            registerExtractor(ServletException.class, (throwable) -> {
                ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
                return ((ServletException) throwable).getRootCause();
            });
        }
    }

}
