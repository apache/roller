package org.tightblog.rendering.processors;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.SharedTheme;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template.ComponentType;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.cache.SiteWideCache;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.velocity.VelocityRenderer;
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
import static org.junit.Assert.*;
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

    private LazyExpiringCache mockCache;
    private SiteWideCache mockSiteCache;
    private WeblogManager mockWM;
    private VelocityRenderer mockVelocityRenderer;
    private ThemeManager mockThemeManager;

    // not done as a @before as not all tests need these mocks
    private void initializeMocks() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        WeblogPageRequest.Creator wprCreator = mock(WeblogPageRequest.Creator.class);
        pageRequest = new WeblogPageRequest();
        when(wprCreator.create(mockRequest)).thenReturn(pageRequest);
        mockWEM = mock(WeblogEntryManager.class);
        processor = new PageProcessor();
        processor.setWeblogPageRequestCreator(wprCreator);
        processor.setWeblogEntryManager(mockWEM);
        mockCache = mock(LazyExpiringCache.class);
        processor.setWeblogPageCache(mockCache);
        mockSiteCache = mock(SiteWideCache.class);
        processor.setSiteWideCache(mockSiteCache);
        mockWM = mock(WeblogManager.class);
        weblog = new Weblog();
        when(mockWM.getWeblogByHandle(any(), eq(true))).thenReturn(weblog);
        processor.setWeblogManager(mockWM);
        mockVelocityRenderer = mock(VelocityRenderer.class);
        processor.setVelocityRenderer(mockVelocityRenderer);
        mockThemeManager = mock(ThemeManager.class);
        processor.setThemeManager(mockThemeManager);
        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        processor = Mockito.spy(processor);
        doReturn(false).when(processor).respondIfNotModified(any(), any(), any(), any());
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
    public void test304NotModifiedContent() throws IOException {
        initializeMocks();

        // confirm respondIfNotModified called with last modified date of weblog if latter non-site
        doReturn(true).when(processor).respondIfNotModified(any(), any(), any(Instant.class), any());
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        weblog.setLastModified(yesterday);

        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(weblog, pageRequest.getWeblog());
        verify(processor).respondIfNotModified(mockRequest, mockResponse, yesterday, DeviceType.NORMAL);

        // confirm respondIfNotModified called with site-wide cache's last modified date for site-wide weblog
        sharedTheme.setSiteWide(true);
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        when(mockSiteCache.getLastModified()).thenReturn(twoDaysAgo);

        Mockito.clearInvocations(processor);
        processor.handleRequest(mockRequest, mockResponse);
        verify(processor).respondIfNotModified(mockRequest, mockResponse, twoDaysAgo, DeviceType.NORMAL);
    }

    @Test
    public void testCachedPageReturned() throws IOException {
        initializeMocks();

        // Confirm setLastModifiedHeader() called if respondIfNotModified is false
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
        verify(processor).setLastModifiedHeader(mockResponse, twoDaysAgo, DeviceType.NORMAL);

        // testing AbstractProcessor's setLastModifiedHeader w/last modified time
        verify(mockResponse).setHeader("ETag", "NORMAL");
        verify(mockResponse).setDateHeader("Last-Modified", twoDaysAgo.toEpochMilli());
        verify(mockResponse).setDateHeader("Expires", 0);

        // verify cached content being returned
        verify(mockWM).incrementHitCount(weblog);
        verify(mockResponse).setContentType(ComponentType.WEBLOG.getContentType());
        verify(mockResponse).setContentLength(7);
        verify(mockSOS).write("mytest1".getBytes());

        // testing AbstractProcessor's setLastModifiedHeader w/o last modified time
        weblog.setLastModified(null);
        pageRequest.setWeblogPageHit(false);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockWM, never()).incrementHitCount(weblog);
        verify(mockResponse).setHeader("ETag", "NORMAL");
        verify(mockResponse).setDateHeader("Expires", 0);
        verify(mockResponse, never()).setDateHeader(eq("Last-Modified"), anyLong());
        verify(mockSOS).write(any());
    }

    @Test
    public void testRenderingProcessing() throws IOException, WebloggerException {
        initializeMocks();

        // test null template returns 404
        pageRequest.setWeblogTemplateName("mytemplate");
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

        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());
        processor.setApplicationContext(mockContext);

        Mockito.clearInvocations(processor, mockResponse, mockWM);
        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(pageRequest.getTemplate(), wt);
        verify(mockWM).incrementHitCount(weblog);
        verify(mockCache).put(anyString(), any());
        verify(mockSiteCache, never()).put(anyString(), any());
        verify(mockVelocityRenderer).render(eq(pageRequest.getTemplate()), any(), any());
        verify(mockResponse).setContentType(ComponentType.CUSTOM_EXTERNAL.getContentType());
        verify(mockResponse).setContentLength(anyInt());
        verify(mockSOS).write(any());

        // test permalink template using site cache, no weblog page hit
        sharedTheme.setSiteWide(true);
        pageRequest.setWeblogPageHit(false);
        pageRequest.setWeblogTemplateName(null);
        pageRequest.setWeblogEntryAnchor("myentry");

        WeblogTemplate wt2 = new WeblogTemplate();
        wt2.setRole(ComponentType.PERMALINK);
        WeblogEntry entry = new WeblogEntry();
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(entry);
        when(mockTheme.getTemplateByAction(ComponentType.PERMALINK)).thenReturn(wt2);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSiteCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(entry, pageRequest.getWeblogEntry());
        assertEquals(wt2, pageRequest.getTemplate());
        verify(mockWM, never()).incrementHitCount(weblog);
        verify(mockCache, never()).put(anyString(), any());
        verify(mockSiteCache).put(anyString(), any());
        verify(mockSOS).write(any());

        // test fallback to weblog template if no permalink one
        WeblogTemplate wt3 = new WeblogTemplate();
        wt3.setRole(ComponentType.WEBLOG);
        when(mockTheme.getTemplateByAction(ComponentType.PERMALINK)).thenReturn(null);
        when(mockTheme.getTemplateByAction(ComponentType.PERMALINK)).thenReturn(wt3);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSiteCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertEquals(wt3, pageRequest.getTemplate());

        // test 404 if weblog entry not published
        entry.setStatus(WeblogEntry.PubStatus.DRAFT);
        pageRequest.setTemplate(null);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSiteCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test 404 if weblog entry null
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(null);
        pageRequest.setTemplate(null);

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSiteCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        assertNull(pageRequest.getTemplate());
        verify(mockResponse).sendError(SC_NOT_FOUND);

        // test 404 if exception during rendering
        when(mockWEM.getWeblogEntryByAnchor(weblog, "myentry")).thenReturn(entry);
        entry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        doThrow(new IllegalArgumentException()).when(mockVelocityRenderer).render(any(), any(), any());

        Mockito.clearInvocations(processor, mockResponse, mockWM, mockCache, mockSiteCache, mockSOS);
        processor.handleRequest(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testCommentFormsSkipCache() throws IOException, WebloggerException {
        initializeMocks();
        WeblogEntryComment wec = new WeblogEntryComment();
        when(mockRequest.getAttribute("commentForm")).thenReturn(wec);

        pageRequest.setWeblogTemplateName("mytemplate");
        WeblogTheme mockTheme = mock(WeblogTheme.class);
        when(mockThemeManager.getWeblogTheme(weblog)).thenReturn(mockTheme);
        when(mockTheme.getTemplateByPath(any())).thenReturn(null);

        processor.handleRequest(mockRequest, mockResponse);
        verify(mockWM, never()).incrementHitCount(any());
        verify(mockSiteCache, never()).get(any());
        verify(mockCache, never()).get(any(), any());
        verify(mockSiteCache, never()).put(any(), any());
        verify(mockCache, never()).put(any(), any());
    }

    @Test
    public void testRespondIfNotModified() throws IOException {
        initializeMocks();
        doCallRealMethod().when(processor).respondIfNotModified(any(), any(), any(), any());

        // test return false on invalid date
        when(mockRequest.getDateHeader("If-Modified-Since")).thenThrow(new IllegalArgumentException());
        boolean val = processor.respondIfNotModified(mockRequest, mockResponse, Instant.now(), DeviceType.NORMAL);
        assertFalse(val);

        // remove thenThrow from previous mock (thenReturn does not override it)
        Mockito.reset(mockRequest);
        // test return false if eTag different from one in header
        long time = Instant.now().getEpochSecond();
        when(mockRequest.getDateHeader("If-Modified-Since")).thenReturn(time);
        when(mockRequest.getHeader("If-None-Match")).thenReturn("1" + DeviceType.NORMAL.name());
        val = processor.respondIfNotModified(mockRequest, mockResponse, Instant.ofEpochMilli(0L), DeviceType.NORMAL);
        assertFalse(val);

        // test return true if eTag same from one in header
        when(mockRequest.getHeader("If-Modified-Since")).thenReturn(Long.toString(time));
        when(mockRequest.getHeader("If-None-Match")).thenReturn(DeviceType.NORMAL.name());
        Mockito.clearInvocations(mockResponse);
        val = processor.respondIfNotModified(mockRequest, mockResponse, Instant.ofEpochMilli(0L), DeviceType.NORMAL);
        verify(mockResponse).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        verify(mockResponse).setHeader("Last-Modified", Long.toString(time));
        assertTrue(val);

        // test return false if last modified after since date
        when(mockRequest.getHeader("If-Modified-Since")).thenReturn(Long.toString(time-10));
        Mockito.clearInvocations(mockResponse);
        val = processor.respondIfNotModified(mockRequest, mockResponse, Instant.ofEpochSecond(time), DeviceType.NORMAL);
        assertFalse(val);
        verify(mockResponse, never()).setStatus(anyInt());
        verify(mockResponse, never()).setHeader(any(), any());
    }

    @Test
    public void testGenerateKey() {
        WeblogPageRequest request = mock(WeblogPageRequest.class);
        when(request.getWeblogHandle()).thenReturn("bobsblog");
        when(request.getWeblogEntryAnchor()).thenReturn("neatoentry");
        when(request.getAuthenticatedUser()).thenReturn("bob");
        when(request.getDeviceType()).thenReturn(DeviceType.TABLET);

        String test1 = PageProcessor.generateKey(request);
        assertEquals("weblogpage.key:bobsblog/entry/neatoentry/user=bob/deviceType=TABLET", test1);

        when(request.getWeblogEntryAnchor()).thenReturn(null);
        when(request.getAuthenticatedUser()).thenReturn(null);
        when(request.getDeviceType()).thenReturn(DeviceType.MOBILE);
        when(request.getWeblogTemplateName()).thenReturn("mytemplate");
        when(request.getWeblogDate()).thenReturn("20171006");
        when(request.getWeblogCategoryName()).thenReturn("finance");
        when(request.getTag()).thenReturn("taxes");
        when(request.getPageNum()).thenReturn(5);

        test1 = PageProcessor.generateKey(request);
        assertEquals("weblogpage.key:bobsblog/page/mytemplate/date/20171006/cat/finance/tag/" +
                "taxes/page=5/deviceType=MOBILE", test1);
    }

}
