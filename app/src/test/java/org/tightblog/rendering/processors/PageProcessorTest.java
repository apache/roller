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
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.SharedTemplate;
import org.tightblog.business.themes.SharedTheme;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Template.ComponentType;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.WeblogTheme;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.util.WebloggerException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
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
import static org.mockito.Mockito.*;

public class PageProcessorTest {

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
    private ThymeleafRenderer mockRenderer;
    private ThemeManager mockThemeManager;
    private ApplicationContext mockApplicationContext;

    // not done as a @before as not all tests need these mocks
    private void initializeMocks() {
        JPAPersistenceStrategy mockStrategy = mock(JPAPersistenceStrategy.class);
        webloggerProperties = new WebloggerProperties();
        when(mockStrategy.getWebloggerProperties()).thenReturn(webloggerProperties);
        webloggerProperties.setLastWeblogChange(Instant.now().minus(2, ChronoUnit.DAYS));
        mockRequest = mock(HttpServletRequest.class);
        // default is page always needs refreshing
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli());
        mockResponse = mock(HttpServletResponse.class);
        WeblogPageRequest.Creator wprCreator = mock(WeblogPageRequest.Creator.class);
        pageRequest = new WeblogPageRequest();
        when(wprCreator.create(mockRequest)).thenReturn(pageRequest);
        mockWEM = mock(WeblogEntryManager.class);
        processor = new PageProcessor();
        processor.setWeblogPageRequestCreator(wprCreator);
        processor.setWeblogEntryManager(mockWEM);
        mockCache = mock(LazyExpiringCache.class);
        mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
        processor.setApplicationContext(mockApplicationContext);
        processor.setWeblogPageCache(mockCache);
        mockWM = mock(WeblogManager.class);
        weblog = new Weblog();
        weblog.setLastModified(Instant.now().minus(2, ChronoUnit.DAYS));
        when(mockWM.getWeblogByHandle(any(), eq(true))).thenReturn(weblog);
        processor.setWeblogManager(mockWM);
        mockRenderer = mock(ThymeleafRenderer.class);
        processor.setThymeleafRenderer(mockRenderer);
        mockThemeManager = mock(ThemeManager.class);
        processor.setThemeManager(mockThemeManager);
        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        processor = Mockito.spy(processor);
        processor.setStrategy(mockStrategy);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        initializeMocks();
        pageRequest.setWeblogHandle("myhandle");
        when(mockWM.getWeblogByHandle("myhandle", true)).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
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
    }

    @Test
    public void testCachedPageReturned() throws IOException {
        initializeMocks();

        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);
        pageRequest.setWeblogPageHit(true);

        CachedContent cachedContent = new CachedContent(10, ComponentType.WEBLOG.getContentType());
        cachedContent.getCachedWriter().print("mytest1");
        cachedContent.flush();
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
        verify(mockSOS).write("mytest1".getBytes());
    }

    @Test
    public void testRenderingProcessing() throws IOException, WebloggerException {
        initializeMocks();

        // test null template returns 404
        pageRequest.setCustomPageName("mytemplate");
        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
        when(mockTheme.getTemplateByPath(any())).thenReturn(null);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test custom internal template returns 404
        WeblogTemplate wt = new WeblogTemplate();
        wt.setRole(ComponentType.CUSTOM_INTERNAL);
        when(mockTheme.getTemplateByPath(any())).thenReturn(wt);

        Mockito.clearInvocations(processor, mockResponse);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test page template & rendering called
        wt.setRole(ComponentType.CUSTOM_EXTERNAL);
        pageRequest.setWeblogPageHit(true);

        ServletOutputStream mockSOS = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(mockSOS);

        CachedContent cachedContent = new CachedContent(10, ComponentType.CUSTOM_EXTERNAL.getContentType());
        cachedContent.getCachedWriter().print("mytest1");
        cachedContent.flush();
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        Mockito.clearInvocations(processor, mockResponse, mockWM);
        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(pageRequest.getTemplate(), wt);
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).put(anyString(), any());
        verify(mockRenderer).render(eq(pageRequest.getTemplate()), any());
        verify(mockResponse).setContentType(ComponentType.CUSTOM_EXTERNAL.getContentType());
        verify(mockResponse).setContentLength("mytest1".length());
        verify(mockSOS).write(any());

        // test permalink template, no weblog page hit
        sharedTheme.setSiteWide(true);
        webloggerProperties.setLastWeblogChange(Instant.now());
        pageRequest.setWeblogPageHit(false);
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
        verify(mockWM, never()).incrementHitCount(weblog);
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
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException, WebloggerException {
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

        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        // testing that sitewide themes get the "site" & (page) "model" added to the rendering map.
        sharedTheme.setSiteWide(true);
        when(mockTheme.getTemplateByPath("mycustompage")).thenReturn(sharedTemplate);
        processor.handleRequest(mockRequest, mockResponse);

        // set up captors on thymeleafRenderer.render()
        ArgumentCaptor<Map<String,Object>> modelCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockRenderer).render(eq(sharedTemplate), modelCaptor.capture());
        Map<String,Object> results = modelCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        Mockito.clearInvocations(processor, mockResponse, mockRenderer);
        // testing that non-sitewide themes just get "model" added to the rendering map.
        sharedTheme.setSiteWide(false);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockRenderer).render(eq(sharedTemplate), modelCaptor.capture());

        results = modelCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));
    }


    @Test
    public void testCommentFormsSkipCache() throws IOException, WebloggerException {
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

        WeblogPageRequest request = mock(WeblogPageRequest.class);
        when(request.getWeblogHandle()).thenReturn("bobsblog");
        when(request.getWeblogEntryAnchor()).thenReturn("neatoentry");
        when(request.getAuthenticatedUser()).thenReturn("bob");
        when(request.getDeviceType()).thenReturn(DeviceType.TABLET);

        String test1 = processor.generateKey(request, false);
        assertEquals("bobsblog/entry/neatoentry/user=bob/deviceType=TABLET", test1);

        when(request.getWeblogEntryAnchor()).thenReturn(null);
        when(request.getAuthenticatedUser()).thenReturn(null);
        when(request.getDeviceType()).thenReturn(DeviceType.MOBILE);
        when(request.getCustomPageName()).thenReturn("mytemplate");
        when(request.getWeblogDate()).thenReturn("20171006");
        when(request.getWeblogCategoryName()).thenReturn("finance");
        when(request.getTag()).thenReturn("taxes");
        when(request.getQueryString()).thenReturn("a=foo&b=123");
        when(request.getPageNum()).thenReturn(5);
        Instant testTime = Instant.now();
        webloggerProperties.setLastWeblogChange(testTime);

        test1 = processor.generateKey(request, true);
        assertEquals("bobsblog/page/mytemplate/date/20171006/cat/finance/tag/" +
                "taxes/page=5/query=a=foo&b=123/deviceType=MOBILE/lastUpdate=" + testTime.toEpochMilli(), test1);
    }

}
