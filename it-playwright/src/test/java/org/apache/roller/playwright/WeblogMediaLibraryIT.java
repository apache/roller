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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.SelectOption;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The media library's main uses: upload several files at once, see which ones
 * failed and retry them, organize files into folders, find them by name, page
 * through a large folder, switch to the list view, edit a file on its details
 * page and come back to the same place, delete files, and insert a file into an
 * entry with its description as the alt text.
 *
 * <p>Runs after {@link NewUserJourneyIT} (alphabetical run order) and reuses the
 * account and weblog it created, where it also turned file uploads on. Each
 * test works in folders of its own, so the tests do not depend on each other.
 * Skips on an instance without form login, or when the journey did not run.
 */
@DisplayName("Media library")
class WeblogMediaLibraryIT extends BaseIT {

    private static final String USERNAME = "bsmith";
    private static final String PASSWORD = "roller123";
    private static final String BLOG_HANDLE = "bobsblog";

    private static final String LOGIN_USERNAME = "input[name='j_username']";
    private static final String LOGIN_PASSWORD = "input[name='j_password']";
    private static final String LOGIN_SUBMIT = "#loginForm input[type='submit'], #loginForm button[type='submit']";

    private static final String LIBRARY_PAGE = "roller-ui/authoring/mediaFileView.rol?weblog=" + BLOG_HANDLE;
    private static final String UPLOAD_PAGE = "roller-ui/authoring/mediaFileAdd.rol?weblog=" + BLOG_HANDLE;

    // library page
    private static final String NEW_FOLDER_BUTTON = "[data-bs-target='#newFolderForm']";
    private static final String NEW_FOLDER_NAME = "#newDirectoryName";
    private static final String NEW_FOLDER_CREATE = "#newDirectoryButton";
    private static final String FOLDER_HEADING = ".media-heading";
    private static final String CARD = ".media-card";
    private static final String CARD_NAME = ".media-card-name";
    private static final String CARD_CHECKBOX = ".media-select";
    private static final String SELECTED_COUNT = "#selectedCount";
    private static final String SELECTION_BAR = "#selectionBar";
    private static final String MOVE_MENU = "#moveTargetMenu";
    private static final String MOVE_BUTTON = "#moveButton";
    private static final String DELETE_BUTTON = "#deleteButton";
    private static final String SEARCH_NAME = "#beanName";
    private static final String SEARCH_SCOPE = "#beanScope";
    private static final String SEARCH_SUBMIT = "#mediaFileSearchForm button[type='submit']";
    private static final String RESULT_SUMMARY = ".media-result-summary";
    private static final String SORT = "#sortByMenu";
    private static final String GRID = "#mediaGrid";
    private static final String LIST_VIEW_BUTTON = "[data-media-view='list']";
    private static final String GRID_VIEW_BUTTON = "[data-media-view='grid']";
    private static final String PAGE_NUMBER = ".roller-pager-page";
    private static final String MESSAGES = "#messages";

    // upload page
    private static final String FILE_INPUT = "#fileInput";
    private static final String UPLOAD_ITEM = ".media-upload-item";
    private static final String UPLOAD_BUTTON = "#uploadButton";
    private static final String UPLOAD_BUTTON_LABEL = "#uploadButtonLabel";
    private static final String UPLOAD_STATUS = "#uploadStatus";
    private static final String CONTINUE_LINK = "#continueLink";
    private static final String SHARED_DETAILS = ".media-upload-shared summary";
    private static final String SHARED_DESCRIPTION = "#uploadDescription";

    // details page
    private static final String DETAILS_NAME = ".media-details-name";
    private static final String DETAILS_URL = "#mediaFileURL";
    private static final String DETAILS_DESCRIPTION = "textarea[name='bean.description']";
    private static final String DETAILS_SAVE = "#mediaFileEditForm input[type='submit']";
    private static final String DETAILS_BACK = ".media-back a";
    private static final String DETAILS_DELETE = "#deleteMediaFileForm button[type='submit']";

    @BeforeEach
    void signIn() {
        goTo(LOGIN_PAGE);
        boolean formLogin = page.locator(LOGIN_USERNAME).count() > 0;
        if ("oidc".equals(expectedAuth()) || expectedAuth().isEmpty()) {
            assumeTrue(formLogin, "no form login: these tests use the journey's database account");
        } else {
            Assertions.assertTrue(formLogin,
                    "form login expected for auth method " + expectedAuth() + " but the login page has none");
        }
        page.locator(LOGIN_USERNAME).fill(USERNAME);
        page.locator(LOGIN_PASSWORD).fill(PASSWORD);
        page.locator(LOGIN_SUBMIT).first().click();
        page.waitForLoadState();

        goTo(LIBRARY_PAGE);
        assumeTrue(page.title().contains("Media Files"),
                "the new-user journey did not create " + BLOG_HANDLE + " for this run");
    }

