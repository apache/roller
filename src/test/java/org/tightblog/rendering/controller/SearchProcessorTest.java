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
import org.tightblog.TestUtils;
import org.tightblog.config.WebConfig;
import org.tightblog.domain.SharedTheme;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.SearchResultsModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SearchProcessorTest {

    private SearchProcessor processor;
    private Weblog weblog;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private WeblogDao mockWD;
    private WeblogTheme mockWeblogTheme;
    private ServletOutputStream mockSOS;
    private ThemeManager mockThemeManager;
    private ThymeleafRenderer mockRenderer;
    private SharedTheme sharedTheme;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() throws IOException {
        mockRequest = TestUtils.createMockServletRequestForWeblogSearchRequest();

        mockWD = mock(WeblogDao.class);
        weblog = new Weblog();
        weblog.setHandle("myblog");
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(weblog);

        mockRenderer = mock(ThymeleafRenderer.class);
        when(mockRenderer.render(any(), any()))
                .thenReturn(new CachedContent(Template.Role.WEBLOG));

        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        mockWeblogTheme = mock(WeblogTheme.class);
        mockThemeManager = mock(ThemeManager.class);
        when(mockThemeManager.getWeblogTheme(any())).thenReturn(mockWeblogTheme);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

        Function<WeblogPageRequest, SiteModel> siteModelFactory = new WebConfig().siteModelFactory();

        processor = new SearchProcessor(mockWD, mockRenderer, mockThemeManager, mock(SearchResultsModel.class),
                siteModelFactory);

        mockApplicationContext = mock(ApplicationContext.class);
        // return empty model map in getModelMap()
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
        processor.setApplicationContext(mockApplicationContext);

        mockSOS = mock(ServletOutputStream.class);
        mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
        processor.getSearchResults(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testCorrectTemplateChosen() throws IOException {
        WeblogTemplate searchResultsTemplate = new WeblogTemplate();
        searchResultsTemplate.setRole(Template.Role.SEARCH_RESULTS);

        WeblogTemplate weblogTemplate = new WeblogTemplate();
        weblogTemplate.setRole(Template.Role.WEBLOG);

        processor.getSearchResults(mockRequest, mockResponse);

        // verify weblog retrieved, NOT FOUND returned due to no matching template
        verify(mockThemeManager).getWeblogTheme(weblog);
        verify(mockWeblogTheme).getTemplateByRole(Template.Role.WEBLOG);
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // weblogTheme should be chosen because no template of type SEARCH_RESULTS
        when(mockWeblogTheme.getTemplateByRole(Template.Role.WEBLOG)).thenReturn(weblogTemplate);

        Mockito.clearInvocations(mockThemeManager, mockWeblogTheme, mockResponse);
        processor.getSearchResults(mockRequest, mockResponse);
        verify(mockWeblogTheme).getTemplateByRole(Template.Role.WEBLOG);
        verify(mockResponse, never()).sendError(SC_NOT_FOUND);
        verify(mockResponse).setContentType("text/html");

        when(mockWeblogTheme.getTemplateByRole(Template.Role.SEARCH_RESULTS)).thenReturn(searchResultsTemplate);

        // test proper page models provided to renderer
        URLModel mockURLModel = mock(URLModel.class);
        when(mockURLModel.getModelName()).thenReturn("model");
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(mockURLModel);
        when(mockApplicationContext.getBean(eq("searchModelSet"), eq(Set.class))).thenReturn(pageModelSet);

        Mockito.clearInvocations(mockThemeManager, mockWeblogTheme, mockResponse, mockSOS, mockRenderer);
        processor.getSearchResults(mockRequest, mockResponse);
        // search results template should now be retrieved, backup weblog template call not occurring
        verify(mockWeblogTheme, never()).getTemplateByRole(Template.Role.WEBLOG);

        // test calls on Response object made
        verify(mockResponse).setContentLength(0);
        verify(mockSOS).write(any());
        // should complete with no error
        verify(mockResponse, never()).sendError(SC_NOT_FOUND);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(any(), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));

        // try a site-wide theme
        sharedTheme.setSiteWide(true);
        Mockito.clearInvocations(mockRenderer, mockResponse);
        processor.getSearchResults(mockRequest, mockResponse);
        verify(mockRenderer).render(any(), stringObjectMapCaptor.capture());
        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        // test 404 if exception during rendering
        Mockito.clearInvocations(mockResponse);
        doThrow(new IllegalArgumentException()).when(mockRenderer).render(any(), any());
        processor.getSearchResults(mockRequest, mockResponse);
        verify(mockResponse, never()).setContentType(anyString());
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
