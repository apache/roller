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

package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests that resource lookups performed on behalf of the authoring UI resolve
 * only within the weblog they are scoped to.
 *
 * Each test sets up two independent weblogs owned by different users and
 * verifies that a resource belonging to one is not reachable through a lookup
 * scoped to the other.
 */
public class WeblogScopedLookupTest {

    public static Log log = LogFactory.getLog(WeblogScopedLookupTest.class);

    User userOne = null;
    User userTwo = null;
    Weblog weblogOne = null;
    Weblog weblogTwo = null;

    @BeforeEach
    public void setUp() throws Exception {

        TestUtils.setupWeblogger();

        // media file creation is rejected outright unless uploads are enabled
        WebloggerFactory.getWeblogger().getPropertiesManager().getProperties()
                .get("uploads.enabled").setValue("true");

        try {
            userOne = TestUtils.setupUser("scopeTestUserOne");
            userTwo = TestUtils.setupUser("scopeTestUserTwo");
            weblogOne = TestUtils.setupWeblog("scopeTestWeblogOne", userOne);
            weblogTwo = TestUtils.setupWeblog("scopeTestWeblogTwo", userTwo);
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

    /**
     * Creates a template owned by the given weblog and returns its id.
     */
    private String createTemplate(Weblog weblog, String name) throws Exception {
        WeblogTemplate template = new WeblogTemplate();
        template.setAction(ComponentType.WEBLOG);
        template.setName(name);
        template.setDescription("Test Weblog Template");
        template.setLink(name);
        template.setLastModified(new java.util.Date());
        template.setWeblog(TestUtils.getManagedWebsite(weblog));

        WebloggerFactory.getWeblogger().getWeblogManager().saveTemplate(template);
        TestUtils.endSession(true);

        return template.getId();
    }

    @Test
    public void testGetTemplateReturnsNullForTemplateOwnedByAnotherWeblog() throws Exception {

        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

        String foreignTemplateId = createTemplate(weblogTwo, "scopeTestForeignTemplate");

        WeblogTemplate found = mgr.getTemplate(
                TestUtils.getManagedWebsite(weblogOne), foreignTemplateId);

        assertNull(found, "template owned by another weblog must not be returned");
    }

    @Test
    public void testGetTemplateReturnsTemplateOwnedByTheGivenWeblog() throws Exception {

        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

        String ownTemplateId = createTemplate(weblogOne, "scopeTestOwnTemplate");

        WeblogTemplate found = mgr.getTemplate(
                TestUtils.getManagedWebsite(weblogOne), ownTemplateId);

        assertNotNull(found, "template owned by the given weblog must be returned");
        assertEquals(ownTemplateId, found.getId());
    }

    @Test
    public void testGetWeblogEntryReturnsNullForEntryOwnedByAnotherWeblog() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogEntry foreign = TestUtils.setupWeblogEntry(
                "scopeTestForeignEntry", weblogTwo, userTwo);
        TestUtils.endSession(true);

        WeblogEntry found = mgr.getWeblogEntry(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "entry owned by another weblog must not be returned");
    }

    @Test
    public void testGetWeblogEntryReturnsEntryOwnedByTheGivenWeblog() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogEntry own = TestUtils.setupWeblogEntry(
                "scopeTestOwnEntry", weblogOne, userOne);
        TestUtils.endSession(true);

        WeblogEntry found = mgr.getWeblogEntry(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "entry owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }

    @Test
    public void testGetWeblogCategoryReturnsNullForCategoryOwnedByAnotherWeblog() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogCategory foreign = TestUtils.setupWeblogCategory(
                TestUtils.getManagedWebsite(weblogTwo), "scopeTestForeignCategory");
        TestUtils.endSession(true);

        WeblogCategory found = mgr.getWeblogCategory(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "category owned by another weblog must not be returned");
    }

    @Test
    public void testGetWeblogCategoryReturnsCategoryOwnedByTheGivenWeblog() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogCategory own = TestUtils.setupWeblogCategory(
                TestUtils.getManagedWebsite(weblogOne), "scopeTestOwnCategory");
        TestUtils.endSession(true);

        WeblogCategory found = mgr.getWeblogCategory(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "category owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }

    @Test
    public void testGetCommentReturnsNullForCommentOwnedByAnotherWeblog() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogEntry foreignEntry = TestUtils.setupWeblogEntry(
                "scopeTestForeignCommentEntry", weblogTwo, userTwo);
        WeblogEntryComment foreign = TestUtils.setupComment(
                "scopeTestForeignComment", foreignEntry);
        TestUtils.endSession(true);

        WeblogEntryComment found = mgr.getComment(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "comment owned by another weblog must not be returned");
    }

    @Test
    public void testGetCommentReturnsCommentOwnedByTheGivenWeblog() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

        WeblogEntry ownEntry = TestUtils.setupWeblogEntry(
                "scopeTestOwnCommentEntry", weblogOne, userOne);
        WeblogEntryComment own = TestUtils.setupComment(
                "scopeTestOwnComment", ownEntry);
        TestUtils.endSession(true);

        WeblogEntryComment found = mgr.getComment(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "comment owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }

    /**
     * Creates a bookmark in a new folder owned by the given weblog.
     */
    private WeblogBookmark createBookmark(Weblog weblog, String name) throws Exception {

        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();

        WeblogBookmarkFolder folder = TestUtils.setupFolder(
                TestUtils.getManagedWebsite(weblog), name + "Folder");
        TestUtils.endSession(true);

        WeblogBookmark bookmark = new WeblogBookmark(
                bmgr.getFolder(folder.getId()), name, "desc",
                "http://example.com/", "http://example.com/feed", "image");
        bmgr.saveBookmark(bookmark);
        TestUtils.endSession(true);

        return bookmark;
    }

    @Test
    public void testGetFolderReturnsNullForFolderOwnedByAnotherWeblog() throws Exception {

        BookmarkManager mgr = WebloggerFactory.getWeblogger().getBookmarkManager();

        WeblogBookmarkFolder foreign = TestUtils.setupFolder(
                TestUtils.getManagedWebsite(weblogTwo), "scopeTestForeignFolder");
        TestUtils.endSession(true);

        WeblogBookmarkFolder found = mgr.getFolderById(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "folder owned by another weblog must not be returned");
    }

    @Test
    public void testGetFolderReturnsFolderOwnedByTheGivenWeblog() throws Exception {

        BookmarkManager mgr = WebloggerFactory.getWeblogger().getBookmarkManager();

        WeblogBookmarkFolder own = TestUtils.setupFolder(
                TestUtils.getManagedWebsite(weblogOne), "scopeTestOwnFolder");
        TestUtils.endSession(true);

        WeblogBookmarkFolder found = mgr.getFolderById(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "folder owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }

    @Test
    public void testGetBookmarkReturnsNullForBookmarkOwnedByAnotherWeblog() throws Exception {

        BookmarkManager mgr = WebloggerFactory.getWeblogger().getBookmarkManager();

        WeblogBookmark foreign = createBookmark(weblogTwo, "scopeTestForeignBookmark");

        WeblogBookmark found = mgr.getBookmark(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "bookmark owned by another weblog must not be returned");
    }

    @Test
    public void testGetBookmarkReturnsBookmarkOwnedByTheGivenWeblog() throws Exception {

        BookmarkManager mgr = WebloggerFactory.getWeblogger().getBookmarkManager();

        WeblogBookmark own = createBookmark(weblogOne, "scopeTestOwnBookmark");

        WeblogBookmark found = mgr.getBookmark(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "bookmark owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }

    /**
     * Creates a media file in the given weblog's default directory.
     */
    private MediaFile createMediaFile(Weblog weblog, String name) throws Exception {

        MediaFileManager mmgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        Weblog managed = TestUtils.getManagedWebsite(weblog);
        MediaFileDirectory directory = mmgr.getDefaultMediaFileDirectory(managed);
        if (directory == null) {
            directory = mmgr.createMediaFileDirectory(managed, "default");
            TestUtils.endSession(true);
            managed = TestUtils.getManagedWebsite(weblog);
            directory = mmgr.getMediaFileDirectory(directory.getId());
        }

        MediaFile mediaFile = new MediaFile();
        mediaFile.setName(name);
        mediaFile.setDescription("scoped lookup test file");
        mediaFile.setCopyrightText("none");
        mediaFile.setSharedForGallery(false);
        mediaFile.setLength(3000);
        mediaFile.setDirectory(directory);
        mediaFile.setWeblog(managed);
        mediaFile.setContentType("image/jpeg");
        mediaFile.setInputStream(getClass().getResourceAsStream("/hawk.jpg"));

        RollerMessages messages = new RollerMessages();
        mmgr.createMediaFile(managed, mediaFile, messages);
        // createMediaFile reports rejection through messages rather than
        // throwing, so a silent failure here would leave the test asserting
        // against a file that was never stored
        assertEquals(0, messages.getErrorCount(),
                "media file fixture was rejected: " + messages);
        TestUtils.endSession(true);

        return mediaFile;
    }

    @Test
    public void testGetMediaFileReturnsNullForFileOwnedByAnotherWeblog() throws Exception {

        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFile foreign = createMediaFile(weblogTwo, "scopeTestForeignFile.jpg");

        MediaFile found = mgr.getMediaFile(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "media file owned by another weblog must not be returned");
    }

    @Test
    public void testGetMediaFileReturnsFileOwnedByTheGivenWeblog() throws Exception {

        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFile own = createMediaFile(weblogOne, "scopeTestOwnFile.jpg");

        MediaFile found = mgr.getMediaFile(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "media file owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }

    @Test
    public void testGetMediaFileDirectoryReturnsNullForDirectoryOwnedByAnotherWeblog() throws Exception {

        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory foreign = mgr.createMediaFileDirectory(
                TestUtils.getManagedWebsite(weblogTwo), "scopeTestForeignDir");
        TestUtils.endSession(true);

        MediaFileDirectory found = mgr.getMediaFileDirectory(
                TestUtils.getManagedWebsite(weblogOne), foreign.getId());

        assertNull(found, "directory owned by another weblog must not be returned");
    }

    @Test
    public void testGetMediaFileDirectoryReturnsDirectoryOwnedByTheGivenWeblog() throws Exception {

        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();

        MediaFileDirectory own = mgr.createMediaFileDirectory(
                TestUtils.getManagedWebsite(weblogOne), "scopeTestOwnDir");
        TestUtils.endSession(true);

        MediaFileDirectory found = mgr.getMediaFileDirectory(
                TestUtils.getManagedWebsite(weblogOne), own.getId());

        assertNotNull(found, "directory owned by the given weblog must be returned");
        assertEquals(own.getId(), found.getId());
    }
}
