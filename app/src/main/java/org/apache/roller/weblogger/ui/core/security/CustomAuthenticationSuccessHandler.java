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
package org.apache.roller.weblogger.ui.core.security;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.util.UrlUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Locale;

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        Object authPrincipal = authentication.getPrincipal();

        UserDetails springUser = (UserDetails) authPrincipal;
        User user = userManager.getEnabledUserByUserName(springUser.getUsername());

        // if authenticated via LDAP but not yet registered in system, redirect to registration page.
        if (authPrincipal instanceof LdapUserDetails) {
            if (user == null) {
                String redirectUrl = UrlUtils.buildFullRequestUrl(request.getScheme(), request.getServerName(),
                        new PortResolverImpl().getServerPort(request), "/tightblog/tb-ui/app/register", null);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
        }

        // Normal login sets the users locale via /tb-ui/app/login-redirect, below is for the case where the
        // user's session has expired (and login-redirect is usually not triggered).
        //
        // The Locale.getAvailableLocales() used to populate the locale list uses underscore separators (en_US)
        // while forLanguageTag() requires the BCP 47 style hyphens (en-US).  For now, just replacing the
        // separator, should be sufficient for our purposes.
        Locale locale = Locale.forLanguageTag(user.getLocale().replace('_', '-'));
        request.getSession().setAttribute("WW_TRANS_I18N_LOCALE", locale);

        super.onAuthenticationSuccess(request, response, authentication);

        user.setLastLogin(Instant.now());
        userManager.saveUser(user);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        // If user went to login page directly instead of being routed there by Spring, the
        // destination page will remain the login page.  In such circumstances, forward on to the
        // Menu page instead.
        String targetUrl = request.getHeader("Referer");
        if (targetUrl != null) {
            if (targetUrl.endsWith("/login")) {
                return targetUrl.substring(0, targetUrl.lastIndexOf("/login")) + "/home";
            } else if (targetUrl.contains("/login?activationCode=")) {
                // here, login page via email activation of account.
                return targetUrl.substring(0, targetUrl.lastIndexOf("/login?activationCode=")) + "/home";
            }
        }

        return super.determineTargetUrl(request, response);
    }
}
