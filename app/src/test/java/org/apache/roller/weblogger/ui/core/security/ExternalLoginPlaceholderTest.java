/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.core.security;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Users who log in through LDAP or the servlet container have a placeholder
 * stored instead of a Roller password. The placeholder is not a password.
 */
class ExternalLoginPlaceholderTest {

    private final Map<String, String> savedProperties = new HashMap<>();
    private String userName;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        setProperty("passwds.encryption.enabled", "true");
        setProperty("passwds.encryption.algorithm", "bcrypt");
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            if (userName != null) {
                TestUtils.teardownUser(userName);
                TestUtils.endSession(true);
            }
        } finally {
            Properties config = config();
            savedProperties.forEach((k, v) -> {
                if (v == null) {
                    config.remove(k);
                } else {
                    config.setProperty(k, v);
                }
            });
        }
    }

    @Test
    void anEncodedPlaceholderIsNotAPassword() throws Exception {
        setProperty("authentication.method", "cma");
        // as Register stores it for container-managed sign-ups
        User user = TestUtils.setupUser("cmauser");
        userName = user.getUserName();
        // (User.resetPassword encodes with the login's password encoder)
        user.setPassword(RollerPasswordEncoders.create().encode(
                WebloggerConfig.getProperty("users.passwords.externalAuthValue", "<externalAuth>")));
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "<externalAuth>")));
    }

    @Test
    void anUnencodedPlaceholderIsNotAPassword() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        // as CustomUserRegistry stores it for directory users
        User user = TestUtils.setupUser("ldapuser");
        userName = user.getUserName();
        user.setPassword("<externalAuth>");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "<externalAuth>")));
    }

    @Test
    void anOrdinaryPlaintextPasswordStillMatches() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        // as older Roller versions stored passwords
        User user = TestUtils.setupUser("plainuser");
        userName = user.getUserName();
        user.setPassword("an ordinary password");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertTrue(provider().authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                userName, "an ordinary password")).isAuthenticated());
        assertTrue(RollerPasswordEncoders.create().matches("an ordinary password", "an ordinary password"));
    }

    @Test
    void theDefaultPlaceholderIsNotAPasswordUnderAnotherConfiguredValue() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        setProperty("users.passwords.externalAuthValue", "<ldap>");
        // stored before the setting was changed
        User user = TestUtils.setupUser("olduser");
        userName = user.getUserName();
        user.setPassword("<externalAuth>");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "<externalAuth>")));
        assertFalse(RollerPasswordEncoders.create().matches("<ldap>", "<ldap>"));
    }

    @Test
    void aPreviousEncodedPlaceholderIsNotAPasswordAfterTheSettingChanges() throws Exception {
        setProperty("authentication.method", "cma");
        // as Register stored it for container-managed sign-ups
        User user = TestUtils.setupUser("oldcmauser");
        userName = user.getUserName();
        user.setPassword(RollerPasswordEncoders.create().encode("<old>"));
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        setProperty("users.passwords.externalAuthValue", "<new>");
        setProperty("users.passwords.externalAuthValue.previous", "<older>,<old>");

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "<old>")));
        assertFalse(RollerPasswordEncoders.create().matches("<old>", user.getPassword()));
    }

    @Test
    void aPreviousUnencodedPlaceholderIsNotAPasswordAfterTheSettingChanges() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.passwords.externalAuthValue", "<new>");
        setProperty("users.passwords.externalAuthValue.previous", "{noop}secret");
        // as CustomUserRegistry stored it before the setting changed
        User user = TestUtils.setupUser("oldldapuser");
        userName = user.getUserName();
        user.setPassword("{noop}secret");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret")));
        assertFalse(RollerPasswordEncoders.create().matches("secret", "{noop}secret"));
    }

    @Test
    void emptyEntriesInThePreviousPlaceholdersAreIgnored() throws Exception {
        setProperty("users.passwords.externalAuthValue.previous", "<older>, <old>,, ,");

        assertTrue(RollerPasswordEncoders.isExternalLoginMarker("<older>"));
        assertTrue(RollerPasswordEncoders.isExternalLoginMarker("<old>"));
        assertFalse(RollerPasswordEncoders.isExternalLoginMarker(""));
        assertFalse(RollerPasswordEncoders.isExternalLoginMarker(" "));
    }

    @Test
    void anEmptyConfiguredPlaceholderIsNotAPassword() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        setProperty("users.passwords.externalAuthValue", "");
        // as CustomUserRegistry stored an empty placeholder before
        User user = TestUtils.setupUser("emptyuser");
        userName = user.getUserName();
        user.setPassword("");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "")));
        assertFalse(RollerPasswordEncoders.create().matches("", ""));
    }

    @Test
    void aStoredPlaceholderWithAnEncoderPrefixIsNotAPassword() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.passwords.externalAuthValue", "{noop}secret");
        // as CustomUserRegistry stores the configured placeholder
        User user = TestUtils.setupUser("prefixuser");
        userName = user.getUserName();
        user.setPassword("{noop}secret");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret")));
        assertFalse(RollerPasswordEncoders.create().matches("secret", "{noop}secret"));
    }

    @Test
    void anEmptyConfiguredPlaceholderIsStoredAsTheDefault() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.passwords.externalAuthValue", "");
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                org.springframework.security.core.userdetails.User.withUsername("ldapuser")
                        .password("unused").authorities("editor").build(),
                null, java.util.List.of()));
        try {
            User user = CustomUserRegistry.getUserDetailsFromAuthentication(null);
            assertEquals("<externalAuth>", user.getPassword());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void theRememberMeSignatureUsesTheDefaultForAnEmptyConfiguredPlaceholder() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("rememberme.enabled", "false");
        RollerRememberMeServices services = new RollerRememberMeServices(new RollerUserDetailsService());
        setProperty("users.passwords.externalAuthValue", "<externalAuth>");
        String signedWithTheDefault = services.makeTokenSignature(1L, "ldapuser", "unused");

        // CustomUserRegistry stores the default for an empty setting
        setProperty("users.passwords.externalAuthValue", "");

        assertEquals(signedWithTheDefault, services.makeTokenSignature(1L, "ldapuser", "unused"));
    }

    @Test
    void theEncoderDoesNotMatchThePlaceholder() {
        // the encoder also checks AtomPub and XML-RPC passwords
        PasswordEncoder encoder = RollerPasswordEncoders.create();
        assertFalse(encoder.matches("<externalAuth>", encoder.encode("<externalAuth>")));
        assertTrue(encoder.matches("secret", encoder.encode("secret")));
    }

    @Test
    void aPasswordThatBcryptTreatsAsEqualToThePlaceholderNeverMatches() throws Exception {
        setProperty("users.passwords.externalAuthValue", "<new>");
        setProperty("users.passwords.externalAuthValue.previous", "<old>");
        PasswordEncoder encoder = RollerPasswordEncoders.create();
        for (String marker : new String[] {"<new>", "<old>", "<externalAuth>"}) {
            String stored = encoder.encode(marker);
            // BCrypt repeats its key, a NUL byte after each copy, so this
            // password gives the same hash as the placeholder
            String sameKey = marker + '\0' + marker;
            assertTrue(new BCryptPasswordEncoder().matches(sameKey, stored.substring("{bcrypt}".length())));
            assertFalse(encoder.matches(sameKey, stored), marker);
        }
        // a password that only starts like a placeholder still matches
        assertTrue(encoder.matches("<old>x", encoder.encode("<old>x")));
    }

    private static DaoAuthenticationProvider provider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(new RollerUserDetailsService());
        provider.setPasswordEncoder(RollerPasswordEncoders.create());
        return provider;
    }

    private void setProperty(String key, String value) throws ReflectiveOperationException {
        Properties config = config();
        // keep the first value seen, even when it is null (not set)
        if (!savedProperties.containsKey(key)) {
            savedProperties.put(key, config.getProperty(key));
        }
        config.setProperty(key, value);
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }
}
