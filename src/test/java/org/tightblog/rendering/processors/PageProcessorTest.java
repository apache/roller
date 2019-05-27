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
import org.springframework.mobile.device.DeviceType;
import org.tightblog.TestUtils;
import org.tightblog.WebloggerTest;
import org.tightblog.config.DynamicProperties;
import org.tightblog.config.WebConfig;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.SharedTemplate;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template.Role;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.repository.WeblogRepository;

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
import java.util.function.Function;

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
    private WeblogEntryManager mockWEM;
    private Weblog weblog;
    private SharedTheme sharedTheme;
    private WeblogTheme mockWeblogTheme;
    private DynamicProperties dp;

    private LazyExpiringCache mockCache;
    private WeblogManager mockWM;
    private WeblogRepository mockWR;
    private ThymeleafRenderer mockRenderer;
    private ServletOutputStream mockSOS;
    private ThemeManager mockThemeManager;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() throws IOException {
        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();

        mockWR = mock(WeblogRepository.class);
        weblog = new Weblog();
        weblog.setLastModified(Instant.now().minus(2, ChronoUnit.DAYS));
        when(mockWR.findByHandleAndVisibleTrue("myblog")).thenReturn(weblog);

        mockCache = mock(LazyExpiringCache.class);
        mockWM = mock(WeblogManager.class);
        mockWEM = mock(WeblogEntryManager.class);
        mockRenderer = mock(ThymeleafRenderer.class);

        mockThemeManager = mock(ThemeManager.class);
        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        mockWeblogTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockWeblogTheme);

        dp = new DynamicProperties();
        dp.setLastSitewideChange(Instant.now().minus(2, ChronoUnit.DAYS));

        Function<WeblogPageRequest, SiteModel> siteModelFactory = new WebConfig().siteModelFactory();

        processor = new PageProcessor(mockWR, mockCache, mockWM, mockWEM,
                mockRenderer, mockThemeManager, mock(PageModel.class),
                siteModelFactory, dp);

        mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
        processor.setApplicationContext(mockApplicationContext);

        mockResponse = mock(HttpServletResponse.class);
        mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWR.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void testReceive304NotModifiedContent() throws IOException {
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
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);

        CachedContent cachedContent = new CachedContent(Role.WEBLOG);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockCache.get(any(), any())).thenReturn(cachedContent);

        processor.handleRequest(mockRequest, mockResponse);

        // verify cached content being returned
        verify(mockWM).incrementHitCount(weblog);
        verify(mockResponse).setContentType(Role.WEBLOG.getContentType());
        verify(mockResponse).setContentLength(7);
        verify(mockResponse).setDateHeader("Last-Modified", twoDaysAgo.toEpochMilli());
        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockSOS).write("mytest1".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testNonExistentTemplateRequestReturns404() throws IOException {
        when(mockWeblogTheme.getTemplateByName(any())).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);

        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache, never()).put(anyString(), any());
    }

    @Test
    public void testUnfoundOrCustomInternalTemplateRequestReturns404() throws IOException {
        // test custom internal template returns 404
        WeblogTemplate wt = new WeblogTemplate();
        wt.setRole(Role.CUSTOM_INTERNAL);
        when(mockWeblogTheme.getTemplateByName("my-custom-page")).thenReturn(wt);
        mockRequest = TestUtils.createMockServletRequestForCustomPageRequest();

        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        // CUSTOM_INTERNAL has incrementHitCounts = false
        verify(mockWM, never()).incrementHitCount(weblog);

        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache, never()).put(anyString(), any());

        // now test with a null template
        Mockito.clearInvocations(mockResponse);
        when(mockWeblogTheme.getTemplateByName(any())).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testUncachedPageRendering() throws IOException {
        WeblogTemplate customTemplate = new WeblogTemplate();
        customTemplate.setRole(Role.CUSTOM_EXTERNAL);
        when(mockWeblogTheme.getTemplateByName(any())).thenReturn(customTemplate);

        mockRequest = TestUtils.createMockServletRequestForCustomPageRequest();

        CachedContent cachedContent = new CachedContent(Role.CUSTOM_EXTERNAL);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        processor.handleRequest(mockRequest, mockResponse);

        // CUSTOM_EXTERNAL has incrementHitCounts = true
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache).put(anyString(), any());
        verify(mockRenderer).render(eq(customTemplate), any());
        verify(mockResponse).setContentType(Role.CUSTOM_EXTERNAL.getContentType());
        verify(mockResponse).setContentLength("mytest1".length());
        verify(mockSOS).write(any());

        // test weblog template
        WeblogTemplate weblogTemplate = new WeblogTemplate();
        weblogTemplate.setRole(Role.WEBLOG);
        when(mockWeblogTheme.getTemplateByRole(Role.WEBLOG)).thenReturn(weblogTemplate);

        Mockito.clearInvocations(mockResponse, mockWM, mockCache, mockRenderer, mockSOS);
        mockRequest = TestUtils.createMockServletRequestForWeblogHomePageRequest();

        processor.handleRequest(mockRequest, mockResponse);
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(weblogTemplate, wpr.getTemplate());

        // test permalink template
        sharedTheme.setSiteWide(true);
        dp.updateLastSitewideChange();

        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();

        WeblogTemplate permalinkTemplate = new WeblogTemplate();
        permalinkTemplate.setRole(Role.PERMALINK);
        WeblogEntry entry = new WeblogEntry();
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        when(mockWEM.getWeblogEntryByAnchor(weblog, "entry-anchor")).thenReturn(entry);
        when(mockWeblogTheme.getTemplateByRole(Role.PERMALINK)).thenReturn(permalinkTemplate);

        Mockito.clearInvocations(mockResponse, mockWM, mockCache, mockRenderer, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(entry, wpr.getWeblogEntry());
        assertEquals(permalinkTemplate, wpr.getTemplate());
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).put(anyString(), any());
        verify(mockSOS).write(any());

        // test fallback to weblog template if no permalink one
        when(mockWeblogTheme.getTemplateByRole(Role.PERMALINK)).thenReturn(null);

        Mockito.clearInvocations(mockResponse, mockWM, mockCache, mockRenderer, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(weblogTemplate, wpr.getTemplate());

        // test 404 if weblog entry not published
        entry.setStatus(WeblogEntry.PubStatus.DRAFT);

        Mockito.clearInvocations(mockResponse, mockWM, mockCache, mockRenderer, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test 404 if no weblog entry could be found for anchor
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(null);
        Mockito.clearInvocations(mockResponse, mockWM, mockCache, mockRenderer, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test 404 if exception during rendering
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(entry);
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        doThrow(new IllegalArgumentException()).when(mockRenderer).render(any(), any());

        Mockito.clearInvocations(mockResponse, mockWM, mockCache, mockSOS);

        WebloggerTest.logExpectedException(log, "IllegalArgumentException");
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException {
        URLModel urlModel = new URLModel(null, null);
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(urlModel);
        when(mockApplicationContext.getBean(eq("pageModelSet"), eq(Set.class))).thenReturn(pageModelSet);

        // setting custom page name to allow for a template to be chosen and hence the rendering to occur
        mockRequest = TestUtils.createMockServletRequestForCustomPageRequest();

        SharedTemplate sharedTemplate = new SharedTemplate();
        sharedTemplate.setRole(Role.CUSTOM_EXTERNAL);

        CachedContent cachedContent = new CachedContent(Role.CUSTOM_EXTERNAL);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        // testing that sitewide themes get the "site" & (page) "model" added to the rendering map.
        sharedTheme.setSiteWide(true);
        when(mockWeblogTheme.getTemplateByName("my-custom-page")).thenReturn(sharedTemplate);

        WeblogEntryComment comment = new WeblogEntryComment();
        when(mockRequest.getAttribute("commentForm")).thenReturn(comment);
        processor.handleRequest(mockRequest, mockResponse);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("url"));
        assertTrue(results.containsKey("site"));
        WeblogPageRequest wpr = (WeblogPageRequest) results.get("model");
        assertEquals(comment, wpr.getCommentForm());
        verify(mockWM).incrementHitCount(weblog);

        Mockito.clearInvocations(mockResponse, mockRenderer, mockWM);
        // testing (1) that non-sitewide themes just get "model" added to the rendering map.
        // (2) new comment form is generated if first request didn't provide one
        // (3) increment hit count not called for component types lacking incrementHitCounts property
        when(mockRequest.getAttribute("commentForm")).thenReturn(null);
        sharedTheme.setSiteWide(false);

        cachedContent = new CachedContent(Role.JAVASCRIPT);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        processor.handleRequest(mockRequest, mockResponse);
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());

        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("url"));
        assertFalse(results.containsKey("site"));
        wpr = (WeblogPageRequest) results.get("model");
        assertNotEquals(comment, wpr.getCommentForm());
        verify(mockWM, never()).incrementHitCount(weblog);
    }

    @Test
    public void testCommentFormsSkipCache() throws IOException {
        mockRequest = TestUtils.createMockServletRequestForCustomPageRequest();
        WeblogEntryComment wec = new WeblogEntryComment();
        when(mockRequest.getAttribute("commentForm")).thenReturn(wec);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockWM, never()).incrementHitCount(any());
        verify(mockCache, never()).get(any(), any());
        verify(mockCache, never()).put(any(), any());
    }

    @Test
    public void testGenerateKey() {
        WeblogPageRequest wpr = mock(WeblogPageRequest.class);
        when(wpr.getWeblogHandle()).thenReturn("bobsblog");
        when(wpr.getWeblogEntryAnchor()).thenReturn("neatoentry");
        when(wpr.getDeviceType()).thenReturn(DeviceType.TABLET);
        when(wpr.isSiteWide()).thenReturn(false);
        when(wpr.getAuthenticatedUser()).thenReturn("bob");

        String test1 = processor.generateKey(wpr);
        assertEquals("bobsblog/entry/neatoentry/user=bob/deviceType=TABLET", test1);

        when(wpr.getAuthenticatedUser()).thenReturn(null);
        when(wpr.getWeblogEntryAnchor()).thenReturn(null);
        when(wpr.getDeviceType()).thenReturn(DeviceType.MOBILE);
        when(wpr.getWeblogDate()).thenReturn("20171006");
        when(wpr.getCategory()).thenReturn("finance");
        when(wpr.getTag()).thenReturn("taxes");
        when(wpr.getQueryString()).thenReturn("a=foo&b=123");
        when(wpr.getPageNum()).thenReturn(5);
        when(wpr.isSiteWide()).thenReturn(true);

        Instant testTime = Instant.now();
        dp.setLastSitewideChange(testTime);

        test1 = processor.generateKey(wpr);
        assertEquals("bobsblog/date/20171006/cat/finance/tag/" +
                "taxes/page=5/query=a=foo&b=123/deviceType=MOBILE/lastUpdate=" + testTime.toEpochMilli(), test1);

        when(wpr.getCustomPageName()).thenReturn("mytemplate");
        test1 = processor.generateKey(wpr);
        assertEquals("bobsblog/page/mytemplate/date/20171006/cat/finance/tag/" +
                "taxes/page=5/query=a=foo&b=123/deviceType=MOBILE/lastUpdate=" + testTime.toEpochMilli(), test1);
    }
}
