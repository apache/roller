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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    void removesStylesAndSizesThatCouldLoadOrRunSomething() {
        for (String style : new String[] {
                "background:url(https://example.com/x.png)", "width:expression(alert(1))",
                "background:image-set(\"x.png\" 1x)", "behavior:url(x.htc)", "-moz-binding:url(x)",
                "color:red;\\75rl(x)", "color:/**/red", "@import 'x.css'"}) {
            assertEquals("<p>x</p>", HTMLSanitizer.sanitize("<p style=\"" + style + "\">x</p>"), style);
        }
        assertEquals("<img src=\"https://example.com/a.png\">",
                HTMLSanitizer.sanitize("<img src=\"https://example.com/a.png\" width=\"10px;x\" height=\"1e9\">"));
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
        long start = System.nanoTime();
        for (String input : inputs) {
            HTMLSanitizer.sanitize(input);
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        // leave room for slow build machines
        assertTrue(millis < 2000, "took " + millis + " ms");
    }
}
