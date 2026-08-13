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

import java.util.Collections;
import java.util.Vector;

import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RollerClientRegistrationRepositoryTest {

    @Test
    void emptyConfigProducesNoRegistrations() {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.OIDC);
            config.when(WebloggerConfig::keys).thenReturn(Collections.emptyEnumeration());

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertNull(repo.findByRegistrationId("google"));
            assertFalse(repo.iterator().hasNext());
            assertTrue(repo.getRegistrations().isEmpty());
        }
    }

    @Test
    void skipsRegistrationWithMissingClientId() {
        Vector<Object> keys = new Vector<>();
        keys.add("oidc.google.client-id");

        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.OIDC);
            config.when(WebloggerConfig::keys).thenReturn(keys.elements());
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-id")).thenReturn("");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-secret")).thenReturn("secret");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.issuer-uri")).thenReturn("https://accounts.google.com");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-name", "google")).thenReturn("Google");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.scope", "openid,profile,email")).thenReturn("openid,profile,email");

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertNull(repo.findByRegistrationId("google"));
            assertTrue(repo.getRegistrations().isEmpty());
        }
    }

    @Test
    void skipsRegistrationWithMissingIssuerUri() {
        Vector<Object> keys = new Vector<>();
        keys.add("oidc.okta.client-id");

        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.OIDC);
            config.when(WebloggerConfig::keys).thenReturn(keys.elements());
            config.when(() -> WebloggerConfig.getProperty("oidc.okta.client-id")).thenReturn("my-client-id");
            config.when(() -> WebloggerConfig.getProperty("oidc.okta.client-secret")).thenReturn("secret");
            config.when(() -> WebloggerConfig.getProperty("oidc.okta.issuer-uri")).thenReturn("");
            config.when(() -> WebloggerConfig.getProperty("oidc.okta.client-name", "okta")).thenReturn("Okta");
            config.when(() -> WebloggerConfig.getProperty("oidc.okta.scope", "openid,profile,email")).thenReturn("openid,profile,email");

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertNull(repo.findByRegistrationId("okta"));
            assertTrue(repo.getRegistrations().isEmpty());
        }
    }

    @Test
    void nonOidcPropertiesAreIgnored() {
        Vector<Object> keys = new Vector<>();
        keys.add("some.other.property");
        keys.add("authentication.method");

        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.OIDC);
            config.when(WebloggerConfig::keys).thenReturn(keys.elements());

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertTrue(repo.getRegistrations().isEmpty());
        }
    }

    @Test
    void noRegistrationsUnlessAuthMethodAllowsOidc() {
        Vector<Object> keys = new Vector<>();
        keys.add("oidc.google.client-id");

        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.ROLLERDB);
            config.when(WebloggerConfig::keys).thenReturn(keys.elements());
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-id")).thenReturn("my-client-id");

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertNull(repo.findByRegistrationId("google"));
            assertTrue(repo.getRegistrations().isEmpty());
            // discovery must not even be attempted
            config.verify(WebloggerConfig::keys, never());
        }
    }

    @Test
    void skipsConfidentialClientWithoutSecret() {
        Vector<Object> keys = new Vector<>();
        keys.add("oidc.google.client-id");

        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.OIDC);
            config.when(WebloggerConfig::keys).thenReturn(keys.elements());
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-id")).thenReturn("my-client-id");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-secret")).thenReturn(null);
            config.when(() -> WebloggerConfig.getProperty("oidc.google.issuer-uri")).thenReturn("https://accounts.google.com");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.client-name", "google")).thenReturn("Google");
            config.when(() -> WebloggerConfig.getProperty("oidc.google.scope", "openid,profile,email")).thenReturn("openid,profile,email");

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertTrue(repo.getRegistrations().isEmpty());
        }
    }

    @Test
    void registrationsMapIsUnmodifiable() {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(WebloggerConfig::getAuthMethod).thenReturn(AuthMethod.OIDC);
            config.when(WebloggerConfig::keys).thenReturn(Collections.emptyEnumeration());

            RollerClientRegistrationRepository repo = new RollerClientRegistrationRepository();

            assertThrows(UnsupportedOperationException.class,
                    () -> repo.getRegistrations().put("test", null));
        }
    }
}
