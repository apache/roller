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

package org.apache.roller.weblogger.config;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The first request from a global administrator fills in an unset site URL.
 */
class SiteUrlForAdminTest {

    private MockedStatic<WebloggerFactory> factory;
    private PropertiesManager properties;

    @BeforeEach
    void setUp() {
        properties = mock(PropertiesManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        WebloggerRuntimeConfig.setRequestAbsoluteContextURL("https://blog.example.org/roller");
    }

    @AfterEach
    void tearDown() {
        WebloggerRuntimeConfig.clearRequestAbsoluteContextURL();
        factory.close();
    }

    @Test
    void anAdministratorsRequestSavesTheSiteUrl() throws Exception {
        User admin = mock(User.class);
        when(admin.hasGlobalPermission("admin")).thenReturn(true);

        WebloggerRuntimeConfig.saveAbsoluteContextURLForAdmin(admin);

        ArgumentCaptor<RuntimeConfigProperty> saved = ArgumentCaptor.forClass(RuntimeConfigProperty.class);
        verify(properties, atLeast(1)).saveProperty(saved.capture());
        List<String> urls = saved.getAllValues().stream()
                .filter(p -> "site.absoluteurl".equals(p.getName()))
                .map(RuntimeConfigProperty::getValue).collect(Collectors.toList());
        assertEquals(List.of("https://blog.example.org/roller"), urls);
    }

    @Test
    void otherUsersDontSaveIt() throws Exception {
        User editor = mock(User.class);
        when(editor.hasGlobalPermission("admin")).thenReturn(false);

        WebloggerRuntimeConfig.saveAbsoluteContextURLForAdmin(editor);
        WebloggerRuntimeConfig.saveAbsoluteContextURLForAdmin(null);

        verify(properties, never()).saveProperty(any());
    }

    @Test
    void aConfiguredSiteUrlIsKept() throws Exception {
        when(properties.getProperty("site.absoluteurl")).thenReturn(
                new RuntimeConfigProperty("site.absoluteurl", "https://configured.example.org"));
        User admin = mock(User.class);
        when(admin.hasGlobalPermission("admin")).thenReturn(true);

        WebloggerRuntimeConfig.saveAbsoluteContextURLForAdmin(admin);

        verify(properties, never()).saveProperty(any());
    }

    @Test
    void anEmptySiteUrlIsOnlySavedIfStillEmpty() throws Exception {
        when(properties.getProperty("site.absoluteurl")).thenReturn(
                new RuntimeConfigProperty("site.absoluteurl", ""));
        when(properties.compareAndSetProperty("site.absoluteurl", "",
                "https://blog.example.org/roller")).thenReturn(true);

        assertTrue(WebloggerRuntimeConfig.saveAbsoluteContextURLIfUnset(
                "https://blog.example.org/roller"));
        verify(properties).compareAndSetProperty("site.absoluteurl", "",
                "https://blog.example.org/roller");
        verify(properties, never()).saveProperty(any());
    }

    @Test
    void aSiteUrlSavedByAnotherRequestIsKept() throws Exception {
        // this request read the property while it was empty, but another
        // request saved a URL before this one could
        when(properties.getProperty("site.absoluteurl")).thenReturn(
                new RuntimeConfigProperty("site.absoluteurl", ""));
        when(properties.compareAndSetProperty(eq("site.absoluteurl"), eq(""), any()))
                .thenReturn(false);

        assertFalse(WebloggerRuntimeConfig.saveAbsoluteContextURLIfUnset(
                "https://blog.example.org/roller"));
        verify(properties, never()).saveProperty(any());
    }
}
