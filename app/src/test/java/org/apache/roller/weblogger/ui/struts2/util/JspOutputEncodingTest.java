/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Checks how the UI JSPs write values with {@code <s:property>}.
 *
 * <p>{@code <s:property>} escapes HTML special characters except the
 * apostrophe, so its output belongs in element content or double-quoted
 * attributes. Inside {@code <script>} blocks values are JavaScript-escaped
 * instead.
 */
class JspOutputEncodingTest {

    private static final Path JSP_ROOT = Paths.get("src/main/webapp/WEB-INF/jsps");

    private static final Pattern SCRIPT_BLOCK =
            Pattern.compile("<script\\b[^>]*>(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern PROPERTY_TAG =
            Pattern.compile("<s:property\\b[^>]*>", Pattern.DOTALL);
    private static final Pattern SINGLE_QUOTED_ATTRIBUTE =
            Pattern.compile("[\\w:.-]+\\s*=\\s*'[^']*<s:property", Pattern.DOTALL);

    /**
     * Pages whose unescaped values are escaped before they reach the page, or
     * that are not HTML.
     */
    private static final List<String> UNESCAPED_ALLOWED = Arrays.asList(
            "tiles/messages.jsp",               // UIAction escapes message arguments
            "editor/MediaFileAddSuccess.jsp",   // same, for upload error messages
            "editor/MediaFileAddResult.jsp");   // a JSON response, not HTML

    private static List<Path> jsps;

    @BeforeAll
    static void findJsps() throws IOException {
        try (Stream<Path> files = Files.walk(JSP_ROOT)) {
            jsps = files.filter(p -> p.toString().endsWith(".jsp")).sorted().collect(Collectors.toList());
        }
        assertFalse(jsps.isEmpty(), "no JSPs found under " + JSP_ROOT.toAbsolutePath());
    }

    @Test
    void propertiesInScriptsAreJavaScriptEscaped() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path jsp : jsps) {
            Matcher script = SCRIPT_BLOCK.matcher(read(jsp));
            while (script.find()) {
                Matcher property = PROPERTY_TAG.matcher(script.group(1));
                while (property.find()) {
                    if (!property.group().contains("escapeJavaScript=\"true\"")) {
                        problems.add(name(jsp) + ": " + oneLine(property.group()));
                    }
                }
            }
        }
        assertEquals(Collections.emptyList(), problems);
    }

    @Test
    void propertiesAreNotInSingleQuotedAttributes() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path jsp : jsps) {
            Matcher attribute = SINGLE_QUOTED_ATTRIBUTE.matcher(withoutScripts(read(jsp)));
            while (attribute.find()) {
                problems.add(name(jsp) + ": " + oneLine(attribute.group()));
            }
        }
        assertEquals(Collections.emptyList(), problems);
    }

    @Test
    void propertiesOutsideScriptsEscapeHtml() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path jsp : jsps) {
            if (UNESCAPED_ALLOWED.contains(name(jsp))) {
                continue;
            }
            Matcher property = PROPERTY_TAG.matcher(withoutScripts(read(jsp)));
            while (property.find()) {
                if (property.group().contains("escapeHtml=\"false\"")) {
                    problems.add(name(jsp) + ": " + oneLine(property.group()));
                }
            }
        }
        assertEquals(Collections.emptyList(), problems);
    }

    private static String read(Path jsp) throws IOException {
        return new String(Files.readAllBytes(jsp), StandardCharsets.UTF_8);
    }

    private static String withoutScripts(String text) {
        return SCRIPT_BLOCK.matcher(text).replaceAll("<script></script>");
    }

    private static String name(Path jsp) {
        return JSP_ROOT.relativize(jsp).toString().replace('\\', '/');
    }

    private static String oneLine(String s) {
        return s.replaceAll("\\s+", " ");
    }
}
