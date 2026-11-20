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

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalLoginAwarePasswordEncoderTest {

    @SuppressWarnings("deprecation")
    private static PasswordEncoder encoder() {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        // as with passwds.encryption.lazyUpgradeFrom=plaintext
        encoders.put(null, NoOpPasswordEncoder.getInstance());
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        return new ExternalLoginAwarePasswordEncoder(new DelegatingPasswordEncoder("bcrypt", encoders));
    }

    @Test
    void thePlaceholderIsNotAPassword() {
        PasswordEncoder encoder = encoder();
        // as Register stores it for container-managed and LDAP sign-ups
        assertFalse(encoder.matches("<externalAuth>", encoder.encode("<externalAuth>")));
        // as CustomUserRegistry stores it for directory users
        assertFalse(encoder.matches("<externalAuth>", "<externalAuth>"));
    }

    @Test
    void otherPasswordsStillWork() {
        PasswordEncoder encoder = encoder();
        assertTrue(encoder.matches("secret", encoder.encode("secret")));
        assertTrue(encoder.matches("old-plain", "old-plain"));
        assertFalse(encoder.matches("wrong", encoder.encode("secret")));
    }
}
