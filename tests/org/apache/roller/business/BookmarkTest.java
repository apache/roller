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

package org.apache.roller.business;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.TestUtils;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Utilities;


/**
 * Test Roller Bookmark Management.
 */
public class BookmarkTest extends TestCase {
    
    public static Log log = LogFactory.getLog(BookmarkTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    
    
    public BookmarkTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(BookmarkTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
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
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    public Roller getRoller() {
        return RollerFactory.getRoller();
    }
    
    
    public void testAddBookmarkToFolder() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        FolderData folder = null;
        BookmarkData bookmark1 = null, bookmark2 = null;
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        folder = new FolderData();
        folder.setName("TestFolder1");
        folder.setDescription("created by testAddBookmarkToFolder()");
        folder.setWebsite(testWeblog);
        folder.setParent(root);
        bmgr.saveFolder(folder);
        
        // Add bookmark by adding to folder
        bookmark1 = new BookmarkData(
                folder,
                "TestBookmark1",
                "created by testAddBookmarkToFolder()",
                "http://www.example.com",
                "http://www.example.com/rss.xml",
                new Integer(1),
                new Integer(12),
                "test.jpg");
        folder.addBookmark(bookmark1);
        
        // Add another bookmark
        bookmark2 = new BookmarkData(
                folder,
                "TestBookmark2",
                "created by testAddBookmarkToFolder()",
                "http://www.example.com",
                "http://www.example.com/rss.xml",
                new Integer(1),
                new Integer(12),
                "test.jpf");
        folder.addBookmark(bookmark2);
        
        TestUtils.endSession(true);
        
        FolderData testFolder = null;
        BookmarkData bookmarkb = null, bookmarka = null;
        
        // See that two bookmarks were stored
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(2, testFolder.getBookmarks().size());
        bookmarka = (BookmarkData)testFolder.getBookmarks().iterator().next();
        bookmarkb = (BookmarkData)testFolder.getBookmarks().iterator().next();
        // Remove one bookmark
        testFolder.removeBookmark(bookmarka);
        bmgr.removeBookmark(bookmarka);
        TestUtils.endSession(true);
        
        // Folder should now contain one bookmark
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(1, testFolder.getBookmarks().size());
        TestUtils.endSession(true);
        
        // Remove folder
        testFolder = bmgr.getFolder(folder.getId());
        bmgr.removeFolder(testFolder);
        TestUtils.endSession(true);
        
        // Folder and one remaining bookmark should be gone
        assertNull( bmgr.getBookmark(bookmarkb.getId()) );
        assertNull( bmgr.getFolder(folder.getId()) );
    }
    
    
    public void testBookmarkImport() throws Exception {
        importBookmarks("/bookmarks.opml");
    }
    
    
    public void importBookmarks(String fileName) throws Exception {
        
        InputStream fis = this.getClass().getResourceAsStream(fileName);
        getRoller().getBookmarkManager().importBookmarks(
                testWeblog, "ZZZ_imports_ZZZ", fileToString(fis));
        TestUtils.endSession(true);
        
        FolderData fd = null;
        
        fd = getRoller().getBookmarkManager().getFolder(testWeblog, "ZZZ_imports_ZZZ");
        assertTrue("no bookmarks found", fd.retrieveBookmarks(true).size() > 0 );
        getRoller().getBookmarkManager().removeFolder(fd);
        TestUtils.endSession(true);
    }
    
    
    public String fileToString( InputStream is ) throws java.io.IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String s = null;
        StringBuffer sb = new StringBuffer();
        while ( (s=br.readLine()) != null ) {
            sb.append( s );
        }
        return sb.toString();
    }
    
    
    /**
     * Creates folder tree like this:
     *    root/
     *       dest/
     *       f1/
     *          b1
     *          f2/
     *             f3/
     */
    public void testMoveFolderContents() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData dest = new FolderData();
        dest.setName("dest");
        dest.setParent(root);
        dest.setWebsite(testWeblog);
        bmgr.saveFolder(dest);
        
        // create source folder f1
        FolderData f1 = new FolderData();
        f1.setName("f1");
        f1.setParent(root);
        f1.setWebsite(testWeblog);
        bmgr.saveFolder(f1);
        
