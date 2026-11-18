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
import org.apache.roller.weblogger.business.plugins.comment.HTMLSubsetPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test utilities.
 */
public class UtilitiesTest  {

    @Test
    public void testHtmlSubsetLinkFormats() {
        assertEquals("<a href=\"https://example.com/\">site</a>",
                htmlSubset("<a href='https://example.com/'>site</a>"));
        assertEquals("<a href=\"http://example.com/\">site</a>",
                htmlSubset("<A HREF=HTTP://example.com/>site</A>"));
        assertEquals("<a href=\"mailto:reader@example.com\">mail</a>",
                htmlSubset("<a href=\"mailto:reader@example.com\">mail</a>"));
        assertEquals("<a href=\"https://example.com/?a=1&amp;b=2\">query</a>",
                htmlSubset("<a href=\"https://example.com/?a=1&b=2\">query</a>"));
        assertEquals("<a href=\"https://example.com/\">site</a>",
                htmlSubset("<a\n href = 'https://example.com/' >site</a>"));
        assertEquals("<a href=\"https://example.com/~reader\">site</a>",
                htmlSubset("<a href=\"https://example.com/&#126;reader\">site</a>"));
    }

    @Test
    public void testHtmlSubsetLinkSubset() {
        assertEquals("<a>file</a>", htmlSubset("<a href=\"ftp://example.com/file\">file</a>"));
        assertEquals("<a>local</a>", htmlSubset("<a href=\"../page\">local</a>"));
        // other attributes are removed, and the link stays
        assertEquals("<a href=\"https://example.com/\">site</a>",
                htmlSubset("<a href=\"https://example.com/\" title=\"site\">site</a>"));
        assertEquals("<a>empty</a>", htmlSubset("<a href=\"\">empty</a>"));
    }

    @Test
    public void testHtmlSubsetRetainsTextAndFormatting() {
        assertNull(Utilities.transformToHTMLSubset(null));
        assertEquals("", htmlSubset(""));
        String text = "<p><b>Bold</b> and <i>italic</i><br>line</p>";
        assertEquals("<p><b>Bold</b> and <i>italic</i><br>line</p>", htmlSubset(text));
        assertEquals("A &lt;example&gt;", htmlSubset("&#65; &lt;example&gt;"));
        assertEquals("<blockquote><pre>code</pre></blockquote><ul><li>one</li></ul><ol><li>two</li></ol>",
                htmlSubset("<blockquote><pre>code</pre></blockquote><ul><li>one</li></ul><ol><li>two</li></ol>"));
        assertEquals("&lt;a href=\"https://example.com/\"&gt;site&lt;/a&gt;",
                htmlSubset("&lt;a href=\"https://example.com/\"&gt;site&lt;/a&gt;"));
        assertEquals("<a href=\"https://example.com/$1\">one</a> <a>two</a>",
                htmlSubset("<a href=\"https://example.com/$1\">one</a> <a href=\"ftp://example.com/\">two</a>"));
    }

    @Test
    public void testHtmlSubsetPluginUsesLinkFormatting() {
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setContentType("text/html");
        HTMLSubsetPlugin plugin = new HTMLSubsetPlugin();
        String text = "<a href=\"ftp://example.com/\">file</a>";
        assertEquals("<a>file</a>", plugin.render(comment, text));
        comment.setContentType("text/plain");
        assertEquals(text, plugin.render(comment, text));
    }

    @Test
    public void testHtmlSubsetRemovesActiveContent() {
        // tags outside the subset are removed; their text stays, encoded
        assertEquals("<b>Hi</b>", htmlSubset("<b>Hi</b><img src=x onerror=alert(1)>"));
        assertEquals("", htmlSubset("<script>alert(1)</script>"));
        assertEquals("", htmlSubset("<style>body{display:none}</style>"));
        assertEquals("<b>x</b>", htmlSubset("<b onmouseover=\"alert(1)\">x</b>"));
        assertEquals("<a>x</a>", htmlSubset("<a href=\"javascript:alert(1)\">x</a>"));
        assertEquals("<a>x</a>", htmlSubset("<a href=\"jav&#x09;ascript:alert(1)\">x</a>"));
        assertEquals("<a>x</a>", htmlSubset("<a href=\"data:text/html,<script>alert(1)</script>\">x</a>"));
        assertEquals("<a href=\"https://example.com/\">x</a>",
                htmlSubset("<svg><a href=\"https://example.com/\" onload=alert(1)>x</a></svg>"));
        assertEquals("<p>x</p>", htmlSubset("<p style=\"background:url(javascript:alert(1))\">x</p>"));
        assertEquals("&lt;!-- --&gt; text", htmlSubset("&lt;!-- --&gt; text"));
        assertEquals("text", htmlSubset("<!-- <script>alert(1)</script> -->text"));
        assertEquals("x", htmlSubset("<iframe src=\"https://example.com/\"></iframe>x"));
        // a quote can't end the href value and add an attribute
        assertEquals("<a href=\"https://example.com/&quot; onclick=&quot;alert(1)\">x</a>",
                htmlSubset("<a href='https://example.com/\" onclick=\"alert(1)'>x</a>"));
    }

