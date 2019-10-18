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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.tightblog.TestUtils;
import org.tightblog.WebloggerTest;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

    private static Logger log = LoggerFactory.getLogger(PreviewProcessorTest.class);

    private PreviewProcessor processor;
    private Weblog weblog;
    private SharedTheme sharedTheme;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private WeblogEntryManager mockWEM;
    private UserManager mockUM;
    private WeblogDao mockWD;
    private ThymeleafRenderer mockRenderer;
    private ThemeManager mockThemeManager;
    private WeblogTheme mockTheme;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() {
        try {
            mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();

            mockWD = mock(WeblogDao.class);
            weblog = new Weblog();
            when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(weblog);

            mockUM = mock(UserManager.class);
            when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.EDIT_DRAFT)).thenReturn(true);

            mockRenderer = mock(ThymeleafRenderer.class);
            CachedContent cachedContent = new CachedContent(Template.Role.JAVASCRIPT);
            when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

            mockThemeManager = mock(ThemeManager.class);
            mockWEM = mock(WeblogEntryManager.class);
            WeblogEntry entry = new WeblogEntry();
            entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
            when(mockWEM.getWeblogEntryByAnchor(weblog, "entry-anchor")).thenReturn(entry);

            mockTheme = mock(WeblogTheme.class);
            when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
            sharedTheme = new SharedTheme();
            sharedTheme.setSiteWide(false);
            when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

            Function<WeblogPageRequest, SiteModel> siteModelFactory = new WebConfig().siteModelFactory();

            processor = new PreviewProcessor(mockWD, mockRenderer, mockThemeManager, mockUM, mock(PageModel.class),
                    mockWEM, siteModelFactory);

            mockApplicationContext = mock(ApplicationContext.class);
            when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
            processor.setApplicationContext(mockApplicationContext);

            mockResponse = mock(HttpServletResponse.class);
            ServletOutputStream mockSOS = mock(ServletOutputStream.class);
            when(mockResponse.getOutputStream()).thenReturn(mockSOS);

            MockitoAnnotations.initMocks(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void test403WithUnauthorizedUser() throws IOException {
        when(mockUM.checkWeblogRole("bob", weblog, WeblogRole.EDIT_DRAFT)).thenReturn(false);
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void test404OnUnknownTheme() throws IOException {
        when(mockRequest.getParameter("theme")).thenReturn("testTheme");
        when(mockThemeManager.getSharedTheme(any())).thenThrow(new IllegalArgumentException());
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void testThemePreviewCausesThemeSwitch() throws IOException {
        weblog.setTheme("currentThemeId");

        // no theme override so use weblog's theme
        when(mockRequest.getParameter("theme")).thenReturn(null);
        Template template = new SharedTemplate();
        when(mockTheme.getTemplateByRole(any())).thenReturn(template);

        processor.getPreviewPage(mockRequest, mockResponse);
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(weblog, wpr.getWeblog());
        assertEquals(template, wpr.getTemplate());
        assertFalse(wpr.getWeblog().isUsedForThemePreview());

        // now check that preview theme used instead when defined
        SharedTheme themeToPreview = new SharedTheme();
        themeToPreview.setId("previewThemeId");
        String previewThemeName = "previewThemeName";
        themeToPreview.setName(previewThemeName);
        when(mockRequest.getParameter("theme")).thenReturn(previewThemeName);
        when(mockThemeManager.getSharedTheme(previewThemeName)).thenReturn(themeToPreview);

        Mockito.clearInvocations(mockResponse, mockRenderer);
        processor.getPreviewPage(mockRequest, mockResponse);
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals("previewThemeId", wpr.getWeblog().getTheme());
        assertTrue(wpr.getWeblog().isUsedForThemePreview());
    }

    @Test
    public void testCorrectTemplatesChosen() throws IOException {
        // Custom External retrieved
        mockRequest = TestUtils.createMockServletRequestForCustomPageRequest();
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Template.Role.CUSTOM_EXTERNAL);
        sharedTemplate.setName("my-custom-page");
        when(mockTheme.getTemplateByName(any())).thenReturn(sharedTemplate);

        processor.getPreviewPage(mockRequest, mockResponse);
        ArgumentCaptor<Template> templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        Template results = templateCaptor.getValue();
        assertEquals("my-custom-page", results.getName());
        assertEquals(Template.Role.CUSTOM_EXTERNAL, results.getRole());

        // Custom Internal blocked -- 404 returned
        Mockito.clearInvocations(mockResponse, mockRenderer);
        sharedTemplate.setRole(Template.Role.CUSTOM_INTERNAL);
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);

        // now test with a null template
        Mockito.clearInvocations(mockResponse);
        when(mockTheme.getTemplateByName(any())).thenReturn(null);
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);

        // Weblog template retrieved if no entry anchor
        mockRequest = TestUtils.createMockServletRequestForWeblogHomePageRequest();
        Mockito.clearInvocations(mockResponse, mockRenderer);
        SharedTemplate weblogTemplate = new SharedTemplate();
        weblogTemplate.setRole(Template.Role.WEBLOG);
        weblogTemplate.setName("myweblogtemplate");
        when(mockTheme.getTemplateByRole(Template.Role.WEBLOG)).thenReturn(weblogTemplate);
        processor.getPreviewPage(mockRequest, mockResponse);
        templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals("myweblogtemplate", results.getName());
        assertEquals(Template.Role.WEBLOG, results.getRole());

        // Permalink template retrieved for a weblog entry
        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();
        Mockito.clearInvocations(mockResponse, mockRenderer);
        SharedTemplate permalinkTemplate = new SharedTemplate();
        permalinkTemplate.setRole(Template.Role.PERMALINK);
        permalinkTemplate.setName("mypermalinktemplate");
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(permalinkTemplate);
        processor.getPreviewPage(mockRequest, mockResponse);
        templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals("mypermalinktemplate", results.getName());
        assertEquals(Template.Role.PERMALINK, results.getRole());

        // Weblog template retrieved for a weblog entry if no permalink template
        when(mockTheme.getTemplateByRole(Template.Role.PERMALINK)).thenReturn(null);
        Mockito.clearInvocations(mockResponse, mockRenderer);
        processor.getPreviewPage(mockRequest, mockResponse);
        templateCaptor = ArgumentCaptor.forClass(Template.class);
        verify(mockRenderer).render(templateCaptor.capture(), any());
        results = templateCaptor.getValue();
        assertEquals("myweblogtemplate", results.getName());
        assertEquals(Template.Role.WEBLOG, results.getRole());

        // test 404 if exception during rendering
        WebloggerTest.logExpectedException(log, "IllegalArgumentException");
        Mockito.clearInvocations(mockResponse);
        doThrow(new IllegalArgumentException()).when(mockRenderer).render(any(), any());
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse, never()).setContentType(anyString());
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);

        // not found returned if no weblog entry for given anchor
        when(mockWEM.getWeblogEntryByAnchor(weblog, "entry-anchor")).thenReturn(null);
        Mockito.clearInvocations(mockResponse);
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
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
        processor.getPreviewPage(mockRequest, mockResponse);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        Mockito.clearInvocations(mockResponse, mockRenderer);
        // testing that non-sitewide themes just get "model" added to the rendering map.
        sharedTheme.setSiteWide(false);
        processor.getPreviewPage(mockRequest, mockResponse);
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());

        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));
    }
}
