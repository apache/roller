package org.tightblog.rendering.processors;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.SharedTheme;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.RendererManager;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.cache.SiteWideCache;
import org.tightblog.rendering.requests.WeblogPageRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
    private RequestDispatcher mockRequestDispatcher;
    private WebloggerProperties properties;
    private WeblogPageRequest.Creator wprCreator;
    private WeblogPageRequest pageRequest;
    private WeblogEntryManager mockWEM;

    private LazyExpiringCache mockCache;
    private SiteWideCache mockSWCache;
    private WeblogManager mockWM;
    private RendererManager mockRendererManager;
    private ThemeManager mockThemeManager;

    // not done as a @before as not all tests need these mocks
    private void initializeMocks() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockRequestDispatcher = mock(RequestDispatcher.class);
        when(mockRequest.getRequestDispatcher(anyString())).thenReturn(mockRequestDispatcher);
        properties = new WebloggerProperties();
        // properties.setCommentHtmlPolicy(HTMLSanitizer.Level.LIMITED);
        wprCreator = mock(WeblogPageRequest.Creator.class);
        pageRequest = new WeblogPageRequest();
        when(wprCreator.create(mockRequest)).thenReturn(pageRequest);
        mockWEM = mock(WeblogEntryManager.class);
        processor = new PageProcessor();
        processor.setWeblogPageRequestCreator(wprCreator);
        processor.setWeblogEntryManager(mockWEM);

        mockCache = mock(LazyExpiringCache.class);
        processor.setWeblogPageCache(mockCache);
        mockSWCache = mock(SiteWideCache.class);
        processor.setSiteWideCache(mockSWCache);
        mockWM = mock(WeblogManager.class);
        processor.setWeblogManager(mockWM);
        mockRendererManager = mock(RendererManager.class);
        processor.setRendererManager(mockRendererManager);
        mockThemeManager = mock(ThemeManager.class);
        processor.setThemeManager(mockThemeManager);
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        initializeMocks();
        pageRequest.setWeblogHandle("myhandle");
        when(mockWM.getWeblogByHandle("myhandle", true)).thenReturn(null);
        processor.getPage(mockRequest, mockResponse);
        verify(mockResponse).sendError(SC_NOT_FOUND);
    }

    @Test
    public void test304NotModifiedContent() throws IOException {
        initializeMocks();
        Weblog weblog = new Weblog();
        when(mockWM.getWeblogByHandle(any(), eq(true))).thenReturn(weblog);

        SharedTheme testTheme = new SharedTheme();
        testTheme.setSiteWide(false);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(testTheme);

        // confirm respondIfNotModified called with null last modified if weblog is non-site & has no last modified date
        processor = Mockito.spy(processor);
        weblog.setLastModified(null);
        doReturn(true).when(processor).respondIfNotModified(any(), any(), any(Instant.class), any());

        processor.getPage(mockRequest, mockResponse);
        assertEquals(weblog, pageRequest.getWeblog());
        verify(processor).respondIfNotModified(mockRequest, mockResponse, null, DeviceType.NORMAL);

        // confirm respondIfNotModified called with weblog's last modified date if weblog is non-site & has last modified date
        Mockito.clearInvocations(processor);
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        weblog.setLastModified(yesterday);
        processor.getPage(mockRequest, mockResponse);
        verify(processor).respondIfNotModified(mockRequest, mockResponse, yesterday, DeviceType.NORMAL);

        // confirm respondIfNotModified called with site-wide cache's last modified date if weblog is site-wide
        Mockito.clearInvocations(processor);
        testTheme.setSiteWide(true);
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        when(mockSWCache.getLastModified()).thenReturn(twoDaysAgo);
        processor.getPage(mockRequest, mockResponse);
        verify(processor).respondIfNotModified(mockRequest, mockResponse, twoDaysAgo, DeviceType.NORMAL);

        // TODO: confirm neither respondIfNotModified nor setLastModifiedHeader called for a request from a logged-in user

        // TODO: confirm setLastModifiedHeader() called if respondIfNotModified is false
/*
        Mockito.clearInvocations(processor);
        doReturn(false).when(processor.respondIfNotModified(any(), any(), any(), any()));
        processor.getPage(mockRequest, mockResponse);
        verify(processor).setLastModifiedHeader(mockResponse, testCurrentMillis, DeviceType.NORMAL); */
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
