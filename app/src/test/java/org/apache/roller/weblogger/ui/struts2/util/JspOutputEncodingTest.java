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
 * instead. {@code <s:param>} strings are escaped by UIAction, so the
 * {@code <s:text>} around them doesn't escape again.
 */
class JspOutputEncodingTest {

    private static final Path JSP_ROOT = Paths.get("src/main/webapp/WEB-INF/jsps");

    private static final Pattern SCRIPT_BLOCK =
            Pattern.compile("<script\\b[^>]*>(.*?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern PROPERTY_TAG =
            Pattern.compile("<s:property\\b[^>]*>", Pattern.DOTALL);
    private static final Pattern TEXT_WITH_BODY =
            Pattern.compile("<s:text\\b((?:[^>/]|/(?!>))*)>(.*?)</s:text>", Pattern.DOTALL);
    private static final Pattern SINGLE_QUOTED_ATTRIBUTE =
            Pattern.compile("[\\w:.-]+\\s*=\\s*'[^']*<s:property", Pattern.DOTALL);
    private static final Pattern URL_WITH_BODY =
            Pattern.compile("<s:url\\b((?:[^>/]|/(?!>))*)>(.*?)</s:url>", Pattern.DOTALL);
    private static final Pattern URL_VAR = Pattern.compile("\\bvar=\"([^\"]+)\"");
    private static final Pattern INLINE_HANDLER_PROPERTY =
            Pattern.compile("(?<![\\w:.-])on[a-z]+\\s*=\\s*\"[^\"]*<s:property",
                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

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

    /**
     * An HTML-escaped value inside an inline event handler is decoded by the
     * browser before it runs as JavaScript, so values go in data-* attributes
     * that the page's script reads instead.
     */
    @Test
    void propertiesAreNotInInlineEventHandlers() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path jsp : jsps) {
            Matcher handler = INLINE_HANDLER_PROPERTY.matcher(withoutScripts(read(jsp)));
            while (handler.find()) {
                problems.add(name(jsp) + ": " + oneLine(handler.group()));
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

    /**
     * UIAction escapes {@code <s:param>} strings, so a {@code <s:text>} that
     * also escapes its output would show them escaped twice.
     */
    @Test
    void textsWithParamsDoNotEscapeAgain() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path jsp : jsps) {
            Matcher text = TEXT_WITH_BODY.matcher(read(jsp));
            while (text.find()) {
                if (text.group(1).contains("escapeHtml=\"true\"") && text.group(2).contains("<s:param")) {
                    problems.add(name(jsp) + ": " + oneLine(text.group(1)));
                }
            }
        }
        assertEquals(Collections.emptyList(), problems);
    }

    /**
     * {@code <s:url>} writes "&" as "&amp;" unless escapeAmp is false, and
     * {@code <s:property>} and the {@code <s:param>} strings of an
     * {@code <s:text>} escape it again. A link built that way reaches the
     * browser with "&amp;" in it and loses every parameter after the first.
     */
    @Test
    void urlsWithParamsAreNotEscapedTwice() throws IOException {
        List<String> problems = new ArrayList<>();
        for (Path jsp : jsps) {
            String text = read(jsp);
            Matcher url = URL_WITH_BODY.matcher(text);
            while (url.find()) {
                Matcher var = URL_VAR.matcher(url.group(1));
                if (!var.find() || url.group(1).contains("escapeAmp=\"false\"")
                        || url.group(2).split("<s:param\\b", -1).length < 3) {
                    continue;
                }
                String name = Pattern.quote(var.group(1));
                // Uses of this URL run until the next <s:url> that sets the
                // same var. <s:a href="%{name}"> isn't checked: its template
                // writes href without escaping it again.
                Matcher redefined = Pattern.compile("<s:url\\b[^>]*\\bvar=\"" + name + "\"")
                        .matcher(text);
                int end = redefined.find(url.end()) ? redefined.start() : text.length();
                Matcher written = Pattern.compile("<s:(property|param)\\b[^>]*value=[\"'](%\\{)?#?"
                        + name + "\\}?[\"'][^>]*>").matcher(text).region(url.end(), end);
                while (written.find()) {
                    // Unescaped is right in HTML, but escaping for JavaScript
                    // leaves &amp; in the string.
                    if (!written.group().contains("escapeHtml=\"false\"")
                            || written.group().contains("escapeJavaScript=\"true\"")) {
                        problems.add(name(jsp) + ": " + var.group(1));
                        break;
                    }
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
