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
        // markup that parses differently once serialized and parsed again,
        // as the rich editor does for undo and code view
        "<form><div></form><form><p>x</p>",
        "<form><math><mtext></form><form><mglyph><b>x</b></mglyph></form>",
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
    };

    private static WebDriver driver;
    private static String script;

    @BeforeClass
    public static void startBrowser() throws Exception {
        script = new String(Files.readAllBytes(Paths.get(
                "../app/src/main/webapp/roller-ui/scripts/rich-editor-content.js")),
                StandardCharsets.UTF_8);
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

    /** A blank page with the editor's textarea and the script loaded. */
    @Before
    public void openPage() {
        driver.get("about:blank");
        js("document.body.innerHTML = '<textarea id=\"edit_content\">Hello world</textarea>';"
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
