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
package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.startup.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InstallTest {
    @Test
    void installerExposesItsTitleAndExceptionName() {
        Install action = spy(new Install());
        doAnswer(i -> i.getArgument(0)).when(action).getText(anyString());
        assertEquals("", action.getRootCauseExceptionName());
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<WebloggerStartup> startup = mockStatic(WebloggerStartup.class)) {
            startup.when(WebloggerStartup::getDatabaseProviderException)
                    .thenReturn(new StartupException("Connection failed", new IllegalStateException("offline")));
            assertEquals("database_error", action.execute());
            assertEquals("installer.error.connection.pageTitle", action.getPageTitle());
            assertEquals("java.lang.IllegalStateException", action.getRootCauseExceptionName());
        }
    }
}
