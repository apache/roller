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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

class RollerOAuth2SuccessHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    private RollerOAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RollerOAuth2SuccessHandler();
    }

    @Test
    void redirectsToMenuUnderContextPath() throws Exception {
        when(request.getContextPath()).thenReturn("/roller");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/roller/roller-ui/menu.rol");
    }

    @Test
    void redirectsToMenuAtRootContext() throws Exception {
        when(request.getContextPath()).thenReturn("");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/roller-ui/menu.rol");
    }
}
