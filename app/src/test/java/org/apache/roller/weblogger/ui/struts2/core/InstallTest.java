/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.ui.struts2.core;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.startup.*;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    /**
     * The installer is run by the person setting the site up, so finishing it
     * records the site's absolute URL from that request when none is set.
     */
    @Test
    void bootstrapRecordsSiteUrlWhenUnset() throws Exception {
        assertEquals(List.of("https://blog.example.org:8443/roller"),
                siteUrlsSavedByBootstrap(null));
    }

    @Test
    void bootstrapKeepsConfiguredSiteUrl() throws Exception {
        assertTrue(siteUrlsSavedByBootstrap("https://configured.example.org").isEmpty());
    }

    private static List<String> siteUrlsSavedByBootstrap(String configured) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("https");
        when(request.isSecure()).thenReturn(true);
        when(request.getServerName()).thenReturn("blog.example.org");
        when(request.getServerPort()).thenReturn(8443);
        when(request.getContextPath()).thenReturn("/roller");
        when(request.getRequestURI()).thenReturn("/roller/roller-ui/install/install!bootstrap.rol");
        when(request.getRequestURL()).thenReturn(new StringBuffer(
                "https://blog.example.org:8443/roller/roller-ui/install/install!bootstrap.rol"));
        Install action = new Install();
        action.setServletRequest(request);

        PropertiesManager properties = mock(PropertiesManager.class);
        if (configured != null) {
            when(properties.getProperty("site.absoluteurl"))
                    .thenReturn(new RuntimeConfigProperty("site.absoluteurl", configured));
        }
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<BootstrapSecurity> bootstrap = mockStatic(BootstrapSecurity.class)) {
            factory.when(WebloggerFactory::isBootstrapped).thenReturn(false);
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            bootstrap.when(() -> BootstrapSecurity.isValid(any())).thenReturn(true);
            assertEquals("success", action.bootstrap());
        }
        ArgumentCaptor<RuntimeConfigProperty> saved =
                ArgumentCaptor.forClass(RuntimeConfigProperty.class);
        verify(properties, atLeast(0)).saveProperty(saved.capture());
        return saved.getAllValues().stream()
                .filter(p -> "site.absoluteurl".equals(p.getName()))
                .map(RuntimeConfigProperty::getValue)
                .collect(Collectors.toList());
    }
}
