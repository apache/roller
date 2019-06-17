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
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test Weblogger Bookmark Management.
 */
public class BookmarkTest  {
    
    public static Log log = LogFactory.getLog(BookmarkTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
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

    @AfterEach
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
    

    @Test
    public void testBookmarkCRUD() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getDefaultFolder(testWeblog);
        
        WeblogBookmarkFolder folder = new WeblogBookmarkFolder("TestFolder2", TestUtils.getManagedWebsite(testWeblog));
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
                "test.jpg");
        folder.addBookmark(bookmark1);
        
        // Add another bookmark
        WeblogBookmark bookmark2 = new WeblogBookmark(
                folder,
                "TestBookmark2",
                "created by testBookmarkCRUD()",
                "http://www.example.com",
                "http://www.example.com/rss.xml",
                "test.jpf");
        folder.addBookmark(bookmark2);
        
        TestUtils.endSession(true);
        
        WeblogBookmarkFolder testFolder = null;
        WeblogBookmark bookmarkb = null, bookmarka = null;
        
        // See that two bookmarks were stored
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(2, testFolder.getBookmarks().size());
        Iterator<WeblogBookmark> iter = testFolder.getBookmarks().iterator();
        bookmarka = iter.next();
        bookmarkb = iter.next();
        
        // Remove one bookmark
        bmgr.removeBookmark(bookmarka);        
        bmgr.removeBookmark(bookmarkb);        
        bmgr.saveFolder(testFolder);
        TestUtils.endSession(true);
                
        // Folder should now contain one bookmark
        assertNull(bmgr.getBookmark(bookmarka.getId()));
        assertNull(bmgr.getBookmark(bookmarkb.getId()));
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(0, testFolder.getBookmarks().size());
        
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
    @Test
    public void _testBookmarkLookups() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getDefaultFolder(testWeblog);
        
        // add some folders
        WeblogBookmarkFolder f1 = new WeblogBookmarkFolder("f1", TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(f1);
        WeblogBookmarkFolder f2 = new WeblogBookmarkFolder("f2", TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(f2);
        WeblogBookmarkFolder f3 = new WeblogBookmarkFolder("f3", TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(f3);
        
        TestUtils.endSession(true);
        
        f1 = bmgr.getFolder(f1.getId());              
        f2 = bmgr.getFolder(f2.getId());              

        // add some bookmarks
        WeblogBookmark b1 = new WeblogBookmark(
                f1, "b1", "testbookmark",
                "http://example.com", "http://example.com/rss",
                "image.gif");
        bmgr.saveBookmark(b1);
        WeblogBookmark b2 = new WeblogBookmark(
                f1, "b2", "testbookmark",
                "http://example.com", "http://example.com/rss",
                "image.gif");
        bmgr.saveBookmark(b2);
        WeblogBookmark b3 = new WeblogBookmark(
                f2, "b3", "testbookmark",
                "http://example.com", "http://example.com/rss",
                "image.gif");
        bmgr.saveBookmark(b3);
        
        TestUtils.endSession(true);
        
        // test lookup by id
        WeblogBookmark testBookmark = bmgr.getBookmark(b1.getId());
        assertNotNull(testBookmark);
        assertEquals("b1", testBookmark.getName());
        
        // test lookup of all bookmarks in single folder
        WeblogBookmarkFolder testFolder = bmgr.getFolder(f1.getId());
        List allBookmarks = bmgr.getBookmarks(testFolder);
        assertNotNull(allBookmarks);
        assertEquals(2, allBookmarks.size());
        
        // getBookmarks(folder, false) should also match folder.getBookmarks()
        assertEquals(allBookmarks.size(), testFolder.getBookmarks().size());
        
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
            WeblogBookmarkFolder root = bmgr.getDefaultFolder(testWeblog);

            WeblogBookmarkFolder dest = new WeblogBookmarkFolder("dest", testWeblog);
            bmgr.saveFolder(dest);

            // create source folder f1
            WeblogBookmarkFolder f1 = new WeblogBookmarkFolder("f1", testWeblog);
            bmgr.saveFolder(f1);

            // create bookmark b1 inside source folder f1
            WeblogBookmark b1 = new WeblogBookmark(
                    f1, "b1", "testbookmark",
                    "http://example.com", "http://example.com/rss",
                    "image.gif");
            f1.addBookmark(b1);

            // create folder f2 inside f1
            WeblogBookmarkFolder f2 = new WeblogBookmarkFolder("f2", testWeblog);
            bmgr.saveFolder(f2);

            // create bookmark b2 inside folder f2
            WeblogBookmark b2 = new WeblogBookmark(
                    f2, "b2", "testbookmark",
                    "http://example.com", "http://example.com/rss",
                    "image.gif");
            f2.addBookmark(b2);

            // create folder f3 inside folder f2
            WeblogBookmarkFolder f3 = new WeblogBookmarkFolder("f3", testWeblog);
            bmgr.saveFolder(f3);

            // crete bookmark b3 inside folder f3
            WeblogBookmark b3 = new WeblogBookmark(
                    f3, "b3", "testbookmark",
                    "http://example.com", "http://example.com/rss",
                    "image.gif");
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
            assertEquals(0, dest.retrieveBookmarks().size());
            assertEquals(3, f1.retrieveBookmarks().size());

            // check that paths and child folders are correct
            assertEquals("f1", f1.getName());
            assertEquals(1, dest.getWeblog().getBookmarkFolders().size());
        
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
        assertTrue(fd.retrieveBookmarks().size() > 0 );
        getRoller().getBookmarkManager().removeFolder(fd);
        TestUtils.endSession(true);
    }
    
    
    private String fileToString( InputStream is ) throws java.io.IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        StringBuilder sb = new StringBuilder();
        while ( (s=br.readLine()) != null ) {
            sb.append( s );
        }
        return sb.toString();
    }
    
}
