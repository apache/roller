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

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.tightblog.domain.SharedTheme;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.repository.WeblogRepository;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SearchProcessorTest {

    private SearchProcessor processor;
    private WeblogPageRequest pageRequest;
    private Weblog weblog;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private WeblogRepository mockWR;
    private WeblogTheme mockWeblogTheme;
    private ServletOutputStream mockSOS;
    private ThymeleafRenderer mockRenderer;
    private SharedTheme sharedTheme;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    private void initializeMocks() throws Exception {
        mockSOS = mock(ServletOutputStream.class);

        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        WeblogPageRequest.Creator wprCreator = mock(WeblogPageRequest.Creator.class);
        pageRequest = new WeblogPageRequest();
        when(wprCreator.create(mockRequest)).thenReturn(pageRequest);

        mockWR = mock(WeblogRepository.class);

        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);

        ThemeManager mockThemeManager = mock(ThemeManager.class);
        mockWeblogTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(any())).thenReturn(mockWeblogTheme);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

        mockApplicationContext = mock(ApplicationContext.class);
        // return empty model map in getModelMap()
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());

        mockRenderer = mock(ThymeleafRenderer.class);
        when(mockRenderer.render(any(), any()))
                .thenReturn(new CachedContent(Template.ComponentType.WEBLOG));

        processor = new SearchProcessor(mockWR, mockRenderer, mockThemeManager);
        processor.setApplicationContext(mockApplicationContext);
        processor.setWeblogPageRequestCreator(wprCreator);
        weblog = new Weblog();
        when(mockWR.findByHandleAndVisibleTrue(any())).thenReturn(weblog);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test404OnMissingWeblog() throws Exception {
        initializeMocks();
        pageRequest.setWeblogHandle("myhandle");
        when(mockWR.findByHandleAndVisibleTrue("myhandle")).thenReturn(null);
        processor.getSearchResults(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testCorrectTemplateChosen() throws Exception {
        initializeMocks();

        WeblogTemplate wtSR = new WeblogTemplate();
        wtSR.setRole(Template.ComponentType.SEARCH_RESULTS);

        WeblogTemplate wtWL = new WeblogTemplate();
        wtWL.setRole(Template.ComponentType.WEBLOG);

        processor.getSearchResults(mockRequest, mockResponse);
        // test weblog added to pageRequest, i.e., got past weblog check
        assertEquals(weblog, pageRequest.getWeblog());
        // verify NOT FOUND returned due to no matching template
        verify(mockResponse).sendError(SC_NOT_FOUND);

        Mockito.clearInvocations(mockResponse);
        when(mockWeblogTheme.getTemplateByAction(Template.ComponentType.WEBLOG)).thenReturn(wtWL);
        processor.getSearchResults(mockRequest, mockResponse);
        // wtWL should be chosen because no template of type SEARCH_RESULTS
        assertEquals(wtWL, pageRequest.getTemplate());
        // should complete with no error
        verify(mockResponse, never()).sendError(SC_NOT_FOUND);
        verify(mockResponse).setContentType("text/html");

        Mockito.clearInvocations(mockRenderer, mockResponse, mockSOS);
        when(mockWeblogTheme.getTemplateByAction(Template.ComponentType.SEARCH_RESULTS)).thenReturn(wtSR);

        // test proper page models provided to renderer
        PageModel mockPageModel = mock(PageModel.class);
        when(mockPageModel.getModelName()).thenReturn("model");
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(mockPageModel);
        when(mockApplicationContext.getBean(eq("searchModelSet"), eq(Set.class))).thenReturn(pageModelSet);
        Set<Model> siteModelSet = new HashSet<>();
        siteModelSet.add(new SiteModel());
        when(mockApplicationContext.getBean(eq("siteModelSet"), eq(Set.class))).thenReturn(siteModelSet);

        processor.getSearchResults(mockRequest, mockResponse);
        assertEquals(wtSR, pageRequest.getTemplate());
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
