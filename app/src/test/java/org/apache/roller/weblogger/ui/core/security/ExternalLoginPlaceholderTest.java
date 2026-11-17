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
import org.springframework.security.crypto.password.PasswordEncoder;

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
        if (userName != null) {
            TestUtils.teardownUser(userName);
            TestUtils.endSession(true);
        }
        Properties config = config();
        savedProperties.forEach((k, v) -> {
            if (v == null) {
                config.remove(k);
            } else {
                config.setProperty(k, v);
            }
        });
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
    void theEncoderDoesNotMatchThePlaceholder() {
        // the encoder also checks AtomPub and XML-RPC passwords
        PasswordEncoder encoder = RollerPasswordEncoders.create();
        assertFalse(encoder.matches("<externalAuth>", encoder.encode("<externalAuth>")));
        assertTrue(encoder.matches("secret", encoder.encode("secret")));
    }

    private static DaoAuthenticationProvider provider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(new RollerUserDetailsService());
        provider.setPasswordEncoder(RollerPasswordEncoders.create());
        return provider;
    }

    private void setProperty(String key, String value) throws ReflectiveOperationException {
        Properties config = config();
        savedProperties.putIfAbsent(key, config.getProperty(key));
        config.setProperty(key, value);
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }
}
