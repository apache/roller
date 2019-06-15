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
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Weblog Category related business operations.
 */
public class WeblogCategoryFunctionalityTest  {
    
    public static Log log = LogFactory.getLog(WeblogCategoryFunctionalityTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    WeblogCategory cat1 = null;
    WeblogCategory cat2 = null;
    WeblogCategory cat3 = null;
    WeblogCategory testCat = null;
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
    public void setUp() {
        
        log.info("BEGIN");
        
        try {
            // setup weblogger
            TestUtils.setupWeblogger();
            
            testUser = TestUtils.setupUser("categoryTestUser");
            testWeblog = TestUtils.setupWeblog("categoryTestWeblog", testUser);
            
            // setup several categories for testing
            cat1 = TestUtils.setupWeblogCategory(testWeblog, "catTest-cat1");
            cat2 = TestUtils.setupWeblogCategory(testWeblog, "catTest-cat2");
            cat3 = TestUtils.setupWeblogCategory(testWeblog, "catTest-cat3");
            
            // a simple test cat at the root level
            testCat = TestUtils.setupWeblogCategory(testWeblog, "catTest-testCat");
            
            TestUtils.endSession(true);
        } catch (Throwable t) {
            log.error("ERROR in setup", t);
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
        } catch (Throwable t) {
            log.error("ERROR in teardown", t);
        }
        
        log.info("END");
    }

    /**
     * Test the hasCategory() method on WeblogCategory.
     */
    @Test
    public void testHasCategory() throws Exception {
        
        log.info("BEGIN");

        // check that root has category
        assertTrue(testWeblog.hasCategory(testCat.getName()));
        
        log.info("END");
    }
    
    
    /**
     * Lookup category by id.
     */
    @Test
    public void testLookupCategoryById() throws Exception {
        
        log.info("BEGIN");
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        WeblogCategory cat = mgr.getWeblogCategory(testCat.getId());
        assertNotNull(cat);
        assertEquals(cat, testCat);
        
        log.info("END");
    }
    
    
    /**
     * Lookup category by name.
     */
    @Test
    public void testLookupCategoryByName() throws Exception {
        
        log.info("BEGIN");
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogCategory cat = mgr.getWeblogCategoryByName(testWeblog, "catTest-cat1");
        assertNotNull(cat);
        assertEquals(cat, cat1);
        
        cat = mgr.getWeblogCategoryByName(testWeblog, "catTest-cat3");
        assertNotNull(cat);
        assertEquals(cat, cat3);
        
        // test lazy lookup, specifying just a name without slashes
        cat = mgr.getWeblogCategoryByName(testWeblog, "catTest-cat1");
        assertNotNull(cat);
        assertEquals(cat, cat1);

        log.info("END");
    }
    
    
    /**
     * Lookup all categories for a weblog.
     */
    @Test
    public void testLookupAllCategoriesByWeblog() throws Exception {
        
        log.info("BEGIN");
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List cats = mgr.getWeblogCategories(testWeblog);
        assertNotNull(cats);
        assertEquals(5, cats.size());
        
        log.info("END");
    }

    /**
     * Test moving entries in category to new category.
     */
    @Test
    public void testMoveWeblogCategoryContents() throws Exception {
        log.info("BEGIN");
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntry e1 = null;
        WeblogEntry e2 = null; 
        try {

            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            testUser = TestUtils.getManagedUser(testUser);

            // add some categories and entries to test with
            WeblogCategory c1 = new WeblogCategory(testWeblog, "c1", null, null);
            mgr.saveWeblogCategory(c1);

            WeblogCategory dest = new WeblogCategory(testWeblog, "dest", null, null);
            mgr.saveWeblogCategory(dest);

            TestUtils.endSession(true);

            c1 = mgr.getWeblogCategory(c1.getId());
            dest = mgr.getWeblogCategory(dest.getId());

            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            testUser = TestUtils.getManagedUser(testUser);
            e1 = TestUtils.setupWeblogEntry("e1", c1, testWeblog, testUser);
            e2 = TestUtils.setupWeblogEntry("e2", c1, PubStatus.DRAFT, testWeblog, testUser);

            TestUtils.endSession(true);

            // need to query for cats again since session was closed
            c1 = mgr.getWeblogCategory(c1.getId());
            dest = mgr.getWeblogCategory(dest.getId());

            // verify number of entries in each category
            assertEquals(0, dest.retrieveWeblogEntries(true).size());
            assertEquals(0, dest.retrieveWeblogEntries(false).size());
            assertEquals(2, c1.retrieveWeblogEntries(false).size());
            assertEquals(1, c1.retrieveWeblogEntries(true).size());

            // move contents of source category c1 to destination category dest
            mgr.moveWeblogCategoryContents(c1, dest);
            mgr.saveWeblogCategory(c1);
            TestUtils.endSession(true);

            // after move, verify number of entries in each category
            dest = mgr.getWeblogCategory(dest.getId());
            c1 = mgr.getWeblogCategory(c1.getId());

            // Hierarchy is flattened under dest      
            assertEquals(2, dest.retrieveWeblogEntries(false).size());
            assertEquals(1, dest.retrieveWeblogEntries(true).size());

            // c1 category should be empty now
            assertEquals(0, c1.retrieveWeblogEntries(false).size());

        } finally {
            mgr.removeWeblogEntry(TestUtils.getManagedWeblogEntry(e1));
            mgr.removeWeblogEntry(TestUtils.getManagedWeblogEntry(e2));
        }
        
        log.info("END");
    }
    
}
