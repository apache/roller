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
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test Weblogger Bookmark Management.
 */
public class BookmarkTest extends TestCase {
    
    public static Log log = LogFactory.getLog(BookmarkTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("bkmrkTestUser");
            testWeblog = TestUtils.setupWeblog("bkmrkTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tearDown", ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    public Weblogger getRoller() {
        return WebloggerFactory.getWeblogger();
    }
    
    
    public void testBookmarkCRUD() throws Exception {
        
        WeblogManager wmgr = getRoller().getWeblogManager();

        // Add bookmark
        WeblogBookmark bookmark1 = new WeblogBookmark(
                testWeblog,
                "TestBookmark1",
                "created by testBookmarkCRUD()",
                "http://www.example1.com");
        testWeblog.addBookmark(bookmark1);
        bookmark1.calculatePosition();
        wmgr.saveBookmark(bookmark1);

        // Add another bookmark
        WeblogBookmark bookmark2 = new WeblogBookmark(
                testWeblog,
                "TestBookmark2",
                "created by testBookmarkCRUD()",
                "http://www.example2.com");
        testWeblog.addBookmark(bookmark2);
        bookmark2.calculatePosition();
        wmgr.saveWeblog(testWeblog);

        TestUtils.endSession(true);
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmark bookmarkb, bookmarka;

        // See that two bookmarks were stored
        List<WeblogBookmark> bookmarks = testWeblog.getBookmarks();
        assertEquals(2, bookmarks.size());
        Iterator<WeblogBookmark> iter = bookmarks.iterator();
        bookmarka = iter.next();
        bookmarkb = iter.next();

        // Remove one bookmark directly
        wmgr.removeBookmark(bookmarka);

        TestUtils.endSession(true);
        assertNull(wmgr.getBookmark(bookmarka.getId()));

        // Weblog should now contain one bookmark
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertNotNull(testWeblog);
        assertEquals(1, testWeblog.getBookmarks().size());
        assertEquals(bookmarkb.getId(), testWeblog.getBookmarks().get(0).getId());

        // Remove other bookmark via removing from weblog
        testWeblog.getBookmarks().remove(bookmarkb);
        wmgr.saveWeblog(testWeblog);
        TestUtils.endSession(true);

        // Last bookmark should be gone
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertEquals(0, testWeblog.getBookmarks().size());
        assertNull(wmgr.getBookmark(bookmarkb.getId()));
    }

    /**
     * Test all bookmark lookup methods.
     */
    public void testBookmarkLookups() throws Exception {
        
        WeblogManager wmgr = getRoller().getWeblogManager();

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // add some bookmarks
        WeblogBookmark b1 = new WeblogBookmark(
                testWeblog, "b1", "testbookmark13",
                "http://example1.com");
        testWeblog.addBookmark(b1);
        wmgr.saveBookmark(b1);
        WeblogBookmark b2 = new WeblogBookmark(
                testWeblog, "b2", "testbookmark14",
                "http://example2.com");
        testWeblog.addBookmark(b2);
        wmgr.saveBookmark(b2);
        WeblogBookmark b3 = new WeblogBookmark(
                testWeblog, "b3", "testbookmark16",
                "http://example3.com");
        testWeblog.addBookmark(b3);
        wmgr.saveBookmark(b3);
        
        TestUtils.endSession(true);
        
        // test lookup by id
        WeblogBookmark testBookmark = wmgr.getBookmark(b1.getId());
        assertNotNull(testBookmark);
        assertEquals("b1", testBookmark.getName());

        // test lookup of all bookmarks for a website
        Weblog testWeblog2 = wmgr.getWeblog(testWeblog.getId());
        List<WeblogBookmark> allBookmarks = testWeblog2.getBookmarks();
        assertNotNull(allBookmarks);
        assertEquals(3, allBookmarks.size());
        
    }
}
