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

package org.apache.roller.playwright;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the entry editor's rich-editor-content.js in a blank page, without a
 * Roller server: which entries open in the HTML textarea instead of the rich
 * editor, and where the textarea fallback inserts media files.
 *
 * <p>Run just this class with {@code mvn verify -Dit.test=RichEditorContentIT}.
 */
@DisplayName("Rich editor content check")
class RichEditorContentIT {

    private static final Path SCRIPT = Paths.get(
            "../app/src/main/webapp/roller-ui/scripts/rich-editor-content.js");

    // the rich editor loads entries with jQuery's html()
    private static final String JQUERY = "META-INF/resources/webjars/jquery/4.0.0/jquery.js";

    private static Playwright playwright;
    private static Browser browser;
    private static String script;
    private static String jquery;

    private Page page;

    @BeforeAll
    static void launchBrowser() throws IOException {
        script = new String(Files.readAllBytes(SCRIPT), StandardCharsets.UTF_8);
        try (InputStream in = RichEditorContentIT.class.getClassLoader().getResourceAsStream(JQUERY)) {
            jquery = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(!Boolean.getBoolean("playwright.headed")));
    }

    @AfterAll
    static void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openPage() {
        page = browser.newPage();
        // the entry editor's textarea, and so the rich editor, is in a form
        page.setContent("<!DOCTYPE html><html><body><form id=\"entry\">"
                + "<textarea id=\"edit_content\">Hello world</textarea></form></body></html>");
        page.addScriptTag(new Page.AddScriptTagOptions().setContent(script));
    }

    @AfterEach
    void closePage() {
        page.close();
    }

