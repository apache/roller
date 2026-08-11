/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.roller.weblogger.ui.core.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * After a successful OIDC login, checks whether the authenticated user already
 * has a Roller account. Existing users are sent to the main menu; new users
 * are redirected to the registration page with their OIDC claims stored in
 * the session for pre-population.
 */
public class RollerOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Log log = LogFactory.getLog(RollerOAuth2SuccessHandler.class);

    public static final String OIDC_USER_ATTR = "oidcUser";
    public static final String OIDC_SUBJECT_ATTR = "oidcSubject";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String oidcSubject = RollerOidcUserService.toOidcSubject(oidcUser);
        String contextPath = request.getContextPath();

        try {
            if (WebloggerFactory.isBootstrapped()) {
                User rollerUser = WebloggerFactory.getWeblogger().getUserManager()
                        .getUserByOpenIdUrl(oidcSubject);

                if (rollerUser != null) {
                    response.sendRedirect(contextPath + "/roller-ui/menu.rol");
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Error checking OIDC user in Roller", e);
        }

        HttpSession session = request.getSession();
        session.setAttribute(OIDC_USER_ATTR, oidcUser);
        session.setAttribute(OIDC_SUBJECT_ATTR, oidcSubject);
        response.sendRedirect(contextPath + "/roller-ui/register.rol");
    }
}
