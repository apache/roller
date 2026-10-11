/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Sends the browser on after Roller has ended its own session.
 *
 * <p>A user who signed in through an OIDC provider is sent to the provider's
 * {@code end_session_endpoint}, so the provider session ends too and the next
 * "Sign in with ..." asks for credentials again. The provider then returns the
 * browser to {@code postLogoutRedirectUri}. Everyone else, and OIDC users whose
 * provider does not advertise an end-session endpoint, go to the front page as
 * before.
 */
public class LogoutRedirect {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public LogoutRedirect(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    /** Uses the client registrations configured in {@code security.xml}. */
    public static LogoutRedirect forServletContext(ServletContext servletContext) {
        return new LogoutRedirect(WebApplicationContextUtils
                .getRequiredWebApplicationContext(servletContext)
                .getBean(ClientRegistrationRepository.class));
    }

    /**
     * @param authentication the authentication of the session being ended,
     *        captured before the session was invalidated; may be null
     * @param postLogoutRedirectUri absolute URL the provider returns to, or
     *        null to let Spring derive it from the request
     */
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication, String postLogoutRedirectUri)
            throws IOException, ServletException {

        if (clientRegistrationRepository != null
                && authentication instanceof OAuth2AuthenticationToken
                && authentication.getPrincipal() instanceof OidcUser) {
            OidcClientInitiatedLogoutSuccessHandler handler =
                    new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
            handler.setPostLogoutRedirectUri(postLogoutRedirectUri != null
                    ? postLogoutRedirectUri : "{baseUrl}/");
            // falls back to the context root when the provider has no end-session endpoint
            handler.onLogoutSuccess(request, response, authentication);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}
