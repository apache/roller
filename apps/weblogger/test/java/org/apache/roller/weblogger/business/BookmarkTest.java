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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.Roller;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Test Roller Bookmark Management.
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
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tearDown", ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    public Roller getRoller() {
        return RollerFactory.getRoller();
    }
    
    
    public void testBookmarkCRUD() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        WeblogBookmarkFolder folder = new WeblogBookmarkFolder(root, "TestFolder2", null, TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(folder);
        TestUtils.endSession(true);
        
        // query for folder again since session ended
        folder = bmgr.getFolder(folder.getId());
        
        // Add bookmark by adding to folder
        WeblogBookmark bookmark1 = new WeblogBookmark(
                folder,
                "TestBookmark1",
                "created by testBookmarkCRUD()",
                "http://www.example.com",
                "http://www.example.com/rss.xml",
                new Integer(1),
                new Integer(12),
                "test.jpg");
        folder.addBookmark(bookmark1);
        
        // Add another bookmark
        WeblogBookmark bookmark2 = new WeblogBookmark(
                folder,
                "TestBookmark2",
                "created by testBookmarkCRUD()",
                "http://www.example.com",
                "http://www.example.com/rss.xml",
                new Integer(1),
                new Integer(12),
                "test.jpf");
        folder.addBookmark(bookmark2);
        
        TestUtils.endSession(true);
        
        
        WeblogBookmarkFolder testFolder = null;
        WeblogBookmark bookmarkb = null, bookmarka = null;
        
        // See that two bookmarks were stored
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(2, testFolder.getBookmarks().size());
        bookmarka = (WeblogBookmark)testFolder.getBookmarks().iterator().next();
        bookmarkb = (WeblogBookmark)testFolder.getBookmarks().iterator().next();
        
        // Remove one bookmark
        bmgr.removeBookmark(bookmarka);
        bmgr.saveFolder(testFolder);
        TestUtils.endSession(true);
        
        // Folder should now contain one bookmark
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(1, testFolder.getBookmarks().size());
        
        // Remove folder
        bmgr.removeFolder(testFolder);
        TestUtils.endSession(true);
        
        // Folder and one remaining bookmark should be gone
        assertNull( bmgr.getBookmark(bookmarkb.getId()) );
        assertNull( bmgr.getFolder(folder.getId()) );
    }
    
    
    /**
     * Test all bookmark lookup methods.
     */
    public void testBookmarkLookups() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        // add some folders
        WeblogBookmarkFolder f1 = new WeblogBookmarkFolder(root, "f1", null, TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(f1);
        WeblogBookmarkFolder f2 = new WeblogBookmarkFolder(f1, "f2", null, TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(f2);
        WeblogBookmarkFolder f3 = new WeblogBookmarkFolder(root, "f3", null, TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(f3);
        
        // add some bookmarks
        WeblogBookmark b1 = new WeblogBookmark(
                f1, "b1", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        bmgr.saveBookmark(b1);
        WeblogBookmark b2 = new WeblogBookmark(
                f1, "b2", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        bmgr.saveBookmark(b2);
        WeblogBookmark b3 = new WeblogBookmark(
                f2, "b3", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        bmgr.saveBookmark(b3);
        
        TestUtils.endSession(true);
        
        // test lookup by id
        WeblogBookmark testBookmark = bmgr.getBookmark(b1.getId());
        assertNotNull(testBookmark);
        assertEquals("b1", testBookmark.getName());
        
        // test lookup of all bookmarks in single folder
        WeblogBookmarkFolder testFolder = bmgr.getFolder(f1.getId());
        List allBookmarks = bmgr.getBookmarks(testFolder, false);
        assertNotNull(allBookmarks);
        assertEquals(2, allBookmarks.size());
        
        // getBookmarks(folder, false) should also match folder.getBookmarks()
        assertEquals(allBookmarks.size(), testFolder.getBookmarks().size());
        
        // test lookup of all bookmarks in folder branch (including subfolders)
        testFolder = bmgr.getFolder(f1.getId());
        allBookmarks = bmgr.getBookmarks(testFolder, true);
        assertNotNull(allBookmarks);
        assertEquals(3, allBookmarks.size());
    }
    
    
    /**
     * Creates folder tree like this:
     *    root/
     *       dest/
     *       f1/
     *          b1
     *          f2/
     *             f3/
     *
     * TODO: this test is commented out because the way this functionality is
     * really done is simply by changing the parent of a folder or bookmark
     * and then saving it, so there really is no need for a full moveFolder()
     * method.  i am leaving this test here for a while just in case we change
     * our minds.
     */
    public void testMoveFolderContents() throws Exception {
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        try {        

            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);

            WeblogBookmarkFolder dest = new WeblogBookmarkFolder(root, "dest", null, testWeblog);
            bmgr.saveFolder(dest);

            // create source folder f1
            WeblogBookmarkFolder f1 = new WeblogBookmarkFolder(root, "f1", null, testWeblog);
            bmgr.saveFolder(f1);

            // create bookmark b1 inside source folder f1
            WeblogBookmark b1 = new WeblogBookmark(
                    f1, "b1", "testbookmark",
                    "http://example.com", "http://example.com/rss",
                    new Integer(1), new Integer(1), "image.gif");
            f1.addBookmark(b1);

            // create folder f2 inside f1
            WeblogBookmarkFolder f2 = new WeblogBookmarkFolder(f1, "f2", null, testWeblog);
            bmgr.saveFolder(f2);

            // create bookmark b2 inside folder f2
            WeblogBookmark b2 = new WeblogBookmark(
                    f2, "b2", "testbookmark",
                    "http://example.com", "http://example.com/rss",
                    new Integer(1), new Integer(1), "image.gif");
            f2.addBookmark(b2);

            // create folder f3 inside folder f2
            WeblogBookmarkFolder f3 = new WeblogBookmarkFolder(f2, "f3", null, testWeblog);
            bmgr.saveFolder(f3);

            // crete bookmark b3 inside folder f3
            WeblogBookmark b3 = new WeblogBookmark(
                    f3, "b3", "testbookmark",
                    "http://example.com", "http://example.com/rss",
                    new Integer(1), new Integer(1), "image.gif");
            f3.addBookmark(b3);

            TestUtils.endSession(true);

            // verify our new tree
            dest = bmgr.getFolder(dest.getId());
            f1 = bmgr.getFolder(f1.getId());
            f2 = bmgr.getFolder(f2.getId());
            f3 = bmgr.getFolder(f3.getId());
            assertEquals(0, dest.getBookmarks().size());
            assertEquals(1, f1.getBookmarks().size());
            assertEquals(1, f2.getBookmarks().size());
            assertEquals(1, f3.getBookmarks().size());
            assertEquals(0, dest.retrieveBookmarks(true).size());
            assertEquals(3, f1.retrieveBookmarks(true).size());

            // test that parent cannot be moved into child
            boolean safe = false;
            try {
                // Move folder into one of it's children
                bmgr.moveFolder(f1, f3);
                TestUtils.endSession(true);
            } catch (WebloggerException e) {
                safe = true;
            }
            assertTrue(safe);

            // move f1 to dest
            f1   = bmgr.getFolder( f1.getId());   //Get managed copy
            dest = bmgr.getFolder( dest.getId()); //Get managed copy
            bmgr.moveFolder(f1, dest);
            TestUtils.endSession(true);

            // after move, verify number of entries in eacch folder
            dest = bmgr.getFolder(dest.getId());
            f1 = bmgr.getFolder(f1.getId());
            assertEquals(3, dest.retrieveBookmarks(true).size());

            // check that paths and child folders are correct
            assertEquals("/dest/f1", f1.getPath());
            assertEquals(1, dest.getFolders().size());
        
            bmgr.removeFolder(f1);
            bmgr.removeFolder(dest);

        } catch(Throwable t) {
            log.error("Exception running test", t);
            throw (Exception) t;
        } finally {
            TestUtils.endSession(true);
        }
    }
    
    
    public void testBookmarkImport() throws Exception {
        
        InputStream fis = this.getClass().getResourceAsStream("/bookmarks.opml");
        getRoller().getBookmarkManager().importBookmarks(
                TestUtils.getManagedWebsite(testWeblog), "ZZZ_imports_ZZZ", fileToString(fis));
        TestUtils.endSession(true);
        
        WeblogBookmarkFolder fd = null;
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        fd = getRoller().getBookmarkManager().getFolder(testWeblog, "ZZZ_imports_ZZZ");
        assertTrue(fd.retrieveBookmarks(true).size() > 0 );
        getRoller().getBookmarkManager().removeFolder(fd);
        TestUtils.endSession(true);
    }
    
    
    private String fileToString( InputStream is ) throws java.io.IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        StringBuffer sb = new StringBuffer();
        while ( (s=br.readLine()) != null ) {
            sb.append( s );
        }
        return sb.toString();
    }
    
}
