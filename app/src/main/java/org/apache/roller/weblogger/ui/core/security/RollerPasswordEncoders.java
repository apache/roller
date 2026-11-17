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
import java.util.Map;

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

    private RollerPasswordEncoders() {
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
     * That placeholder is not a password, so it never matches.
     */
    private record ExternalLoginAware(PasswordEncoder delegate) implements PasswordEncoder {

        @Override
        public String encode(CharSequence rawPassword) {
            return delegate.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            String marker = WebloggerConfig.getProperty("users.passwords.externalAuthValue", "<externalAuth>");
            if (rawPassword != null && marker != null && !marker.isEmpty() && marker.contentEquals(rawPassword)) {
                return false;
            }
            return delegate.matches(rawPassword, encodedPassword);
        }

        @Override
        public boolean upgradeEncoding(String encodedPassword) {
            return delegate.upgradeEncoding(encodedPassword);
        }
    }
}
