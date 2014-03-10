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
import java.util.List;
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
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("folderFuncTestUser");
            testWeblog = TestUtils.setupWeblog("folderFuncTestWeblog", testUser);
            
            // setup a category tree to use for testing
            f1 = TestUtils.setupFolder(testWeblog, "folderFuncTest-f1");
            f2 = TestUtils.setupFolder(testWeblog, "folderFuncTest-f2");
            f3 = TestUtils.setupFolder(testWeblog, "folderFuncTest-f3");
            
            // a simple test folder at the root level
            testFolder = TestUtils.setupFolder(testWeblog, "folderFuncTest-testFolder");
            
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
     * Test the hasBookmarkFolder() method on Weblog.
     */
    public void testHasFolder() throws Exception {
        
        log.info("BEGIN");
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // check that weblog has folder
        assertTrue(testWeblog.hasBookmarkFolder(testFolder.getName()));
        
        log.info("END");
    }
    
    
    /** 
     * Ensure that duplicate folder name will throw WebloggerException 
     */
    public void testUniquenessOfFolderNames() throws Exception {
        
        log.info("BEGIN");
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            testWeblog = TestUtils.getManagedWebsite(testWeblog);

            boolean exception = false;
            try {
                // child folder with same name as first
                WeblogBookmarkFolder dupeFolder = new WeblogBookmarkFolder(testFolder.getName(), testWeblog);
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
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        // test lookup by id
        WeblogBookmarkFolder testFolder = bmgr.getFolder(f1.getId());
        assertNotNull(testFolder);
        assertEquals(f1, testFolder);
        
        log.info("END");
    }
    
    
    /**
     * Test folder lookup by id.
     */
    public void testLookupFolderByName() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder folder = bmgr.getFolder(testWeblog, "folderFuncTest-f1");
        assertNotNull(folder);
        assertEquals(f1, folder);
        
        folder = bmgr.getFolder(testWeblog, "folderFuncTest-f3");
        assertNotNull(folder);
        assertEquals(f3, folder);
        
        // test to check that default folder is accessible
        folder = bmgr.getDefaultFolder(testWeblog);
        assertNotNull(folder);
        assertEquals("default", folder.getName());
        
        log.info("END");
    }
    
    
    public void testLookupAllFoldersByWeblog() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        // get all folders, including root
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List allFolders = bmgr.getAllFolders(testWeblog);
        assertNotNull(allFolders);
        assertEquals(5, allFolders.size());
        
        log.info("END");
    }
    
}
