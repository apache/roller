/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.service;

import org.junit.Before;
import org.junit.Test;
import org.thymeleaf.templateresource.StringTemplateResource;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.domain.Template;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.dao.WeblogTemplateDao;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThemeTemplateResolverTest {

    private ThemeManager mockThemeManager;
    private ThemeTemplateResolver themeTemplateResolver;
    private WeblogTemplateDao mockWeblogTemplateDao;

    @Before
    public void initialize() {
        mockThemeManager = mock(ThemeManager.class);
        mockWeblogTemplateDao = mock(WeblogTemplateDao.class);
        themeTemplateResolver = new ThemeTemplateResolver(mockThemeManager, mockWeblogTemplateDao);
    }

    @Test
    public void testReturnNullIfInvalidResourceId() {
        assertNull(themeTemplateResolver.computeTemplateResource(null, null, null,
                null, null, null));

        assertNull(themeTemplateResolver.computeTemplateResource(null, null, "",
                null, null, null));

        // resourceId should have no more than one colon
        assertNull(themeTemplateResolver.computeTemplateResource(null, null, "abc:def:ghi",
                null, null, null));

        // return null if no template found
        assertNull(themeTemplateResolver.computeTemplateResource(null, null, "abc:def",
                null, null, null));
    }

    @Test
    public void testReturnSharedThemeTemplate() {
        SharedTheme sharedTheme = new SharedTheme();
        sharedTheme.setId("themeId");
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setName("templateId");
        sharedTemplate.setTemplate("shared template contents");
        sharedTemplate.setRole(Template.Role.WEBLOG);
        sharedTheme.addTemplate(sharedTemplate);
        when(mockThemeManager.getSharedTheme("themeId")).thenReturn(sharedTheme);
        StringTemplateResource resource = (StringTemplateResource) themeTemplateResolver.computeTemplateResource(null,
                null, "themeId:templateId", null, null, null);
        assertEquals("shared template contents", resource.getDescription());
    }

    @Test
    public void testReturnWeblogTemplate() {
        WeblogTemplate weblogTemplate = new WeblogTemplate();
        String newUUID = UUID.randomUUID().toString();
        weblogTemplate.setId(newUUID);
        weblogTemplate.setRole(Template.Role.CUSTOM_EXTERNAL);
        weblogTemplate.setTemplate("weblog template contents");
        when(mockWeblogTemplateDao.findById(newUUID)).thenReturn(Optional.of(weblogTemplate));
        StringTemplateResource resource = (StringTemplateResource) themeTemplateResolver.computeTemplateResource(null,
                null, newUUID, null, null, null);
        assertEquals("weblog template contents", resource.getDescription());
    }
}
