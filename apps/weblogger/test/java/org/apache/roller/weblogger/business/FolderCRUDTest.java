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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test basic folder operations.
 */
public class FolderCRUDTest extends TestCase {
    
    public static Log log = LogFactory.getLog(FolderCRUDTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        log.info("BEGIN");
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("folderCRUDTestUser");
            testWeblog = TestUtils.setupWeblog("folderCRUDTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        log.info("END");
    }
    
    public void tearDown() throws Exception {
        
        log.info("BEGIN");
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test WeblogBookmarkFolder.equals() method.
     */
    public void testFolderEquality() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        WeblogBookmarkFolder testFolder = new WeblogBookmarkFolder(null, "root", "root", TestUtils.getManagedWebsite(testWeblog));
        assertTrue(root.equals(testFolder));
        
        testFolder = new WeblogBookmarkFolder(root, "root", "root", TestUtils.getManagedWebsite(testWeblog));
        assertFalse(root.equals(testFolder));
        
        log.info("END");
    }
    
    
    /**
     * Test add/modify/remove of folders.
     */
    public void testBasicCRUD() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        // start out with no folders and no bookmarks
        assertEquals(0, root.getFolders().size());
        assertEquals(0, root.getBookmarks().size());
        
        // add a folder
        WeblogBookmarkFolder newFolder = new WeblogBookmarkFolder(root, "folderBasicCRUD", null, TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(newFolder);
        TestUtils.endSession(true);
        
        // check that folder was saved
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = bmgr.getRootFolder(testWeblog);
        assertEquals(1, root.getFolders().size());
        WeblogBookmarkFolder folder = (WeblogBookmarkFolder) root.getFolders().iterator().next();
        assertEquals(newFolder, folder);
        
        // modify folder
        folder.setName("folderTest1");
        bmgr.saveFolder(folder);
        TestUtils.endSession(true);
        
        // check that folder was saved
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = bmgr.getRootFolder(testWeblog);
        folder = (WeblogBookmarkFolder) root.getFolders().iterator().next();
        assertEquals("folderTest1", folder.getName());
        
        // test remove folder
        bmgr.removeFolder(folder);
        TestUtils.endSession(true);
        
        // make sure folder was removed
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = bmgr.getRootFolder(testWeblog);
        assertEquals(0, root.getFolders().size());
        folder = bmgr.getFolder(newFolder.getId());
        assertNull(folder);
        
        log.info("END");
    }
    
    
    /**
     * Make sure that deleting a folder deletes all child folders.
     */
    public void testFolderCascadingDelete() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        // add a small tree /fold1/fold2
        WeblogBookmarkFolder fold1 = new WeblogBookmarkFolder(root, "fold1", null, testWeblog);
        root.addFolder(fold1);
        bmgr.saveFolder(fold1);
        WeblogBookmarkFolder fold2 = new WeblogBookmarkFolder(fold1, "fold2", null, testWeblog);
        fold1.addFolder(fold2);
        bmgr.saveFolder(fold2);
        TestUtils.endSession(true);
        
        // check that tree can be navigated
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = bmgr.getRootFolder(testWeblog);
        assertEquals(1, root.getFolders().size());
        fold1 = (WeblogBookmarkFolder) root.getFolders().iterator().next();
        assertEquals("fold1", fold1.getName());
        assertEquals(1, fold1.getFolders().size());
        fold2 = (WeblogBookmarkFolder) fold1.getFolders().iterator().next();
        assertEquals("fold2", fold2.getName());
        
        // now delete folder and subfolders should be deleted by cascade
        bmgr.removeFolder(fold1);
        TestUtils.endSession(true);
        
        // verify cascading delete succeeded
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = bmgr.getRootFolder(testWeblog);
        assertEquals(0, root.getFolders().size());
        assertNull(bmgr.getFolder(testWeblog, "/fold1/fold2"));
        
        log.info("END");
    }
    
}
