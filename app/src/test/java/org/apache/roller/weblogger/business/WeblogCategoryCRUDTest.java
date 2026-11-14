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
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test WeblogCategory CRUD actions.
 */
public class WeblogCategoryCRUDTest  {
    
    public static Log log = LogFactory.getLog(WeblogCategoryCRUDTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
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

    @AfterEach
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
    @Test
    public void testWeblogCategoryEquality() throws Exception {
        
        log.info("BEGIN");
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        WeblogCategory testCat = new WeblogCategory(testWeblog, "root", "desc", null);
        WeblogCategory testCat2 = new WeblogCategory(testWeblog, "root2", "desc2", null);
        assertFalse(testCat2.equals(testCat));
        mgr.removeWeblogCategory(testCat);
        mgr.removeWeblogCategory(testCat2);

        log.info("END");
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testBasicCRUD() throws Exception {
        
        log.info("BEGIN");
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogCategory cat = null;

        // root category is always available
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // make sure we are starting with 1 categories
        assertEquals(1, testWeblog.getWeblogCategories().size());

        // add a new category
        WeblogCategory newCat = new WeblogCategory(testWeblog, "catTestCategory", null, null);
        mgr.saveWeblogCategory(newCat);
        TestUtils.endSession(true);
        
        // make sure category was added
        cat = null;
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
        cat = null;
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals("testtest", cat.getName());
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // remove category
        mgr.removeWeblogCategory(cat);
        TestUtils.endSession(true);

        // make sure cat was removed
        cat = null;
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
    @Test
    public void testCategoryCascadingDelete() throws Exception {
        
        log.info("BEGIN");
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        // root category is always available
        testWeblog = TestUtils.getManagedWebsite(testWeblog);

        // add a category above default one
        WeblogCategory testCat = new WeblogCategory(testWeblog, "SampleCategory", null, null);
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
    

    /**
     * Entry counts per category include entries in any status and leave out
     * categories with no entries.
     */
    @Test
    public void testCategoryEntryCounts() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);

        WeblogCategory busy = TestUtils.setupWeblogCategory(testWeblog, "Busy");
        WeblogCategory empty = TestUtils.setupWeblogCategory(testWeblog, "Empty");
        TestUtils.setupWeblogEntry("count-1", busy, WeblogEntry.PubStatus.PUBLISHED, testWeblog, testUser);
        TestUtils.setupWeblogEntry("count-2", busy, WeblogEntry.PubStatus.DRAFT, testWeblog, testUser);
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        Map<String, Long> counts = mgr.getWeblogCategoryEntryCounts(testWeblog);
        assertEquals(Long.valueOf(2), counts.get(busy.getId()));
        assertFalse(counts.containsKey(empty.getId()));
    }
    /**
     * After a reorder, a new session must read the weblog's own category list in
     * the new order: themes read it, and the shared cache keeps the old list unless
     * the change is carried into it.
     */
    @Test
    public void testReorderIsSeenByLaterSessions() throws Exception {

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogCategory first = TestUtils.setupWeblogCategory(testWeblog, "OrderFirst");
        WeblogCategory second = TestUtils.setupWeblogCategory(testWeblog, "OrderSecond");
        TestUtils.endSession(true);

        // warm the shared cache with the old order
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        String oldFirst = testWeblog.getWeblogCategories().get(testWeblog.getWeblogCategories().size() - 2).getName();
        assertEquals("OrderFirst", oldFirst);
        TestUtils.endSession(true);

        // what Categories.reorder() does
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogCategory a = mgr.getWeblogCategory(first.getId());
        WeblogCategory b = mgr.getWeblogCategory(second.getId());
        int pa = a.getPosition();
        a.setPosition(b.getPosition());
        b.setPosition(pa);
        WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(testWeblog);
        testWeblog.getWeblogCategories().sort(java.util.Comparator.comparingInt(WeblogCategory::getPosition));
        WebloggerFactory.getWeblogger().flush();
        TestUtils.endSession(true);

        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        int size = testWeblog.getWeblogCategories().size();
        assertEquals("OrderSecond", testWeblog.getWeblogCategories().get(size - 2).getName());
        assertEquals("OrderFirst", testWeblog.getWeblogCategories().get(size - 1).getName());
    }
}
