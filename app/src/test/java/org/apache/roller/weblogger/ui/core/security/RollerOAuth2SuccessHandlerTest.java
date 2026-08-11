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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.mockito.Mockito.*;

class RollerOAuth2SuccessHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private Authentication authentication;

    @Mock
    private Weblogger roller;

    @Mock
    private UserManager userManager;

    @Mock
    private User rollerUser;

    private RollerOAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RollerOAuth2SuccessHandler();
        when(request.getContextPath()).thenReturn("/roller");
        when(request.getSession()).thenReturn(session);
        when(authentication.getPrincipal()).thenReturn(createOidcUser());
    }

    @Test
    void existingUserRedirectsToMenu() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::isBootstrapped).thenReturn(true);
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);
            when(roller.getUserManager()).thenReturn(userManager);
            when(userManager.getUserByOpenIdUrl("https://accounts.example.com#user123")).thenReturn(rollerUser);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect("/roller/roller-ui/menu.rol");
            verify(session, never()).setAttribute(anyString(), any());
        }
    }

    @Test
    void newUserRedirectsToRegistration() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::isBootstrapped).thenReturn(true);
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);
            when(roller.getUserManager()).thenReturn(userManager);
            when(userManager.getUserByOpenIdUrl("https://accounts.example.com#user123")).thenReturn(null);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect("/roller/roller-ui/register.rol");
            verify(session).setAttribute(eq(RollerOAuth2SuccessHandler.OIDC_USER_ATTR), any(OidcUser.class));
            verify(session).setAttribute(eq(RollerOAuth2SuccessHandler.OIDC_SUBJECT_ATTR), eq("https://accounts.example.com#user123"));
        }
    }

    @Test
    void notBootstrappedRedirectsToRegistration() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::isBootstrapped).thenReturn(false);

            handler.onAuthenticationSuccess(request, response, authentication);

            verify(response).sendRedirect("/roller/roller-ui/register.rol");
            verify(session).setAttribute(eq(RollerOAuth2SuccessHandler.OIDC_USER_ATTR), any(OidcUser.class));
        }
    }

    private OidcUser createOidcUser() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("iss", "https://accounts.example.com");
        claims.put("aud", List.of("client-id"));
        claims.put("iat", Instant.now());
        claims.put("exp", Instant.now().plusSeconds(3600));

        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(),
                Instant.now().plusSeconds(3600), claims);
        return new DefaultOidcUser(List.of(), idToken);
    }
}