    private boolean hasActiveHtml(String html) {
        return (Boolean) page.evaluate("html => RollerRichEditor.hasActiveHtml(html)", html);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "<script>document.title = 'x'</script>",
        "<p>text</p><img src=\"x.png\" onerror=\"document.title = 'x'\">",
        "<p ONCLICK=\"go()\">text</p>",
        "<a href=\"javascript:go()\">link</a>",
        "<a href=\" JaVa&#x09;Script:go()\">link</a>",
        "<a href=\"&#x01;javascript:go()\">link</a>",
        "<a href=\"vbscript:go\">link</a>",
        "<img src=\"a.png\" srcset=\"a.png 1x, javascript:go() 2x\">",
        "<svg><a xlink:href=\"javascript:go()\"><text>x</text></a></svg>",
        "<form><button formaction=\"javascript:go()\">x</button></form>",
        "<form action=\"javascript:go()\"></form>",
        "<object data=\"x.swf\"></object>",
        "<video poster=\"javascript:go()\"></video>",
        "<table background=\"javascript:go()\"><tr><td>x</td></tr></table>",
        "<blockquote cite=\"javascript:go()\">x</blockquote>",
        "<iframe src=\"https://www.youtube.com/embed/x\"></iframe>",
        "<style>body { display: none }</style>",
        "<td onclick=\"go()\">cell outside a table</td>",
        "<caption onmouseover=\"go()\">x</caption>",
        "<template><img src=x onerror=\"go()\"></template>",
        "<noscript><p title=\"</noscript><img src=x onerror=go()>\"></noscript>",
        "<textarea><img src=x onerror=go()></textarea>",
        "<xmp><img src=x onerror=go()></xmp>",
        "<noembed><img src=x onerror=go()></noembed>",
        "<form><math><mtext></form><form><mglyph><style></math><img src onerror=go()>",
        "<math><mtext><table><mglyph><style><img src=x onerror=go()></style></mglyph></table></mtext></math>",
        "<title><img src=x onerror=go()></title>",
        // markup that parses differently once serialized and parsed again,
        // as the rich editor does for undo and code view
        "<form><div></form><form><p>x</p>",
        "<form><math><mtext></form><form><mglyph><b>x</b></mglyph></form>",
        // inside the entry form a nested form's start tag is ignored, so the
        // image is an element there
        "<math><mtext><p><form></form><mglyph><![CDATA[><img src onerror=go()>]]>",
        // a control named "attributes" hides its form's attributes
        "</form><form style=\"animation-name:spin;animation-duration:1ms\" onanimationstart=\"go()\">"
            + "<input name=\"attributes\">x</form>",
        // the rich editor drops a form's wrapper on save
        "<form action=\"https://example.com/subscribe\"><input name=\"email\"></form>",
        // controls join the entry form and are submitted with it
        "<input type=\"hidden\" name=\"bean.status\" value=\"PUBLISHED\">",
        "<button formaction=\"https://example.com/x\">Save</button>",
        "<button>Save</button>",
        "<select name=\"bean.categoryId\"><option>x</option></select>",
        "<p formaction=\"https://example.com/x\">text</p>",
        // an image's name replaces that property on its document
        "<img name=body><img src onerror=go()>",
        "<form name=body></form><img src onerror=go()>",
        "<img name=documentElement><img src onerror=go()>",
        "<img name=querySelectorAll><img src onerror=go()>",
        "<img name=body><table><tr><td onclick=\"go()\">x</td></tr></table>",
        // jQuery parses HTML that starts with a table part inside a table
        "<col><td onclick=\"go()\">x</td>",
        // leaving code view removes <meta> tags, which here ends the comment
        "<!-- -<meta>-> <input> -->",
        "<!-- <iframe src=\"https://example.com/\"></iframe> -->",
        // the rich editor shows these as HTML: a link's href in its link
        // popover, and a URL-like word when the author types a space after it
        "<p><a href=\"&lt;b&gt;bold&lt;/b&gt;\">text</a></p>",
        "<p>See https://example.com/&lt;b&gt;bold&lt;/b&gt;</p>",
        "<p>See www.example.com/&lt;b&gt;bold&lt;/b&gt;</p>",
        // Bootstrap acts on clicks on these anywhere in the page
        "<p><a data-toggle=\"modal\" data-target=\"#x\" href=\"/x\">text</a></p>",
        "<p><a data-bs-toggle=\"collapse\" href=\"#x\">text</a></p>",
        "<meta name=\"referrer\" content=\"no-referrer\">",
        "<meta itemprop=\"isbn\" http-equiv=\"refresh\" content=\"0\">",
    })
    void opensInTheTextarea(String html) {
        assertTrue(hasActiveHtml(html), html);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "<p>Hello <b>world</b> and <i>everyone</i></p>",
        "<a href=\"https://example.com/\" title=\"JavaScript: The Good Parts\">book</a>",
        "<img src=\"/blog/mediaresource/x.png\" alt=\"javascript: is a URL scheme\">",
        "<p>Type javascript: into the address bar</p>",
        "<p data-note=\"vbscript: old\">text</p>",
        "<a href=\"/wiki/javascript:void\">article</a>",
        "<a href=\"https://example.com/?q=javascript:\">search</a>",
        "<img src=\"a.png\" srcset=\"a.png 1x, b.png 2x\" alt=\"\">",
        "<table><caption>c</caption><tr><td>1</td><td>2</td></tr></table>",
        "<ul><li>one</li><li>two</li></ul><blockquote cite=\"https://example.com/\">q</blockquote>",
        "<pre><code>if (a &lt; b) { run(); }</code></pre>",
        "<pre>\n\nfirst line after a blank one</pre>",
        "<p>One</p>\n<p>Two</p>\n",
        "<img name=\"body\" src=\"a.png\" alt=\"\"><p>text</p>",
        "<img name=\"querySelector\" src=\"a.png\" alt=\"\">",
        "<img name=\"querySelectorAll\" src=\"a.png\" alt=\"\">",
        "<img name=\"createElement\" src=\"a.png\" alt=\"\">",
        "<!-- more --><p>text</p>",
        "<!-- <b>bold</b> and <meta> --><p>text</p>",
        "<table><tr><td>1</td></tr></table><p>after the table</p>",
        "<p>Use &lt;b&gt; for bold, as https://example.com/ shows</p>",
        // an SVG tooltip, and microdata
        "<svg width=\"10\" height=\"10\"><title>Chart</title><rect width=\"10\" height=\"10\"></rect></svg>",
        "<div itemscope itemtype=\"https://schema.org/Book\"><meta itemprop=\"isbn\" content=\"123\"></div>",
    })
    void staysInTheRichEditor(String html) {
        assertFalse(hasActiveHtml(html), html);
    }

    /**
     * The rich editor loads an entry with jQuery's html(), inside the entry
     * form, where a nested form's start tag is ignored, so what the check lets
     * through must not hold an event handler once jQuery has loaded it there.
     * None of these handlers runs without a click.
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "<form><div></form><form><p onclick=\"go()\">x</p>",
        "</form><form onclick=\"go()\"><input name=\"attributes\">x</form>",
        "<col><td onclick=\"go()\">x</td>",
        "<caption onclick=\"go()\">x</caption>",
        "<!--<tr>--><td onclick=\"go()\">x</td>",
        "<p>Hello <b>world</b></p>",
        "<table><caption>c</caption><tr><td>1</td><td>2</td></tr></table>",
    })
    void whatStaysInTheRichEditorHasNoEventHandlersOnceJqueryLoadsIt(String html) {
        page.addScriptTag(new Page.AddScriptTagOptions().setContent(jquery));
        boolean handlers = (Boolean) page.evaluate("html => {"
                + "  const editor = document.getElementById('entry').appendChild(document.createElement('div'));"
                + "  jQuery(editor).html(html);"
                + "  return Array.from(editor.querySelectorAll('*')).some(element =>"
                + "      element.getAttributeNames().some(name => name.startsWith('on')));"
                + "}", html);
        assertTrue(hasActiveHtml(html) || !handlers, html);
    }

    private String insert(String text) {
        page.evaluate("text => RollerRichEditor.insertAtCursor("
                + "document.getElementById('edit_content'), text)", text);
        return (String) page.evaluate("() => document.getElementById('edit_content').value");
    }

    private void track() {
        page.evaluate("() => RollerRichEditor.trackCursor(document.getElementById('edit_content'))");
    }

    @Test
    void insertsAtTheEndWhenTheCursorWasNeverPlaced() {
        track();
        assertEquals("Hello world[media]", insert("[media]"));
    }

    @Test
    void insertsAtTheStartWhenTheCursorWasPlacedThere() {
        track();
        page.focus("#edit_content");
        page.evaluate("() => document.getElementById('edit_content').setSelectionRange(0, 0)");
        assertEquals("[media]Hello world", insert("[media]"));
        // the cursor follows the inserted text
        assertEquals("[media][more]Hello world", insert("[more]"));
    }

    @Test
    void replacesTheSelection() {
        track();
        page.focus("#edit_content");
        page.evaluate("() => document.getElementById('edit_content').setSelectionRange(6, 11)");
        assertEquals("Hello [media]", insert("[media]"));
    }
}
