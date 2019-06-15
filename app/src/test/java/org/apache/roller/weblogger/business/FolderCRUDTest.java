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
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test basic folder operations.
 */
public class FolderCRUDTest  {
    
    public static Log log = LogFactory.getLog(FolderCRUDTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
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

    @AfterEach
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
     * Test add/modify/remove of folders.
     */
    @Test
    public void testBasicCRUD() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getDefaultFolder(testWeblog);
        
        // start out with just default folder and no bookmarks
        assertEquals(1, testWeblog.getBookmarkFolders().size());
        assertEquals(0, root.getBookmarks().size());
        
        // add a folder
        WeblogBookmarkFolder newFolder = new WeblogBookmarkFolder("folderBasicCRUD", TestUtils.getManagedWebsite(testWeblog));
        bmgr.saveFolder(newFolder);
        TestUtils.endSession(true);
        
        // check that folder was saved
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertEquals(2, testWeblog.getBookmarkFolders().size());
        WeblogBookmarkFolder folder = testWeblog.getBookmarkFolders().get(1);
        assertEquals(newFolder, folder);
        
        // modify folder
        folder.setName("folderTest1");
        bmgr.saveFolder(folder);
        TestUtils.endSession(true);
        
        // check that folder was saved
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        folder = testWeblog.getBookmarkFolders().get(1);
        assertEquals("folderTest1", folder.getName());
        
        // test remove folder
        bmgr.removeFolder(folder);
        TestUtils.endSession(true);
        
        // make sure folder was removed
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertEquals(1, testWeblog.getBookmarkFolders().size());
        folder = bmgr.getFolder(newFolder.getId());
        assertNull(folder);
        
        log.info("END");
    }

}
