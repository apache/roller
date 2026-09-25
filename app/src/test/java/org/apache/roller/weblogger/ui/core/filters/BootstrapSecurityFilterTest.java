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

package org.apache.roller.weblogger.ui.core.filters;

import java.lang.reflect.Field;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class BootstrapSecurityFilterTest {

    private final BootstrapSecurityFilter filter = new BootstrapSecurityFilter();
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach
    void reset() throws Exception {
        setCompleted(false);
    }

    /** OIDC sign-in can provision the first account, so it waits for the setup token too. */
    @Test
    void oidcSignInIsGatedUntilSetupCompletes() throws Exception {
        for (String path : new String[] {"/oauth2/authorization/keycloak", "/login/oauth2/code/keycloak"}) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            filter.doFilter(request(path), response, chain);
            verify(response).sendRedirect("/roller/roller-ui/bootstrap-token.rol");
        }
        verifyNoInteractions(chain);
    }

    @Test
    void oidcSignInPassesOnceSetupCompletes() throws Exception {
        setCompleted(true);
        HttpServletRequest request = request("/login/oauth2/code/keycloak");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendRedirect(anyString());
    }

    private static HttpServletRequest request(String path) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContextPath()).thenReturn("/roller");
        when(request.getRequestURI()).thenReturn("/roller" + path);
        return request;
    }

    private static void setCompleted(boolean completed) throws Exception {
        Field field = BootstrapSecurity.class.getDeclaredField("completed");
        field.setAccessible(true);
        field.set(null, completed);
    }
}
