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
*
* Source file modified from the original ASF source; all changes made
* are also under Apache License.
*/
package org.apache.roller.weblogger.business;

import java.util.Iterator;
import java.util.List;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.WebloggerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test Weblogger Bookmark Management.
 */
public class BookmarkTest extends WebloggerTest {
    User testUser = null;
    Weblog testWeblog = null;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        try {
            testUser = setupUser("bkmrkTestUser");
            testWeblog = setupWeblog("bkmrkTestWeblog", testUser);
            endSession(true);
        } catch (Exception ex) {
            throw new Exception("Test setup failed", ex);
        }
    }
    
    @After
    public void tearDown() throws Exception {
        try {
            teardownWeblog(testWeblog.getId());
            teardownUser(testUser.getId());
            endSession(true);
        } catch (Exception ex) {
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    @Test
    public void testBookmarkCRUD() throws Exception {
        // Add bookmark
        WeblogBookmark bookmark1 = new WeblogBookmark(
                testWeblog,
                "TestBookmark1",
                "http://www.example1.com", "created by testBookmarkCRUD()"
        );
        testWeblog.addBookmark(bookmark1);
        bookmark1.calculatePosition();
        weblogManager.saveBookmark(bookmark1);

        // Add another bookmark
        WeblogBookmark bookmark2 = new WeblogBookmark(
                testWeblog,
                "TestBookmark2",
                "http://www.example2.com", "created by testBookmarkCRUD()"
        );
        testWeblog.addBookmark(bookmark2);
        bookmark2.calculatePosition();
        weblogManager.saveWeblog(testWeblog);

        endSession(true);
        testWeblog = getManagedWeblog(testWeblog);
        WeblogBookmark bookmarkb, bookmarka;

        // See that two bookmarks were stored
        List<WeblogBookmark> bookmarks = testWeblog.getBookmarks();
        assertEquals(2, bookmarks.size());
        Iterator<WeblogBookmark> iter = bookmarks.iterator();
        bookmarka = iter.next();
        bookmarkb = iter.next();

        // Remove one bookmark directly
        weblogManager.removeBookmark(bookmarka);
        endSession(true);
        assertNull(weblogManager.getBookmark(bookmarka.getId()));

        // Weblog should now contain one bookmark
        testWeblog = getManagedWeblog(testWeblog);
        assertNotNull(testWeblog);
        assertEquals(1, testWeblog.getBookmarks().size());
        assertEquals(bookmarkb.getId(), testWeblog.getBookmarks().get(0).getId());

        // Remove other bookmark via removing from weblog
        testWeblog.getBookmarks().remove(bookmarkb);
        weblogManager.saveWeblog(testWeblog);
        endSession(true);

        // Last bookmark should be gone
        testWeblog = getManagedWeblog(testWeblog);
        assertEquals(0, testWeblog.getBookmarks().size());
        assertNull(weblogManager.getBookmark(bookmarkb.getId()));
    }

    /**
     * Test all bookmark lookup methods.
     */
    @Test
    public void testBookmarkLookups() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);

        // add some bookmarks
        WeblogBookmark b1 = new WeblogBookmark(testWeblog, "b1", "http://example1.com", "testbookmark13"
        );
        testWeblog.addBookmark(b1);
        weblogManager.saveBookmark(b1);
        WeblogBookmark b2 = new WeblogBookmark(testWeblog, "b2", "http://example2.com", "testbookmark14"
        );
        testWeblog.addBookmark(b2);
        weblogManager.saveBookmark(b2);
        WeblogBookmark b3 = new WeblogBookmark(testWeblog, "b3", "http://example3.com", "testbookmark16"
        );
        testWeblog.addBookmark(b3);
        weblogManager.saveBookmark(b3);
        
        endSession(true);
        
        // test lookup by id
        WeblogBookmark testBookmark = weblogManager.getBookmark(b1.getId());
        assertNotNull(testBookmark);
        assertEquals("b1", testBookmark.getName());

        // test lookup of all bookmarks for a website
        Weblog testWeblog2 = weblogManager.getWeblog(testWeblog.getId());
        List<WeblogBookmark> allBookmarks = testWeblog2.getBookmarks();
        assertNotNull(allBookmarks);
        assertEquals(3, allBookmarks.size());
    }
}
