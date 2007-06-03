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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test deleting of Folder parent objects to test cascading deletes.
 */
public class FolderParentDeletesTest extends TestCase {
    
    public static Log log = LogFactory.getLog(FolderFunctionalityTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        log.info("BEGIN");
        
        try {
            testUser = TestUtils.setupUser("folderParentDeletesTestUser");
            testWeblog = TestUtils.setupWeblog("folderParentDeletesTestWeblog", testUser);
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
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test that deleting a folders parent object deletes all folders.
     */
    public void testFolderParentDeletes() throws Exception {
        
        log.info("BEGIN");
        
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder root = bmgr.getRootFolder(testWeblog);
        
        // add a small tree /parentDelete-fold1/parentDelete-fold2
        WeblogBookmarkFolder fold1 = new WeblogBookmarkFolder(root, "parentDelete-fold1", null, TestUtils.getManagedWebsite(testWeblog));
        root.addFolder(fold1);
        bmgr.saveFolder(fold1);
        WeblogBookmarkFolder fold2 = new WeblogBookmarkFolder(fold1, "parentDelete-fold2", null, TestUtils.getManagedWebsite(testWeblog));
        fold1.addFolder(fold2);
        bmgr.saveFolder(fold2);
        TestUtils.endSession(true);
        
        // now delete the weblog owning these categories
        Exception ex = null;
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            umgr.removeWebsite(TestUtils.getManagedWebsite(testWeblog));
            TestUtils.endSession(true);
        } catch (WebloggerException e) {
            ex = e;
        }
        assertNull(ex);
        
        log.info("END");
    }
    
}
