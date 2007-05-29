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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.TestUtils;
import org.apache.roller.pojos.User;
import org.apache.roller.pojos.WeblogCategory;
import org.apache.roller.pojos.Weblog;


/**
 * Test deleting of WeblogCategory parent objects to test cascading deletes.
 */
public class WeblogCategoryParentDeletesTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogCategoryParentDeletesTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() {
        
        log.info("BEGIN");
        
        try {
            testUser = TestUtils.setupUser("categoryParentDeletesTestUser");
            testWeblog = TestUtils.setupWeblog("categoryParentDeletesTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
        
        log.info("END");
    }
    
    public void tearDown() {
        
        log.info("BEGIN");
        
        try {
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test that deleting a categories parent object deletes all categories.
     */
    public void testCategoryParentDeletes() throws Exception {
        
        log.info("BEGIN");
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        // root category is always available
        WeblogCategory root = mgr.getRootWeblogCategory(TestUtils.getManagedWebsite(testWeblog));
        
        // add a small category tree /subcat/subcat2
        WeblogCategory subcat = new WeblogCategory(TestUtils.getManagedWebsite(testWeblog), root, "categoryParentDeletes1", null, null);
        root.addCategory(subcat);
        mgr.saveWeblogCategory(subcat);
        WeblogCategory subcat2 = new WeblogCategory(TestUtils.getManagedWebsite(testWeblog), subcat, "categoryParentDeletes2", null, null);
        subcat.addCategory(subcat2);
        mgr.saveWeblogCategory(subcat2);
        TestUtils.endSession(true);
        
        // now delete the weblog owning these categories
        Exception ex = null;
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            umgr.removeWebsite(TestUtils.getManagedWebsite(testWeblog));
            TestUtils.endSession(true);
        } catch (RollerException e) {
            ex = e;
        }
        assertNull(ex);
        
        log.info("END");
    }
    
}
