/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.servlets;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;
import org.apache.roller.weblogger.util.cache.CachedContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Only the server decides when a page render skips the page cache; a query
 * parameter from the visitor does not.
 */
class PageServletCacheTest {

    private static final byte[] CACHED =
            "cached page".getBytes(StandardCharsets.UTF_8);

    private PageServlet servlet;
    private WeblogPageCache pageCache;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletOutputStream out;

    private static void set(Object target, String name, Object value)
            throws Exception {
        Field field = PageServlet.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        servlet = new PageServlet();
        pageCache = mock(WeblogPageCache.class);
        set(servlet, "processReferrers", false);
        set(servlet, "weblogPageCache", pageCache);
        set(servlet, "siteWideCache", mock(SiteWideCache.class));

        CachedContent cached = mock(CachedContent.class);
        when(cached.getContent()).thenReturn(CACHED);
        when(cached.getContentType()).thenReturn("text/html");
        when(pageCache.generateKey(any(WeblogPageRequest.class)))
                .thenReturn("key");
        when(pageCache.get(anyString(), anyLong())).thenReturn(cached);

        request = mock(HttpServletRequest.class);
        when(request.getDateHeader("If-Modified-Since")).thenReturn(-1L);
        response = mock(HttpServletResponse.class);
        out = mock(ServletOutputStream.class);
        when(response.getOutputStream()).thenReturn(out);
    }

    private void get() throws Exception {
        Weblog weblog = mock(Weblog.class);
        try (MockedStatic<WebloggerRuntimeConfig> config =
                     mockStatic(WebloggerRuntimeConfig.class);
             MockedConstruction<WeblogPageRequest> parsed = mockConstruction(
                     WeblogPageRequest.class, (mocked, ctx) -> {
                         when(mocked.getWeblog()).thenReturn(weblog);
                         when(mocked.getDeviceType())
                                 .thenReturn(DeviceType.standard);
                     })) {
            servlet.doGet(request, response);
        }
    }

    @Test
    void skipCacheParameterStillServesTheCachedPage() throws Exception {
        when(request.getParameter("skipCache")).thenReturn("1");

        get();

        verify(pageCache).get(anyString(), anyLong());
        verify(out).write(CACHED);
    }

    @Test
    void skipCacheAttributeSkipsTheCache() throws Exception {
        when(request.getAttribute("skipCache")).thenReturn("true");

        get();

        verify(pageCache, never()).get(anyString(), anyLong());
        verify(out, never()).write(CACHED);
    }
}
