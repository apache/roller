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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // the box Roller logs at startup until an operator completes initial setup
    private static final Pattern SETUP_TOKEN = Pattern.compile(
            "Enter this one-time setup token[^\\r\\n]*\\R\\| ([A-Za-z0-9_-]{43}) +\\|");

    /**
     * Cookies of the session that redeemed the one-time setup token, or null
     * when the instance had already been set up. Only that session may
     * register the first user.
     */
    protected static String setupSession;
    private static boolean setupChecked;

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
        completeInitialSetup();
    }

    /**
     * Unlocks a fresh install the way an operator does: reads the one-time
     * setup token from the server log, redeems it, and creates the database
     * tables if the instance has none yet. Runs once per test run.
     */
    private static void completeInitialSetup() {
        if (setupChecked) {
            return;
        }
        setupChecked = true;

        BrowserContext setup = browser.newContext(new Browser.NewContextOptions().setBaseURL(baseUrl));
        try {
            Page p = setup.newPage();
            p.navigate("roller-ui/bootstrap-token.rol");
            if (p.locator("#setup-token").count() == 0) {
                return;
            }
            p.locator("#setup-token").fill(readSetupToken());
            p.locator("#setup-token-submit").click();
            p.waitForLoadState();

            // a database Roller has not installed yet: create the tables, then
            // follow the installer's link to start the application
            var createTables = p.locator("form[action*='install!create'] input[type='submit']");
            if (createTables.count() > 0) {
                createTables.click();
                p.locator("a[href*='install!bootstrap']").click();
                p.waitForLoadState();
            }

            // with no form login there is no one to register, so the operator
            // completes setup by signing in as the provider's administrator
            p.navigate(LOGIN_PAGE);
            if (p.locator(FORM_LOGIN_USERNAME).count() == 0 && p.locator(PROVIDER_BUTTON).count() > 0) {
                signInWithProvider(p, OIDC_ADMIN, OIDC_ADMIN);
                p.waitForURL(baseUrl + "roller-ui/menu.rol");
            }
            setupSession = setup.storageState();
        } finally {
            setup.close();
        }
    }

    /**
     * The newest setup token in the server log ({@code -Droller.test.logFile}).
     * Logging is asynchronous, so the startup message may take a moment to land.
     */
    private static String readSetupToken() {
        Path log = Paths.get(System.getProperty("roller.test.logFile", "../logs/roller.log"));
        for (int attempt = 0; attempt < 20; attempt++) {
            try {
                if (Files.exists(log)) {
                    Matcher matcher = SETUP_TOKEN.matcher(Files.readString(log));
                    String token = null;
                    while (matcher.find()) {
                        token = matcher.group(1);
                    }
                    if (token != null) {
                        return token;
                    }
                }
                Thread.sleep(500);
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot read Roller's server log: " + log, ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new IllegalStateException("No initial setup token found in " + log.toAbsolutePath()
                + "; point -Droller.test.logFile at the running instance's roller.log");
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
                .setViewportSize(1280, 1024)
                .setStorageState(usesSetupSession() ? setupSession : null));
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

    protected static final String LOGIN_PAGE = "roller-ui/login.rol";
    protected static final String FORM_LOGIN_USERNAME = "input[name='j_username']";

    // the compose stack's Keycloak realm seeds this administrator (password = username)
    protected static final String OIDC_ADMIN = "admin";
    protected static final String PROVIDER_BUTTON = "a[href*='/oauth2/authorization/']";
    private static final Pattern PROVIDER_USERNAME_LABEL = Pattern.compile("username", Pattern.CASE_INSENSITIVE);
    private static final String PROVIDER_PASSWORD = "input[type='password']";
    private static final String PROVIDER_SUBMIT = "input[type='submit'], button[type='submit']";

    /** Clicks through Roller's provider button and the provider's own login form. */
    protected static void signInWithProvider(Page p, String username, String password) {
        p.navigate(LOGIN_PAGE);
        p.locator(PROVIDER_BUTTON).first().click();

        p.getByLabel(PROVIDER_USERNAME_LABEL).first().fill(username);
        p.locator(PROVIDER_PASSWORD).first().fill(password);
        p.locator(PROVIDER_SUBMIT).first().click();
    }

    /** Whether this suite registers the first user, which needs the setup session. */
    protected boolean usesSetupSession() {
        return false;
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

    // authoring dialogs (Bootstrap modals)
    private static final String CATEGORY_ADD_LINK = "a[onclick*='showCategoryAddModal']";
    private static final String CATEGORY_DIALOG = "#category-edit-modal";
    private static final String CATEGORY_NAME_FIELD = "#categoryEditForm_bean_name";
    private static final String CATEGORY_SAVE_BUTTON = CATEGORY_DIALOG + " .modal-footer .btn-primary";
    private static final String CATEGORY_TABLE = "table.rollertable";
    private static final String ENTRY_DELETE_LINK = ".entry-delete-link";
    private static final String ENTRY_DELETE_DIALOG = "#delete-entry-modal";
    private static final String ENTRY_DELETE_NO = ENTRY_DELETE_DIALOG + " .modal-footer button[data-bs-dismiss='modal']";
    private static final String MEDIA_INSERT_LINK = "a[onclick*='onClickMediaFileInsert']";
    private static final String MEDIA_INSERT_DIALOG = "#mediafile_edit_lightbox";
    private static final String MEDIA_CHOOSER_FRAME = "#mediaFileEditor";
    private static final String MEDIA_CHOOSER_FILE = ".mediafile-select-target";

    /** Adds a category through the Add Category dialog on the categories page. */
    protected void addCategoryThroughDialog(String weblogHandle, String name) {
        goTo("roller-ui/authoring/categories.rol?weblog=" + weblogHandle);
        page.locator(CATEGORY_ADD_LINK).first().click();
        assertThat(page.locator(CATEGORY_DIALOG)).isVisible();

        page.locator(CATEGORY_NAME_FIELD).fill(name);
        // the dialog validates on keyup, which fill() does not fire
        page.locator(CATEGORY_NAME_FIELD).press("End");
        page.locator(CATEGORY_SAVE_BUTTON).click();

        // a successful save closes the dialog and reloads the list
        assertThat(page.locator(CATEGORY_DIALOG)).isHidden();
        assertThat(page.locator(CATEGORY_TABLE)).containsText(name);
    }

    /** Opens the delete dialog for an entry on the entries page and cancels it. */
    protected void cancelEntryDeleteDialog(String weblogHandle, String title) {
        goTo("roller-ui/authoring/entries.rol?weblog=" + weblogHandle);
        page.locator(ENTRY_DELETE_LINK).first().click();
        assertThat(page.locator(ENTRY_DELETE_DIALOG)).isVisible();

        page.locator(ENTRY_DELETE_NO).click();
        assertThat(page.locator(ENTRY_DELETE_DIALOG)).isHidden();
        assertThat(page.getByText(title).first()).isVisible();
    }

    /**
     * Inserts the weblog's first media file into a new entry through the
     * Insert Media File dialog, and checks that the editor received it.
     */
    protected void insertMediaFileThroughDialog(String weblogHandle) {
        goTo("roller-ui/authoring/entryAdd.rol?weblog=" + weblogHandle);
        // Bootstrap ignores hide() while a modal is still fading in, and the
        // chooser can load faster than that, so wait for the shown event
        page.evaluate("sel => document.querySelector(sel).addEventListener('shown.bs.modal',"
                + " () => window.rollerDialogShown = true, {once: true})", MEDIA_INSERT_DIALOG);
        page.locator(MEDIA_INSERT_LINK).click();
        page.waitForFunction("() => window.rollerDialogShown === true");
        assertThat(page.locator(MEDIA_INSERT_DIALOG)).isVisible();

        page.frameLocator(MEDIA_CHOOSER_FRAME).locator(MEDIA_CHOOSER_FILE).first().click();
        assertThat(page.locator(MEDIA_INSERT_DIALOG)).isHidden();

        var richText = page.locator(ENTRY_RICH_TEXT);
        if (richText.count() > 0) {
            assertThat(richText.first().locator("img")).hasCount(1);
        } else {
            assertThat(page.locator(ENTRY_TEXTAREA)).hasValue(Pattern.compile("<img src=\"[^\"]+\""));
        }
    }

    /**
     * Expands the entry editor's advanced settings and checks the publishing
     * time's hour, minute and second selects sit on one line rather than as
     * stacked form rows.
     */
    protected void assertPublishingTimeOnOneLine() {
        page.locator("a[href='#collapseAdvanced']").click();
        assertThat(page.locator("#collapseAdvanced")).isVisible();
        double hours = page.locator("select[name='bean.hours']").boundingBox().y;
        double minutes = page.locator("select[name='bean.minutes']").boundingBox().y;
        double seconds = page.locator("select[name='bean.seconds']").boundingBox().y;
        org.junit.jupiter.api.Assertions.assertTrue(
                Math.abs(hours - minutes) < 2 && Math.abs(minutes - seconds) < 2,
                "publishing time selects should share a line, but sit at y=" + hours + ", " + minutes + ", " + seconds);
    }

    /** Asserts the entry is the latest one rendered on the weblog itself. */
    protected void assertEntryOnBlog(String weblogHandle, String title, String text) {
        goTo(weblogHandle + "/");
        assertThat(page.locator(RENDERED_ENTRY_TITLE).first()).containsText(title);
        assertThat(page.locator(RENDERED_ENTRY_CONTENT).first()).containsText(text);
    }
}
