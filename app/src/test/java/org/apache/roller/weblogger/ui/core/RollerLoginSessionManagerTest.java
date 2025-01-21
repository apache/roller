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
import org.apache.roller.weblogger.util.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RollerLoginSessionManagerTest {
    private RollerLoginSessionManager sessionManager;
    private Cache mockCache;

    @BeforeEach
    void setUp() {
        mockCache = mock(Cache.class);
        sessionManager = new RollerLoginSessionManager(mockCache);
    }

    @Test
    void testRegisterSession() {
        RollerSession mockSession = mock(RollerSession.class);
        String userName = "testUser";

        sessionManager.register(userName, mockSession);

        verify(mockCache).put(userName, mockSession);
    }

    @Test
    void testGetSession() {
        RollerSession mockSession = mock(RollerSession.class);
        String userName = "testUser";
        when(mockCache.get(userName)).thenReturn(mockSession);

        RollerSession result = sessionManager.get(userName);

        assertEquals(mockSession, result);
        verify(mockCache).get(userName);
    }

    @Test
    void testInvalidateSession() {
        String userName = "testUser";

        sessionManager.invalidate(userName);

        verify(mockCache).remove(userName);
    }

    @Test
    void testCacheHandlerInvalidation() {
        User mockUser = mock(User.class);
        String userName = "testUser";
        when(mockUser.getUserName()).thenReturn(userName);

        sessionManager.new SessionCacheHandler().invalidate(mockUser);

        verify(mockCache).remove(userName);
    }

    @Test
    void testNullInputHandling() {
        RollerSession mockSession = mock(RollerSession.class);

        sessionManager.register(null, mockSession);
        sessionManager.invalidate(null);
        sessionManager.get(null);

        verify(mockCache, never()).put(any(), any());
        verify(mockCache, never()).remove(any());
        verify(mockCache, never()).get(any());
    }

    @Test
    void testSessionTimeout() {
        String userName = "testUser";
        when(mockCache.get(userName)).thenReturn(null);

        RollerSession result = sessionManager.get(userName);

        assertNull(result);
        verify(mockCache).get(userName);
    }
}