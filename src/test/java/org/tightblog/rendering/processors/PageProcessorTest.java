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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.WebloggerTest;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Template.ComponentType;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PageProcessorTest {

    private static Logger log = LoggerFactory.getLogger(PageProcessorTest.class);

    private PageProcessor processor;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private WeblogPageRequest pageRequest;
    private WeblogEntryManager mockWEM;
    private Weblog weblog;
    private SharedTheme sharedTheme;
    private WebloggerProperties webloggerProperties;

    private LazyExpiringCache mockCache;
    private WeblogManager mockWM;
    private WeblogRepository mockWR;
    private ThymeleafRenderer mockRenderer;
    private ThemeManager mockThemeManager;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    // not done as a @before as not all tests need these mocks
    private void initializeMocks() {
        WebloggerPropertiesRepository mockPropertiesRepository = mock(WebloggerPropertiesRepository.class);
        webloggerProperties = new WebloggerProperties();
        webloggerProperties.setLastWeblogChange(Instant.now().minus(2, ChronoUnit.DAYS));
        when(mockPropertiesRepository.findOrNull()).thenReturn(webloggerProperties);
        mockRequest = mock(HttpServletRequest.class);
        // default is page always needs refreshing
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli());
        mockResponse = mock(HttpServletResponse.class);
        WeblogPageRequest.Creator wprCreator = mock(WeblogPageRequest.Creator.class);
        pageRequest = new WeblogPageRequest();
        when(wprCreator.create(mockRequest)).thenReturn(pageRequest);
        mockWEM = mock(WeblogEntryManager.class);
        mockWR = mock(WeblogRepository.class);
        mockCache = mock(LazyExpiringCache.class);
        mockWM = mock(WeblogManager.class);
        mockRenderer = mock(ThymeleafRenderer.class);
        mockThemeManager = mock(ThemeManager.class);
        processor = new PageProcessor(mockWR, mockPropertiesRepository, mockCache, mockWM, mockWEM,
                mockRenderer, mockThemeManager);
        processor.setWeblogPageRequestCreator(wprCreator);
        mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
        processor.setApplicationContext(mockApplicationContext);
        weblog = new Weblog();
        weblog.setLastModified(Instant.now().minus(2, ChronoUnit.DAYS));
        when(mockWR.findByHandleAndVisibleTrue(any())).thenReturn(weblog);
        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        processor = Mockito.spy(processor);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        initializeMocks();
        pageRequest.setWeblogHandle("myhandle");
        when(mockWR.findByHandleAndVisibleTrue("myhandle")).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void testReceive304NotModifiedContent() throws IOException {
        initializeMocks();
        sharedTheme.setSiteWide(true);

        // date header more recent than last change, so should return 304
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().toEpochMilli());

        processor.handleRequest(mockRequest, mockResponse);
        verify(mockRequest).getDateHeader(any());
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache).incrementRequestsHandledBy304();
    }

    @Test
    public void testCachedPageReturned() throws IOException {
        initializeMocks();

        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);

        CachedContent cachedContent = new CachedContent(ComponentType.WEBLOG);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockCache.get(any(), any())).thenReturn(cachedContent);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        processor.handleRequest(mockRequest, mockResponse);

        // verify cached content being returned
        verify(mockWM).incrementHitCount(weblog);
        verify(mockResponse).setContentType(ComponentType.WEBLOG.getContentType());
        verify(mockResponse).setContentLength(7);
        verify(mockResponse).setDateHeader("Last-Modified", twoDaysAgo.toEpochMilli());
        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockSOS).write("mytest1".getBytes());
    }

    @Test
    public void testNonExistentTemplateRequestReturns404() throws IOException {
        initializeMocks();

        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
        when(mockTheme.getTemplateByPath(any())).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache, never()).put(anyString(), any());
    }

    @Test
    public void testCustomInternalTemplateRequestReturns404() throws IOException {
        initializeMocks();

        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);

        // test custom internal template returns 404
        WeblogTemplate wt = new WeblogTemplate();
        wt.setRole(ComponentType.CUSTOM_INTERNAL);
        when(mockTheme.getTemplateByPath(any())).thenReturn(wt);

        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);
        // CUSTOM_INTERNAL has incrementHitCounts = false
        verify(mockWM, never()).incrementHitCount(weblog);

        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache, never()).put(anyString(), any());
    }

    @Test
    public void testUncachedPageRendering() throws IOException {
        initializeMocks();

        pageRequest.setCustomPageName("mytemplate");
        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);

        WeblogTemplate wt = new WeblogTemplate();
        wt.setRole(ComponentType.CUSTOM_EXTERNAL);
        when(mockTheme.getTemplateByPath(any())).thenReturn(wt);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        CachedContent cachedContent = new CachedContent(ComponentType.CUSTOM_EXTERNAL);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(wt, pageRequest.getTemplate());
        // CUSTOM_EXTERNAL has incrementHitCounts = true
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache).put(anyString(), any());
        verify(mockRenderer).render(eq(pageRequest.getTemplate()), any());
        verify(mockResponse).setContentType(ComponentType.CUSTOM_EXTERNAL.getContentType());
        verify(mockResponse).setContentLength("mytest1".length());
        verify(mockSOS).write(any());

        // test permalink template, no weblog page hit
        sharedTheme.setSiteWide(true);
        webloggerProperties.setLastWeblogChange(Instant.now());
        pageRequest.setCustomPageName(null);
        pageRequest.setWeblogEntryAnchor("myentry");

        WeblogTemplate wt2 = new WeblogTemplate();
        wt2.setRole(ComponentType.PERMALINK);
        WeblogEntry entry = new WeblogEntry();
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(entry);
        when(mockTheme.getTemplateByAction(ComponentType.PERMALINK)).thenReturn(wt2);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(entry, pageRequest.getWeblogEntry());
        assertEquals(wt2, pageRequest.getTemplate());
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).put(anyString(), any());
        verify(mockSOS).write(any());

        // test fallback to weblog template if no permalink one
        WeblogTemplate wt3 = new WeblogTemplate();
        wt3.setRole(ComponentType.WEBLOG);
        when(mockTheme.getTemplateByAction(ComponentType.PERMALINK)).thenReturn(null);
        when(mockTheme.getTemplateByAction(ComponentType.PERMALINK)).thenReturn(wt3);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(wt3, pageRequest.getTemplate());

        // test 404 if weblog entry not published
        entry.setStatus(WeblogEntry.PubStatus.DRAFT);
        pageRequest.setTemplate(null);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test 404 if weblog entry null
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(null);
        pageRequest.setTemplate(null);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test 404 if exception during rendering
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(entry);
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        doThrow(new IllegalArgumentException()).when(mockRenderer).render(any(), any());

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSOS);

        WebloggerTest.logExpectedException(log, "IllegalArgumentException");
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException {
        initializeMocks();
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(new PageModel());
        when(mockApplicationContext.getBean(eq("pageModelSet"), eq(Set.class))).thenReturn(pageModelSet);
        Set<Model> siteModelSet = new HashSet<>();
        siteModelSet.add(new SiteModel());
        when(mockApplicationContext.getBean(eq("siteModelSet"), eq(Set.class))).thenReturn(siteModelSet);
        // setting custom page name to allow for a template to be chosen and hence the rendering to occur
        pageRequest.setCustomPageName("mycustompage");
        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(any())).thenReturn(mockTheme);
        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Template.ComponentType.CUSTOM_EXTERNAL);

        CachedContent cachedContent = new CachedContent(ComponentType.CUSTOM_EXTERNAL);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        // testing that sitewide themes get the "site" & (page) "model" added to the rendering map.
        sharedTheme.setSiteWide(true);
        when(mockTheme.getTemplateByPath("mycustompage")).thenReturn(sharedTemplate);
        processor.handleRequest(mockRequest, mockResponse);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        // testing that non-sitewide themes just get "model" added to the rendering map.
        sharedTheme.setSiteWide(false);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());

        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));
    }

    @Test
    public void testCommentFormsSkipCache() throws IOException {
        initializeMocks();
        WeblogEntryComment wec = new WeblogEntryComment();
        when(mockRequest.getAttribute("commentForm")).thenReturn(wec);

        pageRequest.setCustomPageName("mytemplate");
        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
        when(mockTheme.getTemplateByPath(any())).thenReturn(null);

        processor.handleRequest(mockRequest, mockResponse);
        verify(mockWM, never()).incrementHitCount(any());
        verify(mockCache, never()).get(any(), any());
        verify(mockCache, never()).put(any(), any());
    }

    @Test
    public void testGenerateKey() {
        initializeMocks();

        WeblogPageRequest request = new WeblogPageRequest(); // mock(WeblogPageRequest.class);
        request.setWeblogHandle("bobsblog");
        request.setWeblogEntryAnchor("neatoentry");
        request.setAuthenticatedUser("bob");
        request.setDeviceType(DeviceType.TABLET);
        request.setSiteWide(false);

        String test1 = processor.generateKey(request);
        assertEquals("bobsblog/entry/neatoentry/user=bob/deviceType=TABLET", test1);

        request.setWeblogEntryAnchor(null);
        request.setAuthenticatedUser(null);
        request.setDeviceType(DeviceType.MOBILE);
        request.setCustomPageName("mytemplate");
        request.setWeblogDate("20171006");
        request.setCategory("finance");
        request.setTag("taxes");
        request.setQueryString("a=foo&b=123");
        request.setPageNum(5);
        request.setSiteWide(true);

        Instant testTime = Instant.now();
        webloggerProperties.setLastWeblogChange(testTime);

        test1 = processor.generateKey(request);
        assertEquals("bobsblog/page/mytemplate/date/20171006/cat/finance/tag/" +
                "taxes/page=5/query=a=foo&b=123/deviceType=MOBILE/lastUpdate=" + testTime.toEpochMilli(), test1);
    }

}
