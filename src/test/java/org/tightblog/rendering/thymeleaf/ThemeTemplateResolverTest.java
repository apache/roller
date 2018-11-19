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
package org.tightblog.rendering.thymeleaf;

import org.junit.Before;
import org.junit.Test;
import org.thymeleaf.templateresource.StringTemplateResource;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.domain.Template;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.repository.WeblogTemplateRepository;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ThemeTemplateResolverTest {

    private ThemeManager mockThemeManager;
    private ThemeTemplateResolver themeTemplateResolver;
    private WeblogTemplateRepository mockWeblogTemplateRepository;

    @Before
    public void initialize() {
        mockThemeManager = mock(ThemeManager.class);
        mockWeblogTemplateRepository = mock(WeblogTemplateRepository.class);
        themeTemplateResolver = new ThemeTemplateResolver(mockThemeManager, mockWeblogTemplateRepository);
    }

    @Test
    public void testReturnNullIfNoResourceId() {
        assertNull(themeTemplateResolver.computeTemplateResource(null, null, null,
                null, null, null));
    }

    @Test
    public void testReturnSharedTheme() {
        SharedTheme sharedTheme = new SharedTheme();
        sharedTheme.setId("foo");
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setName("bar");
        sharedTemplate.setTemplate("shared template contents");
        sharedTemplate.setRole(Template.ComponentType.WEBLOG);
        sharedTheme.addTemplate(sharedTemplate);
        when(mockThemeManager.getSharedTheme("foo")).thenReturn(sharedTheme);
        StringTemplateResource resource = (StringTemplateResource) themeTemplateResolver.computeTemplateResource(null,
                null, "foo:bar", null, null, null);
        assertEquals("shared template contents", resource.getDescription());
    }

    @Test
    public void testReturnWeblogTheme() {
        WeblogTemplate weblogTemplate = new WeblogTemplate();
        weblogTemplate.setId("1234");
        weblogTemplate.setRole(Template.ComponentType.CUSTOM_EXTERNAL);
        weblogTemplate.setTemplate("weblog template contents");
        when(mockWeblogTemplateRepository.findById("1234")).thenReturn(Optional.of(weblogTemplate));
        StringTemplateResource resource = (StringTemplateResource) themeTemplateResolver.computeTemplateResource(null,
                null, "1234", null, null, null);
        assertEquals("weblog template contents", resource.getDescription());
    }
}
