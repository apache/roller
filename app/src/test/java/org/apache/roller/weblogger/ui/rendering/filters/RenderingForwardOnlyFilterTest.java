/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.roller.weblogger.ui.rendering.filters;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Weblog pages, feeds, search and theme resources are served only through a
 * forward from the weblog's own URL. Comment posts, RSD and media files keep
 * their direct addresses.
 */
class RenderingForwardOnlyFilterTest {

    private static final String FILTER = "RenderingForwardOnlyFilter";
    private static final List<String> FORWARD_ONLY = Arrays.asList(
            "/roller-ui/rendering/feed/*", "/roller-ui/rendering/page/*",
            "/roller-ui/rendering/resources/*", "/roller-ui/rendering/search/*");
    private static final List<String> DIRECT = Arrays.asList(
            "/roller-ui/rendering/comment/*", "/roller-ui/rendering/media-resources/*",
            "/roller-ui/rendering/rsd/*");

    @Test
    void webXmlMapsTheFilterForDirectRequestsToWeblogContent() throws Exception {
        List<String> patterns = new ArrayList<>();
        for (String mapping : blocks(uncommentedWebXml(), "filter-mapping")) {
            if (!values(mapping, "filter-name").contains(FILTER)) {
                continue;
            }
            assertEquals(Arrays.asList("REQUEST"), values(mapping, "dispatcher"), mapping);
            patterns.addAll(values(mapping, "url-pattern"));
        }
        assertEquals(new TreeSet<>(FORWARD_ONLY), new TreeSet<>(patterns));
    }

    @Test
    void everyRenderingServletIsEitherForwardOnlyOrKeptDirect() throws Exception {
        List<String> rendering = new ArrayList<>();
        for (String mapping : blocks(uncommentedWebXml(), "servlet-mapping")) {
            for (String pattern : values(mapping, "url-pattern")) {
                if (pattern.startsWith("/roller-ui/rendering/")) {
                    rendering.add(pattern);
                }
            }
        }
        List<String> known = new ArrayList<>(FORWARD_ONLY);
        known.addAll(DIRECT);
        assertEquals(new TreeSet<>(known), new TreeSet<>(rendering),
                "a new rendering servlet needs a choice here");
    }

    @Test
    void aDirectRequestGetsNotFound() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        new RenderingForwardOnlyFilter().doFilter(request, response, chain);

        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
        verifyNoInteractions(chain);
    }

    @Test
    void aForwardIsServed() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getDispatcherType()).thenReturn(DispatcherType.FORWARD);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        new RenderingForwardOnlyFilter().doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(response, never()).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private static String uncommentedWebXml() throws Exception {
        return new String(Files.readAllBytes(Paths.get("src/main/webapp/WEB-INF/web.xml")),
                StandardCharsets.UTF_8).replaceAll("(?s)<!--.*?-->", "");
    }

    private static List<String> blocks(String xml, String element) {
        return values(xml, element, "(?s)<" + element + ">(.*?)</" + element + ">");
    }

    private static List<String> values(String xml, String element) {
        return values(xml, element, "<" + element + ">\\s*([^<]+?)\\s*</" + element + ">");
    }

    private static List<String> values(String xml, String element, String regex) {
        List<String> found = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(xml);
        while (m.find()) {
            found.add(m.group(1));
        }
        return found;
    }
}
