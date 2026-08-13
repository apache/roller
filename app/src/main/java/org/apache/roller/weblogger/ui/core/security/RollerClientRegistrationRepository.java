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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

/**
 * Builds OAuth2/OIDC client registrations from Roller properties.
 *
 * <p>No registrations are served unless {@code authentication.method} is
 * {@code oidc} or {@code db-oidc}, so configuring providers under another
 * method does not open the {@code /oauth2/authorization/*} endpoints.
 *
 * <p>OIDC discovery is deferred until first access so the identity provider
 * does not need to be reachable during application startup. Each provider is
 * resolved and cached independently: one unreachable provider does not block
 * the others, and a failed provider is retried with a bounded backoff instead
 * of on every request.
 *
 * <p>Properties follow the pattern:
 * <pre>
 * oidc.{registrationId}.client-id=...
 * oidc.{registrationId}.client-secret=...
 * oidc.{registrationId}.issuer-uri=...
 * oidc.{registrationId}.client-name=...  (optional, defaults to registrationId)
 * oidc.{registrationId}.scope=openid,profile,email  (optional)
 * oidc.{registrationId}.client-authentication-method=none  (optional, for a
 *     public client using PKCE; without it a client-secret is required)
 * </pre>
 */
public class RollerClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {

    private static final Log log = LogFactory.getLog(RollerClientRegistrationRepository.class);
    private static final String PREFIX = "oidc.";
    private static final long RETRY_BACKOFF_MS = 60_000;

    private final Map<String, ClientRegistration> resolved = new ConcurrentHashMap<>();
    private final Map<String, Long> failedAt = new ConcurrentHashMap<>();

    @Override
    public ClientRegistration findByRegistrationId(String registrationId) {
        return getRegistrations().get(registrationId);
    }

    @Override
    public Iterator<ClientRegistration> iterator() {
        return getRegistrations().values().iterator();
    }

    /** Whether the configured authentication method allows OIDC login at all. */
    static boolean oidcEnabled() {
        AuthMethod method = WebloggerConfig.getAuthMethod();
        return method == AuthMethod.OIDC || method == AuthMethod.DB_OIDC;
    }

    /**
     * The successfully resolved registrations, in configuration order. Providers
     * that have not resolved yet are attempted, unless they failed within the
     * retry backoff window.
     */
    public Map<String, ClientRegistration> getRegistrations() {
        if (!oidcEnabled()) {
            return Collections.emptyMap();
        }

        Map<String, String> configured = configuredProviderIds();
        long now = System.currentTimeMillis();

        for (Map.Entry<String, String> entry : configured.entrySet()) {
            String id = entry.getKey();
            if (resolved.containsKey(id)) {
                continue;
            }
            Long lastFailure = failedAt.get(id);
            if (lastFailure != null && now - lastFailure < RETRY_BACKOFF_MS) {
                continue;
            }
            ClientRegistration registration = buildRegistration(id, entry.getValue());
            if (registration != null) {
                resolved.put(id, registration);
                failedAt.remove(id);
            } else {
                failedAt.put(id, now);
            }
        }

        // return in configuration order, only what resolved
        Map<String, ClientRegistration> result = new LinkedHashMap<>();
        for (String id : configured.keySet()) {
            ClientRegistration registration = resolved.get(id);
            if (registration != null) {
                result.put(id, registration);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /** Registration ids that have an {@code oidc.<id>.client-id} property set. */
    static Map<String, String> configuredProviderIds() {
        Map<String, String> registrationIds = new LinkedHashMap<>();
        Enumeration<Object> keys = WebloggerConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            if (key.startsWith(PREFIX) && key.endsWith(".client-id")) {
                String id = key.substring(PREFIX.length(), key.length() - ".client-id".length());
                String clientId = WebloggerConfig.getProperty(key);
                if (clientId != null && !clientId.isBlank()) {
                    registrationIds.put(id, clientId);
                }
            }
        }
        return registrationIds;
    }

    private ClientRegistration buildRegistration(String id, String clientId) {
        String clientSecret = WebloggerConfig.getProperty(PREFIX + id + ".client-secret");
        String issuerUri = WebloggerConfig.getProperty(PREFIX + id + ".issuer-uri");
        String clientName = WebloggerConfig.getProperty(PREFIX + id + ".client-name", id);
        String scopeStr = WebloggerConfig.getProperty(PREFIX + id + ".scope", "openid,profile,email");
        String clientAuthMethod = WebloggerConfig.getProperty(PREFIX + id + ".client-authentication-method");

        if (clientId == null || clientId.isBlank() || issuerUri == null || issuerUri.isBlank()) {
            log.warn("Skipping OIDC registration '" + id + "': client-id and issuer-uri are required");
            return null;
        }

        boolean publicClient = "none".equalsIgnoreCase(clientAuthMethod);
        if (!publicClient && (clientSecret == null || clientSecret.isBlank())) {
            log.error("Skipping OIDC registration '" + id + "': no client-secret configured. Set oidc."
                    + id + ".client-secret, or oidc." + id
                    + ".client-authentication-method=none for a public client using PKCE.");
            return null;
        }

        try {
            ClientRegistration.Builder builder = ClientRegistrations.fromIssuerLocation(issuerUri)
                    .registrationId(id)
                    .clientId(clientId)
                    .clientName(clientName)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .scope(scopeStr.split(","));

            if (publicClient) {
                builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
            } else {
                builder.clientSecret(clientSecret);
            }

            ClientRegistration registration = builder.build();
            log.info("Registered OIDC provider: " + id + " (issuer: " + issuerUri + ")");
            return registration;
        } catch (Exception e) {
            log.error("Failed to configure OIDC provider '" + id + "' (issuer: " + issuerUri
                    + "), will retry in " + (RETRY_BACKOFF_MS / 1000) + "s", e);
            return null;
        }
    }
}
