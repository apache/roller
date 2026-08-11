/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.playwright;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Browser lifecycle for the Roller UI tests.
 *
 * <p>Each test gets a fresh browser context, so sessions never leak between
 * tests. A Playwright trace is recorded and kept only when a test fails; open
 * one with {@code npx playwright show-trace <file>}.
 */
@ExtendWith(BaseIT.TraceOnFailure.class)
abstract class BaseIT {

    /** Roller's base URL, always with a trailing slash. */
    protected static String baseUrl;

    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    private boolean failed;

    @BeforeAll
    static void launchBrowser() {
        String url = System.getProperty("roller.baseUrl", "http://localhost:8080/roller/");
        baseUrl = url.endsWith("/") ? url : url + "/";

        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(!Boolean.getBoolean("playwright.headed")));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openContext() {
        context = browser.newContext(new Browser.NewContextOptions()
                .setBaseURL(baseUrl)
                .setViewportSize(1280, 1024));
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true));
        page = context.newPage();
        failed = false;
    }

    @AfterEach
    void closeContext(org.junit.jupiter.api.TestInfo info) {
        Path trace = null;
        if (failed) {
            trace = tracesDir().resolve(info.getTestMethod()
                    .map(java.lang.reflect.Method::getName).orElse("test") + ".zip");
            trace.getParent().toFile().mkdirs();
        }
        context.tracing().stop(new Tracing.StopOptions().setPath(trace));
        context.close();
    }

    /** Marks the trace for keeping when the test method threw. Runs before {@code @AfterEach}. */
    static class TraceOnFailure implements AfterTestExecutionCallback {
        @Override
        public void afterTestExecution(ExtensionContext ctx) {
            if (ctx.getExecutionException().isPresent()) {
                ctx.getTestInstance().ifPresent(instance -> ((BaseIT) instance).failed = true);
            }
        }
    }

    private static Path tracesDir() {
        return Paths.get(System.getProperty("playwright.tracesDir", "target/playwright-traces"));
    }

    /** Navigates to a path relative to Roller's base URL. */
    protected void goTo(String relativePath) {
        page.navigate(relativePath.startsWith("/") ? relativePath.substring(1) : relativePath);
    }

    /**
     * The authentication method this instance is supposed to be running
     * ({@code -Droller.expectedAuth}), or empty when the suite should just
     * adapt to whatever the instance offers.
     */
    protected static String expectedAuth() {
        return System.getProperty("roller.expectedAuth", "");
    }

    // the entry editor, shared by the new-user journey and the OIDC suite
    private static final String ENTRY_TITLE_FIELD = "#entry_bean_title";
    private static final String ENTRY_RICH_TEXT = ".note-editable";
    private static final String ENTRY_TEXTAREA = "#edit_content";
    private static final String ENTRY_POST_BUTTON = "input.btn-success[type='submit']";

    // the rendered weblog
    private static final String RENDERED_ENTRY_TITLE = "p.entryTitle, .entryTitle";
    private static final String RENDERED_ENTRY_CONTENT = "p.entryContent, .entryContent";

    /** Publishes an entry through the editor of the given weblog. */
    protected void publishEntry(String weblogHandle, String title, String text) {
        goTo("roller-ui/authoring/entryAdd.rol?weblog=" + weblogHandle);
        assertThat(page).hasTitle(java.util.regex.Pattern.compile("New Entry"));

        page.locator(ENTRY_TITLE_FIELD).fill(title);

        // Roller's editor is configurable: a rich text editor replaces the
        // textarea with a contenteditable, otherwise the textarea is used as is
        var richText = page.locator(ENTRY_RICH_TEXT);
        if (richText.count() > 0) {
            richText.first().click();
            richText.first().fill(text);
        } else {
            page.locator(ENTRY_TEXTAREA).fill(text);
        }

        page.locator(ENTRY_POST_BUTTON).first().click();
        assertThat(page).hasTitle(java.util.regex.Pattern.compile("Edit Entry"));
    }

    /** Asserts the entry is the latest one rendered on the weblog itself. */
    protected void assertEntryOnBlog(String weblogHandle, String title, String text) {
        goTo(weblogHandle + "/");
        assertThat(page.locator(RENDERED_ENTRY_TITLE).first()).containsText(title);
        assertThat(page.locator(RENDERED_ENTRY_CONTENT).first()).containsText(text);
    }
}
