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
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
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
import org.tightblog.repository.WeblogRepository;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeedProcessorTest {

    private FeedProcessor feedProcessor;
    private Weblog weblog;
    private SharedTheme sharedTheme;
    private DynamicProperties dp;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private LazyExpiringCache mockCache;
    private WeblogRepository mockWR;
    private ThymeleafRenderer mockThymeleafRenderer;

    @Before
    public void initializeMocks() {
        mockWR = mock(WeblogRepository.class);
        weblog = new Weblog();
        when(mockWR.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(weblog);

        ThemeManager mockThemeManager = mock(ThemeManager.class);
        sharedTheme = new SharedTheme();
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

        mockCache = mock(LazyExpiringCache.class);
        mockThymeleafRenderer = mock(ThymeleafRenderer.class);
        dp = new DynamicProperties();

        feedProcessor = new FeedProcessor(mockWR, mockCache, mockThymeleafRenderer, mockThemeManager,
                mock(FeedModel.class), dp);

        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(eq("feedModelSet"), eq(Set.class)))
                .thenReturn(new HashSet<>());
        feedProcessor.setApplicationContext(mockApplicationContext);

        mockRequest = TestUtils.createMockServletRequestForWeblogFeedRequest();
        mockResponse = mock(HttpServletResponse.class);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWR.findByHandleAndVisibleTrue(TestUtils.BLOG_HANDLE)).thenReturn(null);
        feedProcessor.getFeed(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void testReceive304NotModifiedContent() throws IOException {
        sharedTheme.setSiteWide(true);
        Instant now = Instant.now();
        dp.setLastSitewideChange(now.minus(2, ChronoUnit.DAYS));

        // date header more recent than last change, so should return 304
        when(mockRequest.getDateHeader(any())).thenReturn(now.toEpochMilli());

        Mockito.clearInvocations(mockRequest);
        feedProcessor.getFeed(mockRequest, mockResponse);
        verify(mockRequest).getDateHeader(any());
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache).incrementRequestsHandledBy304();
    }

    @Test
    public void testCachedFeedReturned() throws IOException {
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);

        CachedContent cachedContent = new CachedContent(Template.ComponentType.ATOMFEED);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockCache.get(any(), any())).thenReturn(cachedContent);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        feedProcessor.getFeed(mockRequest, mockResponse);

        // verify cached content being returned
        verify(mockResponse).setContentType(Template.ComponentType.ATOMFEED.getContentType());
        verify(mockResponse).setContentLength(7);
        verify(mockResponse).setDateHeader("Last-Modified", twoDaysAgo.toEpochMilli());
        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockSOS).write("mytest1".getBytes());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
    }

    @Test
    public void testRenderedFeedReturned() throws IOException {
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        weblog.setLastModified(threeDaysAgo);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        CachedContent renderedContent = new CachedContent(Template.ComponentType.ATOMFEED);
        renderedContent.setContent("mytest24".getBytes(StandardCharsets.UTF_8));

        when(mockThymeleafRenderer.render(any(), any())).thenReturn(renderedContent);

        feedProcessor.getFeed(mockRequest, mockResponse);

        // verify rendered content being returned
        verify(mockResponse).setContentType(Template.ComponentType.ATOMFEED.getContentType());
        verify(mockResponse).setContentLength(8);
        verify(mockResponse).setDateHeader("Last-Modified", threeDaysAgo.toEpochMilli());
        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockSOS).write("mytest24".getBytes());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();

        // test 404 on rendering error
        Mockito.clearInvocations(mockResponse, mockCache, mockSOS);
        when(mockThymeleafRenderer.render(any(), any())).thenThrow(IllegalArgumentException.class);
        feedProcessor.getFeed(mockRequest, mockResponse);
        verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        verify(mockResponse, never()).setContentType(any());
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
