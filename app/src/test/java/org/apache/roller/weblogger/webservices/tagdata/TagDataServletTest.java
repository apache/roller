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

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class TagDataServletTest {

    private static final List<String> TAGS = List.of("ends\\", "say\"hi\"", "a<b>&c");

    private static String get(String format) throws Exception {
        Weblog weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("blog");
        WeblogManager weblogs = mock(WeblogManager.class);
        when(weblogs.getWeblogByHandle("blog")).thenReturn(weblog);
        List<TagStat> stats = new ArrayList<>();
        for (String name : TAGS) {
            TagStat stat = new TagStat();
            stat.setName(name);
            stat.setCount(2);
            stats.add(stat);
        }
        WeblogEntryManager entries = mock(WeblogEntryManager.class);
        when(entries.getTags(eq(weblog), isNull(), isNull(), anyInt(), anyInt())).thenReturn(stats);
        URLStrategy urls = mock(URLStrategy.class);
        when(urls.getWeblogCollectionURL(any(), any(), any(), any(), anyList(), anyInt(), anyBoolean()))
                .thenReturn("https://example.org/blog/tags/x?a=1&b=2");
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getWeblogManager()).thenReturn(weblogs);
        when(weblogger.getWeblogEntryManager()).thenReturn(entries);
        when(weblogger.getUrlStrategy()).thenReturn(urls);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getPathInfo()).thenReturn("/weblog/blog");
        when(request.getParameter("format")).thenReturn(format);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body, true));

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            new TagDataServlet().doGet(request, response);
        }
        return body.toString();
    }

    @Test
    public void jsonKeepsTagNames() throws Exception {
        String json = get("json");
        JsonNode counts = new ObjectMapper().readTree(json).get("tagcounts");
        List<String> names = new ArrayList<>();
        counts.forEach(count -> names.add(count.get("tag").asText()));
        assertEquals(TAGS, names, json);
    }

    @Test
    public void xmlKeepsTagNames() throws Exception {
        String xml = get("xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        NodeList categories = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "category");
        List<String> names = new ArrayList<>();
        for (int i = 0; i < categories.getLength(); i++) {
            names.add(((Element) categories.item(i)).getAttribute("term"));
        }
        assertEquals(TAGS, names, xml);
    }
}
