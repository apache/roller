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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Properties;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Logs in through the database login provider, set up as RollerContext sets
 * it up, and checks what happens to a password stored with an old encoding.
 */
class PasswordUpgradeTest {

    private final Map<String, String> savedProperties = new HashMap<>();
    private String userName;
    private String legacyHash;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        setProperty("passwds.encryption.enabled", "true");
        setProperty("passwds.encryption.algorithm", "bcrypt");
        setProperty("passwds.encryption.lazyUpgradeFrom", "SHA");

        // the unsalted, unprefixed SHA-1 hex hashes older Roller versions stored
        legacyHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                .digest("secret".getBytes(StandardCharsets.UTF_8)));
        User user = TestUtils.setupUser("passwordupgrade");
        userName = user.getUserName();
        user.setPassword(legacyHash);
        WebloggerFactory.getWeblogger().getUserManager().saveUser(user);
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.teardownUser(userName);
        TestUtils.endSession(true);
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
    void successfulLoginStoresTheConfiguredEncoding() throws Exception {
        DaoAuthenticationProvider provider = provider();

        provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret"));
        TestUtils.endSession(true);

        String stored = storedPassword();
        assertTrue(stored.startsWith("{bcrypt}"), "stored: " + stored);
        // and the new hash works for the next login
        provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret"));
        assertEquals(stored, storedPassword(), "an up-to-date hash is left alone");
    }

    @Test
    void aPasswordTooLongToReEncodeStillLogsIn() throws Exception {
        // 73 bytes, one more than BCrypt encodes
        String longPassword = "a".repeat(73);
        String longHash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                .digest(longPassword.getBytes(StandardCharsets.UTF_8)));
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        User user = mgr.getUserByUserName(userName);
        user.setPassword(longHash);
        mgr.saveUser(user);
        TestUtils.endSession(true);

        assertTrue(provider().authenticate(UsernamePasswordAuthenticationToken.unauthenticated(
                userName, longPassword)).isAuthenticated());
        TestUtils.endSession(true);

        assertEquals(longHash, storedPassword());
    }

    @Test
    void failedLoginLeavesThePasswordAlone() throws Exception {
        assertThrows(BadCredentialsException.class, () -> provider().authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(userName, "wrong")));
        TestUtils.endSession(true);

        assertEquals(legacyHash, storedPassword());
    }

    @Test
    void nothingIsStoredWhileEncryptionIsOff() throws Exception {
        setProperty("passwds.encryption.enabled", "false");

        provider().authenticate(UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret"));
        TestUtils.endSession(true);

        assertEquals(legacyHash, storedPassword());
    }

    @Test
    void nothingIsStoredWhenTheAlgorithmIsNoop() throws Exception {
        setProperty("passwds.encryption.algorithm", "noop");

        provider().authenticate(UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret"));
        TestUtils.endSession(true);

        assertEquals(legacyHash, storedPassword());
    }

    @Test
    void aPasswordChangedDuringTheLoginIsKept() throws Exception {
        String reset = "{bcrypt}" + new BCryptPasswordEncoder().encode("reset");
        RollerUserDetailsService users = new RollerUserDetailsService();
        // an administrator resets the password after this login has read the old one
        UserDetailsService resetAfterRead = name -> {
            UserDetails details = users.loadUserByUsername(name);
            try {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                User user = mgr.getUserByUserName(name);
                user.setPassword(reset);
                mgr.saveUser(user);
                WebloggerFactory.getWeblogger().flush();
            } catch (WebloggerException e) {
                throw new IllegalStateException(e);
            }
            return details;
        };
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(resetAfterRead);
        provider.setPasswordEncoder(RollerPasswordEncoders.create());
        provider.setUserDetailsPasswordService(users);

        provider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(userName, "secret"));
        TestUtils.endSession(true);

        assertEquals(reset, storedPassword());
    }

    private static DaoAuthenticationProvider provider() {
        RollerUserDetailsService users = new RollerUserDetailsService();
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(RollerPasswordEncoders.create());
        provider.setUserDetailsPasswordService(users);
        return provider;
    }

    private String storedPassword() throws Exception {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        return mgr.getUserByUserName(userName).getPassword();
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
