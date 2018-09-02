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
package org.tightblog.rendering.requests;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class WeblogPageRequestTest {

    private HttpServletRequest mockRequest;

    @Before
    public void initializeMocks() {
        mockRequest = mock(HttpServletRequest.class);
    }

    @Test
    public void testEntryPermalinkPage() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/entry/blog-anchor");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("entry/blog-anchor", wpr.getExtraPathInfo());
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("entry", wpr.getContext());
        assertEquals("blog-anchor", wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertNull(wpr.getQuery());
        // pageNum = 0 so index
        assertFalse(wpr.isNoIndex());
    }

    @Test
    public void testCategoryPageNoTag() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/category/stamps");
        when(mockRequest.getParameter("page")).thenReturn("1");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("category/stamps", wpr.getExtraPathInfo());
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("category", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertEquals("stamps", wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertNull(wpr.getQuery());
        // pageNum > 0 so no index
        assertTrue(wpr.isNoIndex());
    }

    @Test
    public void testCategoryPageWithTag() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/category/stamps/tag/semipostals");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("category/stamps/tag/semipostals", wpr.getExtraPathInfo());
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("category", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertEquals("stamps", wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertEquals("semipostals", wpr.getTag());
        assertNull(wpr.getQuery());
    }

    @Test
    public void testExtraPathsIgnored() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/category/stamps/coins");
        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest request = creator.create(mockRequest);
        assertEquals(request.getWeblogHandle(), "myblog");
        assertEquals(request.getCategory(), "stamps");
    }

    @Test
    public void testTagPage() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/tag/commemoratives");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("tag", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertEquals("commemoratives", wpr.getTag());
        assertNull(wpr.getQuery());
    }

    @Test
    public void testMonthDatePage() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/date/201804");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("date", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals("201804", wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertNull(wpr.getQuery());
    }

    @Test
    public void testDayDatePage() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/date/20180402");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("date", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals("20180402", wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertNull(wpr.getQuery());
    }

    @Test
    public void testInvalidDatesIgnored() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/date/20181946");
        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest request = creator.create(mockRequest);
        assertEquals("myblog", request.getWeblogHandle());
        assertNull(request.getWeblogDate());
    }

    @Test
    public void testCustomPage() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/page/first-day-covers");
        when(mockRequest.getParameter("date")).thenReturn("20181002");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("page", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertEquals("first-day-covers", wpr.getCustomPageName());
        assertNull(wpr.getCategory());
        assertEquals("20181002", wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertNull(wpr.getQuery());
    }

    @Test
    public void testSearchPage() {
        when(mockRequest.getPathInfo()).thenReturn("/myblog/search");
        when(mockRequest.getParameter("q")).thenReturn("definitives");
        when(mockRequest.getParameter("cat")).thenReturn("stamps");

        WeblogPageRequest.Creator creator = new WeblogPageRequest.Creator();
        WeblogPageRequest wpr = creator.create(mockRequest);
        assertEquals("search", wpr.getExtraPathInfo());
        assertEquals("myblog", wpr.getWeblogHandle());
        assertEquals("search", wpr.getContext());
        assertNull(wpr.getWeblogEntryAnchor());
        assertNull(wpr.getCustomPageName());
        assertEquals("stamps", wpr.getCategory());
        assertNull(wpr.getWeblogDate());
        assertNull(wpr.getTag());
        assertEquals("definitives", wpr.getQuery());
    }

    @Test
    public void testIsValidDateString() {
        assertTrue(WeblogPageRequest.isValidDateString("20160229"));
        assertFalse(WeblogPageRequest.isValidDateString("20170229"));
        assertTrue(WeblogPageRequest.isValidDateString("201805"));
        assertFalse(WeblogPageRequest.isValidDateString("201815"));
        assertFalse(WeblogPageRequest.isValidDateString("20180547"));
        assertFalse(WeblogPageRequest.isValidDateString("2018"));
        assertFalse(WeblogPageRequest.isValidDateString("201805011"));
        assertFalse(WeblogPageRequest.isValidDateString("pumpkin"));
    }

}
