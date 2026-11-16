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
package org.apache.roller.weblogger.ui.core.filters;

import java.util.Arrays;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.config.AuthMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomOpenIDAuthenticationProcessingFilterTest {

    private static final String LOGIN_URL = "/roller_j_openid_security_check";

    private CustomOpenIDAuthenticationProcessingFilter filter;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        filter = new CustomOpenIDAuthenticationProcessingFilter();
        filter.setFilterProcessesUrl(LOGIN_URL);  // as in security.xml

        request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(LOGIN_URL);
        when(request.getRequestURI()).thenReturn("/roller" + LOGIN_URL);
        when(request.getContextPath()).thenReturn("/roller");
    }

    @TestFactory
    Stream<DynamicTest> handlesLoginUrlOnlyWhenOpenIdLoginIsEnabled() {
        return Arrays.stream(AuthMethod.values()).map(method ->
                DynamicTest.dynamicTest(method.getPropertyName(), () -> {
                    filter.setAuthMethod(() -> method);

                    boolean expected = method == AuthMethod.OPENID || method == AuthMethod.DB_OPENID;
                    assertEquals(expected,
                            filter.requiresAuthentication(request, mock(HttpServletResponse.class)));
                }));
    }
}
