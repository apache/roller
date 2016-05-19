/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Sends a 408 code to the front end for invalid sessions that are not trapped by the CSRF checking in
 * CustomAccessDeniedHandlerImpl (i.e., primarily for Ajax GET calls), so a login page redirect or other
 * appropriate handling can be done.
 *
 * Based on sample originally from DuyHai Doan:
 * https://doanduyhai.wordpress.com/2012/04/21/spring-security-part-vi-session-timeout-handling-for-ajax-calls/
 */
public class AjaxInvalidSessionRedirectFilter extends GenericFilterBean {

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
