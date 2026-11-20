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

package org.apache.roller.weblogger.util;

import java.time.Duration;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HTMLSanitizerTest {

    @Test
    void keepsFormattingTablesAndLinks() {
        assertNull(HTMLSanitizer.sanitize(null));
        String html = "<h2 class=\"title\">Hi</h2><p align=\"center\"><b>bold</b> <em>em</em>"
                + " <code>x</code></p><table border=\"1\"><tbody><tr><td colspan=\"2\">cell</td>"
                + "</tr></tbody></table><a href=\"https://example.com/\" title=\"site\">site</a>"
                + " <a href=\"mailto:reader@example.com\">mail</a>"
                + "<img src=\"https://example.com/a.png\" alt=\"a\" width=\"10\" height=\"50%\">";
        assertEquals(html, HTMLSanitizer.sanitize(html));
        assertEquals("<p style=\"color: red; font-weight: bold\">x</p>",
                HTMLSanitizer.sanitize("<p style=\"color: red; font-weight: bold\">x</p>"));
    }

    @Test
    void removesActiveContent() {
        assertEquals("<b>Hi</b>", HTMLSanitizer.sanitize("<b>Hi</b><script>alert(1)</script>"));
        assertEquals("", HTMLSanitizer.sanitize("<style>body{display:none}</style>"));
        assertEquals("<b>x</b>", HTMLSanitizer.sanitize("<b onclick=\"alert(1)\">x</b>"));
        assertEquals("<img>", HTMLSanitizer.sanitize("<img src=x onerror=alert(1)>"));
        assertEquals("<a>x</a>", HTMLSanitizer.sanitize("<a href=\"javascript:alert(1)\">x</a>"));
        assertEquals("<a>x</a>", HTMLSanitizer.sanitize("<a href=\"/relative\">x</a>"));
        assertEquals("x", HTMLSanitizer.sanitize("<iframe src=\"https://example.com/\"></iframe>x"));
        assertEquals("", HTMLSanitizer.sanitize("<object data=\"x.swf\"></object><embed src=\"x.swf\">"));
        assertEquals("", HTMLSanitizer.sanitize("<form action=\"https://example.com/\"><input name=q></form>"));
        assertEquals("", HTMLSanitizer.sanitize("<link rel=stylesheet href=\"https://example.com/x.css\">"));
        assertEquals("x", HTMLSanitizer.sanitize("<svg onload=alert(1)>x</svg>"));
        assertEquals("<div>x</div>", HTMLSanitizer.sanitize("<div data-x=\"1\" formaction=\"y\">x</div>"));
    }

    @Test
    void encodesQuotesInTextButNotInAttributes() {
        assertEquals("x&quot; onmouseover=&quot;alert(1)",
                HTMLSanitizer.sanitize("x\" onmouseover=\"alert(1)"));
        assertEquals("it&#39;s", HTMLSanitizer.sanitize("it's"));
        // already encoded quotes aren't encoded twice
        assertEquals("&quot;x&quot; &#39;y&#39;", HTMLSanitizer.sanitize("&quot;x&quot; &#39;y&#39;"));
        String html = "<a href=\"https://example.com/?a='b'\" title=\"it's &quot;x&quot; &gt; y\">"
                + "&quot;q&quot; &amp; &lt;b&gt;</a>";
        assertEquals(html, HTMLSanitizer.sanitize(html));
        for (String input : new String[] {html, "x\" y='z'", "<p>\"a\" & 'b'</p>", "&amp;quot;"}) {
            String once = HTMLSanitizer.sanitize(input);
            assertEquals(once, HTMLSanitizer.sanitize(once), input);
        }
    }

    @Test
    void keepsTextAfterALessThanSignThatNeverEnds() {
        assertEquals("Why x&lt;y matters", HTMLSanitizer.sanitize("Why x<y matters"));
        assertEquals("<b>Why</b> x&lt;y &lt;z", HTMLSanitizer.sanitize("<b>Why</b> x<y <z"));
        assertEquals("a &lt;!-- b", HTMLSanitizer.sanitize("a <!-- b"));
        assertEquals("Me&lt;You", HTMLSanitizer.sanitizeText("Me<You"));
        assertEquals("Me&lt;You & x&region", HTMLSanitizer.sanitizeText("Me<You & x&region"));
        assertTrue(HTMLSanitizer.xssEnabled);
        User user = new User();
        user.setScreenName("Me<You");
        assertEquals("Me&lt;You", user.getScreenName());
    }

    @Test
    void plainTextKeepsAmpersands() {
        assertNull(HTMLSanitizer.sanitizeText(null));
        assertEquals("a&b@example.com", HTMLSanitizer.sanitizeText("a&b@example.com"));
        assertEquals("Tom & Jerry", HTMLSanitizer.sanitizeText("Tom & Jerry"));
        assertEquals("https://example.com/id?a=1&b=2",
                HTMLSanitizer.sanitizeText("https://example.com/id?a=1&b=2"));
        // tags are still removed and quotes and angle brackets still encoded
        assertEquals("Tom", HTMLSanitizer.sanitizeText("<script>alert(1)</script>Tom"));
        assertEquals("<b>Tom</b>", HTMLSanitizer.sanitizeText("<b onclick=\"x()\">Tom</b>"));
        assertEquals("O&#39;Brien &quot;x&quot;", HTMLSanitizer.sanitizeText("O'Brien \"x\""));
        assertEquals("a &lt; b &gt; c", HTMLSanitizer.sanitizeText("a < b > c"));
        // text that looks like an entity is kept as typed
        for (String text : new String[] {
                "https://example.com/id?a=1&region=us&copy=1", "tom&reggie@example.com",
                "O&#39;Brien", "&lt;script&gt;", "&amp;lt;script&amp;gt;", "Tom &amp; Jerry"}) {
            assertEquals(text, HTMLSanitizer.sanitizeText(text));
        }
        assertEquals("<b>a&region</b>", HTMLSanitizer.sanitizeText("<b>a&region</b>"));
        for (String text : new String[] {"O'Brien \"x\"", "a < b & c > d", "<b>Tom</b> & <i>Jerry"}) {
            String once = HTMLSanitizer.sanitizeText(text);
            assertEquals(once, HTMLSanitizer.sanitizeText(once), text);
        }
        // attribute values stay encoded
        assertEquals("<a href=\"https://example.com/?a=1&amp;b=2\" title=\"&amp;quot;\">Tom & Jerry</a>",
                HTMLSanitizer.sanitizeText(
                        "<a href=\"https://example.com/?a=1&amp;b=2\" title=\"&amp;quot;\">Tom & Jerry</a>"));
        assertEquals("<b>x</b>", HTMLSanitizer.sanitizeText("<b style=\"&amp;#x75;rl(x)\">x</b>"));
    }

    @Test
    void plainTextFieldsKeepAmpersands() {
        assertTrue(HTMLSanitizer.xssEnabled);
        User user = new User();
        user.setEmailAddress("a&b@example.com");
        assertEquals("a&b@example.com", user.getEmailAddress());
        user.setOpenIdUrl("https://example.com/id?a=1&b=2");
        assertEquals("https://example.com/id?a=1&b=2", user.getOpenIdUrl());
        user.setFullName("Tom & Jerry<script>alert(1)</script>");
        assertEquals("Tom & Jerry", user.getFullName());
        user.setScreenName("Tom & Jerry");
        assertEquals("Tom & Jerry", user.getScreenName());
        user.setUserName("tom&jerry");
        assertEquals("tom&jerry", user.getUserName());

        WeblogBookmark bookmark = new WeblogBookmark();
        bookmark.setName("Tom & Jerry");
        assertEquals("Tom & Jerry", bookmark.getName());
        bookmark.setDescription("Cat & mouse");
        assertEquals("Cat & mouse", bookmark.getDescription());
        WeblogBookmarkFolder folder = new WeblogBookmarkFolder();
        folder.setName("Tom & Jerry");
        assertEquals("Tom & Jerry", folder.getName());
    }

    @Test
    void keepsImageMapsAndLinkListAndCellAttributes() {
        for (String html : new String[] {
                "<a href=\"https://example.com/\" target=\"_blank\" rel=\"noopener nofollow\">x</a>",
                "<img src=\"https://example.com/a.png\" usemap=\"#m\">"
                        + "<map name=\"m\"><area shape=\"rect\" coords=\"0,0,10,10\" alt=\"a\"></map>",
                "<param name=\"movie\" value=\"x\">",
                "<ol><li value=\"3\">x</li></ol>",
                "<table><tbody><tr><th scope=\"col\">h</th><td nowrap>c</td></tr></tbody></table>"}) {
            assertEquals(html, HTMLSanitizer.sanitize(html));
        }
        assertEquals("<table><tbody><tr><td nowrap>c</td></tr></tbody></table>", HTMLSanitizer.sanitize(
                "<table><tbody><tr><td nowrap=\"nowrap\">c</td></tr></tbody></table>"));
        // an area's link was never kept
        assertEquals("<map><area></map>",
                HTMLSanitizer.sanitize("<map><area href=\"https://example.com/\"></map>"));
    }

    @Test
    void removesStylesAndSizesThatCouldLoadOrRunSomething() {
        for (String style : new String[] {
                "background:url(x.png)", "background:url('javascript:alert(1)')",
                "background:url(data:image/png;base64,AAAA)", "background:url (ftp://example.com/x)",
                "background:url(https://example.com/x.png", "background:url(https://example.com/a b)",
                "width:expression(alert(1))", "width:EXPRESSION (alert(1))", "width:eval(x)",
                "width:eval (x)", "background:javascript (x)", "background:javascript:alert(1)",
                "background:image-set(\"x.png\" 1x)", "behavior:url(https://example.com/x.htc)",
                "-moz-binding:url(https://example.com/x)", "width:\\65xpression(alert(1))",
                "color:/**/red", "@import 'x.css'", "@import url(https://example.com/x.css)",
                "red", ":red", "color:"}) {
            assertEquals("<p>x</p>", HTMLSanitizer.sanitize("<p style=\"" + style + "\">x</p>"), style);
        }
        assertEquals("<img src=\"https://example.com/a.png\">",
                HTMLSanitizer.sanitize("<img src=\"https://example.com/a.png\" width=\"10px;x\" height=\"1e9\">"));
    }

    @Test
    void removesOnlyTheUnsafeStyleDeclarations() {
        for (String style : new String[] {
                "scroll-behavior: smooth", "background: url(https://example.com/x.png) no-repeat",
                "background-image: url('http://example.com/x.png')",
                "font-family: 'Times New Roman', serif; color: red"}) {
            assertEquals("<p style=\"" + style + "\">x</p>",
                    HTMLSanitizer.sanitize("<p style=\"" + style + "\">x</p>"), style);
        }
        assertEquals("<p style=\"background: url(&quot;https://example.com/x.png&quot;)\">x</p>",
                HTMLSanitizer.sanitize("<p style='background: url(\"https://example.com/x.png\")'>x</p>"));
        assertEquals("<p style=\"color: red\">x</p>",
                HTMLSanitizer.sanitize("<p style=\"color: red;\\75rl(x)\">x</p>"));
        assertEquals("<p style=\"color: red; font-weight: bold\">x</p>", HTMLSanitizer.sanitize(
                "<p style=\"color: red; width: expression(alert(1)); font-weight: bold;"
                + " background: url(x.png)\">x</p>"));
        // a semicolon inside a quoted url() doesn't let the rest through
        assertEquals("<p style=\"color: red\">x</p>", HTMLSanitizer.sanitize(
                "<p style=\"background: url('https://example.com/a;b:url(x)'); color: red\">x</p>"));
    }

    @Test
    void sizesAreAsciiDigitsWithNoSpaces() {
        assertEquals("<img src=\"https://example.com/a.png\" width=\"10\" height=\"50%\">",
                HTMLSanitizer.sanitize("<img src=\"https://example.com/a.png\" width=\"10\" height=\"50%\">"));
        assertEquals("<img src=\"https://example.com/a.png\">",
                HTMLSanitizer.sanitize("<img src=\"https://example.com/a.png\" width=\" 10 \" height=\"\u0661\u0660\">"));
    }

    @Test
    void longInputsTakeLinearTime() {
        // each of these made one of the old regular expressions backtrack
        String[] inputs = {
            "<a " + "x=\"\" ".repeat(20_000),
            "<p style=\"" + "a:b ".repeat(20_000) + "\">",
            "<p style=\"background:url(" + "'".repeat(20_000) + "\">",
            "<" + "a".repeat(50_000),
            "<!--" + "-".repeat(50_000),
        };
        // a generous bound, so that only a real hang fails on a slow build machine
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            for (String input : inputs) {
                HTMLSanitizer.sanitize(input);
            }
        });
    }
}
