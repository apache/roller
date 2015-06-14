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
        
        // setup weblogger
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
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        WeblogManager wmgr = getRoller().getWeblogManager();

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // Add bookmark by adding to folder
        WeblogBookmark bookmark1 = new WeblogBookmark(
                testWeblog,
                "TestBookmark1",
                "created by testBookmarkCRUD()",
                "http://www.example.com",
                "test.jpg");
        bmgr.saveBookmark(bookmark1);

        // Add another bookmark
        WeblogBookmark bookmark2 = new WeblogBookmark(
                testWeblog,
                "TestBookmark2",
                "created by testBookmarkCRUD()",
                "http://www.example.com",
                "test.jpf");
        bmgr.saveBookmark(bookmark2);

        TestUtils.endSession(true);

        WeblogBookmark bookmarkb = null, bookmarka = null;
        
        // See that two bookmarks were stored
        List<WeblogBookmark> bookmarks = bmgr.getBookmarks(testWeblog);
        assertEquals(2, bookmarks.size());
        Iterator<WeblogBookmark> iter = bookmarks.iterator();
        bookmarka = iter.next();
        bookmarkb = iter.next();
        
        // Remove one bookmark via Bookmark Manager
        bmgr.removeBookmark(bookmarka);
        TestUtils.endSession(true);
                
        // Folder should now contain one bookmark
        assertNull(bmgr.getBookmark(bookmarka.getId()));
        assertNotNull(bmgr.getBookmark(bookmarkb.getId()));
        Weblog weblogTest2 = wmgr.getWeblog(testWeblog.getId());
        assertEquals(1, weblogTest2.getBookmarks().size());

        // Remove other bookmark via Weblog Manager
        weblogTest2.getBookmarks().remove(bookmarkb);
        wmgr.saveWeblog(weblogTest2);
        TestUtils.endSession(true);

        // Last bookmark should be gone
        weblogTest2 = wmgr.getWeblog(testWeblog.getId());
        assertEquals(0, weblogTest2.getBookmarks().size());
    }

    /**
     * Test all bookmark lookup methods.
     */
    public void testBookmarkLookups() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        WeblogManager wmgr = getRoller().getWeblogManager();

        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // add some bookmarks
        WeblogBookmark b1 = new WeblogBookmark(
                testWeblog, "b1", "testbookmark13",
                "http://example.com",
                "image.gif");
        bmgr.saveBookmark(b1);
        WeblogBookmark b2 = new WeblogBookmark(
                testWeblog, "b2", "testbookmark14",
                "http://example.com",
                "image.gif");
        bmgr.saveBookmark(b2);
        WeblogBookmark b3 = new WeblogBookmark(
                testWeblog, "b3", "testbookmark16",
                "http://example.com",
                "image.gif");
        bmgr.saveBookmark(b3);
        
        TestUtils.endSession(true);
        
        // test lookup by id
        WeblogBookmark testBookmark = bmgr.getBookmark(b1.getId());
        assertNotNull(testBookmark);
        assertEquals("b1", testBookmark.getName());

        // test lookup of all bookmarks for a website
        Weblog testWeblog2 = wmgr.getWeblog(testWeblog.getId());
        List<WeblogBookmark> allBookmarks = bmgr.getBookmarks(testWeblog2);
        assertNotNull(allBookmarks);
        assertEquals(3, allBookmarks.size());
        
    }
}
