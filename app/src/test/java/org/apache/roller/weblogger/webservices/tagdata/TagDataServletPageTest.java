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

package org.apache.roller.weblogger.webservices.tagdata;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The tag data servlet multiplies the page number by its page size to get a
 * query offset, so it only accepts page numbers from 0 up to the same limit
 * the weblog and feed pagers use.
 */
public class TagDataServletPageTest {

    @BeforeAll
    public static void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    private static HttpServletResponse get(String page) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getPathInfo()).thenReturn("/weblog/nosuchtagdataweblog");
        when(request.getParameter("page")).thenReturn(page);
        HttpServletResponse response = mock(HttpServletResponse.class);
        new TagDataServlet().doGet(request, response);
        return response;
    }

    @Test
    public void negativePageIsRejected() throws Exception {
        verify(get("-1")).sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL");
    }

    @Test
    public void hugePageIsRejected() throws Exception {
        verify(get("2147483647")).sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL");
        verify(get("10001")).sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL");
    }

    @Test
    public void pagesUpToTheLimitAreAccepted() throws Exception {
        // gets past page parsing to the weblog lookup
        verify(get("0")).sendError(HttpServletResponse.SC_BAD_REQUEST, "Weblog not found");
        verify(get("10000")).sendError(HttpServletResponse.SC_BAD_REQUEST, "Weblog not found");
    }
}
