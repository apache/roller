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
package org.tightblog.rendering.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tightblog.config.WebConfig;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.dao.WeblogDao;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreviewControllerTest {

    private static final String BLOG_HANDLE = "myblog";
    private static final String ENTRY_ANCHOR = "entry-anchor";

    private PreviewController controller;
    private Weblog weblog;
    private SharedTheme sharedTheme;

    private WeblogEntryManager mockWEM;
    private UserManager mockUM;
    private WeblogDao mockWD;
    private ThymeleafRenderer mockRenderer;
    private WeblogTheme mockTheme;
    private ApplicationContext mockApplicationContext;
    private Principal mockPrincipal;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() {
        try {
            mockPrincipal = mock(Principal.class);
            when(mockPrincipal.getName()).thenReturn("bob");

            mockWD = mock(WeblogDao.class);
            weblog = new Weblog();
            when(mockWD.findByHandleAndVisibleTrue(BLOG_HANDLE)).thenReturn(weblog);

            mockUM = mock(UserManager.class);
            when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.EDIT_DRAFT)).thenReturn(true);

            mockRenderer = mock(ThymeleafRenderer.class);
            CachedContent cachedContent = new CachedContent(Template.Role.JAVASCRIPT);
            when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

            ThemeManager mockThemeManager = mock(ThemeManager.class);
            mockWEM = mock(WeblogEntryManager.class);
            WeblogEntry entry = new WeblogEntry();
            entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
            when(mockWEM.getWeblogEntryByAnchor(weblog, ENTRY_ANCHOR)).thenReturn(entry);

            mockTheme = mock(WeblogTheme.class);
            when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
            sharedTheme = new SharedTheme();
            sharedTheme.setSiteWide(false);
            when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

            Function<WeblogPageRequest, SiteModel> siteModelFactory = new WebConfig().siteModelFactory();

            controller = new PreviewController(mockWD, mockRenderer, mockThemeManager, mockUM, mock(PageModel.class),
                    mockWEM, siteModelFactory);

            mockApplicationContext = mock(ApplicationContext.class);
            when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
            controller.setApplicationContext(mockApplicationContext);

            MockitoAnnotations.initMocks(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
        ResponseEntity<Resource> result = controller.getEntryPreview("myblog", "myanchor", mockPrincipal, null);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void test403WithUnauthorizedUser() throws IOException {
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.EDIT_DRAFT)).thenReturn(false);
        ResponseEntity<Resource> result = controller.getEntryPreview("myblog", "myanchor", mockPrincipal, null);
        assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
    }

    @Test
    public void testCorrectTemplatesChosen() throws IOException {
        SharedTemplate weblogTemplate = new SharedTemplate();
        weblogTemplate.setRole(Template.Role.WEBLOG);
        weblogTemplate.setName("myweblogtemplate");
        when(mockTheme.getTemplateByRole(Template.Role.WEBLOG)).thenReturn(weblogTemplate);

        // Permalink template retrieved for a weblog entry
        Mockito.clearInvocations(mockRenderer);
        SharedTemplate permalinkTemplate = new SharedTemplate();
        permalinkTemplate.setRole(Template.Role.PERMALINK);
        permalinkTemplate.setName("mypermalinktemplate");
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(permalinkTemplate);
        controller.getEntryPreview(BLOG_HANDLE, ENTRY_ANCHOR, mockPrincipal, null);
        ArgumentCaptor<Template> templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        Template results = templateCaptor.getValue();
        assertEquals(permalinkTemplate.getName(), results.getName());
        assertEquals(Template.Role.PERMALINK, results.getRole());

        // Weblog template retrieved for a weblog entry if no permalink template
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(null);
        Mockito.clearInvocations(mockRenderer);
        ResponseEntity<Resource> result = controller.getEntryPreview(BLOG_HANDLE, ENTRY_ANCHOR, mockPrincipal, null);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals(weblogTemplate.getName(), results.getName());
        assertEquals(Template.Role.WEBLOG, results.getRole());

        // not found returned if no weblog entry for given anchor
        when(mockWEM.getWeblogEntryByAnchor(weblog, ENTRY_ANCHOR)).thenReturn(null);
        result = controller.getEntryPreview(BLOG_HANDLE, ENTRY_ANCHOR, mockPrincipal, null);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException {
        URLModel mockURLModel = mock(URLModel.class);
        when(mockURLModel.getModelName()).thenReturn("model");
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(mockURLModel);
        when(mockApplicationContext.getBean(eq("pageModelSet"), eq(Set.class))).thenReturn(pageModelSet);
        // setting custom page name to allow for a template to be chosen and hence the rendering to occur
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Template.Role.CUSTOM_EXTERNAL);

        // testing that sitewide themes get the "site" & (page) "model" added to the rendering map.
        sharedTheme.setSiteWide(true);
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(sharedTemplate);
        controller.getEntryPreview(BLOG_HANDLE, ENTRY_ANCHOR, mockPrincipal, null);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        Mockito.clearInvocations(mockRenderer);
        // testing that non-sitewide themes just get "model" added to the rendering map.
        sharedTheme.setSiteWide(false);
        controller.getEntryPreview(BLOG_HANDLE, ENTRY_ANCHOR, mockPrincipal, null);
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());

        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));
    }
}
