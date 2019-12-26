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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.TestUtils;
import org.tightblog.WebloggerTest;
import org.tightblog.config.DynamicProperties;
import org.tightblog.config.WebConfig;
import org.tightblog.domain.Template;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.dao.UserDao;
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
import org.tightblog.rendering.service.ThymeleafRenderer;
import org.tightblog.dao.WeblogDao;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PageControllerTest {
    private static Logger log = LoggerFactory.getLogger(PageControllerTest.class);

    private static final String TEST_BLOG_HANDLE = TestUtils.BLOG_HANDLE;
    private static final String TEST_ENTRY_ANCHOR = TestUtils.ENTRY_ANCHOR;
    private static final String TEST_GENERATED_PAGE = "<p>hello</p>";

    private PageController controller;
    private HttpServletRequest mockRequest;
    private WeblogEntryManager mockWEM;
    private Weblog weblog;
    private SharedTheme sharedTheme;
    private WeblogTheme mockWeblogTheme;
    private DynamicProperties dp;
    private Principal mockPrincipal;
    private WeblogEntryListGenerator mockWELG;
    private WeblogTemplate weblogTemplate;

    private LazyExpiringCache mockCache;
    private WeblogManager mockWM;
    private WeblogDao mockWD;
    private ThymeleafRenderer mockRenderer;
    private ThemeManager mockThemeManager;
    private ApplicationContext mockApplicationContext;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() throws IOException {
        mockRequest = TestUtils.createMockServletRequest();
        mockPrincipal = mock(Principal.class);

        UserDao mockUD = mock(UserDao.class);
        mockWD = mock(WeblogDao.class);
        weblog = new Weblog();
        weblog.setLastModified(Instant.now().minus(2, ChronoUnit.DAYS));
        weblog.setHandle(TEST_BLOG_HANDLE);
        when(mockWD.findByHandleAndVisibleTrue(TEST_BLOG_HANDLE)).thenReturn(weblog);

        mockCache = mock(LazyExpiringCache.class);
        mockWM = mock(WeblogManager.class);
        mockWEM = mock(WeblogEntryManager.class);
        mockRenderer = mock(ThymeleafRenderer.class);

        CachedContent cachedContent = new CachedContent(Role.CUSTOM_EXTERNAL);
        cachedContent.setContent(TEST_GENERATED_PAGE.getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        mockThemeManager = mock(ThemeManager.class);
        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        mockWeblogTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockWeblogTheme);

        dp = new DynamicProperties();
        dp.setLastSitewideChange(Instant.now().minus(2, ChronoUnit.DAYS));

        Function<WeblogPageRequest, SiteModel> siteModelFactory = new WebConfig().siteModelFactory();

        PageModel mockPageModel = mock(PageModel.class);
        mockWELG = mock(WeblogEntryListGenerator.class);
        when(mockPageModel.getWeblogEntryListGenerator()).thenReturn(mockWELG);

        controller = new PageController(mockWD, mockCache, mockWM, mockWEM,
                mockRenderer, mockThemeManager, mockPageModel,
                siteModelFactory, mockUD, dp);

        mockApplicationContext = mock(ApplicationContext.class);
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
        controller.setApplicationContext(mockApplicationContext);

        MockitoAnnotations.initMocks(this);

        weblogTemplate = new WeblogTemplate();
        weblogTemplate.setRole(Role.WEBLOG);
        when(mockWeblogTheme.getTemplateByRole(Role.WEBLOG)).thenReturn(weblogTemplate);
    }

    @Test
    public void test404OnMissingWeblog() {
        when(mockWD.findByHandleAndVisibleTrue(TEST_BLOG_HANDLE)).thenReturn(null);
        ResponseEntity<Resource> result = controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest,
                mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        verify(mockCache, never()).incrementIncomingRequests();
    }

    @Test
    public void testReceive304NotModifiedContent() {
        sharedTheme.setSiteWide(true);

        // date header more recent than last change, so should return 304
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().toEpochMilli());

        ResponseEntity<Resource> result = controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest,
                mockPrincipal);
        verify(mockRequest).getDateHeader(any());
        assertEquals(HttpStatus.NOT_MODIFIED, result.getStatusCode());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache).incrementRequestsHandledBy304();
    }

    @Test
    public void testCachedPageReturned() {
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        weblog.setLastModified(twoDaysAgo);

        CachedContent cachedContent = new CachedContent(Role.WEBLOG);
        cachedContent.setContent(TEST_GENERATED_PAGE.getBytes(StandardCharsets.UTF_8));
        when(mockCache.get(any(), any())).thenReturn(cachedContent);

        ResponseEntity<Resource> result = controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest,
                mockPrincipal);

        // verify cached content being returned
        verify(mockWM).incrementHitCount(weblog);
        assertNotNull(result.getHeaders().getContentType());
        assertEquals(Template.Role.WEBLOG.getContentType(), result.getHeaders().getContentType().toString());
        assertEquals(TEST_GENERATED_PAGE.length(), result.getHeaders().getContentLength());
        assertEquals(twoDaysAgo.truncatedTo(ChronoUnit.SECONDS).toEpochMilli(),
                result.getHeaders().getLastModified());
        assertEquals(CacheControl.noCache().getHeaderValue(), result.getHeaders().getCacheControl());
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
    }

    @Test
    public void testNonExistentTemplateRequestReturns404() {
        when(mockWeblogTheme.getTemplateByRole(Role.WEBLOG)).thenReturn(null);
        ResponseEntity<Resource> result = controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest,
                mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache, never()).put(anyString(), any());
    }

    @Test
    public void testUnfoundOrCustomInternalTemplateRequestReturnsHomePage() throws IOException {
        String customPageName = "my-custom-page";

        // test custom internal template returns 404
        WeblogTemplate wt = new WeblogTemplate();
        wt.setRole(Role.CUSTOM_INTERNAL);
        when(mockWeblogTheme.getTemplateByName(customPageName)).thenReturn(wt);

        ResponseEntity<Resource> result = controller.getByCustomPage(TEST_BLOG_HANDLE, customPageName,
                null, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        // home page uses weblog template
        assertEquals(weblogTemplate, wpr.getTemplate());

        // now test with a null template
        Mockito.clearInvocations(mockRenderer);
        when(mockWeblogTheme.getTemplateByName(customPageName)).thenReturn(null);
        result = controller.getByCustomPage(TEST_BLOG_HANDLE, customPageName,
                null, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        // home page uses weblog template
        assertEquals(weblogTemplate, wpr.getTemplate());
    }

    @Test
    public void testGetHomePage() throws IOException {
        ResponseEntity<Resource> result = controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertNull(wpr.getWeblogEntry());
        assertEquals(weblogTemplate, wpr.getTemplate());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertFalse(wpr.isPermalink());
        assertFalse(wpr.isNoIndex());
        assertFalse(wpr.isSearchResults());

        // test chrono pager called
        wpr.getWeblogEntriesPager();
        verify(mockWELG).getChronoPager(weblog, null, null,
                null, 0, 0, false);
    }

    @Test
    public void testGetByEntry() throws IOException {
        // test entry generated with permalink template
        WeblogTemplate permalinkTemplate = new WeblogTemplate();
        permalinkTemplate.setRole(Role.PERMALINK);
        when(mockWeblogTheme.getTemplateByRole(Role.PERMALINK)).thenReturn(permalinkTemplate);

        WeblogEntry entry = new WeblogEntry();
        entry.setAnchor(TEST_ENTRY_ANCHOR);
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        when(mockWEM.getWeblogEntryByAnchor(weblog, entry.getAnchor())).thenReturn(entry);

        ResponseEntity<Resource> result = controller.getByEntry(TEST_BLOG_HANDLE, TEST_ENTRY_ANCHOR, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(TEST_ENTRY_ANCHOR, wpr.getWeblogEntry().getAnchor());
        assertEquals(permalinkTemplate, wpr.getTemplate());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertTrue(wpr.isPermalink());
        // pageNum = 0 so index
        assertFalse(wpr.isNoIndex());
        assertFalse(wpr.isSearchResults());

        // test permalink pager called
        wpr.getWeblogEntriesPager();
        verify(mockWELG).getPermalinkPager(eq(weblog), any(), any());

        // test redirect to home page (i.e., usage of weblog template) if weblog entry not published
        entry.setStatus(WeblogEntry.PubStatus.DRAFT);

        Mockito.clearInvocations(mockWM, mockCache, mockRenderer, mockWELG);
        result = controller.getByEntry(TEST_BLOG_HANDLE, TEST_ENTRY_ANCHOR,
                mockRequest, mockPrincipal);
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(weblogTemplate, wpr.getTemplate());
        assertEquals(HttpStatus.OK, result.getStatusCode());

        // test redirect to home page (i.e., usage of weblog template) if weblog entry not found
        when(mockWEM.getWeblogEntryByAnchor(weblog, TEST_ENTRY_ANCHOR)).thenReturn(null);
        Mockito.clearInvocations(mockWM, mockCache, mockRenderer);
        result = controller.getByEntry(TEST_BLOG_HANDLE, TEST_ENTRY_ANCHOR, mockRequest,
                mockPrincipal);
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(weblogTemplate, wpr.getTemplate());
        assertEquals(HttpStatus.OK, result.getStatusCode());

        // test fallback to weblog template if no permalink one
        when(mockWeblogTheme.getTemplateByRole(Role.PERMALINK)).thenReturn(null);

        Mockito.clearInvocations(mockWM, mockCache, mockRenderer);
        result = controller.getByEntry(TEST_BLOG_HANDLE, TEST_ENTRY_ANCHOR, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(weblogTemplate, wpr.getTemplate());

        // test 404 if exception during rendering
        when(mockWEM.getWeblogEntryByAnchor(weblog, TEST_ENTRY_ANCHOR)).thenReturn(entry);
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        doThrow(new IllegalArgumentException()).when(mockRenderer).render(any(), any());

        Mockito.clearInvocations(mockWM, mockCache);

        WebloggerTest.logExpectedException(log, "IllegalArgumentException");
        result = controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest,
                mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testGetByCategory() throws IOException {
        String categoryName = "my-category";
        String tagName = "my-tag";

        // test with optional tag
        ResponseEntity<Resource> result = controller.getByCategory(TEST_BLOG_HANDLE, categoryName, 0, tagName,
                mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertNull(wpr.getWeblogEntry());
        assertEquals(weblogTemplate, wpr.getTemplate());
        assertNull(wpr.getCustomPageName());
        assertEquals(categoryName, wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertEquals(tagName, wpr.getTag());
        assertFalse(wpr.isPermalink());
        assertFalse(wpr.isNoIndex());
        assertFalse(wpr.isSearchResults());

        // test chrono pager called
        wpr.getWeblogEntriesPager();
        verify(mockWELG).getChronoPager(weblog, null, categoryName,
                tagName, 0, 0, false);

        // test without tag
        Mockito.clearInvocations(mockRenderer, mockWELG);
        result = controller.getByCategory(TEST_BLOG_HANDLE, categoryName, 0, null, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(categoryName, wpr.getCategory());
        assertNull(wpr.getTag());
    }

    @Test
    public void testGetByTag() throws IOException {
        String tagName = "my-tag";

        ResponseEntity<Resource> result = controller.getByTag(TEST_BLOG_HANDLE, tagName, 0, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertNull(wpr.getWeblogEntry());
        assertEquals(weblogTemplate, wpr.getTemplate());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertEquals(tagName, wpr.getTag());
        assertFalse(wpr.isPermalink());
        assertFalse(wpr.isNoIndex());
        assertFalse(wpr.isSearchResults());

        // test chrono pager called
        wpr.getWeblogEntriesPager();
        verify(mockWELG).getChronoPager(weblog, null, null,
                tagName, 0, 0, false);
    }

    @Test
    public void testGetByDate() throws IOException {
        String monthDate = "201804";

        ResponseEntity<Resource> result = controller.getByDate(TEST_BLOG_HANDLE, monthDate, 0, mockRequest, mockPrincipal);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertNull(wpr.getWeblogEntry());
        assertEquals(weblogTemplate, wpr.getTemplate());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals(monthDate, wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertFalse(wpr.isPermalink());
        assertTrue(wpr.isNoIndex());
        assertFalse(wpr.isSearchResults());

        // test chrono pager called
        wpr.getWeblogEntriesPager();
        verify(mockWELG).getChronoPager(weblog, monthDate, null,
                null, 0, 0, false);
    }

    @Test
    public void testGetCustomPage() throws IOException {
        String customPageName = "my-custom-page";

        WeblogTemplate customTemplate = new WeblogTemplate();
        customTemplate.setRole(Role.CUSTOM_EXTERNAL);
        when(mockWeblogTheme.getTemplateByName(any())).thenReturn(customTemplate);

        ResponseEntity<Resource> result = controller.getByCustomPage(TEST_BLOG_HANDLE,
                customPageName, null, mockRequest, mockPrincipal);

        // CUSTOM_EXTERNAL has incrementHitCounts = true
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).incrementIncomingRequests();
        verify(mockCache, never()).incrementRequestsHandledBy304();
        verify(mockCache).put(anyString(), any());
        verify(mockRenderer).render(eq(customTemplate), any());
        assertNotNull(result.getHeaders().getContentType());
        assertEquals(Role.CUSTOM_EXTERNAL.getContentType(), result.getHeaders().getContentType().toString());
        assertEquals(TEST_GENERATED_PAGE.length(), result.getHeaders().getContentLength());

        WeblogPageRequest wpr = TestUtils.extractWeblogPageRequestFromMockRenderer(mockRenderer);
        assertEquals(TEST_BLOG_HANDLE, wpr.getWeblogHandle());
        assertEquals(customPageName, wpr.getCustomPageName());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCategory());
        assertNull(wpr.getTag());
        assertEquals(customTemplate, wpr.getTemplate());
    }

    @Test
    public void testModelSetCorrectlyFilled() throws IOException {
        URLModel urlModel = new URLModel(null);
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(urlModel);
        when(mockApplicationContext.getBean(eq("pageModelSet"), eq(Set.class))).thenReturn(pageModelSet);

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
        controller.getByCustomPage(TEST_BLOG_HANDLE, "my-custom-page", null, mockRequest, mockPrincipal);

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(eq(sharedTemplate), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("url"));
        assertTrue(results.containsKey("site"));
        WeblogPageRequest wpr = (WeblogPageRequest) results.get("model");
        assertEquals(comment, wpr.getCommentForm());
        verify(mockWM, never()).incrementHitCount(weblog);

        Mockito.clearInvocations(mockRenderer, mockWM);
        // testing (1) that non-sitewide themes just get "model" added to the rendering map.
        // (2) new comment form is generated if first request didn't provide one
        // (3) increment hit count not called for component types lacking incrementHitCounts property
        when(mockRequest.getAttribute("commentForm")).thenReturn(null);
        sharedTheme.setSiteWide(false);

        cachedContent = new CachedContent(Role.JAVASCRIPT);
        cachedContent.setContent("mytest1".getBytes(StandardCharsets.UTF_8));
        when(mockRenderer.render(any(), any())).thenReturn(cachedContent);

        controller.getByCustomPage(TEST_BLOG_HANDLE, "my-custom-page", null, mockRequest, mockPrincipal);
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
    public void testCommentFormsSkipCache() {
        WeblogEntryComment wec = new WeblogEntryComment();
        when(mockRequest.getAttribute("commentForm")).thenReturn(wec);
        controller.getHomePage(TEST_BLOG_HANDLE, 0, mockRequest, mockPrincipal);
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

        String test1 = controller.generateKey(wpr);
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

        test1 = controller.generateKey(wpr);
        assertEquals("bobsblog/date/20171006/cat/finance/tag/" +
                "taxes/page=5/query=a=foo&b=123/deviceType=MOBILE/lastUpdate=" + testTime.toEpochMilli(), test1);

        when(wpr.getCustomPageName()).thenReturn("mytemplate");
        test1 = controller.generateKey(wpr);
        assertEquals("bobsblog/page/mytemplate/date/20171006/cat/finance/tag/" +
                "taxes/page=5/query=a=foo&b=123/deviceType=MOBILE/lastUpdate=" + testTime.toEpochMilli(), test1);
    }

    @Test
    public void testIsValidDateString() {
        assertTrue(PageController.isValidDateString("20160229"));
        assertFalse(PageController.isValidDateString("20170229"));
        assertTrue(PageController.isValidDateString("201805"));
        assertFalse(PageController.isValidDateString("201815"));
        assertFalse(PageController.isValidDateString("20180547"));
        assertFalse(PageController.isValidDateString("2018"));
        assertFalse(PageController.isValidDateString("201805011"));
        assertFalse(PageController.isValidDateString("pumpkin"));
    }

}
