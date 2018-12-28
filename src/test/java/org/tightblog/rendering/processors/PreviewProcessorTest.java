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
package org.tightblog.rendering.processors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.repository.WeblogRepository;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreviewProcessorTest {

    private PreviewProcessor processor;
    private WeblogPageRequest pageRequest;
    private Weblog weblog;
    private SharedTheme sharedTheme;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private WeblogEntryManager mockWEM;
    private UserManager mockUM;
    private WeblogRepository mockWR;
    private ThymeleafRenderer mockRenderer;
    private ThemeManager mockThemeManager;
    private WeblogTheme mockTheme;
    private Principal mockPrincipal;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() {
        try {
            mockPrincipal = mock(Principal.class);
            when(mockPrincipal.getName()).thenReturn("bob");
            mockRequest = mock(HttpServletRequest.class);
            // default is page always needs refreshing
            when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli());
            mockResponse = mock(HttpServletResponse.class);
            ServletOutputStream mockSOS = mock(ServletOutputStream.class);
            when(mockResponse.getOutputStream()).thenReturn(mockSOS);
            WeblogPageRequest.Creator wprCreator = mock(WeblogPageRequest.Creator.class);
            pageRequest = new WeblogPageRequest();
            when(wprCreator.create(mockRequest)).thenReturn(pageRequest);
            mockWEM = mock(WeblogEntryManager.class);
            mockUM = mock(UserManager.class);
            weblog = new Weblog();
            weblog.setHandle("bobsblog");
            when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.EDIT_DRAFT)).thenReturn(true);
            mockWR = mock(WeblogRepository.class);
            mockRenderer = mock(ThymeleafRenderer.class);
            mockThemeManager = mock(ThemeManager.class);
            processor = new PreviewProcessor(mockWR, mockRenderer, mockThemeManager, mockUM, mockWEM);
            processor.setWeblogPageRequestCreator(wprCreator);
            when(mockWR.findByHandleAndVisibleTrue(any())).thenReturn(weblog);
            CachedContent cachedContent = new CachedContent(Template.ComponentType.JAVASCRIPT);
            when(mockRenderer.render(any(), any())).thenReturn(cachedContent);
            mockApplicationContext = mock(ApplicationContext.class);
            when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
            processor.setApplicationContext(mockApplicationContext);
            mockTheme = mock(WeblogTheme.class);
            when(mockThemeManager.getWeblogTheme(any())).thenReturn(mockTheme);
            sharedTheme = new SharedTheme();
            sharedTheme.setSiteWide(false);
            when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
            processor = Mockito.spy(processor);
            MockitoAnnotations.initMocks(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        pageRequest.setWeblogHandle("myhandle");
        when(mockWR.findByHandleAndVisibleTrue("myhandle")).thenReturn(null);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void test403WithUnauthorizedUser() throws IOException {
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.EDIT_DRAFT)).thenReturn(false);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void test404OnUnknownTheme() throws IOException {
        when(mockRequest.getParameter("theme")).thenReturn("testTheme");
        when(mockThemeManager.getSharedTheme(any())).thenThrow(new IllegalArgumentException());
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testThemePreviewCausesThemeSwitch() throws IOException {
        weblog.setTheme("currentThemeId");

        // no theme override so use weblog's theme
        when(mockRequest.getParameter("theme")).thenReturn(null);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        // gets to 404 because no template
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("currentThemeId", pageRequest.getWeblog().getTheme());

        // now check that preview theme used instead when defined
        SharedTheme themeToPreview = new SharedTheme();
        themeToPreview.setId("previewThemeId");
        String previewThemeName = "previewThemeName";
        themeToPreview.setName(previewThemeName);
        when(mockRequest.getParameter("theme")).thenReturn(previewThemeName);
        when(mockThemeManager.getSharedTheme(previewThemeName)).thenReturn(themeToPreview);
        when(mockTheme.getTemplateByAction(any())).thenReturn(null);

        Mockito.clearInvocations(mockResponse);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("previewThemeId", pageRequest.getWeblog().getTheme());
    }

    @Test
    public void testCorrectTemplatesChosen() throws IOException {
        // Custom External retrieved
        pageRequest.setCustomPageName("mycustompage");
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Template.ComponentType.CUSTOM_EXTERNAL);
        sharedTemplate.setName("mycustompage");
        when(mockTheme.getTemplateByPath(any())).thenReturn(sharedTemplate);

        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        ArgumentCaptor<Template> templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        Template results = templateCaptor.getValue();
        assertEquals("mycustompage", results.getName());
        assertEquals(Template.ComponentType.CUSTOM_EXTERNAL, results.getRole());

        // Custom Internal blocked -- 404 returned
        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        pageRequest.setTemplate(null);
        sharedTemplate.setRole(Template.ComponentType.CUSTOM_INTERNAL);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        assertNull(pageRequest.getTemplate());

        // now test with a null template
        Mockito.clearInvocations(mockResponse);
        when(mockTheme.getTemplateByPath(any())).thenReturn(null);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        assertNull(pageRequest.getTemplate());

        // Weblog template retrieved if no entry anchor
        pageRequest.setTemplate(null);
        pageRequest.setCustomPageName(null);
        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        SharedTemplate weblogTemplate = new SharedTemplate();
        weblogTemplate.setRole(Template.ComponentType.WEBLOG);
        weblogTemplate.setName("myweblogtemplate");
        when(mockTheme.getTemplateByAction(Template.ComponentType.WEBLOG)).thenReturn(weblogTemplate);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals("myweblogtemplate", results.getName());
        assertEquals(Template.ComponentType.WEBLOG, results.getRole());

        // Permalink template retrieved for a weblog entry
        pageRequest.setTemplate(null);
        pageRequest.setWeblogEntry(null);
        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        SharedTemplate permalinkTemplate = new SharedTemplate();
        permalinkTemplate.setRole(Template.ComponentType.PERMALINK);
        permalinkTemplate.setName("mypermalinktemplate");
        when(mockTheme.getTemplateByAction(Template.ComponentType.PERMALINK)).thenReturn(permalinkTemplate);
        WeblogEntry weblogEntry = new WeblogEntry();
        weblogEntry.setAnchor("entryAnchor");
        pageRequest.setWeblogEntryAnchor("entryAnchor");
        when(mockWEM.getWeblogEntryByAnchor(weblog, "entryAnchor")).thenReturn(weblogEntry);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals("mypermalinktemplate", results.getName());
        assertEquals(Template.ComponentType.PERMALINK, results.getRole());
        assertEquals(pageRequest.getWeblogEntry(), weblogEntry);

        // Weblog template retrieved for a weblog entry if no permalink template
        when(mockTheme.getTemplateByAction(Template.ComponentType.PERMALINK)).thenReturn(null);
        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals("myweblogtemplate", results.getName());
        assertEquals(Template.ComponentType.WEBLOG, results.getRole());
        assertEquals(pageRequest.getWeblogEntry(), weblogEntry);

        // not found returned if no weblog for given anchor
        pageRequest.setTemplate(null);
        when(mockWEM.getWeblogEntryByAnchor(weblog, "entryAnchor")).thenReturn(null);
        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);

        // test 404 if exception during rendering
        Mockito.clearInvocations(mockResponse);
        when(mockWEM.getWeblogEntryByAnchor(weblog, "entryAnchor")).thenReturn(weblogEntry);
        doThrow(new IllegalArgumentException()).when(mockRenderer).render(any(), any());
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockResponse, never()).setContentType(anyString());
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException {
        Set<Model> previewModelSet = new HashSet<>();
        previewModelSet.add(new PageModel());
        when(mockApplicationContext.getBean(eq("previewModelSet"), eq(Set.class))).thenReturn(previewModelSet);
        Set<Model> siteModelSet = new HashSet<>();
        siteModelSet.add(new SiteModel());
        when(mockApplicationContext.getBean(eq("siteModelSet"), eq(Set.class))).thenReturn(siteModelSet);
        // setting custom page name to allow for a template to be chosen and hence the rendering to occur
        pageRequest.setCustomPageName("mycustompage");
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Template.ComponentType.CUSTOM_EXTERNAL);

        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        // testing that sitewide themes get the "site" & (page) "model" added to the rendering map.
        sharedTheme.setSiteWide(true);
        when(mockTheme.getTemplateByPath("mycustompage")).thenReturn(sharedTemplate);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        // testing that non-sitewide themes just get "model" added to the rendering map.
        sharedTheme.setSiteWide(false);
        processor.getPreviewPage(mockRequest, mockResponse, mockPrincipal);
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());

        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));
    }
}