    @Test
    public void testSanitizeToHtmlSubsetTakesRawHtml() {
        assertNull(Utilities.sanitizeToHTMLSubset(null));
        assertEquals("<b>Hi</b> &amp; bye", Utilities.sanitizeToHTMLSubset("<b>Hi</b> &amp; bye"));
        assertEquals("<b>Hi</b>", Utilities.sanitizeToHTMLSubset("<b>Hi</b><img src=x onerror=alert(1)>"));
    }

    private String htmlSubset(String text) {
        return Utilities.transformToHTMLSubset(Utilities.escapeHTML(text));
    }

    @Test
    public void testExtractHTML() {
        String test = "<a>keep me</a>";
        String expect = "<a></a>";
        String result = Utilities.extractHTML(test);
        assertEquals(expect, result);
    }

    @Test
    public void testRemoveHTML() {
        String test = "<br><br><p>a <b>bold</b> sentence with a <a href=\"http://example.com\">link</a></p>";
        String expect = "a bold sentence with a link";
        String result = Utilities.removeHTML(test, false);
        assertEquals(expect, result);
    }

    @Test
    public void testRemoveHTMLUnclosedTag() {
        assertEquals("about", Utilities.removeHTML("about <img src=x", false));
        assertEquals("about", Utilities.removeHTML("about <p>", false).trim());
        assertEquals("a", Utilities.removeHTML("a </p", false));
        assertEquals("a", Utilities.removeHTML("a <!-- note", false));
        assertEquals("", Utilities.removeHTML("<img src=x", false));
    }

    @Test
    public void testRemoveHTMLKeepsLessThanText() {
        assertEquals("a < b", Utilities.removeHTML("a < b", false));
        assertEquals("1 <2", Utilities.removeHTML("1 <2", false));
        assertEquals("x <", Utilities.removeHTML("x <", false));
        assertEquals("x<y", Utilities.removeHTML("x<y", false));
        assertEquals("List<String", Utilities.removeHTML("List<String", false));
        assertEquals("C<C++ fans", Utilities.removeHTML("C<C++ fans", false));
        assertEquals("a<b and b<c", Utilities.removeHTML("a<b and b<c", false));
    }

    @Test
    public void testRemoveHTMLDefaultAddsSpaceOnlyForTags() {
        assertEquals("about", Utilities.removeHTML("about <img src=x"));
        assertEquals("about", Utilities.removeHTML("about<img/src=x"));
        assertEquals("x<y", Utilities.removeHTML("x<y"));
        assertEquals("C<C++ fans", Utilities.removeHTML("C<C++ fans"));
        assertEquals("a < b", Utilities.removeHTML("a < b"));
        assertEquals("a b", Utilities.removeHTML("a<br>b"));
    }

    @Test
    public void testRemoveHTMLUnclosedTagAfterLessThanText() {
        assertEquals("a < b", Utilities.removeHTML("a < b <img src=x", false));
        assertEquals("a < b < c", Utilities.removeHTML("a < b < c", false));
        assertEquals("1 <2 <3", Utilities.removeHTML("1 <2 <3", false));
    }

    @Test
    public void testTruncateNicely1() {
        String test = "blah blah blah blah blah";
        String expect = "blah blah blah";
        String result = Utilities.truncateNicely(test, 11, 15, "");
        assertEquals(expect, result);
    }

    @Test
    public void testTruncateNicely2() {
        String test = "<p><b>blah1 blah2</b> <i>blah3 blah4 blah5</i></p>";
        String expect = "<p><b>blah1 blah2</b> <i>blah3</i></p>";
        String result = Utilities.truncateNicely(test, 15, 20, "");
        //System.out.println(result);
        assertEquals(expect, result);
    }
    
    @Test
    public void testAddNoFollow() {
        assertEquals("<p>this some text with a <a href=\"http://example.com\" rel=\"nofollow\">link</a></p>",
                Utilities.addNofollow("<p>this some text with a <a href=\"http://example.com\">link</a>"));
        assertEquals("<p>this some text with a <a href=\"http://example.com\" rel=\"nofollow\">link</a></p>",
                Utilities.addNofollow("<p>this some text with a <A href=\"http://example.com\">link</a>"));

        // links that already have it are left alone
        assertEquals("<a href=\"http://example.com\" rel=\"nofollow\">one</a>"
                + " <a href=\"/two\" rel=\"nofollow\">two</a>",
                Utilities.addNofollow("<a href=\"http://example.com\" rel=\"nofollow\">one</a> <a href=\"/two\">two</a>"));
        assertEquals("<a href=\"/x\" rel=\"Nofollow external\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" rel=\"Nofollow external\">x</a>"));

