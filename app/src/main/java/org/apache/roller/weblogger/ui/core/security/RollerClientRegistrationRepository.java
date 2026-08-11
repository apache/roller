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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Builds OAuth2/OIDC client registrations from Roller properties.
 *
 * <p>Properties follow the pattern:
 * <pre>
 * oidc.{registrationId}.client-id=...
 * oidc.{registrationId}.client-secret=...
 * oidc.{registrationId}.issuer-uri=...
 * oidc.{registrationId}.client-name=...  (optional, defaults to registrationId)
 * oidc.{registrationId}.scope=openid,profile,email  (optional)
 * </pre>
 */
public class RollerClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private static final Log log = LogFactory.getLog(RollerClientRegistrationRepository.class);
    private static final String PREFIX = "oidc.";

    private final Map<String, ClientRegistration> registrations;

    public RollerClientRegistrationRepository() {
        this.registrations = buildRegistrations();
        if (!registrations.isEmpty()) {
            log.info("Configured OIDC providers: " + registrations.keySet());
        }
    }

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return registrations.get(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return registrations.values().iterator();
    }

    public Map<String, ClientRegistration> getRegistrations() {
        return Collections.unmodifiableMap(registrations);
    }

    private Map<String, ClientRegistration> buildRegistrations() {
        Map<String, String> registrationIds = new LinkedHashMap<>();

        Enumeration<Object> keys = WebloggerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith(PREFIX) && key.endsWith(".client-id")) {
                String id = key.substring(PREFIX.length(), key.length() - ".client-id".length());
                registrationIds.put(id, WebloggerConfig.getProperty(key));
            }
        }

        Map<String, ClientRegistration> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : registrationIds.entrySet()) {
            String id = entry.getKey();
            String clientId = entry.getValue();
            String clientSecret = WebloggerConfig.getProperty(PREFIX + id + ".client-secret");
            String issuerUri = WebloggerConfig.getProperty(PREFIX + id + ".issuer-uri");
            String clientName = WebloggerConfig.getProperty(PREFIX + id + ".client-name", id);
            String scopeStr = WebloggerConfig.getProperty(PREFIX + id + ".scope", "openid,profile,email");

            if (clientId == null || clientId.isBlank() || issuerUri == null || issuerUri.isBlank()) {
                log.warn("Skipping OIDC registration '" + id + "': client-id and issuer-uri are required");
                continue;
            }

            try {
                ClientRegistration.Builder builder = ClientRegistrations.fromIssuerLocation(issuerUri)
                        .registrationId(id)
                        .clientId(clientId)
                        .clientName(clientName)
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .scope(scopeStr.split(","));

                if (clientSecret != null && !clientSecret.isBlank()) {
                    builder.clientSecret(clientSecret);
                }

                result.put(id, builder.build());
                log.info("Registered OIDC provider: " + id + " (issuer: " + issuerUri + ")");
            } catch (Exception e) {
                log.error("Failed to configure OIDC provider '" + id + "' (issuer: " + issuerUri + "): " + e.getMessage());
            }
        }

        return result;
    }
}
