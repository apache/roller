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


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

/**
 * Builds the password encoder Roller uses for logins and stored passwords,
 * from the passwds.encryption.* settings in roller.properties.
 */
public final class RollerPasswordEncoders {

    private static final Log log = LogFactory.getLog(RollerPasswordEncoders.class);

    /** Stored in place of a password when none is configured. */
    public static final String DEFAULT_EXTERNAL_LOGIN_MARKER = "<externalAuth>";

    private RollerPasswordEncoders() {
    }

    /**
     * The value stored in place of a Roller password for users who log in
     * through LDAP or the servlet container: users.passwords.externalAuthValue,
     * or the default when that setting is empty.
     */
    public static String externalLoginMarker() {
        String marker = WebloggerConfig.getProperty("users.passwords.externalAuthValue");
        return marker == null || marker.isEmpty() ? DEFAULT_EXTERNAL_LOGIN_MARKER : marker;
    }

    /**
     * Whether a password is the configured external-login marker, the default
     * one, or one listed in users.passwords.externalAuthValue.previous, which
     * accounts may still have stored from before the setting changed. None of
     * them matches at login, so none can be chosen as a Roller password.
     */
    public static boolean isExternalLoginMarker(CharSequence password) {
        return password != null && externalLoginMarkers().contains(password.toString());
    }

    /**
     * The configured marker, the default one and the previous ones. The
     * previous ones are a comma-separated list, so a marker that contains a
     * comma can't be listed there.
     */
    private static Set<String> externalLoginMarkers() {
        Set<String> markers = new LinkedHashSet<>();
        markers.add(externalLoginMarker());
        markers.add(DEFAULT_EXTERNAL_LOGIN_MARKER);
        String previous = WebloggerConfig.getProperty("users.passwords.externalAuthValue.previous");
        for (String marker : StringUtils.split(StringUtils.defaultString(previous), ',')) {
            // an empty entry is never a marker, so an empty password isn't one
            String trimmed = marker.trim();
            if (!trimmed.isEmpty()) {
                markers.add(trimmed);
            }
        }
        return markers;
    }

    @SuppressWarnings("deprecation")
    public static PasswordEncoder create() {

        Map<String, PasswordEncoder> encoders = new HashMap<>();

        // outdated digest encoder used for lazy upgrades from pws encoded by old roller versions.
        String migrateFrom = WebloggerConfig.getProperty("passwds.encryption.lazyUpgradeFrom");

        if (migrateFrom == null || migrateFrom.isEmpty()) {
            log.debug("lazy pw upgrade disabled");
        } else if (migrateFrom.equals("plaintext")) {
            encoders.put(null, org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
        } else if (migrateFrom.equals("MD5")) {
            encoders.put(null, new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
        } else if (migrateFrom.equals("SHA")) {
            encoders.put(null, new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
        } else {
            throw new RuntimeException("passwds.encryption.lazyUpgradeFrom="+migrateFrom+" is no valid encoding to upgrade from.");
        }

        // supported encoders
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        // pbkdf2 stores only salt+hash, so its parameters must stay as they were
        // when existing passwords were encoded or those passwords stop verifying.
        // scrypt and argon2 encode their parameters, so they can take v5_8.
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_5());
        // provided by bouncy castle dependency
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());

        // just for testing
        encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());

        String algorithm = WebloggerConfig.getProperty("passwds.encryption.algorithm");

        if (WebloggerConfig.getBooleanProperty("passwds.encryption.enabled")) {

            if ("SHA".equals(algorithm) || "MD5".equals(algorithm)) {
                throw new RuntimeException("passwds.encryption.algorithm="+algorithm+" is outdated,"
                        + " please set passwds.encryption.algorithm to 'bcrypt' for automatic lazy upgrade.");
            }

            if (!encoders.containsKey(algorithm)) {
                throw new RuntimeException("passwds.encryption.algorithm="+algorithm+" is not supported.");
            }
        } else {
            log.warn("New passwords are stored in plain text!");
            algorithm = "noop";
        }

        log.info("Password Encryption Algorithm set to '" + algorithm + "'");

        return new ExternalLoginAware(new DelegatingPasswordEncoder(algorithm, encoders));
    }

    /**
     * Users who log in through LDAP or the servlet container have no Roller
     * password; Roller stores users.passwords.externalAuthValue in its place.
     * That placeholder is not a password, so it never matches, and neither do
     * the default or a previous placeholder, a password the delegate treats as
     * equal to one of them, and an empty password.
     */
    private record ExternalLoginAware(PasswordEncoder delegate) implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return delegate.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            if (rawPassword == null || rawPassword.length() == 0) {
                return false;
            }
            Set<String> markers = externalLoginMarkers();
            // a stored marker is checked before the delegate sees it, since a
            // marker such as {noop}secret would otherwise match its own suffix
            if (markers.contains(encodedPassword)) {
                return false;
            }
            if (markers.contains(rawPassword.toString())) {
                logRefusal();
                return false;
            }
            if (!delegate.matches(rawPassword, encodedPassword)) {
                return false;
            }
            // BCrypt, for one, treats some other passwords as equal to a
            // marker, so refuse any password whose stored value a marker
            // matches too
            for (String marker : markers) {
                if (delegate.matches(marker, encodedPassword)) {
                    logRefusal();
                    return false;
                }
            }
            return true;
        }

        private static void logRefusal() {
            // the password itself is never logged
            log.info("Refused a password that Roller treats as an external-login marker"
                    + " (users.passwords.externalAuthValue or its .previous list). An"
                    + " administrator can set a new password for an account that uses one.");
        }

        @Override
        public boolean upgradeEncoding(String encodedPassword) {
            return delegate.upgradeEncoding(encodedPassword);
        }
    }

    /**
     * A random password for users who log in through OpenID Connect and never
     * type one. It is 64 characters, so every encoder accepts it (BCrypt takes
     * at most 72 bytes), and comes from a secure random source, since it still
     * guards the account's database login.
     */
    public static String randomUnusedPassword() {
        return RandomStringUtils.secure().nextAlphanumeric(64);
    }
}