        // create bookmark b1 inside source folder f1
        BookmarkData b1 = new BookmarkData(
                f1, "b1", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        f1.addBookmark(b1);
        
        // create folder f2 inside f1
        FolderData f2 = new FolderData();
        f2.setName("f2");
        f2.setParent(f1);
        f2.setWebsite(testWeblog);
        bmgr.saveFolder(f2);
        
        // create bookmark b2 inside folder f2
        BookmarkData b2 = new BookmarkData(
                f2, "b2", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        f2.addBookmark(b2);
        
        // create folder f3 inside folder f2
        FolderData f3 = new FolderData();
        f3.setName("f3");
        f3.setParent(f2);
        f3.setWebsite(testWeblog);
        bmgr.saveFolder(f3);
        
        // crete bookmark b3 inside folder f3
        BookmarkData b3 = new BookmarkData(
                f3, "b3", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        f3.addBookmark(b3);
        
        TestUtils.endSession(true);
        
        // test that parent cannot be moved into child
        boolean safe = false;
        try {
            
            // Move folder into one of it's children
            f1 = bmgr.getFolder(f1.getId());
            f3 = bmgr.getFolder(f3.getId());
            bmgr.moveFolderContents(f1, f3);
            //f3.save();
            //f1.save();
            TestUtils.endSession(true);
        } catch (RollerException e) {
            safe = true;
        }
        assertTrue(safe);
        
        // verify number of entries in each folder
        dest = bmgr.getFolder(dest.getId());
        f1 = bmgr.getFolder(f1.getId());
        assertEquals(0, dest.retrieveBookmarks(true).size());
        assertEquals(0, dest.retrieveBookmarks(false).size());
        assertEquals(1, f1.retrieveBookmarks(false).size());
        
        List f1list = f1.retrieveBookmarks(true);
        assertEquals(3, f1list.size());
        
        // move contents of source category c1 to destination catetory dest
        f1.moveContents(dest);
        bmgr.saveFolder(f1);
        bmgr.saveFolder(dest);
        TestUtils.endSession(true);
        
        // after move, verify number of entries in eacch folder
        dest = bmgr.getFolder(dest.getId());
        f1 = bmgr.getFolder(f1.getId());
        assertEquals(3, dest.retrieveBookmarks(true).size());
        assertEquals(3, dest.retrieveBookmarks(false).size());
        assertEquals(0, f1.retrieveBookmarks(true).size());
        assertEquals(0, f1.retrieveBookmarks(false).size());
    }
    
    
    /** Test bookmark folder paths. */
    public void testPaths() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        try {
            FolderData root = bmgr.getRootFolder(testWeblog);
            
            FolderData f1 = new FolderData();
            f1.setName("f1");
            f1.setParent(root);
            f1.setWebsite(testWeblog);
            bmgr.saveFolder(f1);
            
            FolderData f2 = new FolderData();
            f2.setName("f2");
            f2.setParent(f1);
            f2.setWebsite(testWeblog);
            bmgr.saveFolder(f2);
            
            FolderData f3 = new FolderData();
            f3.setName("f3");
            f3.setParent(f2);
            f3.setWebsite(testWeblog);
            bmgr.saveFolder(f3);
            
            TestUtils.endSession(true);
        } catch (RollerException e) {
            TestUtils.endSession(true);
            log.error(e);
        }
        
        try {
            
            assertEquals("f1",bmgr.getFolderByPath(testWeblog, null, "/f1").getName());
            assertEquals("f2",bmgr.getFolderByPath(testWeblog, null, "/f1/f2").getName());
            assertEquals("f3",bmgr.getFolderByPath(testWeblog, null, "/f1/f2/f3").getName());
            
            FolderData f3 = bmgr.getFolderByPath(testWeblog, null, "/f1/f2/f3");
            String pathString = bmgr.getPath(f3);
            String[] pathArray = Utilities.stringToStringArray(pathString,"/");
            assertEquals("f1", pathArray[0]);
            assertEquals("f2", pathArray[1]);
            assertEquals("f3", pathArray[2]);
            
        } catch (RollerException e) {
            TestUtils.endSession(true);
            log.error(e);
        }
    }
    
    
    /** Ensure that duplicate folder name will throw RollerException */
    public void testUniquenessOfFolderNames() throws Exception {
        
        boolean exception = false;
        FolderData f3 = null;
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        try {
            FolderData root = bmgr.getRootFolder(testWeblog);
            
            FolderData f1 = new FolderData();
            f1.setName("f1");
            f1.setParent(root);
            f1.setWebsite(testWeblog);
            bmgr.saveFolder(f1);
            
            // first child folder
            FolderData f2 = new FolderData();
            f2.setName("f2");
            f2.setParent(f1);
            f2.setWebsite(testWeblog);
            bmgr.saveFolder(f2);
            
            TestUtils.endSession(true);
            
            // child folder with same name as first
            f3 = new FolderData();
            f3.setName("f2");
            f3.setParent(f1);
            f3.setWebsite(testWeblog);
            bmgr.saveFolder(f3);
            
            TestUtils.endSession(true);
        } catch (RollerException e) {
            exception = true;
        }
        
        assertTrue(exception);
    }
    
}
