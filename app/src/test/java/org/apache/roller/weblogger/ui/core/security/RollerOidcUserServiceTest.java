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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.junit.jupiter.api.Assertions.*;

class RollerOidcUserServiceTest {

    @Test
    void toOidcSubjectFormatsIssuerAndSub() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");
        claims.put("iss", "https://accounts.example.com");
        claims.put("aud", List.of("client-id"));
        claims.put("iat", Instant.now());
        claims.put("exp", Instant.now().plusSeconds(3600));

        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(),
                Instant.now().plusSeconds(3600), claims);
        OidcUser oidcUser = new DefaultOidcUser(List.of(), idToken);

        String result = RollerOidcUserService.toOidcSubject(oidcUser);

        assertEquals("https://accounts.example.com#user123", result);
    }

    @Test
    void toOidcSubjectHandlesTrailingSlashInIssuer() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "abc");
        claims.put("iss", "https://provider.example.com/");
        claims.put("aud", List.of("client-id"));
        claims.put("iat", Instant.now());
        claims.put("exp", Instant.now().plusSeconds(3600));

        OidcIdToken idToken = new OidcIdToken("token-value", Instant.now(),
                Instant.now().plusSeconds(3600), claims);
        OidcUser oidcUser = new DefaultOidcUser(List.of(), idToken);

        String result = RollerOidcUserService.toOidcSubject(oidcUser);

        assertEquals("https://provider.example.com/#abc", result);
    }
}
