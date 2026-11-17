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

package org.apache.roller.weblogger.ui.rendering.util;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The page request parameter becomes a pager offset (page * entries per
 * page), so a parsed page number has to stay small enough that the offset
 * can't overflow.
 */
public class PageParameterTest {

    private static HttpServletRequest request(String servlet, String path,
            String page) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(servlet);
        when(request.getPathInfo()).thenReturn(path);
        when(request.getRequestURL()).thenReturn(
                new StringBuffer("http://localhost" + servlet + path));
        when(request.getParameter("page")).thenReturn(page);
        return request;
    }

    private static int feedPage(String page) throws Exception {
        return new WeblogFeedRequest(request("/roller-ui/rendering/feed",
                "/blog/entries/atom", page)).getPage();
    }

    private static int weblogPage(String page) throws Exception {
        return new WeblogPageRequest(request("/roller-ui/rendering/page",
                "/blog/", page)).getPageNum();
    }

    @Test
    public void ordinaryPageNumbersAreKept() throws Exception {
        assertEquals(0, feedPage(null));
        assertEquals(9, feedPage("9"));
        assertEquals(0, weblogPage(null));
        assertEquals(9, weblogPage("9"));
    }

    @Test
    public void hugePageNumbersCannotOverflowTheOffset() throws Exception {
        for (String page : new String[] {"2147483647", "1000000000"}) {
            int feed = feedPage(page);
            int weblog = weblogPage(page);
            assertTrue(feed > 0 && weblog > 0);
            // an offset at a generous page length still fits in an int
            assertDoesNotThrow(() -> Math.multiplyExact(feed, 1000));
            assertDoesNotThrow(() -> Math.multiplyExact(weblog, 1000));
        }
    }

    @Test
    public void negativePageNumbersMeanTheFirstPage() throws Exception {
        assertEquals(0, feedPage("-5"));
        assertEquals(0, weblogPage("-5"));
    }
}