        // an existing rel gets nofollow added to its value
        assertEquals("<a href=\"/x\" rel=\"external nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" rel=\"external\">x</a>"));
        assertEquals("<a href=\"/x\" rel=\"noopener nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" REL='noopener'>x</a>"));
        assertEquals("<a href=\"/x\" rel=\"external nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" rel=external>x</a>"));
        assertEquals("<a href=\"/x\" rel=\"nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" rel>x</a>"));
        // browsers use the first of two rel attributes, and so does jsoup
        assertEquals("<a href=\"/x\" rel=\"noopener nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" rel=\"noopener\" rel=\"external\">x</a>"));

        // "rel=" inside another attribute's value isn't the link's rel
        assertEquals("<a href=\"/x\" title=\"foo rel=external\" rel=\"noopener nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" title='foo rel=external' rel=\"noopener\">x</a>"));
        assertEquals("<a href=\"/x\" title=\"a rel='b'\" rel=\"nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" title=\"a rel='b'\">x</a>"));
        assertEquals("<a href=\"/x\" data-rel=\"external\" rel=\"nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" data-rel=\"external\">x</a>"));
        assertEquals("<a href=\"/x\" rel=\"external nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\"rel=\"external\">x</a>"));

        // a ">" inside a quoted attribute value doesn't end the tag
        assertEquals("<a href=\"/x\" title=\"a &gt; b\" rel=\"nofollow\">x</a>",
                Utilities.addNofollow("<a href=\"/x\" title=\"a > b\">x</a>"));
        assertEquals("<a href=\"/x?a&gt;b\" rel=\"nofollow\">x</a>",
                Utilities.addNofollow("<a href='/x?a>b'>x</a>"));

        // href doesn't have to come first, and links without one are left alone
        assertEquals("<a title=\"x\" href=\"/y\" rel=\"nofollow\">y</a>",
                Utilities.addNofollow("<a title=x href=/y>y</a>"));
        assertEquals("<a name=\"top\">t</a>", Utilities.addNofollow("<a name=\"top\">t</a>"));

        // escaped text stays escaped
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt; <a href=\"/x\" rel=\"nofollow\">x</a> &amp; &nbsp;",
                Utilities.addNofollow("&lt;script&gt;alert(1)&lt;/script&gt; <a href=\"/x\">x</a> &amp; &nbsp;"));

        // a tag that never ends is dropped, as browsers do
        assertEquals("", Utilities.addNofollow("<a href=\"x\""));
        assertEquals("no links", Utilities.addNofollow("no links"));
        assertNull(Utilities.addNofollow(null));
    }

    @Test
    public void testUnclosedLinksTakeLinearTime() {
        String escaped = "&lt;a href=".repeat(50000);
        String raw = "<a href=".repeat(50000);

        long start = System.nanoTime();
        // a tag that never ends is dropped
        assertEquals("", Utilities.transformToHTMLSubset(escaped));
        assertEquals("", Utilities.transformToHTMLSubset(Utilities.escapeHTML(raw)));
        assertEquals("", Utilities.addNofollow(raw));
        long millis = (System.nanoTime() - start) / 1_000_000;
        // a quadratic scan takes far longer than this; leave room for slow
        // build machines
        assertTrue(millis < 2000, "took " + millis + " ms");
    }

    @Test
    public void testPlainTextExcerpt() {
        // markup is removed and the text is HTML-encoded once
        assertEquals("Older", Utilities.plainTextExcerpt("<em>Older</em>", 15, 20, "..."));
        assertEquals("A &amp; B bold", Utilities.plainTextExcerpt("A &amp; B <b>bold</b>", 15, 20, "..."));
        assertEquals("&lt;i&gt; is italic",
                Utilities.plainTextExcerpt("&lt;i&gt; is italic", 15, 20, "..."));
        assertEquals("&quot;quoted&quot;", Utilities.plainTextExcerpt("\"quoted\"", 15, 20, "..."));

        // long text is cut at a word boundary, then encoded
        assertEquals("The quick brown fox...",
                Utilities.plainTextExcerpt("The quick brown fox jumps over", 15, 20, "..."));
        assertEquals("&lt;em&gt;Emphasis&lt;/em&gt;",
                Utilities.plainTextExcerpt("&lt;em&gt;Emphasis&lt;/em&gt;", 20, 20, "..."));
        assertEquals("", Utilities.plainTextExcerpt(null, 15, 20, "..."));
    }
}