    @Test
    @DisplayName("uploads several files in one go and lists them on the success page and in the folder")
    void uploadsSeveralFiles() {
        String folder = createFolder("multi");
        upload(folder, png("alpha.png", Color.RED), png("beta.png", Color.GREEN), png("gamma.png", Color.BLUE));

        assertThat(page.locator(UPLOAD_ITEM)).hasCount(3);
        page.locator(UPLOAD_BUTTON).click();

        // with no failures the page moves on to the success page by itself
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));
        assertThat(page.locator("img.mediaFileImage")).hasCount(3);

        openFolder(folder);
        assertThat(page.locator(CARD)).hasCount(3);
        assertThat(page.locator(FOLDER_HEADING)).containsText("3 file(s)");
    }

    @Test
    @DisplayName("keeps saved files when another fails, and retries only the failed file")
    void reportsFailedFilesAndRetriesOnlyThem() {
        String folder = createFolder("partial");
        // the server refuses names containing ".."
        upload(folder, png("good.png", Color.ORANGE), png("bad..name.png", Color.BLACK));
        page.locator(UPLOAD_BUTTON).click();

        Locator good = page.locator(UPLOAD_ITEM).filter(new Locator.FilterOptions().setHasText("good.png"));
        Locator bad = page.locator(UPLOAD_ITEM).filter(new Locator.FilterOptions().setHasText("bad..name.png"));
        assertThat(good).hasAttribute("data-state", "uploaded");
        assertThat(bad).hasAttribute("data-state", "failed");
        assertThat(bad).containsText("invalid");
        assertThat(page.locator(UPLOAD_STATUS)).containsText("1 uploaded, 1 failed");
        assertThat(page.locator(UPLOAD_BUTTON_LABEL)).hasText("Retry failed files");
        assertThat(page.locator(CONTINUE_LINK)).isVisible();

        // a retry sends only the failed file, so the saved one is never stored twice
        page.locator(UPLOAD_BUTTON).click();
        assertThat(bad).hasAttribute("data-state", "failed");
        assertThat(good).hasAttribute("data-state", "uploaded");

        page.locator(CONTINUE_LINK).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));
        assertThat(page.locator("img.mediaFileImage")).hasCount(1);

        openFolder(folder);
        assertThat(page.locator(CARD_NAME)).hasText(new String[] {"good.png"});
    }

    @Test
    @DisplayName("moves selected files to another folder")
    void movesFilesBetweenFolders() {
        String source = createFolder("from");
        String target = createFolder("to");
        upload(source, png("one.png", Color.RED), png("two.png", Color.GREEN));
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));

        openFolder(source);
        assertThat(page.locator(SELECTION_BAR)).isHidden();
        card("one.png").locator(CARD_CHECKBOX).check();
        assertThat(page.locator(SELECTION_BAR)).isVisible();
        assertThat(page.locator(SELECTED_COUNT)).hasText("1 selected");
        page.locator(MOVE_BUTTON).click();
        page.locator(MOVE_MENU).getByText(target, new Locator.GetByTextOptions().setExact(true)).click();

        assertThat(page.locator(MESSAGES)).containsText("moved");
        assertThat(page.locator(FOLDER_HEADING)).containsText(source);
        assertThat(page.locator(CARD_NAME)).hasText(new String[] {"two.png"});

        openFolder(target);
        assertThat(page.locator(CARD_NAME)).hasText(new String[] {"one.png"});
    }

    @Test
    @DisplayName("finds files by the start of their name, in any case, in one folder or all")
    void searchesByName() {
        String first = createFolder("searchA");
        String second = createFolder("searchB");
        String stem = "find" + System.nanoTime();
        upload(first, png(stem + "-here.png", Color.RED), png("other.png", Color.GREEN));
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));
        upload(second, png(stem.toUpperCase() + "-there.png", Color.BLUE));
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));

        openFolder(first);
        page.locator(SEARCH_NAME).fill(stem);
        page.locator(SEARCH_SUBMIT).click();
        assertThat(page.locator(RESULT_SUMMARY)).containsText("1 matching file(s)");
        assertThat(page.locator(CARD_NAME)).hasText(new String[] {stem + "-here.png"});

        page.locator(SEARCH_SCOPE).selectOption("all");
        page.locator(SEARCH_SUBMIT).click();
        assertThat(page.locator(RESULT_SUMMARY)).containsText("2 matching file(s)");
        assertThat(page.locator(RESULT_SUMMARY)).containsText("All folders");

        page.getByText("Clear search").first().click();
        assertThat(page.locator(FOLDER_HEADING)).containsText(first);
        assertThat(page.locator(CARD)).hasCount(2);

        // a type chip searches at once
        page.locator("label[for='type-mediaFileView.others']").click();
        assertThat(page.locator(RESULT_SUMMARY)).containsText("0 matching file(s)");
        page.locator("label[for='type-mediaFileView.image']").click();
        assertThat(page.locator(RESULT_SUMMARY)).containsText("2 matching file(s)");
    }

    @Test
    @DisplayName("pages through a large folder without repeating or skipping files")
    void pagesThroughALargeFolder() {
        String folder = createFolder("paging");
        // 50 files is more than the 48 a page shows; one upload takes up to 20
        for (int batch = 0; batch < 3; batch++) {
            List<FilePayload> files = new ArrayList<>();
            for (int i = 0; i < (batch < 2 ? 20 : 10); i++) {
                files.add(png(String.format("page-%02d.png", batch * 20 + i), Color.GRAY));
            }
            upload(folder, files.toArray(new FilePayload[0]));
            page.locator(UPLOAD_BUTTON).click();
            assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));
        }

        openFolder(folder);
        page.locator(SORT).selectOption("name");
        assertThat(page.locator(PAGE_NUMBER).first()).hasText("Page 1 of 2");
        assertThat(page.locator(CARD)).hasCount(48);
        Set<String> names = new HashSet<>(page.locator(CARD_NAME).allTextContents());

        page.locator(".roller-pager").first().getByText("Next").click();
        assertThat(page.locator(PAGE_NUMBER).first()).hasText("Page 2 of 2");
        assertThat(page.locator(CARD_NAME)).hasText(new String[] {"page-48.png", "page-49.png"});
        names.addAll(page.locator(CARD_NAME).allTextContents());
        Assertions.assertEquals(50, names.size(), "every file appears on exactly one page");

        // the sort survives paging
        assertThat(page.locator(SORT)).hasValue("name");
    }

    @Test
    @DisplayName("switches to the list view and remembers it")
    void remembersTheListView() {
        String folder = createFolder("list");
        upload(folder, png("listed.png", Color.CYAN));
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));

        openFolder(folder);
        page.locator(LIST_VIEW_BUTTON).click();
        assertThat(page.locator(GRID)).hasClass(Pattern.compile("media-list"));
        assertThat(page.locator(LIST_VIEW_BUTTON)).hasAttribute("aria-pressed", "true");

        page.reload();
        assertThat(page.locator(GRID)).hasClass(Pattern.compile("media-list"));

        page.locator(GRID_VIEW_BUTTON).click();
        assertThat(page.locator(GRID)).not().hasClass(Pattern.compile("media-list"));
    }

    @Test
    @DisplayName("edits a file on its details page and returns to the same folder")
    void editsDetailsAndReturns() {
        String folder = createFolder("details");
        upload(folder, png("detail.png", Color.MAGENTA));
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));

        openFolder(folder);
        page.locator(SORT).selectOption("name");
        String libraryUrl = page.url();
        card("detail.png").locator(CARD_NAME).click();

        assertThat(page).hasTitle(Pattern.compile("Media file details"));
        assertThat(page.locator(DETAILS_NAME)).hasText("detail.png");
        assertThat(page.locator(DETAILS_URL)).hasValue(Pattern.compile("/mediaresource/"));

        page.locator(DETAILS_DESCRIPTION).fill("A magenta square");
        page.locator(DETAILS_SAVE).click();
        assertThat(page.locator(MESSAGES)).containsText("updated");
        assertThat(page.locator(DETAILS_DESCRIPTION)).hasValue("A magenta square");

        // back to the folder, with the sort the user had chosen
        page.locator(DETAILS_BACK).click();
        assertThat(page.locator(FOLDER_HEADING)).containsText(folder);
        assertThat(page.locator(SORT)).hasValue("name");
        Assertions.assertTrue(page.url().contains("bean.sortBy=name"), "returned to " + page.url()
                + ", expected the library page " + libraryUrl);
    }

    @Test
    @DisplayName("deletes files from the selection toolbar and from the details page")
    void deletesFiles() {
        String folder = createFolder("delete");
        upload(folder, png("keep.png", Color.RED), png("drop1.png", Color.GREEN), png("drop2.png", Color.BLUE));
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));

        openFolder(folder);
        card("drop1.png").locator(CARD_CHECKBOX).check();
        page.onceDialog(dialog -> {
            Assertions.assertTrue(dialog.message().contains("1 selected file"), dialog.message());
            dialog.accept();
        });
        page.locator(DELETE_BUTTON).click();
        assertThat(page.locator(MESSAGES)).containsText("deleted");
        assertThat(page.locator(CARD)).hasCount(2);

        card("drop2.png").locator(CARD_NAME).click();
        page.onceDialog(dialog -> dialog.accept());
        page.locator(DETAILS_DELETE).click();
        assertThat(page.locator(FOLDER_HEADING)).containsText(folder);
        assertThat(page.locator(CARD_NAME)).hasText(new String[] {"keep.png"});
    }

    @Test
    @DisplayName("inserts a file into an entry with its description as the alt text")
    void insertsWithDescriptionAsAltText() {
        String folder = createFolder("insert");
        upload(folder, png("insert-me.png", Color.PINK));
        page.locator(SHARED_DETAILS).click();
        page.locator(SHARED_DESCRIPTION).fill("A pink \"square\" <test>");
        page.locator(UPLOAD_BUTTON).click();
        assertThat(page).hasTitle(Pattern.compile("Media File Upload Complete"));

        goTo("roller-ui/authoring/entryAdd.rol?weblog=" + BLOG_HANDLE);
        page.evaluate("() => document.querySelector('#mediafile_edit_lightbox').addEventListener('shown.bs.modal',"
                + " () => window.rollerDialogShown = true, {once: true})");
        page.locator("a[onclick*='onClickMediaFileInsert']").click();
        page.waitForFunction("() => window.rollerDialogShown === true");

        var chooser = page.frameLocator("#mediaFileEditor");
        chooser.locator("#chooserFolder").selectOption(new SelectOption().setLabel(folder));
        chooser.locator("button.media-card-name").filter(new Locator.FilterOptions().setHasText("insert-me.png")).click();
        assertThat(page.locator("#mediafile_edit_lightbox")).isHidden();

        // the editor escapes the description; browsers differ on escaping < and > in attributes
        if (page.locator(".note-editable").count() > 0) {
            assertThat(page.locator(".note-editable img")).hasAttribute("alt", "A pink \"square\" <test>");
        } else {
            assertThat(page.locator("#edit_content")).hasValue(
                    Pattern.compile("alt=\"A pink &quot;square&quot; (&lt;|<)test(&gt;|>)\""));
        }
    }

    // ----------------------------------------------------------------------

    /** Creates a folder with a unique name from the library page and returns its name. */
    private String createFolder(String prefix) {
        String name = prefix + (System.nanoTime() % 1_000_000);
        goTo(LIBRARY_PAGE);
        page.locator(NEW_FOLDER_BUTTON).click();
        page.locator(NEW_FOLDER_NAME).fill(name);
        page.locator(NEW_FOLDER_CREATE).click();
        assertThat(page.locator(FOLDER_HEADING)).containsText(name);
        return name;
    }

    private void openFolder(String folder) {
        goTo(LIBRARY_PAGE + "&directoryName=" + folder);
        assertThat(page.locator(FOLDER_HEADING)).containsText(folder);
    }

    /** Opens the upload page for a folder and adds files to its list, without sending them. */
    private void upload(String folder, FilePayload... files) {
        goTo(UPLOAD_PAGE + "&directoryName=" + folder);
        assertThat(page).hasTitle(Pattern.compile("Upload files"));
        assertThat(page.locator("#uploadFolder option:checked")).hasText(folder);
        page.locator(FILE_INPUT).setInputFiles(files);
        assertThat(page.locator(UPLOAD_ITEM)).hasCount(files.length);
    }

    private Locator card(String name) {
        return page.locator(CARD).filter(new Locator.FilterOptions()
                .setHas(page.locator(CARD_NAME, new Page.LocatorOptions().setHasText(name))));
    }

    private static FilePayload png(String name, Color color) {
        try {
            BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    image.setRGB(x, y, color.getRGB());
                }
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bytes);
            return new FilePayload(name, "image/png", bytes.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
