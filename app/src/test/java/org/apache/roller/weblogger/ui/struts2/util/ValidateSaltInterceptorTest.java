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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.cache.SaltCache;
import org.apache.struts2.StrutsStatics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ValidateSaltInterceptorTest {

    private ValidateSaltInterceptor interceptor;

    @Mock
    private ActionInvocation invocation;

    @Mock
    private ActionContext context;

    @Mock
    private HttpServletRequest request;

    @Mock
    private RollerSession rollerSession;

    @Mock
    private SaltCache saltCache;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        interceptor = new ValidateSaltInterceptor();
        when(invocation.getInvocationContext()).thenReturn(context);
        when(context.get(StrutsStatics.HTTP_REQUEST)).thenReturn(request);
    }

    @Test
    public void testValidMultipartSaltIsConsumed() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            configureMultipartPost("/roller-ui/mediaFileAdd!save.rol");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            when(saltCache.get("validSalt")).thenReturn("userId");
            when(invocation.invoke()).thenReturn("success");

            assertEquals("success", interceptor.intercept(invocation));

            verify(saltCache).remove("validSalt");
            verify(invocation).invoke();
        }
    }

    @Test
    public void testMultipartPostWithoutSaltIsRejectedEvenWithResponseSaltAttribute() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class)) {
            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);

            configureMultipartPost("/roller-ui/bookmarksImport!save.rol");
            when(request.getParameter("salt")).thenReturn(null);
            when(request.getAttribute("salt")).thenReturn("responseSalt");

            assertThrows(ServletException.class, () -> interceptor.intercept(invocation));

            verify(invocation, never()).invoke();
        }
    }

    @Test
    public void testInvalidMultipartSaltIsRejectedForAnyRollerAction() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            configureMultipartPost("/roller-ui/arbitrary!save.rol");
            when(request.getParameter("salt")).thenReturn("invalidSalt");
            when(saltCache.get("invalidSalt")).thenReturn(null);

            assertThrows(ServletException.class, () -> interceptor.intercept(invocation));

            verify(invocation, never()).invoke();
            verify(saltCache, never()).remove(anyString());
        }
    }

    @Test
    public void testMultipartSaltCannotBeReplayed() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            configureMultipartPost("/roller-ui/mediaFileEdit!save.rol");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            when(saltCache.get("validSalt")).thenReturn("userId", (String) null);

            interceptor.intercept(invocation);
            assertThrows(ServletException.class, () -> interceptor.intercept(invocation));

            verify(invocation, times(1)).invoke();
            verify(saltCache, times(1)).remove("validSalt");
        }
    }

    @Test
    public void testOrdinaryPostIsNotValidatedTwice() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(invocation.invoke()).thenReturn("success");

        assertEquals("success", interceptor.intercept(invocation));

        verify(request, never()).getParameter("salt");
        verify(invocation).invoke();
    }

    private void configureMultipartPost(String servletPath) {
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data; boundary=abc123");
        when(request.getServletPath()).thenReturn(servletPath);
    }

    private static class TestUser extends User {
        private static final long serialVersionUID = 1L;
        private final String id;

        TestUser(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
