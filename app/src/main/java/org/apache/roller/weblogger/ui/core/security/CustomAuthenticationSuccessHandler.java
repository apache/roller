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

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        Object authPrincipal = authentication.getPrincipal();

        // if authenticated via LDAP but not yet registered in system, redirect to registration page.
        if (authPrincipal instanceof LdapUserDetails) {
            UserDetails springUser = (UserDetails) authPrincipal;
            User user = userManager.getUserByUserName(springUser.getUsername());

            if (user == null) {
                String redirectUrl = UrlUtils.buildFullRequestUrl(request.getScheme(), request.getServerName(),
                        new PortResolverImpl().getServerPort(request), "/tightblog/tb-ui/register.rol", null);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
        }

        super.onAuthenticationSuccess(request, response, authentication);

        if (authPrincipal instanceof UserDetails) {
            UserDetails springUser = (UserDetails) authPrincipal;
            User user = userManager.getUserByUserName(springUser.getUsername());

            if (user != null) {
                user.setLastLogin(Instant.now());
                userManager.saveUser(user);
            }
        }
    }
}
