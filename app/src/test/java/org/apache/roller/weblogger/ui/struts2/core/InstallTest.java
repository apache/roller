/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
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
