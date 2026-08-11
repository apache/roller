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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RollerOidcUserServiceTest {

    private static final String ISSUER = "https://accounts.example.com";
    private static final String SUBJECT = ISSUER + "#user123";

    @Mock
    private Weblogger roller;

    @Mock
    private UserManager userManager;

    @Mock
    private User existingUser;

    private RollerOidcUserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RollerOidcUserService();
    }

    @Test
    void toOidcSubjectFormatsIssuerAndSub() {
        assertEquals(SUBJECT, RollerOidcUserService.toOidcSubject(oidcUser(Map.of())));
    }

    @Test
    void toOidcSubjectHandlesTrailingSlashInIssuer() {
        OidcUser user = oidcUser(Map.of("iss", "https://provider.example.com/", "sub", "abc"));
        assertEquals("https://provider.example.com/#abc", RollerOidcUserService.toOidcSubject(user));
    }

    /**
     * Regression: the returned principal must carry the Roller roles, otherwise
     * the very first request after login is denied by the authorization rules.
     */
    @Test
    void existingUserPrincipalCarriesRollerAuthorities() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(existingUser);
            when(existingUser.getEnabled()).thenReturn(Boolean.TRUE);
            when(userManager.getRoles(existingUser)).thenReturn(List.of("editor", "admin"));

            OidcUser result = service.resolveUser(oidcUser(Map.of()));

            assertEquals(Set.of("editor", "admin"), authorityNames(result));
            verify(userManager, never()).addUser(any(User.class));
        }
    }

    /**
     * Regression: Roller looks users up by principal.getName() throughout the
     * rendering layer and servlet filters (ParsedRequest, RoleAssignmentFilter,
     * Register). An OidcUser's default name is the "sub" claim, an opaque
     * provider ID that matches no Roller account, which made every rendered
     * weblog page throw an NPE for a signed-in OIDC user. The principal's name
     * must be the resolved Roller username.
     */
    @Test
    void existingUserPrincipalNameIsRollerUsername() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(existingUser);
            when(existingUser.getEnabled()).thenReturn(Boolean.TRUE);
            when(existingUser.getUserName()).thenReturn("bob");
            when(userManager.getRoles(existingUser)).thenReturn(List.of("editor"));

            OidcUser result = service.resolveUser(oidcUser(Map.of()));

            assertEquals("bob", result.getName());
        }
    }

    @Test
    void provisionedUserPrincipalNameIsRollerUsername() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor"));

            OidcUser result = service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "jsmith",
                    "email", "jsmith@example.com")));

            assertEquals("jsmith", result.getName());
        }
    }

    @Test
    void newUserPrincipalCarriesRollerAuthorities() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor"));

            OidcUser result = service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "jsmith",
                    "email", "jsmith@example.com")));

            assertEquals(Set.of("editor"), authorityNames(result));
        }
    }

    @Test
    void newUserIsProvisionedFromClaims() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor"));

            service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "jsmith",
                    "name", "Jane Smith",
                    "email", "jsmith@example.com")));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userManager).addUser(captor.capture());
            User created = captor.getValue();

            assertEquals("jsmith", created.getUserName());
            assertEquals("Jane Smith", created.getFullName());
            assertEquals("jsmith@example.com", created.getEmailAddress());
            assertEquals(SUBJECT, created.getOpenIdUrl());
            assertEquals(Boolean.TRUE, created.getEnabled());
            assertNotNull(created.getId());
            assertNotNull(created.getPassword(), "password is NOT NULL in the schema");
            assertNotNull(created.getDateCreated(), "datecreated is NOT NULL in the schema");
        }
    }

    @Test
    void adminRoleIsGrantedFromFlatRolesClaim() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor", "admin"));

            service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "boss",
                    "email", "boss@example.com",
                    "roles", List.of("editor", "admin"))));

            verify(userManager).grantRole(eq("admin"), any(User.class));
        }
    }

    @Test
    void adminRoleIsGrantedFromKeycloakRealmAccessClaim() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor", "admin"));

            service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "boss",
                    "email", "boss@example.com",
                    "realm_access", Map.of("roles", List.of("admin")))));

            verify(userManager).grantRole(eq("admin"), any(User.class));
        }
    }

    @Test
    void nonAdminUserDoesNotGetAdminRole() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor"));

            service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "plain",
                    "email", "plain@example.com",
                    "roles", List.of("editor"))));

            verify(userManager, never()).grantRole(eq("admin"), any(User.class));
        }
    }

    /**
     * Regression: addUser() grants "admin" to the very first user, but that row
     * is not yet flushed, so grantRole()'s database-backed duplicate check can
     * not see it. Without a flush in between the role is inserted twice.
     */
    @Test
    void pendingRolesAreFlushedBeforeGrantingAdmin() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor", "admin"));

            service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "first",
                    "email", "first@example.com",
                    "roles", List.of("admin"))));

            InOrder inOrder = inOrder(userManager, roller);
            inOrder.verify(userManager).addUser(any(User.class));
            inOrder.verify(roller).flush();
            inOrder.verify(userManager).grantRole(eq("admin"), any(User.class));
        }
    }

    @Test
    void disabledUserIsRejected() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(existingUser);
            when(existingUser.getEnabled()).thenReturn(Boolean.FALSE);

            OidcUser user = oidcUser(Map.of());
            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveUser(user));
        }
    }

    @Test
    void missingEmailIsRejected() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);

            OidcUser user = oidcUser(Map.of("preferred_username", "noemail"));
            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveUser(user));
        }
    }

    @Test
    void notBootstrappedIsRejected() {
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::isBootstrapped).thenReturn(false);

            OidcUser user = oidcUser(Map.of());
            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveUser(user));
        }
    }

    /**
     * The migration path: a site with database users turns on OIDC, and the
     * provider asserts a verified email matching the existing account.
     */
    @Test
    void existingUsernameIsLinkedWhenEmailIsVerified() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getUserByUserName("matt")).thenReturn(existingUser);
            when(existingUser.getEmailAddress()).thenReturn("matt@example.com");
            when(existingUser.getEnabled()).thenReturn(Boolean.TRUE);
            when(userManager.getRoles(existingUser)).thenReturn(List.of("editor"));

            OidcUser result = service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "matt",
                    "email", "matt@example.com",
                    "email_verified", Boolean.TRUE)));

            verify(existingUser).setOpenIdUrl(SUBJECT);
            verify(userManager).saveUser(existingUser);
            verify(userManager, never()).addUser(any(User.class));
            assertEquals(Set.of("editor"), authorityNames(result));
        }
    }

    @Test
    void existingUsernameIsNotLinkedWhenEmailIsUnverified() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getUserByUserName("matt")).thenReturn(existingUser);
            when(existingUser.getEmailAddress()).thenReturn("matt@example.com");

            OidcUser user = oidcUser(Map.of(
                    "preferred_username", "matt",
                    "email", "matt@example.com",
                    "email_verified", Boolean.FALSE));

            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveUser(user));
            verify(userManager, never()).saveUser(any(User.class));
            verify(userManager, never()).addUser(any(User.class));
        }
    }

    @Test
    void existingUsernameIsNotLinkedWhenEmailDiffers() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller()) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getUserByUserName("matt")).thenReturn(existingUser);
            when(existingUser.getEmailAddress()).thenReturn("someone-else@example.com");

            OidcUser user = oidcUser(Map.of(
                    "preferred_username", "matt",
                    "email", "matt@example.com",
                    "email_verified", Boolean.TRUE));

            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveUser(user));
            verify(userManager, never()).saveUser(any(User.class));
        }
    }

    @Test
    void autoProvisionDisabledRejectsNewUser() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller();
             MockedStatic<WebloggerConfig> config = autoProvision(false)) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);

            OidcUser user = oidcUser(Map.of(
                    "preferred_username", "newcomer",
                    "email", "newcomer@example.com"));

            assertThrows(OAuth2AuthenticationException.class, () -> service.resolveUser(user));
            verify(userManager, never()).addUser(any(User.class));
        }
    }

    /**
     * Regression: provisioning must not depend on the runtime "allow new
     * users" toggle, which is off by default and governs form registration.
     * The identity provider decides who may sign in, so on a stock install
     * every provider user after the first must still get an account.
     */
    @Test
    void formRegistrationPolicyDoesNotBlockProvisioning() throws Exception {
        try (MockedStatic<WebloggerFactory> factory = bootstrappedRoller();
             MockedStatic<WebloggerRuntimeConfig> config = registrationEnabled(false)) {
            when(userManager.getUserByOpenIdUrl(SUBJECT)).thenReturn(null);
            when(userManager.getUserCount()).thenReturn(5L);
            when(userManager.getRoles(any(User.class))).thenReturn(List.of("editor"));

            service.resolveUser(oidcUser(Map.of(
                    "preferred_username", "newcomer",
                    "email", "newcomer@example.com")));

            verify(userManager).addUser(any(User.class));
        }
    }

    private MockedStatic<WebloggerFactory> bootstrappedRoller() {
        MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::isBootstrapped).thenReturn(true);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);
        when(roller.getUserManager()).thenReturn(userManager);
        return factory;
    }

    private MockedStatic<WebloggerRuntimeConfig> registrationEnabled(boolean enabled) {
        MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class);
        config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("users.registration.enabled"))
                .thenReturn(enabled);
        return config;
    }

    private MockedStatic<WebloggerConfig> autoProvision(boolean enabled) {
        MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class);
        config.when(() -> WebloggerConfig.getBooleanProperty("users.oidc.autoProvision.enabled"))
                .thenReturn(enabled);
        return config;
    }

    private Set<String> authorityNames(OidcUser user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    private OidcUser oidcUser(Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("iss", ISSUER);
        claims.put("aud", List.of("client-id"));
        claims.put("iat", Instant.now());
        claims.put("exp", Instant.now().plusSeconds(3600));
        claims.putAll(extraClaims);

        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(),
                Instant.now().plusSeconds(3600), claims);
        return new DefaultOidcUser(List.of(), idToken);
    }
}
