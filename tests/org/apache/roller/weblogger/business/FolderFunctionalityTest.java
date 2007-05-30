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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Test folder business functions and lookups.
 */
public class FolderFunctionalityTest extends TestCase {
    
    public static Log log = LogFactory.getLog(FolderFunctionalityTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    WeblogBookmarkFolder f1 = null;
    WeblogBookmarkFolder f2 = null;
    WeblogBookmarkFolder f3 = null;
    WeblogBookmarkFolder testFolder = null;
    
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
        WeblogBookmarkFolder root = bmgr.getRootFolder(TestUtils.getManagedWebsite(testWeblog));
        
        // walk first level
        Set folders = root.getFolders();
        assertEquals(2, folders.size());
        assertTrue(folders.contains(testFolder));
        
        // find cat1
        WeblogBookmarkFolder folder = null;
        for(Iterator it = folders.iterator(); it.hasNext(); ) {
            folder = (WeblogBookmarkFolder) it.next();
            if(folder.getName().equals(f1.getName())) {
                break;
            }
        }
        
        // walk second level
        folders = folder.getFolders();
        assertEquals(1, folders.size());
        assertTrue(folders.contains(f2));
        
        // find cat2
        folder = (WeblogBookmarkFolder) folders.iterator().next();
        
        // walk third level
        folders = folder.getFolders();
        assertEquals(1, folders.size());
        assertTrue(folders.contains(f3));
        
        // find cat3
        folder = (WeblogBookmarkFolder) folders.iterator().next();
        
        // make sure this is the end of the tree
        folders = folder.getFolders();
        assertEquals(0, folders.size());
        
        log.info("END");
    }
    
    
    /**
     * Test the hasFolder() method on WeblogBookmarkFolder.
     */
    public void testHasFolder() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        // check that root has folder
        assertTrue(root.hasFolder(testFolder.getName()));
        
        log.info("END");
    }
    
    
    /** 
     * Ensure that duplicate folder name will throw RollerException 
     */
    public void testUniquenessOfFolderNames() throws Exception {
        
        log.info("BEGIN");
        try {
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();

            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);

            boolean exception = false;
            try {
                // child folder with same name as first
                WeblogBookmarkFolder dupeFolder = new WeblogBookmarkFolder(root, testFolder.getName(), null, testWeblog);
                bmgr.saveFolder(dupeFolder);
                TestUtils.endSession(true);
            } catch (Throwable e) {
                exception = true;
            }

            assertTrue(exception);
            
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw); 
            t.printStackTrace(pw);
            log.info(sw.toString());
        }        
        TestUtils.endSession(true);

        log.info("END");
    }
    
    
    /**
     * Test folder lookup by id.
     */
    public void testLookupFolderById() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        // test lookup by id
        WeblogBookmarkFolder testFolder = bmgr.getFolder(f1.getId());
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
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder folder = bmgr.getFolder(testWeblog, "/folderFuncTest-f1");
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
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List allFolders = bmgr.getAllFolders(testWeblog);
        assertNotNull(allFolders);
        assertEquals(5, allFolders.size());
        
        log.info("END");
    }
    
}
