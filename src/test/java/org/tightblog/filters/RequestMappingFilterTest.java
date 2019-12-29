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
package org.tightblog.filters;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.rendering.controller.CommentController;
import org.tightblog.rendering.controller.FeedController;
import org.tightblog.rendering.controller.MediaFileController;
import org.tightblog.rendering.controller.PageController;
import org.tightblog.rendering.controller.SearchController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestMappingFilterTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private RequestDispatcher mockRequestDispatcher;

    @Before
    public void initializeMocks() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockRequestDispatcher = mock(RequestDispatcher.class);
        when(mockRequest.getRequestDispatcher(anyString())).thenReturn(mockRequestDispatcher);
    }

    @Test
    public void testHandleRequestReturnsFalseOnFrontPageBlog() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        when(mockRequest.getRequestURI()).thenReturn("tightblog/");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertFalse(filter.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void testHandleRequestReturnsFalseOnInvalidWeblogHandle() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        Set<String> invalidHandles = new HashSet<>();
        invalidHandles.add("images");
        filter.setInvalidWeblogHandles(invalidHandles);
        when(mockRequest.getRequestURI()).thenReturn("tightblog/images///");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertFalse(filter.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void testHandleRequestReturnsFalseOnUnknownContext() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        // posts is not valid context (should be "entry")
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog/posts/myentry");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertFalse(filter.handleRequest(mockRequest, mockResponse));
    }

    @Test
    public void testHandleRequestFindsBlogHomePage() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog///");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertTrue(filter.handleRequest(mockRequest, mockResponse));
        String expectedURL = filter.calculateForwardUrl(mockRequest, "myblog", null, null);
        verify(mockRequest).getRequestDispatcher(expectedURL);
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testHandleRequestFindsBlogEntry() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog/entry/myblogentry///");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertTrue(filter.handleRequest(mockRequest, mockResponse));
        String expectedURL = filter.calculateForwardUrl(mockRequest, "myblog", "entry", "myblogentry");
        verify(mockRequest).getRequestDispatcher(expectedURL);
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testHandleRequestFindsBlogCustomPage() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog/page/events");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertTrue(filter.handleRequest(mockRequest, mockResponse));
        String expectedURL = filter.calculateForwardUrl(mockRequest, "myblog", "page", "events");
        verify(mockRequest).getRequestDispatcher(expectedURL);
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testHandleRequestFindsBlogSearch() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog/search");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertTrue(filter.handleRequest(mockRequest, mockResponse));
        String expectedURL = filter.calculateForwardUrl(mockRequest, "myblog", "search", null);
        verify(mockRequest).getRequestDispatcher(expectedURL);
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testHandleRequestFindsBlogFeed() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog/feed/category/stamps/");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertTrue(filter.handleRequest(mockRequest, mockResponse));
        String expectedURL = filter.calculateForwardUrl(mockRequest, "myblog", "feed", "category/stamps");
        verify(mockRequest).getRequestDispatcher(expectedURL);
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testHandleRequestFindsMediaFile() throws Exception {
        RequestMappingFilter filter = new RequestMappingFilter();
        filter.setInvalidWeblogHandles(new HashSet<>());
        when(mockRequest.getRequestURI()).thenReturn("tightblog/myblog///");
        when(mockRequest.getContextPath()).thenReturn("tightblog");
        assertTrue(filter.handleRequest(mockRequest, mockResponse));
        String expectedURL = filter.calculateForwardUrl(mockRequest, "myblog", null, null);
        verify(mockRequest).getRequestDispatcher(expectedURL);
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    @Test
    public void testCalculateForwardUrl() {
        RequestMappingFilter filter = new RequestMappingFilter();

        String pageTest1 = filter.calculateForwardUrl(mockRequest, "handle", null, "data");
        assertEquals(filter.generateForwardUrl(PageController.PATH, "handle", null, "data"), pageTest1);
        String pageTest2 = filter.calculateForwardUrl(mockRequest, "handle", "page", "data");
        assertEquals(filter.generateForwardUrl(PageController.PATH, "handle", "page", "data"), pageTest2);
        String pageTest3 = filter.calculateForwardUrl(mockRequest, "handle", "entry", "data");
        assertEquals(filter.generateForwardUrl(PageController.PATH, "handle", "entry", "data"), pageTest3);
        String pageTest4 = filter.calculateForwardUrl(mockRequest, "handle", "date", "data");
        assertEquals(filter.generateForwardUrl(PageController.PATH, "handle", "date", "data"), pageTest4);
        String pageTest5 = filter.calculateForwardUrl(mockRequest, "handle", "category", "data");
        assertEquals(filter.generateForwardUrl(PageController.PATH, "handle", "category", "data"), pageTest5);
        String pageTest6 = filter.calculateForwardUrl(mockRequest, "handle", "tag", "data");
        assertEquals(filter.generateForwardUrl(PageController.PATH, "handle", "tag", "data"), pageTest6);

        String feedTest = filter.calculateForwardUrl(mockRequest, "handle", "feed", "data");
        assertEquals(filter.generateForwardUrl(FeedController.PATH, "handle", null, "data"), feedTest);
        String mediaTest = filter.calculateForwardUrl(mockRequest, "handle", "mediafile", "data");
        assertEquals(filter.generateForwardUrl(MediaFileController.PATH, "handle", null, "data"), mediaTest);
        String searchTest = filter.calculateForwardUrl(mockRequest, "handle", "search", "data");
        assertEquals(filter.generateForwardUrl(SearchController.PATH, "handle", null, null), searchTest);

        // unknown context should return null
        String nullTest = filter.calculateForwardUrl(mockRequest, "handle", "xyz", "data");
        assertNull(nullTest);

        when(mockRequest.getMethod()).thenReturn("POST");
        // POST of context = page should return null
        String pageTest7 = filter.calculateForwardUrl(mockRequest, "handle", "page", "data");
        assertNull(pageTest7);
        // no content, should return null
        String commentTest1 = filter.calculateForwardUrl(mockRequest, "handle", "entrycomment", "data");
        assertNull(commentTest1);
        when(mockRequest.getParameter("content")).thenReturn("comment content");
        String commentTest2 = filter.calculateForwardUrl(mockRequest, "handle", "entrycomment", "data");
        assertEquals(filter.generateForwardUrl(CommentController.PATH, "handle", "entry", "data"), commentTest2);
    }

    @Test
    public void testGenerateForwardUrl() {
        RequestMappingFilter filter = new RequestMappingFilter();
        assertEquals("proc/handle", filter.generateForwardUrl("proc", "handle", null, null));
        assertEquals("proc/handle/context", filter.generateForwardUrl("proc", "handle", "context", null));
        assertEquals("proc/handle/data", filter.generateForwardUrl("proc", "handle", null, "data"));
        assertEquals("proc/handle/context/data", filter.generateForwardUrl("proc", "handle", "context", "data"));
    }

}