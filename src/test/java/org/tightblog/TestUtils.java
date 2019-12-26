/*
 * Copyright 2019 the original author or authors.
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
package org.tightblog;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.rendering.service.ThymeleafRenderer;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestUtils {

    public static final String BLOG_HANDLE = "myblog";
    public static final String ENTRY_ANCHOR = "entry-anchor";

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    public static HttpServletRequest createMockServletRequestForWeblogEntryRequest() {
        return createBaseMockServletRequest(addBlogHandle("page/%s/entry/" + ENTRY_ANCHOR));
    }

    public static HttpServletRequest createMockServletRequestForWeblogHomePageRequest() {
        return createBaseMockServletRequest(addBlogHandle("page/%s"));
    }

    public static HttpServletRequest createMockServletRequestForWeblogFeedRequest() {
        return createBaseMockServletRequest(addBlogHandle("feed/%s"));
    }

    private static String addBlogHandle(String urlPath) {
        return String.format("/tb-ui/rendering/" + urlPath, BLOG_HANDLE);
    }

    public static WeblogPageRequest extractWeblogPageRequestFromMockRenderer(ThymeleafRenderer mockRenderer)
        throws IOException {
        ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockRenderer).render(any(), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        return (WeblogPageRequest) results.get("model");
    }

    public static WeblogSearchRequest extractWeblogSearchRequestFromMockRenderer(ThymeleafRenderer mockRenderer)
            throws IOException {
        ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockRenderer).render(any(), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        return (WeblogSearchRequest) results.get("model");
    }

    // Spring REST parses servlet path directly, so not necessary to place in HttpServletRequest object.
    public static HttpServletRequest createMockServletRequest() {
        return createBaseMockServletRequest(null);
    }

    private static HttpServletRequest createBaseMockServletRequest(String servletPath) {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("bob");

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getServletPath()).thenReturn(servletPath);
        when(mockRequest.getLocale()).thenReturn(Locale.GERMAN);
        when(mockRequest.getUserPrincipal()).thenReturn(mockPrincipal);

        // have page need refreshing
        when(mockRequest.getDateHeader(any())).thenReturn(Instant.now().minus(7, ChronoUnit.DAYS).toEpochMilli());

        return mockRequest;
    }

}
