/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.core.security;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.roller.weblogger.TestUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every way of logging in that security.xml sets up records the login in its
 * session, which is what lets a later password change end that session.
 */
class SecurityXmlLoginWiringTest {

    private static GenericXmlApplicationContext context;
    private static final List<Object> filters = new ArrayList<>();

    @BeforeAll
    static void loadSecurityXml() throws Exception {
        TestUtils.setupWeblogger();
        context = new GenericXmlApplicationContext(
                new FileSystemResource("src/main/webapp/WEB-INF/security.xml"));
        for (SecurityFilterChain chain : context.getBean(FilterChainProxy.class).getFilterChains()) {
            filters.addAll(chain.getFilters());
        }
    }

    @AfterAll
    static void close() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void formLoginRecordsTheLogin() throws Exception {
        long formLogins = 0;
        for (Object filter : filters) {
            if (filter instanceof UsernamePasswordAuthenticationFilter) {
                formLogins++;
                assertInstanceOf(RollerLoginSuccessHandler.class, successHandler(filter));
            }
        }
        assertEquals(1, formLogins, "security.xml should set up one form login");
    }

    @Test
    void everyLoginFilterRecordsTheLogin() throws Exception {
        long loginFilters = 0;
        for (Object filter : filters) {
            if (filter instanceof AbstractAuthenticationProcessingFilter) {
                loginFilters++;
                assertInstanceOf(RollerLoginSuccessHandler.class, successHandler(filter),
                        filter.getClass().getSimpleName() + " should record the login");
            }
        }
        // the login form and single sign-on
        assertTrue(loginFilters >= 2, "expected the form and single sign-on filters, found " + loginFilters);
    }

    @Test
    void rememberMeRecordsTheLogin() {
        long rememberMeFilters = 0;
        for (Object filter : filters) {
            if (filter instanceof RememberMeAuthenticationFilter) {
                rememberMeFilters++;
                assertInstanceOf(RollerRememberMeServices.class,
                        ((RememberMeAuthenticationFilter) filter).getRememberMeServices());
            }
        }
        assertEquals(1, rememberMeFilters, "security.xml should set up remember-me");
    }

    private static Object successHandler(Object filter) throws Exception {
        Field field = AbstractAuthenticationProcessingFilter.class.getDeclaredField("successHandler");
        field.setAccessible(true);
        return field.get(filter);
    }
}
