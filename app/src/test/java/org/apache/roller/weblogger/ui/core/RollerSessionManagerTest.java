/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.ui.core;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RollerSessionManagerTest {

    private RollerSessionManager sessionManager;

    @Mock
    private Cache mockCache;

    @Mock
    private RollerSession mockSession;

    @Mock
    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        try (MockedStatic<CacheManager> mockedCacheManager = mockStatic(CacheManager.class)) {
            mockedCacheManager.when(() -> CacheManager.constructCache(isNull(), any())).thenReturn(mockCache);
            mockedCacheManager.when(() -> CacheManager.registerHandler(any())).then(invocation -> null);
            sessionManager = new RollerSessionManager();
        }
    }

    @Test
    void testRegisterSession() {
        String userName = "testUser";
        sessionManager.register(userName, mockSession);
        verify(mockCache).put(userName, mockSession);
    }

    @Test
    void testCacheHandlerInvalidateUser() {
        String userName = "testUser";
        when(mockUser.getUserName()).thenReturn(userName);

        // Create handler directly from instance
        RollerSessionManager.SessionCacheHandler handler = sessionManager.new SessionCacheHandler();
        handler.invalidate(mockUser);

        verify(mockCache).remove(userName);
    }

    @Test
    void testRegisterNullUserName() {
        sessionManager.register(null, mockSession);
        verify(mockCache, never()).put(any(), any());
    }

    @Test
    void testRegisterNullSession() {
        sessionManager.register("testUser", null);
        verify(mockCache, never()).put(any(), any());
    }

    @Test
    void testGetSession() {
        String userName = "testUser";
        when(mockCache.get(userName)).thenReturn(mockSession);

        RollerSession result = sessionManager.get(userName);
        assertEquals(mockSession, result);
        verify(mockCache).get(userName);
    }

    @Test
    void testGetSessionNullUserName() {
        RollerSession result = sessionManager.get(null);
        assertNull(result);
        verify(mockCache, never()).get(any());
    }

    @Test
    void testInvalidateSession() {
        String userName = "testUser";
        sessionManager.invalidate(userName);
        verify(mockCache).remove(userName);
    }

    @Test
    void testInvalidateNullUserName() {
        sessionManager.invalidate(null);
        verify(mockCache, never()).remove(any());
    }

    @Test
    void testCacheHandlerInvalidateUserWithNullUsername() {
        when(mockUser.getUserName()).thenReturn(null);
        sessionManager.new SessionCacheHandler().invalidate(mockUser);
        verify(mockCache, never()).remove(any());
    }
}