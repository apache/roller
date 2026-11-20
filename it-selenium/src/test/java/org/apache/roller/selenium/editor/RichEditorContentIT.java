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

package org.apache.roller.selenium.editor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Runs the entry editor's rich-editor-content.js in a blank page in Firefox,
 * without a Roller server: which entries open in the HTML textarea instead of
 * the rich editor, and where the textarea fallback inserts media files.
 *
 * <p>The module's verify phase starts Roller in Jetty first. To run just this
 * class without it, from it-selenium:
 * {@code mvn test-compile failsafe:integration-test failsafe:verify -Dit.test=RichEditorContentIT}
 */
public class RichEditorContentIT {

    private static final String[] OPEN_IN_THE_TEXTAREA = {
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
        "<title><img src=x onerror=go()></title>",
        "<form><math><mtext></form><form><mglyph><style></math><img src onerror=go()>",
        "<math><mtext><table><mglyph><style><img src=x onerror=go()></style></mglyph></table></mtext></math>",
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
    };

    // checked as the rich editor loads them, with jQuery inside the entry
    // form; none of these handlers runs without a click
    private static final String[] LOADED_IN_THE_ENTRY_FORM = {
        "<form><div></form><form><p onclick=\"go()\">x</p>",
        "</form><form onclick=\"go()\"><input name=\"attributes\">x</form>",
        "<col><td onclick=\"go()\">x</td>",
        "<caption onclick=\"go()\">x</caption>",
        "<!--<tr>--><td onclick=\"go()\">x</td>",
        "<p>Hello <b>world</b></p>",
        "<table><caption>c</caption><tr><td>1</td><td>2</td></tr></table>",
    };

    private static final String[] STAY_IN_THE_RICH_EDITOR = {
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
        // jQuery parses HTML with a link tag in it outside the page
        "<!-- <link rel=\"stylesheet\" href=\"a.css\"> --><p>text</p>",
    };

    // the rich editor loads entries with jQuery's html()
    private static final String JQUERY = "META-INF/resources/webjars/jquery/3.7.1/jquery.js";

    private static WebDriver driver;
    private static String script;
    private static String jquery;

    @BeforeClass
    public static void startBrowser() throws Exception {
        script = new String(Files.readAllBytes(Paths.get(
                "../app/src/main/webapp/roller-ui/scripts/rich-editor-content.js")),
                StandardCharsets.UTF_8);
        try (InputStream in = RichEditorContentIT.class.getClassLoader().getResourceAsStream(JQUERY)) {
            jquery = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-headless");
        driver = new FirefoxDriver(options);
    }

    @AfterClass
    public static void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** A blank page with the editor's textarea, in a form, and the script loaded. */
    @Before
    public void openPage() {
        driver.get("about:blank");
        js("document.body.innerHTML = '<form id=\"entry\">"
                + "<textarea id=\"edit_content\">Hello world</textarea></form>';"
                + "var s = document.createElement('script');"
                + "s.textContent = arguments[0];"
                + "document.head.appendChild(s);", script);
    }

    private static Object js(String code, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(code, args);
    }

    private static boolean hasActiveHtml(String html) {
        return (Boolean) js("return RollerRichEditor.hasActiveHtml(arguments[0]);", html);
    }

    @Test
    public void entriesWithActiveHtmlOpenInTheTextarea() {
        StringBuilder missed = new StringBuilder();
        for (String html : OPEN_IN_THE_TEXTAREA) {
            if (!hasActiveHtml(html)) {
                missed.append("\n  ").append(html);
            }
        }
        assertEquals("should open in the textarea:", "", missed.toString());
    }

    @Test
    public void otherEntriesStayInTheRichEditor() {
        StringBuilder flagged = new StringBuilder();
        for (String html : STAY_IN_THE_RICH_EDITOR) {
            if (hasActiveHtml(html)) {
                flagged.append("\n  ").append(html);
            }
        }
        assertEquals("should stay in the rich editor:", "", flagged.toString());
    }

    /**
     * The rich editor loads an entry with jQuery's html(), inside the entry
     * form, where a nested form's start tag is ignored, so what the check lets
     * through must not hold an event handler once jQuery has loaded it there.
     * about:blank is in quirks mode and the editor page isn't, so jQuery loads
     * the entry into a standards-mode document here.
     */
    @Test
    public void whatStaysInTheRichEditorHasNoEventHandlersOnceJqueryLoadsIt() {
        js("var s = document.createElement('script');"
                + "s.textContent = arguments[0];"
                + "document.head.appendChild(s);", jquery);
        StringBuilder missed = new StringBuilder();
        for (String html : LOADED_IN_THE_ENTRY_FORM) {
            boolean handlers = (Boolean) js(
                    "var page = document.implementation.createHTMLDocument('');"
                    + "var form = page.body.appendChild(page.createElement('form'));"
                    + "var holder = form.appendChild(page.createElement('div'));"
                    + "jQuery(holder).html(arguments[0]);"
                    + "return Array.from(holder.querySelectorAll('*')).some(function (element) {"
                    + "  return element.getAttributeNames().some(function (name) {"
                    + "    return name.indexOf('on') === 0;"
                    + "  });"
                    + "});", html);
            if (handlers && !hasActiveHtml(html)) {
                missed.append("\n  ").append(html);
            }
        }
        assertEquals("should open in the textarea:", "", missed.toString());
    }

    private static String insert(String text) {
        js("RollerRichEditor.insertAtCursor(document.getElementById('edit_content'), arguments[0]);",
                text);
        return (String) js("return document.getElementById('edit_content').value;");
    }

    private static void trackAndPlaceCursor(int start, int end) {
        js("var t = document.getElementById('edit_content');"
                + "RollerRichEditor.trackCursor(t);"
                + "if (arguments[0] >= 0) { t.focus(); t.setSelectionRange(arguments[0], arguments[1]); }",
                start, end);
    }

    @Test
    public void insertsAtTheEndWhenTheCursorWasNeverPlaced() {
        trackAndPlaceCursor(-1, -1);
        assertEquals("Hello world[media]", insert("[media]"));
    }

    @Test
    public void insertsAtTheStartWhenTheCursorWasPlacedThere() {
        trackAndPlaceCursor(0, 0);
        assertEquals("[media]Hello world", insert("[media]"));
        // the cursor follows the inserted text
        assertEquals("[media][more]Hello world", insert("[more]"));
    }

    @Test
    public void replacesTheSelection() {
        trackAndPlaceCursor(6, 11);
        assertEquals("Hello [media]", insert("[media]"));
    }

    @Test
    public void theScriptLoaded() {
        assertTrue((Boolean) js("return typeof RollerRichEditor.hasActiveHtml === 'function';"));
        assertFalse(hasActiveHtml("<p>x</p>"));
    }
}
