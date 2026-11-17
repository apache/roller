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

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The administrator who registers first is the one setting the site up, so
 * that registration records the site's absolute URL when none is configured.
 * Later registrations never touch it.
 */
class RegisterSiteUrlTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<BootstrapSecurity> bootstrap;
    private MockedStatic<RollerContext> context;
    private PropertiesManager properties;

    @BeforeEach
    void setUp() throws Exception {
        properties = mock(PropertiesManager.class);
        UserManager users = mock(UserManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        when(weblogger.getUserManager()).thenReturn(users);
        when(properties.getProperty("users.registration.enabled"))
                .thenReturn(new RuntimeConfigProperty("users.registration.enabled", "true"));

        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        bootstrap = mockStatic(BootstrapSecurity.class);
        context = mockStatic(RollerContext.class);
        context.when(RollerContext::getPasswordEncoder)
                .thenReturn(NoOpPasswordEncoder.getInstance());
    }

    @AfterEach
    void tearDown() {
        context.close();
        bootstrap.close();
        factory.close();
    }

    private static Register register(String scheme, String host, int port) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn(scheme);
        when(request.isSecure()).thenReturn("https".equals(scheme));
        when(request.getServerName()).thenReturn(host);
        when(request.getServerPort()).thenReturn(port);
        when(request.getContextPath()).thenReturn("/roller");
        when(request.getRequestURI()).thenReturn("/roller/roller-ui/register!save.rol");
        when(request.getRequestURL()).thenReturn(new StringBuffer(
                scheme + "://" + host + ":" + port + "/roller/roller-ui/register!save.rol"));
        when(request.getSession()).thenReturn(mock(HttpSession.class));

        Register action = new Register();
        action.setServletRequest(request);
        action.getBean().setUserName("admin");
        action.getBean().setScreenName("Admin");
        action.getBean().setEmailAddress("admin@example.com");
        action.getBean().setPasswordText("s3cret-Passw0rd");
        action.getBean().setPasswordConfirm("s3cret-Passw0rd");
        return action;
    }

    private List<String> savedSiteUrls() throws Exception {
        ArgumentCaptor<RuntimeConfigProperty> saved =
                ArgumentCaptor.forClass(RuntimeConfigProperty.class);
        verify(properties, atLeast(0)).saveProperty(saved.capture());
        return saved.getAllValues().stream()
                .filter(p -> "site.absoluteurl".equals(p.getName()))
                .map(RuntimeConfigProperty::getValue)
                .collect(Collectors.toList());
    }

    @Test
    void firstAdministratorRecordsSiteUrl() throws Exception {
        bootstrap.when(BootstrapSecurity::isCompleted).thenReturn(false);
        bootstrap.when(() -> BootstrapSecurity.isValid(any())).thenReturn(true);

        assertEquals("success", register("https", "blog.example.org", 443).save());
        assertEquals(List.of("https://blog.example.org/roller"), savedSiteUrls());
    }

    @Test
    void firstAdministratorKeepsConfiguredSiteUrl() throws Exception {
        bootstrap.when(BootstrapSecurity::isCompleted).thenReturn(false);
        bootstrap.when(() -> BootstrapSecurity.isValid(any())).thenReturn(true);
        when(properties.getProperty("site.absoluteurl")).thenReturn(
                new RuntimeConfigProperty("site.absoluteurl", "https://configured.example.org"));

        assertEquals("success", register("https", "blog.example.org", 443).save());
        assertTrue(savedSiteUrls().isEmpty(), "a configured URL must not be replaced");
    }

    @Test
    void laterRegistrationsDoNotRecordSiteUrl() throws Exception {
        bootstrap.when(BootstrapSecurity::isCompleted).thenReturn(true);
        when(properties.getProperty("site.absoluteurl"))
                .thenReturn(new RuntimeConfigProperty("site.absoluteurl", ""));

        assertEquals("success", register("http", "other.example.net", 80).save());
        assertTrue(savedSiteUrls().isEmpty(),
                "only the initial administrator's registration may set the URL");
    }
}
