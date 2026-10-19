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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LogoutRedirectTest {

    private static final String END_SESSION = "https://idp.example.com/realms/roller/logout";

    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/roller");
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("blog.example.com");
        when(request.getServerPort()).thenReturn(443);
        when(request.getRequestURI()).thenReturn("/roller/roller-ui/logout.rol");
        response = mock(HttpServletResponse.class);
        when(response.encodeRedirectURL(anyString())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void oidcUserIsSentToTheProviderEndSessionEndpoint() throws Exception {
        ClientRegistrationRepository repo = id -> registration(Map.of("end_session_endpoint", END_SESSION));

        new LogoutRedirect(repo).sendRedirect(request, response, oidcAuthentication(),
                "https://blog.example.com/roller/");

        String target = redirectTarget();
        assertTrue(target.startsWith(END_SESSION + "?"), target);
        assertTrue(target.contains("id_token_hint=id-token-value"), target);
        assertTrue(target.contains("post_logout_redirect_uri=https://blog.example.com/roller/"), target);
    }

    @Test
    void oidcUserWithoutEndSessionEndpointGoesToTheFrontPage() throws Exception {
        ClientRegistrationRepository repo = id -> registration(Map.of());

        new LogoutRedirect(repo).sendRedirect(request, response, oidcAuthentication(),
                "https://blog.example.com/roller/");

        assertEquals("/roller/", redirectTarget());
    }

    @Test
    void formLoginUserGoesToTheFrontPage() throws Exception {
        ClientRegistrationRepository repo = mock(ClientRegistrationRepository.class);
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                "bsmith", null, List.of(new SimpleGrantedAuthority("editor")));

        new LogoutRedirect(repo).sendRedirect(request, response, authentication, "https://blog.example.com/roller/");

        assertEquals("/roller/", redirectTarget());
        verifyNoInteractions(repo);
    }

    @Test
    void anonymousLogoutGoesToTheFrontPage() throws Exception {
        new LogoutRedirect(mock(ClientRegistrationRepository.class))
                .sendRedirect(request, response, null, null);

        assertEquals("/roller/", redirectTarget());
    }

    private String redirectTarget() throws Exception {
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(url.capture());
        return url.getValue();
    }

    private static ClientRegistration registration(Map<String, Object> metadata) {
        return ClientRegistration.withRegistrationId("keycloak")
                .clientId("web_app")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://idp.example.com/auth")
                .tokenUri("https://idp.example.com/token")
                .jwkSetUri("https://idp.example.com/certs")
                .userNameAttributeName("sub")
                .providerConfigurationMetadata(metadata)
                .build();
    }

    private static OAuth2AuthenticationToken oidcAuthentication() {
        OidcIdToken idToken = new OidcIdToken("id-token-value", Instant.now(),
                Instant.now().plusSeconds(60), Map.of("sub", "user123"));
        DefaultOidcUser user = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("editor")), idToken);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "keycloak");
    }
}
