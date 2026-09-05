/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.ui.struts2.editor;

import com.opensymphony.xwork2.DefaultTextProvider;
import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.TextProviderFactory;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Scope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that authoring actions resolve the resource named by a request
 * parameter within the weblog the request is acting on, rather than by id
 * alone.
 *
 * The interceptor stack resolves and authorizes the action weblog before
 * myPrepare() runs, so these tests set the action weblog directly and then
 * ask each action to prepare against an id owned by a different weblog.
 */
public class AuthoringActionScopingTest {

    public static Log log = LogFactory.getLog(AuthoringActionScopingTest.class);

    User userOne = null;
    User userTwo = null;
    Weblog weblogOne = null;
    Weblog weblogTwo = null;

    @BeforeEach
    public void setUp() throws Exception {

        TestUtils.setupWeblogger();

        try {
            userOne = TestUtils.setupUser("actScopeUserOne");
            userTwo = TestUtils.setupUser("actScopeUserTwo");
            weblogOne = TestUtils.setupWeblog("actScopeWeblogOne", userOne);
            weblogTwo = TestUtils.setupWeblog("actScopeWeblogTwo", userTwo);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {

        try {
            TestUtils.teardownWeblog(weblogOne.getId());
            TestUtils.teardownWeblog(weblogTwo.getId());
            TestUtils.teardownUser(userOne.getUserName());
            TestUtils.teardownUser(userTwo.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }

    // ----------------------------------------------------- entry removal

    @Test
    public void testEntryRemoveDoesNotLoadEntryOwnedByAnotherWeblog() throws Exception {

        WeblogEntry foreign = TestUtils.setupWeblogEntry(
                "actScopeForeignEntry", weblogTwo, userTwo);
        TestUtils.endSession(true);

        EntryRemove action = new EntryRemove();
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.setRemoveId(foreign.getId());
        action.myPrepare();

        assertNull(action.getRemoveEntry(),
                "remove must not load an entry owned by another weblog");
    }

    @Test
    public void testEntryRemoveLoadsEntryOwnedByTheActionWeblog() throws Exception {

        WeblogEntry own = TestUtils.setupWeblogEntry(
                "actScopeOwnEntry", weblogOne, userOne);
        TestUtils.endSession(true);

        EntryRemove action = new EntryRemove();
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.setRemoveId(own.getId());
        action.myPrepare();

        assertNotNull(action.getRemoveEntry(),
                "remove must load an entry owned by the action weblog");
        assertEquals(own.getId(), action.getRemoveEntry().getId());
    }

    // ----------------------------------------------------- entry editing

    @Test
    public void testEntryEditDoesNotLoadEntryOwnedByAnotherWeblog() throws Exception {

        WeblogEntry foreign = TestUtils.setupWeblogEntry(
                "actScopeForeignEditEntry", weblogTwo, userTwo);
        TestUtils.endSession(true);

        EntryEdit action = new EntryEdit();
        prepareText(action);
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.getBean().setId(foreign.getId());
        action.myPrepare();

        assertNull(action.getEntry(),
                "editor must not load an entry owned by another weblog");
    }

    @Test
    public void testEntryEditReportsMissingEntry() throws Exception {
        EntryEdit action = new EntryEdit();
        prepareText(action);
        action.setActionName("entryEdit");
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.getBean().setId("missing-entry");
        action.myPrepare();

        assertEquals(EntryEdit.ERROR, action.execute());
        assertTrue(action.hasActionErrors());
    }

    // -------------------------------------------------- category removal

    @Test
    public void testCategoryRemoveDoesNotLoadCategoryOwnedByAnotherWeblog() throws Exception {

        WeblogCategory foreign = TestUtils.setupWeblogCategory(
                TestUtils.getManagedWebsite(weblogTwo), "actScopeForeignCat");
        TestUtils.endSession(true);

        CategoryRemove action = new CategoryRemove();
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.setRemoveId(foreign.getId());
        action.myPrepare();

        assertNull(action.getCategory(),
                "remove must not load a category owned by another weblog");
    }

    @Test
    public void testCategoryRemoveLoadsCategoryOwnedByTheActionWeblog() throws Exception {

        WeblogCategory own = TestUtils.setupWeblogCategory(
                TestUtils.getManagedWebsite(weblogOne), "actScopeOwnCat");
        TestUtils.endSession(true);

        CategoryRemove action = new CategoryRemove();
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.setRemoveId(own.getId());
        action.myPrepare();

        assertNotNull(action.getCategory(),
                "remove must load a category owned by the action weblog");
        assertEquals(own.getId(), action.getCategory().getId());
    }

    @Test
    public void testCategoryEditReportsMissingCategory() throws Exception {
        CategoryEdit action = new CategoryEdit();
        prepareText(action);
        action.setActionName("categoryEdit");
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.getBean().setId("missing-category");
        action.myPrepare();

        assertEquals(CategoryEdit.ERROR, action.execute());
        assertTrue(action.hasActionErrors());
    }

    @Test
    public void testCategoryRemoveRejectsMissingTarget() throws Exception {
        WeblogCategory own = TestUtils.setupWeblogCategory(
                TestUtils.getManagedWebsite(weblogOne), "actScopeRemoveCat");
        TestUtils.endSession(true);

        CategoryRemove action = new CategoryRemove();
        prepareText(action);
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.setRemoveId(own.getId());
        action.setTargetCategoryId("missing-target-category");
        action.myPrepare();

        assertEquals(CategoryRemove.INPUT, action.remove());
        assertTrue(action.hasActionErrors());
        assertNotNull(WebloggerFactory.getWeblogger().getWeblogEntryManager()
                .getWeblogCategory(TestUtils.getManagedWebsite(weblogOne), own.getId()));
    }

    @Test
    public void testWeblogConfigPreservesCategoryWhenSelectionIsMissing()
            throws Exception {
        Weblog managed = TestUtils.getManagedWebsite(weblogOne);
        String originalCategoryId = managed.getBloggerCategory().getId();
        WeblogConfig action = new WeblogConfig();
        prepareText(action);
        action.setActionWeblog(managed);
        action.getBean().copyFrom(managed);
        action.getBean().setBloggerCategoryId("missing-blogger-category");

        assertEquals(WeblogConfig.INPUT, action.save());
        assertTrue(action.hasActionErrors());
        assertEquals(originalCategoryId,
                managed.getBloggerCategory().getId());
    }

    // -------------------------------------------------- bookmark editing

    @Test
    public void testBookmarkEditDoesNotLoadBookmarkOwnedByAnotherWeblog() throws Exception {

        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();

        WeblogBookmarkFolder folder = TestUtils.setupFolder(
                TestUtils.getManagedWebsite(weblogTwo), "actScopeForeignBmFolder");
        TestUtils.endSession(true);

        WeblogBookmark foreign = new WeblogBookmark(
                bmgr.getFolder(folder.getId()), "actScopeForeignBm", "desc",
                "http://example.com/", "http://example.com/feed", "image");
        bmgr.saveBookmark(foreign);
        TestUtils.endSession(true);

        BookmarkEdit action = new BookmarkEdit();
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.getBean().setId(foreign.getId());
        action.myPrepare();

        assertNull(action.getBookmark(),
                "editor must not load a bookmark owned by another weblog");
    }

    @Test
    public void testBookmarkAndFolderEditorsReportMissingResources() throws Exception {
        BookmarkEdit bookmark = new BookmarkEdit();
        prepareText(bookmark);
        bookmark.setActionName("bookmarkEdit");
        bookmark.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        bookmark.getBean().setId("missing-bookmark");
        bookmark.myPrepare();

        assertEquals(BookmarkEdit.ERROR, bookmark.execute());
        assertTrue(bookmark.hasActionErrors());

        FolderEdit folder = new FolderEdit();
        prepareText(folder);
        folder.setActionName("folderEdit");
        folder.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        folder.getBean().setId("missing-folder");
        folder.myPrepare();

        assertEquals(FolderEdit.ERROR, folder.execute());
        assertTrue(folder.hasActionErrors());
    }

    @Test
    public void testBookmarkMoveRejectsMissingTarget() throws Exception {
        Bookmarks action = new Bookmarks();
        prepareText(action);
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.setTargetFolderId("missing-target-folder");
        action.setSelectedBookmarks(new String[0]);

        assertEquals(Bookmarks.LIST, action.move());
        assertTrue(action.hasActionErrors());
    }

    @Test
    public void testMissingCommentFilterReturnsNoComments() throws Exception {
        Comments action = new Comments();
        prepareText(action);
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.getBean().setEntryId("missing-comment-entry");

        assertEquals(Comments.LIST, action.execute());
        assertTrue(action.hasActionErrors());
        assertTrue(action.getPager().getItems().isEmpty());
    }

    @Test
    public void testMediaActionsReportMissingResources() throws Exception {
        MediaFileImageDim dimensions = new MediaFileImageDim();
        prepareText(dimensions);
        dimensions.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        dimensions.setMediaFileId("missing-media-file");

        assertEquals(MediaFileImageDim.ERROR, dimensions.execute());
        assertTrue(dimensions.hasActionErrors());

        EntryAddWithMediaFile entry = new EntryAddWithMediaFile();
        prepareText(entry);
        entry.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        entry.setSelectedImages(new String[] {"missing-media-file"});

        assertEquals(EntryAddWithMediaFile.ERROR, entry.execute());
        assertTrue(entry.hasActionErrors());

        TestMediaFileAction media = new TestMediaFileAction();
        prepareText(media);
        media.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        media.setMediaFileId("missing-media-file");
        media.deleteMissing();
        assertTrue(media.hasActionErrors());
    }

    @Test
    public void testMediaMoveRejectsMissingDirectory() throws Exception {
        TestMediaFileAction media = new TestMediaFileAction();
        prepareText(media);
        media.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        media.setSelectedMediaFiles(new String[] {"missing-media-file"});
        media.setSelectedDirectory("missing-media-directory");

        media.moveMissing();

        assertTrue(media.hasActionErrors());
    }

    private static class TestMediaFileAction extends MediaFileBase {
        private static final long serialVersionUID = 1L;

        void deleteMissing() {
            doDeleteMediaFile();
        }

        void moveMissing() {
            doMoveSelected();
        }
    }

    private static void prepareText(UIAction action) {
        action.setContainer(TEST_CONTAINER);
    }

    private static final TextProvider TEST_TEXT_PROVIDER = new DefaultTextProvider() {
        @Override
        public String getText(String key) {
            return key;
        }

        @Override
        public String getText(String key, List<?> args) {
            return key;
        }

        @Override
        public String getText(String key, String defaultValue, String value) {
            return key;
        }
    };

    private static final TextProviderFactory TEST_TEXT_PROVIDER_FACTORY =
            new TextProviderFactory() {
                @Override
                @SuppressWarnings("rawtypes")
                public TextProvider createInstance(Class type) {
                    return TEST_TEXT_PROVIDER;
                }

                @Override
                public TextProvider createInstance(ResourceBundle bundle) {
                    return TEST_TEXT_PROVIDER;
                }
            };

    private static final Container TEST_CONTAINER = new Container() {
        @Override
        public void inject(Object object) {
        }

        @Override
        public <T> T inject(Class<T> implementation) {
            return null;
        }

        @Override
        public <T> T getInstance(Class<T> type, String name) {
            return getInstance(type);
        }

        @Override
        public <T> T getInstance(Class<T> type) {
            if (type == TextProviderFactory.class) {
                return type.cast(TEST_TEXT_PROVIDER_FACTORY);
            }
            if (type == String.class) {
                return type.cast("false");
            }
            return null;
        }

        @Override
        public Set<String> getInstanceNames(Class<?> type) {
            return Collections.emptySet();
        }

        @Override
        public void setScopeStrategy(Scope.Strategy strategy) {
        }

        @Override
        public void removeScopeStrategy() {
        }
    };
}
