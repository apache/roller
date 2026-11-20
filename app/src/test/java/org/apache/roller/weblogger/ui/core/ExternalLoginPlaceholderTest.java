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

package org.apache.roller.weblogger.ui.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.security.CustomUserRegistry;
import org.apache.roller.weblogger.ui.core.security.ExternalLoginAwarePasswordEncoder;
import org.apache.roller.weblogger.ui.core.security.RollerRememberMeServices;
import org.apache.roller.weblogger.ui.core.security.RollerUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Users who log in through LDAP or the servlet container have a placeholder
 * stored instead of a Roller password. The placeholder is not a password at
 * the login form, whose provider RollerContext gives its password encoder.
 */
class ExternalLoginPlaceholderTest {

    private static final String DAO_PROVIDER =
            "org.springframework.security.authentication.dao.DaoAuthenticationProvider#0";

    private final Map<String, String> savedProperties = new HashMap<>();
    private String userName;
    private Object savedEncoder;

    @BeforeEach
    void setUp() throws Exception {
        savedEncoder = encoderField().get(null);
        TestUtils.setupWeblogger();
        setProperty("passwds.encryption.enabled", "true");
        setProperty("passwds.encryption.algorithm", "bcrypt");
        setProperty("rememberme.enabled", "true");
        setProperty("securelogin.enabled", "false");
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
            SecurityContextHolder.clearContext();
            // provider() replaces the encoder RollerContext shares
            encoderField().set(null, savedEncoder);
        }
    }

    @Test
    void anEncodedPlaceholderIsNotAPassword() throws Exception {
        setProperty("authentication.method", "cma");
        DaoAuthenticationProvider provider = provider();
        // as Register stores it for container-managed sign-ups
        User user = TestUtils.setupUser("cmauser");
        userName = user.getUserName();
        user.resetPassword("<externalAuth>");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "<externalAuth>")));
    }

    @Test
    void anUnencodedPlaceholderIsNotAPassword() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        DaoAuthenticationProvider provider = provider();
        // as CustomUserRegistry stores it for directory users
        User user = TestUtils.setupUser("ldapuser");
        userName = user.getUserName();
        user.setPassword("<externalAuth>");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "<externalAuth>")));
    }

    @Test
    void theDefaultPlaceholderIsNotAPasswordUnderAnotherConfiguredValue() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        setProperty("users.passwords.externalAuthValue", "<ldap>");
        DaoAuthenticationProvider provider = provider();
        // stored before the setting was changed
        User user = TestUtils.setupUser("olduser");
        userName = user.getUserName();
        user.setPassword("<externalAuth>");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "<externalAuth>")));
    }

    @Test
    void aPreviousEncodedPlaceholderIsNotAPasswordAfterTheSettingChanges() throws Exception {
        setProperty("authentication.method", "cma");
        setProperty("users.passwords.externalAuthValue", "<old>");
        DaoAuthenticationProvider provider = provider();
        // as Register stored it for container-managed sign-ups
        User user = TestUtils.setupUser("oldcmauser");
        userName = user.getUserName();
        user.resetPassword("<old>");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        setProperty("users.passwords.externalAuthValue", "<new>");
        setProperty("users.passwords.externalAuthValue.previous", "<older>,<old>");

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "<old>")));
    }

    @Test
    void aPreviousUnencodedPlaceholderIsNotAPasswordAfterTheSettingChanges() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.passwords.externalAuthValue", "<new>");
        setProperty("users.passwords.externalAuthValue.previous", "{noop}secret");
        DaoAuthenticationProvider provider = provider();
        // as CustomUserRegistry stored it before the setting changed
        User user = TestUtils.setupUser("oldldapuser");
        userName = user.getUserName();
        user.setPassword("{noop}secret");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "secret")));
    }

    @Test
    void emptyEntriesInThePreviousPlaceholdersAreIgnored() throws Exception {
        setProperty("users.passwords.externalAuthValue.previous", "<old>,, ,");

        assertTrue(ExternalLoginAwarePasswordEncoder.isExternalLoginMarker("<old>"));
        assertFalse(ExternalLoginAwarePasswordEncoder.isExternalLoginMarker(""));
        assertFalse(ExternalLoginAwarePasswordEncoder.isExternalLoginMarker(" "));
    }

    @Test
    void anEmptyConfiguredPlaceholderIsNotAPassword() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        setProperty("users.passwords.externalAuthValue", "");
        DaoAuthenticationProvider provider = provider();
        // as CustomUserRegistry stored an empty placeholder before
        User user = TestUtils.setupUser("emptyuser");
        userName = user.getUserName();
        user.setPassword("");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "")));
    }

    @Test
    void aStoredPlaceholderWithAnEncoderPrefixIsNotAPassword() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.passwords.externalAuthValue", "{noop}secret");
        DaoAuthenticationProvider provider = provider();
        // as CustomUserRegistry stores the configured placeholder
        User user = TestUtils.setupUser("prefixuser");
        userName = user.getUserName();
        user.setPassword("{noop}secret");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertThrows(AuthenticationException.class, () -> provider.authenticate(
                new UsernamePasswordAuthenticationToken(userName, "secret")));
    }

    @Test
    void anOrdinaryPlaintextPasswordStillMatches() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("passwds.encryption.lazyUpgradeFrom", "plaintext");
        DaoAuthenticationProvider provider = provider();
        User user = TestUtils.setupUser("plainuser");
        userName = user.getUserName();
        user.setPassword("an ordinary password");
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);

        assertTrue(provider.authenticate(new UsernamePasswordAuthenticationToken(
                userName, "an ordinary password")).isAuthenticated());
    }

    @Test
    void anEmptyConfiguredPlaceholderIsStoredAsTheDefault() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("users.passwords.externalAuthValue", "");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User("ldapuser", "unused",
                        List.of(new SimpleGrantedAuthority("editor"))),
                null, List.of()));

        User user = CustomUserRegistry.getUserDetailsFromAuthentication(null);

        assertEquals("<externalAuth>", user.getPassword());
    }

    @Test
    void theRememberMeSignatureUsesTheDefaultForAnEmptyConfiguredPlaceholder() throws Exception {
        setProperty("authentication.method", "ldap");
        setProperty("rememberme.enabled", "false");
        SigningRememberMeServices services = new SigningRememberMeServices();
        setProperty("users.passwords.externalAuthValue", "<externalAuth>");
        String signedWithTheDefault = services.sign();

        // CustomUserRegistry stores the default for an empty setting
        setProperty("users.passwords.externalAuthValue", "");

        assertEquals(signedWithTheDefault, services.sign());
    }

    private static class SigningRememberMeServices extends RollerRememberMeServices {

        SigningRememberMeServices() {
            super(new RollerUserDetailsService());
        }

        String sign() {
            return makeTokenSignature(1L, "ldapuser", "unused");
        }
    }

    @Test
    void aPasswordThatBcryptTreatsAsEqualToThePlaceholderNeverMatches() throws Exception {
        setProperty("users.passwords.externalAuthValue", "<new>");
        setProperty("users.passwords.externalAuthValue.previous", "<old>");
        provider();
        PasswordEncoder encoder = RollerContext.getPasswordEncoder();
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

    @Test
    void anEmptyOrMissingStoredPasswordDoesNotMatch() throws Exception {
        setProperty("passwds.encryption.lazyUpgradeFrom", "");
        provider();
        PasswordEncoder encoder = RollerContext.getPasswordEncoder();
        assertFalse(encoder.matches("secret", ""));
        assertFalse(encoder.matches("secret", null));
    }

    /** The login form's provider, given its encoder the way RollerContext does at startup. */
    private static DaoAuthenticationProvider provider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(new RollerUserDetailsService());
        WebApplicationContext ctx = mock(WebApplicationContext.class);
        when(ctx.containsBean(DAO_PROVIDER)).thenReturn(true);
        when(ctx.getBean(DAO_PROVIDER)).thenReturn(provider);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE))
                .thenReturn(ctx);
        new RollerContext().initializeSecurityFeatures(servletContext);
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

    private static Field encoderField() throws ReflectiveOperationException {
        Field field = RollerContext.class.getDeclaredField("encoder");
        field.setAccessible(true);
        return field;
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }
}
