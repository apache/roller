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
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
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
    
    
    /**
     * Test add/modify/remove of folders (no bookmarks).
     */
    public void testFolderCRUD() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        // start out with no folders and no bookmarks
        assertEquals(0, root.getFolders().size());
        assertEquals(0, root.getBookmarks().size());
        
        // add a folder
        FolderData folder = new FolderData(root, "TestFolder1", null, testWeblog);
        bmgr.saveFolder(folder);
        TestUtils.endSession(true);
        
        // check that folder was saved
        root = bmgr.getRootFolder(testWeblog);
        folder = (FolderData) root.getFolders().iterator().next();
        assertEquals("TestFolder1", folder.getName());
        
        // modify folder
        folder.setName("folderTest1");
        bmgr.saveFolder(folder);
        TestUtils.endSession(true);
        
        // check that folder was saved
        root = bmgr.getRootFolder(testWeblog);
        folder = (FolderData) root.getFolders().iterator().next();
        assertEquals("folderTest1", folder.getName());
        
        // add a subfolder
        FolderData subfolder = new FolderData(folder, "subfolderTest1", null, testWeblog);
        bmgr.saveFolder(subfolder);
        TestUtils.endSession(true);
        
        // check that subfolder was saved and we can navigate to it
        root = bmgr.getRootFolder(testWeblog);
        assertEquals(1, root.getFolders().size());
        folder = (FolderData) root.getFolders().iterator().next();
        assertEquals("folderTest1", folder.getName());
        assertEquals(1, folder.getFolders().size());
        subfolder = (FolderData) folder.getFolders().iterator().next();
        assertEquals("subfolderTest1", subfolder.getName());
        
        // test remove folder, which should cascade to subfolders
        bmgr.removeFolder(folder);
        TestUtils.endSession(true);
        
        // check that folders were removed
        root = bmgr.getRootFolder(testWeblog);
        assertEquals(0, root.getFolders().size());
    }
    
    
    /** 
     * Ensure that duplicate folder name will throw RollerException 
     */
    public void testUniquenessOfFolderNames() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData f1 = new FolderData(root, "f1", null, testWeblog);
        bmgr.saveFolder(f1);
        
        // first child folder
        FolderData f2 = new FolderData(f1, "f2", null, testWeblog);
        bmgr.saveFolder(f2);
        
        TestUtils.endSession(true);
        
        // need to requery for folder since session was closed
        f1 = bmgr.getFolder(f1.getId());
        
        boolean exception = false;
        try {
            // child folder with same name as first
            FolderData f3 = new FolderData(f1, "f2", null, testWeblog);
            bmgr.saveFolder(f3);
            TestUtils.endSession(true);
        } catch (RollerException e) {
            exception = true;
        }
        
        assertTrue(exception);
    }
    
    
    /**
     * Test all folder lookup methods.
     */
    public void testFolderLookups() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData f1 = new FolderData(root, "f1", null, testWeblog);
        bmgr.saveFolder(f1);
        FolderData f2 = new FolderData(f1, "f2", null, testWeblog);
        bmgr.saveFolder(f2);
        FolderData f3 = new FolderData(root, "f3", null, testWeblog);
        bmgr.saveFolder(f3);
        
        TestUtils.endSession(true);
        
        // test lookup by id
        FolderData testFolder = bmgr.getFolder(f1.getId());
        assertNotNull(testFolder);
        assertEquals("/f1", testFolder.getPath());
        
        // test lookup root folder
        testFolder = null;
        testFolder = bmgr.getRootFolder(testWeblog);
        assertNotNull(testFolder);
        assertEquals("/", testFolder.getPath());
        assertNull(testFolder.getParent());
        
        // test lookup by path
        testFolder = null;
        testFolder = bmgr.getFolder(testWeblog, "/f1/f2");
        assertNotNull(testFolder);
        assertEquals("/f1/f2", testFolder.getPath());
        assertEquals("f2", testFolder.getName());
        assertEquals("f1", testFolder.getParent().getName());
        
        // test lookup of all folders for weblog
        List allFolders = bmgr.getAllFolders(testWeblog);
        assertNotNull(allFolders);
        assertEquals(4, allFolders.size());
    }
    
    
    /**
     * Test WeblogCategoryData.equals() method.
     */
    public void testFolderEquality() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData testFolder = new FolderData(null, "root", "root", testWeblog);
        assertTrue(root.equals(testFolder));
        
        testFolder = new FolderData(root, "root", "root", testWeblog);
        testFolder.setId(root.getId());
        assertFalse(root.equals(testFolder));
    }
    
    
    /** 
     * Test bookmark folder paths. 
     */
    public void testFolderPaths() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData f1 = new FolderData(root, "f1", null, testWeblog);
        bmgr.saveFolder(f1);
        FolderData f2 = new FolderData(f1, "f2", null, testWeblog);
        bmgr.saveFolder(f2);
        FolderData f3 = new FolderData(f2, "f3", null, testWeblog);
        bmgr.saveFolder(f3);
        
        TestUtils.endSession(true);
        
        assertEquals("f1",bmgr.getFolder(testWeblog, "/f1").getName());
        assertEquals("f2",bmgr.getFolder(testWeblog, "/f1/f2").getName());
        assertEquals("f3",bmgr.getFolder(testWeblog, "/f1/f2/f3").getName());
        
        f3 = bmgr.getFolder(testWeblog, "/f1/f2/f3");
        String pathString = f3.getPath();
        String[] pathArray = Utilities.stringToStringArray(pathString,"/");
        assertEquals("f1", pathArray[0]);
        assertEquals("f2", pathArray[1]);
        assertEquals("f3", pathArray[2]);
    }
    
    
    public void testBookmarkCRUD() throws Exception {
        
        BookmarkManager bmgr = getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData folder = new FolderData(root, "TestFolder2", null, testWeblog);
        bmgr.saveFolder(folder);
        TestUtils.endSession(true);
        
        // query for folder again since session ended
        folder = bmgr.getFolder(folder.getId());
        
        // Add bookmark by adding to folder
        BookmarkData bookmark1 = new BookmarkData(
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
        BookmarkData bookmark2 = new BookmarkData(
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
        
        
        FolderData testFolder = null;
        BookmarkData bookmarkb = null, bookmarka = null;
        
        // See that two bookmarks were stored
        testFolder = bmgr.getFolder(folder.getId());
        assertEquals(2, testFolder.getBookmarks().size());
        bookmarka = (BookmarkData)testFolder.getBookmarks().iterator().next();
        bookmarkb = (BookmarkData)testFolder.getBookmarks().iterator().next();
        
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
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        // add some folders
        FolderData f1 = new FolderData(root, "f1", null, testWeblog);
        bmgr.saveFolder(f1);
        FolderData f2 = new FolderData(f1, "f2", null, testWeblog);
        bmgr.saveFolder(f2);
        FolderData f3 = new FolderData(root, "f3", null, testWeblog);
        bmgr.saveFolder(f3);
        
        // add some bookmarks
        BookmarkData b1 = new BookmarkData(
                f1, "b1", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        bmgr.saveBookmark(b1);
        BookmarkData b2 = new BookmarkData(
                f1, "b2", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        bmgr.saveBookmark(b2);
        BookmarkData b3 = new BookmarkData(
                f2, "b3", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        bmgr.saveBookmark(b3);
        
        TestUtils.endSession(true);
        
        // test lookup by id
        BookmarkData testBookmark = bmgr.getBookmark(b1.getId());
        assertNotNull(testBookmark);
        assertEquals("b1", testBookmark.getName());
        
        // test lookup of all bookmarks in single folder
        FolderData testFolder = bmgr.getFolder(f1.getId());
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
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        FolderData dest = new FolderData(root, "dest", null, testWeblog);
        bmgr.saveFolder(dest);
        
        // create source folder f1
        FolderData f1 = new FolderData(root, "f1", null, testWeblog);
        bmgr.saveFolder(f1);
        
        // create bookmark b1 inside source folder f1
        BookmarkData b1 = new BookmarkData(
                f1, "b1", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        f1.addBookmark(b1);
        
        // create folder f2 inside f1
        FolderData f2 = new FolderData(f1, "f2", null, testWeblog);
        bmgr.saveFolder(f2);
        
        // create bookmark b2 inside folder f2
        BookmarkData b2 = new BookmarkData(
                f2, "b2", "testbookmark",
                "http://example.com", "http://example.com/rss",
                new Integer(1), new Integer(1), "image.gif");
        f2.addBookmark(b2);
        
        // create folder f3 inside folder f2
        FolderData f3 = new FolderData(f2, "f3", null, testWeblog);
        bmgr.saveFolder(f3);
        
        // crete bookmark b3 inside folder f3
        BookmarkData b3 = new BookmarkData(
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
        } catch (RollerException e) {
            safe = true;
        }
        assertTrue(safe);
        
        // move f1 to dest
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
        TestUtils.endSession(true);
    }
    
    
    public void testBookmarkImport() throws Exception {
        
        InputStream fis = this.getClass().getResourceAsStream("/bookmarks.opml");
        getRoller().getBookmarkManager().importBookmarks(
                testWeblog, "ZZZ_imports_ZZZ", fileToString(fis));
        TestUtils.endSession(true);
        
        FolderData fd = null;
        
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
