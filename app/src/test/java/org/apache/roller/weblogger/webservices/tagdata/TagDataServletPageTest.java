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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private static String get(String handle, String format, int page) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getPathInfo()).thenReturn("/weblog/" + handle);
        when(request.getParameter("format")).thenReturn(format);
        when(request.getParameter("page")).thenReturn(String.valueOf(page));
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        new TagDataServlet().doGet(request, response);
        return body.toString();
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            count++;
        }
        return count;
    }

    @Test
    public void eachPageHoldsThePageSizeAndPagesDontOverlap() throws Exception {
        // the default services.tagdata.max
        int max = 30;
        User user = TestUtils.setupUser("tagdatapageuser");
        Weblog weblog = TestUtils.setupWeblog("tagdatapageweblog", user);
        TestUtils.endSession(true);
        try {
            WeblogEntry entry = TestUtils.setupWeblogEntry("tagdatapageentry",
                    TestUtils.getManagedWebsite(weblog), TestUtils.getManagedUser(user));
            // one tag more than two full pages: pagetag00 ... pagetag60
            for (int i = 0; i <= 2 * max; i++) {
                entry.addTag(String.format("pagetag%02d", i));
            }
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(entry);
            TestUtils.endSession(true);

            ObjectMapper mapper = new ObjectMapper();
            for (int page = 0; page < 2; page++) {
                JsonNode tags = mapper.readTree(get(weblog.getHandle(), "json", page)).get("tagcounts");
                assertEquals(max, tags.size());
                assertEquals(String.format("pagetag%02d", page * max), tags.get(0).get("tag").asText());
                assertEquals(String.format("pagetag%02d", page * max + max - 1),
                        tags.get(max - 1).get("tag").asText());

                String xml = get(weblog.getHandle(), "xml", page);
                assertEquals(max, count(xml, "<atom:category "));
                assertTrue(xml.contains(String.format("term=\"pagetag%02d\"", page * max + max - 1)));
                assertFalse(xml.contains(String.format("term=\"pagetag%02d\"", page * max + max)));
                assertTrue(xml.contains("rel=\"next\""));
            }

            JsonNode last = mapper.readTree(get(weblog.getHandle(), "json", 2)).get("tagcounts");
            assertEquals(1, last.size());
            assertEquals("pagetag60", last.get(0).get("tag").asText());
            String lastXml = get(weblog.getHandle(), "xml", 2);
            assertEquals(1, count(lastXml, "<atom:category "));
            assertFalse(lastXml.contains("rel=\"next\""));
        } finally {
            TestUtils.teardownWeblog(weblog.getId());
            TestUtils.teardownUser(user.getUserName());
            TestUtils.endSession(true);
        }
    }
}
