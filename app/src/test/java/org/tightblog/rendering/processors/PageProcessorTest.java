package org.tightblog.rendering.processors;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.RendererManager;
import org.tightblog.rendering.cache.LazyExpiringCache;
import org.tightblog.rendering.cache.SiteWideCache;
import org.tightblog.rendering.requests.WeblogPageRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Before
    public void initializePerTest() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockRequestDispatcher = mock(RequestDispatcher.class);
        when(mockRequest.getRequestDispatcher(anyString())).thenReturn(mockRequestDispatcher);
        properties = new WebloggerProperties();
        // properties.setCommentHtmlPolicy(HTMLSanitizer.Level.LIMITED);
        wprCreator = mock(WeblogPageRequest.Creator.class);
        pageRequest = new WeblogPageRequest();
        when(wprCreator.create(any())).thenReturn(pageRequest);
        mockWEM = mock(WeblogEntryManager.class);
        processor = new PageProcessor();
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
    public void testGenerateKey() {
    }
}
