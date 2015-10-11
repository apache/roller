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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test WeblogCategory CRUD actions.
 */
public class WeblogCategoryCRUDTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogCategoryCRUDTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() {
        
        log.info("BEGIN");
        
        try {
            // setup weblogger
            TestUtils.setupWeblogger();
            
            testUser = TestUtils.setupUser("categoryCRUDTestUser");
            testWeblog = TestUtils.setupWeblog("categoryCRUDTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
        
        log.info("END");
    }
    
    public void tearDown() {
        
        log.info("BEGIN");
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test WeblogCategory.equals() method.
     */
    public void testWeblogCategoryEquality() throws Exception {
        
        log.info("BEGIN");
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        WeblogCategory testCat = new WeblogCategory(testWeblog, null, "desc");
        WeblogCategory testCat2 = new WeblogCategory(testWeblog, "root2", "desc2");
        assertFalse(testCat2.equals(testCat));
        mgr.removeWeblogCategory(testCat);
        mgr.removeWeblogCategory(testCat2);

        log.info("END");
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testBasicCRUD() throws Exception {
        
        log.info("BEGIN");
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogCategory cat;

        // root category is always available
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // make sure we are starting with 1 categories
        assertEquals(1, testWeblog.getWeblogCategories().size());

        // add a new category
        WeblogCategory newCat = new WeblogCategory(testWeblog, "catTestCategory", null);
        testWeblog.addCategory(newCat);
        mgr.saveWeblogCategory(newCat);
        TestUtils.endSession(true);
        
        // make sure category was added
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals(newCat, cat);
        
        // make sure category count increased
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // update category
        cat.setName("testtest");
        mgr.saveWeblogCategory(cat);
        TestUtils.endSession(true);

        // verify category was updated
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals("testtest", cat.getName());
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // remove category
        mgr.removeWeblogCategory(cat);
        TestUtils.endSession(true);

        // make sure cat was removed
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNull(cat);
        
        // make sure category count decreased
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertEquals(1, testWeblog.getWeblogCategories().size());
        
        log.info("END");
    }
    
    
    /**
     * Make sure that deleting a category deletes all child categories.
     */
    public void testCategoryCascadingDelete() throws Exception {
        
        log.info("BEGIN");
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        
        // root category is always available
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // add a category above default one
        WeblogCategory testCat = new WeblogCategory(testWeblog, "SampleCategory", null);
        testWeblog.addCategory(testCat);
        mgr.saveWeblogCategory(testCat);
        TestUtils.endSession(true);
        
        // check that testCat can be retrieved
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        assertEquals(2, testWeblog.getWeblogCategories().size());
        testCat = testWeblog.getWeblogCategories().get(1);
        assertEquals("SampleCategory", testCat.getName());

        // now delete category and subcats should be deleted by cascade
        mgr.removeWeblogCategory(testCat);
        TestUtils.endSession(true);
        
        // verify cascading delete succeeded
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertEquals(1, testWeblog.getWeblogCategories().size());
        assertNull(mgr.getWeblogCategoryByName(TestUtils.getManagedWebsite(testWeblog), "SampleCategory"));
        
        log.info("END");
    }
    
}
