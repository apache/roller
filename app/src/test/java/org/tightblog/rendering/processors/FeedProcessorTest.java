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
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.SharedTheme;
import org.tightblog.business.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.requests.WeblogFeedRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.util.WebloggerException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FeedProcessorTest {

    private FeedProcessor processor;
    private WeblogFeedRequest feedRequest;
    private Weblog weblog;
    private WebloggerProperties webloggerProperties;
    private SharedTheme sharedTheme;

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private LazyExpiringCache mockCache;
    private WeblogManager mockWM;
    private ThymeleafRenderer mockThymeleafRenderer;

    // not done as a @before as not all tests need these mocks
    private void initializeMocks() {
        JPAPersistenceStrategy mockStrategy = mock(JPAPersistenceStrategy.class);
        webloggerProperties = new WebloggerProperties();
        when(mockStrategy.getWebloggerProperties()).thenReturn(webloggerProperties);
        mockRequest = mock(HttpServletRequest.class);
        // default is page always needs refreshing
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli());
        mockResponse = mock(HttpServletResponse.class);
        WeblogFeedRequest.Creator wfrCreator = mock(WeblogFeedRequest.Creator.class);
        feedRequest = new WeblogFeedRequest();
        when(wfrCreator.create(mockRequest)).thenReturn(feedRequest);
        processor = new FeedProcessor();
        processor.setWeblogFeedRequestCreator(wfrCreator);
        mockCache = mock(LazyExpiringCache.class);
        processor.setWeblogFeedCache(mockCache);
        mockWM = mock(WeblogManager.class);
        weblog = new Weblog();
        when(mockWM.getWeblogByHandle(any(), eq(true))).thenReturn(weblog);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(eq("feedModelSet"), eq(Set.class)))
                .thenReturn(new HashSet<>());
        processor.setApplicationContext(mockApplicationContext);
        mockThymeleafRenderer = mock(ThymeleafRenderer.class);
        processor.setThymeleafRenderer(mockThymeleafRenderer);
        ThemeManager mockThemeManager = mock(ThemeManager.class);
        sharedTheme = new SharedTheme();
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        processor.setThemeManager(mockThemeManager);
        processor.setWeblogManager(mockWM);
        processor.setStrategy(mockStrategy);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        initializeMocks();
        feedRequest.setWeblogHandle("myhandle");
        when(mockWM.getWeblogByHandle("myhandle", true)).thenReturn(null);
        processor.getFeed(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testReceive304NotModifiedContent() throws IOException {
        initializeMocks();

        sharedTheme.setSiteWide(true);
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        webloggerProperties.setLastWeblogChange(twoDaysAgo);

        // date header more recent than last change, so should return 304
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().toEpochMilli());

        Mockito.clearInvocations(mockRequest);
        processor.getFeed(mockRequest, mockResponse);
        verify(mockRequest).getDateHeader(any());
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    @Test
    public void testCachedFeedReturned() throws IOException {
        initializeMocks();
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);

        CachedContent cachedContent = new CachedContent(10, Template.ComponentType.ATOMFEED);
        cachedContent.getCachedWriter().print("mytest1");
        cachedContent.flush();
        when(mockCache.get(any(), any())).thenReturn(cachedContent);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        processor.getFeed(mockRequest, mockResponse);

        // verify cached content being returned
        verify(mockResponse).setContentType(Template.ComponentType.ATOMFEED.getContentType());
        verify(mockResponse).setContentLength(7);
        verify(mockResponse).setDateHeader("Last-Modified", twoDaysAgo.toEpochMilli());
        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockSOS).write("mytest1".getBytes());
    }

    @Test
    public void testRenderedFeedReturned() throws IOException, WebloggerException {
        initializeMocks();
        Instant threeDaysAgo = Instant.now().minus(3, ChronoUnit.DAYS);
        weblog.setLastModified(threeDaysAgo);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        CachedContent renderedContent = new CachedContent(10, Template.ComponentType.ATOMFEED);
        renderedContent.getCachedWriter().print("mytest24");
        renderedContent.flush();

        when(mockThymeleafRenderer.render(any(), any())).thenReturn(renderedContent);

        processor.getFeed(mockRequest, mockResponse);

        // verify rendered content being returned
        verify(mockResponse).setContentType(Template.ComponentType.ATOMFEED.getContentType());
        verify(mockResponse).setContentLength(8);
        verify(mockResponse).setDateHeader("Last-Modified", threeDaysAgo.toEpochMilli());
        verify(mockResponse).setHeader("Cache-Control", "no-cache");
        verify(mockSOS).write("mytest24".getBytes());
    }

    @Test
    public void testGenerateKey() {
        initializeMocks();

        // comment & category test
        WeblogFeedRequest request = new WeblogFeedRequest();
        request.setWeblogHandle("bobsblog");
        request.setWeblogCategoryName("sports");
        request.setPageNum(14);

        String test1 = processor.generateKey(request, false);
        assertEquals("bobsblog/cat/sports/page=14", test1);

        // entry & tag test, site-wide
        request.setWeblogCategoryName(null);
        request.setTag("skiing");
        request.setSiteWide(true);
        request.setPageNum(0);

        Instant testTime = Instant.now();
        webloggerProperties.setLastWeblogChange(testTime);

        test1 = processor.generateKey(request, true);
        assertEquals("bobsblog/tag/skiing/lastUpdate=" + testTime.toEpochMilli(), test1);
    }

}
