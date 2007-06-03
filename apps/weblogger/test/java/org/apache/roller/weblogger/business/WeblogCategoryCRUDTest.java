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

import java.util.List;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
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
            TestUtils.teardownUser(testUser.getId());
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
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogCategory root = mgr.getRootWeblogCategory(testWeblog);
        
        WeblogCategory testCat = new WeblogCategory(testWeblog, null, "root", "root", null);
        assertTrue(root.equals(testCat));
        
        testCat = new WeblogCategory(testWeblog, root, "root", "root", null);
        assertFalse(root.equals(testCat));
        
        log.info("END");
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testBasicCRUD() throws Exception {
        
        log.info("BEGIN");
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogCategory cat = null;
        List cats = null;
        
        // root category is always available
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogCategory root = mgr.getRootWeblogCategory(testWeblog);
        
        // make sure we are starting with 0 categories (beneath root)
        assertEquals(0, root.getWeblogCategories().size());
        
        // add a new category
        WeblogCategory newCat = new WeblogCategory(testWeblog, root, "catTestCategory", null, null);
        mgr.saveWeblogCategory(newCat);
        TestUtils.endSession(true);
        
        // make sure category was added
        cat = null;
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals(newCat, cat);
        
        // make sure category count increased
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = mgr.getRootWeblogCategory(testWeblog);
        assertEquals(1, root.getWeblogCategories().size());
        
        // update category
        cat.setName("testtest");
        mgr.saveWeblogCategory(cat);
        TestUtils.endSession(true);
        
        // verify category was updated
        cat = null;
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals("testtest", cat.getName());
        
        // remove category
        mgr.removeWeblogCategory(cat);
        TestUtils.endSession(true);
        
        // make sure cat was removed
        cat = null;
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNull(cat);
        
        // make sure category count decreased
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = mgr.getRootWeblogCategory(testWeblog);
        assertEquals(0, root.getWeblogCategories().size());
        
        log.info("END");
    }
    
    
    /**
     * Make sure that deleting a category deletes all child categories.
     */
    public void testCategoryCascadingDelete() throws Exception {
        
        log.info("BEGIN");
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        // root category is always available
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogCategory root = mgr.getRootWeblogCategory(testWeblog);
        
        // add a small category tree /subcat/subcat2
        WeblogCategory subcat = new WeblogCategory(testWeblog, root, "subcatTest1", null, null);
        root.addCategory(subcat);
        mgr.saveWeblogCategory(subcat);
        WeblogCategory subcat2 = new WeblogCategory(testWeblog, subcat, "subcatTest2", null, null);
        subcat.addCategory(subcat2);
        mgr.saveWeblogCategory(subcat2);
        TestUtils.endSession(true);
        
        // check that subcat tree can be navigated
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = mgr.getRootWeblogCategory(testWeblog);
        assertEquals(1, root.getWeblogCategories().size());
        subcat = (WeblogCategory) root.getWeblogCategories().iterator().next();
        assertEquals("subcatTest1", subcat.getName());
        assertEquals(1, subcat.getWeblogCategories().size());
        subcat2 = (WeblogCategory) subcat.getWeblogCategories().iterator().next();
        assertEquals("subcatTest2", subcat2.getName());
        
        // now delete category and subcats should be deleted by cascade
        mgr.removeWeblogCategory(subcat);
        TestUtils.endSession(true);
        
        // verify cascading delete succeeded
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        root = mgr.getRootWeblogCategory(testWeblog);
        assertEquals(0, root.getWeblogCategories().size());
        assertNull(mgr.getWeblogCategoryByPath(TestUtils.getManagedWebsite(testWeblog), "/subcatTest1/subcatTest2"));
        
        log.info("END");
    }
    
}
