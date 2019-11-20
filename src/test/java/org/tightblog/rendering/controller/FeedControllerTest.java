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
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.tightblog.TestUtils;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.SharedTheme;
import org.tightblog.rendering.model.FeedModel;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.requests.WeblogFeedRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.dao.WeblogDao;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeedControllerTest {

    private FeedController feedProcessor;
    private Weblog weblog;
    private SharedTheme sharedTheme;
    private DynamicProperties dp;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private LazyExpiringCache mockCache;
    private WeblogDao mockWR;
    private ThymeleafRenderer mockThymeleafRenderer;

    @Before
    public void initializeMocks() {
        mockWR = mock(WeblogDao.class);
        weblog = new Weblog();
        when(mockWR.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(weblog);

        ThemeManager mockThemeManager = mock(ThemeManager.class);
        sharedTheme = new SharedTheme();
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

        mockCache = mock(LazyExpiringCache.class);
        mockThymeleafRenderer = mock(ThymeleafRenderer.class);
        dp = new DynamicProperties();

        feedProcessor = new FeedController(mockWR, mockCache, mockThymeleafRenderer, mockThemeManager,
                mock(FeedModel.class), dp);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(eq("feedModelSet"), eq(Set.class)))
                .thenReturn(new HashSet<>());
        feedProcessor.setApplicationContext(mockApplicationContext);

        mockRequest = TestUtils.createMockServletRequest();
        mockResponse = mock(HttpServletResponse.class);
    }

    @Test
    public void test404OnMissingWeblog() {
        when(mockWR.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(null);
        ResponseEntity<Resource> result = feedProcessor.getFeed(TestUtils.BLOG_HANDLE, null, null,
                null, null, mockRequest);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void testReceive304NotModifiedContent() {
        sharedTheme.setSiteWide(true);
        Instant now = Instant.now();
        dp.setLastSitewideChange(now.minus(2, ChronoUnit.DAYS));

        // date header more recent than last change, so should return 304
        when(mockRequest.getDateHeader(any())).thenReturn(now.toEpochMilli());

        Mockito.clearInvocations(mockRequest);
        ResponseEntity<Resource> result = feedProcessor.getFeed(TestUtils.BLOG_HANDLE, null, null,
                null, null, mockRequest);
        verify(mockRequest).getDateHeader(any());
        assertEquals(HttpStatus.NOT_MODIFIED, result.getStatusCode());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache).incrementRequestsHandledBy304();
    }

    @Test
    public void testCachedFeedReturned() throws IOException {
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);

        CachedContent cachedContent = new CachedContent(Template.Role.ATOMFEED);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockCache.get(any(), any())).thenReturn(cachedContent);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        ResponseEntity<Resource> result = feedProcessor.getFeed(TestUtils.BLOG_HANDLE, null, null,
                null, null, mockRequest);

        // verify cached content being returned
        assertNotNull(result.getHeaders().getContentType());
        assertEquals(Template.Role.ATOMFEED.getContentType(), result.getHeaders().getContentType().toString());
        assertEquals(7, result.getHeaders().getContentLength());
        assertEquals(twoDaysAgo.truncatedTo(ChronoUnit.SECONDS).toEpochMilli(),
                result.getHeaders().getLastModified());
        assertEquals(CacheControl.noCache().getHeaderValue(), result.getHeaders().getCacheControl());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
    }

    @Test
    public void testRenderedFeedReturned() throws IOException {
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        weblog.setLastModified(threeDaysAgo);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        CachedContent renderedContent = new CachedContent(Template.Role.ATOMFEED);
        renderedContent.setContent("mytest24".getBytes(StandardCharsets.UTF_8));

        when(mockThymeleafRenderer.render(any(), any())).thenReturn(renderedContent);

        ResponseEntity<Resource> result = feedProcessor.getFeed(TestUtils.BLOG_HANDLE, null, null,
                null, null, mockRequest);

        // verify rendered content being returned
        assertNotNull(result.getHeaders().getContentType());
        assertEquals(Template.Role.ATOMFEED.getContentType(), result.getHeaders().getContentType().toString());
        assertEquals(8, result.getHeaders().getContentLength());
        assertEquals(threeDaysAgo.truncatedTo(ChronoUnit.SECONDS).toEpochMilli(),
                result.getHeaders().getLastModified());
        assertEquals(CacheControl.noCache().getHeaderValue(), result.getHeaders().getCacheControl());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();

        // test 404 on rendering error
        Mockito.clearInvocations(mockResponse, mockCache, mockSOS);
        when(mockThymeleafRenderer.render(any(), any())).thenThrow(IllegalArgumentException.class);
        result = feedProcessor.getFeed(null, null, null, null, null, mockRequest);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testGenerateKey() {
        // comment & category test
        WeblogFeedRequest request = mock(WeblogFeedRequest.class);
        when(request.getWeblogHandle()).thenReturn("bobsblog");
        when(request.getCategoryName()).thenReturn("sports");
        when(request.getPageNum()).thenReturn(14);

        String test1 = feedProcessor.generateKey(request, false);
        assertEquals("bobsblog/cat/sports/page=14", test1);

        // entry & tag test, site-wide
        when(request.getCategoryName()).thenReturn(null);
        when(request.getTag()).thenReturn("skiing");
        when(request.isSiteWide()).thenReturn(true);
        when(request.getPageNum()).thenReturn(0);

        Instant testTime = Instant.now();
        dp.setLastSitewideChange(testTime);

        test1 = feedProcessor.generateKey(request, true);
        assertEquals("bobsblog/tag/skiing/lastUpdate=" + testTime.toEpochMilli(), test1);
    }
}
