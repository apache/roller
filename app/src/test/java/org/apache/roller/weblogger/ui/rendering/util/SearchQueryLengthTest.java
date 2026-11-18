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

import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Search requests keep at most the first 256 characters of a query, which is
 * plenty for a person and keeps one request from asking the query parser for
 * unbounded work, such as thousands of nested parentheses.
 */
class SearchQueryLengthTest {

    private static final String DEEP = "(".repeat(10_000) + "roller";

    @Test
    void searchPageQueryIsCapped() throws Exception {
        HttpServletRequest request = request(WeblogSearchRequest.SEARCH_SERVLET, "/team", DEEP);
        String query = new WeblogSearchRequest(request).getQuery();
        assertEquals(256, query.length());
        assertEquals(DEEP.substring(0, 256), query);
    }

    @Test
    void searchFeedQueryIsCapped() throws Exception {
        HttpServletRequest request = request("/roller-ui/rendering/feed", "/team/entries/atom", DEEP);
        assertEquals(256, new WeblogFeedRequest(request).getTerm().length());
    }

    @Test
    void shortQueryIsKept() throws Exception {
        HttpServletRequest request = request(WeblogSearchRequest.SEARCH_SERVLET, "/team", "roller weblog");
        assertEquals("roller weblog", new WeblogSearchRequest(request).getQuery());
    }

    @Test
    void capDoesNotSplitACharacter() throws Exception {
        // a character outside the Basic Multilingual Plane straddling the cap
        String query = "a".repeat(255) + "😀" + "tail";
        HttpServletRequest request = request(WeblogSearchRequest.SEARCH_SERVLET, "/team", query);
        assertEquals("a".repeat(255), new WeblogSearchRequest(request).getQuery());
    }

    @Test
    void capDropsAQuoteItLeavesUnbalanced() throws Exception {
        // the cap falls inside the quoted phrase
        String query = "x".repeat(250) + " \"roller weblog\"";
        HttpServletRequest request = request(WeblogSearchRequest.SEARCH_SERVLET, "/team", query);
        assertEquals("x".repeat(250) + " roll", new WeblogSearchRequest(request).getQuery());
    }

    @Test
    void capKeepsBalancedQuotes() throws Exception {
        String query = "\"roller weblog\" " + "x".repeat(300);
        HttpServletRequest request = request(WeblogSearchRequest.SEARCH_SERVLET, "/team", query);
        assertEquals(query.substring(0, 256), new WeblogSearchRequest(request).getQuery());
    }

    private static HttpServletRequest request(String servlet, String pathInfo, String query) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn(servlet);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost" + servlet + pathInfo));
        when(request.getParameter("q")).thenReturn(query);
        return request;
    }
}
