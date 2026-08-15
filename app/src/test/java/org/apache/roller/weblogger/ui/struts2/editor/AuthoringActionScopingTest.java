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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        action.setActionWeblog(TestUtils.getManagedWebsite(weblogOne));
        action.getBean().setId(foreign.getId());
        action.myPrepare();

        assertNull(action.getEntry(),
                "editor must not load an entry owned by another weblog");
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
}
