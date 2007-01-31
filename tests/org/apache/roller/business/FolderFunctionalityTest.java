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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.TestUtils;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Utilities;


/**
 * Test folder business functions and lookups.
 */
public class FolderFunctionalityTest extends TestCase {
    
    public static Log log = LogFactory.getLog(FolderFunctionalityTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    FolderData f1 = null;
    FolderData f2 = null;
    FolderData f3 = null;
    FolderData testFolder = null;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        log.info("BEGIN");
        
        try {
            testUser = TestUtils.setupUser("folderFuncTestUser");
            testWeblog = TestUtils.setupWeblog("folderFuncTestWeblog", testUser);
            
            // setup a category tree to use for testing
            f1 = TestUtils.setupFolder(testWeblog, "folderFuncTest-f1", null);
            f2 = TestUtils.setupFolder(testWeblog, "folderFuncTest-f2", f1);
            f3 = TestUtils.setupFolder(testWeblog, "folderFuncTest-f3", f2);
            
            // a simple test folder at the root level
            testFolder = TestUtils.setupFolder(testWeblog, "folderFuncTest-testFolder", null);
            
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
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test that we can walk a folder tree.
     */
    public void testWalkFolderTree() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        // start at root
        FolderData root = bmgr.getRootFolder(TestUtils.getManagedWebsite(testWeblog));
        
        // walk first level
        Set folders = root.getFolders();
        assertEquals(2, folders.size());
        assertTrue(folders.contains(testFolder));
        
        // find cat1
        FolderData folder = null;
        for(Iterator it = folders.iterator(); it.hasNext(); ) {
            folder = (FolderData) it.next();
            if(folder.getName().equals(f1.getName())) {
                break;
            }
        }
        
        // walk second level
        folders = folder.getFolders();
        assertEquals(1, folders.size());
        assertTrue(folders.contains(f2));
        
        // find cat2
        folder = (FolderData) folders.iterator().next();
        
        // walk third level
        folders = folder.getFolders();
        assertEquals(1, folders.size());
        assertTrue(folders.contains(f3));
        
        // find cat3
        folder = (FolderData) folders.iterator().next();
        
        // make sure this is the end of the tree
        folders = folder.getFolders();
        assertEquals(0, folders.size());
        
        log.info("END");
    }
    
    
    /**
     * Test the hasFolder() method on FolderData.
     */
    public void testHasFolder() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        // check that root has folder
        assertTrue(root.hasFolder(testFolder.getName()));
        
        log.info("END");
    }
    
    
    /** 
     * Ensure that duplicate folder name will throw RollerException 
     */
    public void testUniquenessOfFolderNames() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        FolderData root = bmgr.getRootFolder(testWeblog);
        
        boolean exception = false;
        try {
            // child folder with same name as first
            FolderData dupeFolder = new FolderData(root, testFolder.getName(), null, TestUtils.getManagedWebsite(testWeblog));
            bmgr.saveFolder(dupeFolder);
            TestUtils.endSession(true);
        } catch (RollerException e) {
            exception = true;
        }
        
        assertTrue(exception);
        
        log.info("END");
    }
    
    
    /**
     * Test folder lookup by id.
     */
    public void testLookupFolderById() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        // test lookup by id
        FolderData testFolder = bmgr.getFolder(f1.getId());
        assertNotNull(testFolder);
        assertEquals(f1, testFolder);
        
        log.info("END");
    }
    
    
    /**
     * Test folder lookup by id.
     */
    public void testLookupFolderByPath() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        FolderData folder = bmgr.getFolder(testWeblog, "/folderFuncTest-f1");
        assertNotNull(folder);
        assertEquals(f1, folder);
        
        folder = bmgr.getFolder(testWeblog, "/folderFuncTest-f1/folderFuncTest-f2/folderFuncTest-f3");
        assertNotNull(folder);
        assertEquals(f3, folder);
        
        // test lazy lookup, with no slashes
        folder = bmgr.getFolder(testWeblog, "folderFuncTest-f1");
        assertNotNull(folder);
        assertEquals(f1, folder);
        
        // if no path is specified then we should get root folder
        folder = bmgr.getFolder(testWeblog, null);
        assertNotNull(folder);
        assertEquals("/", folder.getPath());
        
        log.info("END");
    }
    
    
    public void testLookupAllFoldersByWeblog() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        // get all folders, including root
        List allFolders = bmgr.getAllFolders(testWeblog);
        assertNotNull(allFolders);
        assertEquals(5, allFolders.size());
        
        log.info("END");
    }
    
}
