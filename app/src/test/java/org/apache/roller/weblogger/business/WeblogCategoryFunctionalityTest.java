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

import java.util.List;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Test Weblog Category related business operations.
 */
public class WeblogCategoryFunctionalityTest extends WebloggerTest {

    User testUser = null;
    Weblog testWeblog = null;
    WeblogCategory cat1 = null;
    WeblogCategory cat2 = null;
    WeblogCategory cat3 = null;
    WeblogCategory testCat = null;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("categoryTestUser");
        testWeblog = setupWeblog("categoryTestWeblog", testUser);

        // setup several categories for testing
        cat1 = setupWeblogCategory(testWeblog, "catTest-cat1");
        cat2 = setupWeblogCategory(testWeblog, "catTest-cat2");
        cat3 = setupWeblogCategory(testWeblog, "catTest-cat3");

        // a simple test cat at the root level
        testCat = setupWeblogCategory(testWeblog, "catTest-testCat");

        endSession(true);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
    }

    @Test
    public void testHasCategory() throws Exception {
        // check that root has category
        assertTrue(testWeblog.hasCategory(testCat.getName()));
    }
    
    @Test
    public void testLookupCategoryById() throws Exception {
        WeblogCategory cat = weblogManager.getWeblogCategory(testCat.getId());
        assertNotNull(cat);
        assertEquals(cat, testCat);
    }

    @Test
    public void testLookupCategoryByName() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);
        WeblogCategory cat = weblogManager.getWeblogCategoryByName(testWeblog, "catTest-cat1");
        assertNotNull(cat);
        assertEquals(cat, cat1);
        
        cat = weblogManager.getWeblogCategoryByName(testWeblog, "catTest-cat3");
        assertNotNull(cat);
        assertEquals(cat, cat3);
        
        // test lazy lookup, specifying just a name without slashes
        cat = weblogManager.getWeblogCategoryByName(testWeblog, "catTest-cat1");
        assertNotNull(cat);
        assertEquals(cat, cat1);
    }

    @Test
    public void testLookupAllCategoriesByWeblog() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);
        List cats = weblogManager.getWeblogCategories(testWeblog);
        assertNotNull(cats);
        assertEquals(5, cats.size());
    }

    @Test
    public void testMoveWeblogCategoryContents() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);

        // add some categories and entries to test with
        WeblogCategory c1 = new WeblogCategory(testWeblog, "c1");
        testWeblog.addCategory(c1);
        weblogManager.saveWeblogCategory(c1);

        WeblogCategory dest = new WeblogCategory(testWeblog, "dest");
        testWeblog.addCategory(dest);
        weblogManager.saveWeblogCategory(dest);

        endSession(true);

        c1 = weblogManager.getWeblogCategory(c1.getId());
        dest = weblogManager.getWeblogCategory(dest.getId());

        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        setupWeblogEntry("e1", c1, PubStatus.PUBLISHED, testWeblog, testUser);
        setupWeblogEntry("e2", c1, PubStatus.DRAFT, testWeblog, testUser);
        endSession(true);

        // need to query for cats again since session was closed
        c1 = weblogManager.getWeblogCategory(c1.getId());
        dest = weblogManager.getWeblogCategory(dest.getId());

        // verify number of entries in each category
        assertEquals(0, retrieveWeblogEntries(dest, false).size());
        assertEquals(0, retrieveWeblogEntries(dest, true).size());
        assertEquals(2, retrieveWeblogEntries(c1, false).size());
        assertEquals(1, retrieveWeblogEntries(c1, true).size());

        // move contents of source category c1 to destination category dest
        weblogManager.moveWeblogCategoryContents(c1, dest);
        weblogManager.saveWeblogCategory(c1);
        endSession(true);

        // after move, verify number of entries in each category
        dest = weblogManager.getWeblogCategory(dest.getId());
        c1 = weblogManager.getWeblogCategory(c1.getId());

        // Hierarchy is flattened under dest
        assertEquals(2, retrieveWeblogEntries(dest, false).size());
        assertEquals(1, retrieveWeblogEntries(dest, true).size());

        // c1 category should be empty now
        assertEquals(0, retrieveWeblogEntries(c1, false).size());
    }

    private List<WeblogEntry> retrieveWeblogEntries(WeblogCategory category, boolean publishedOnly) {
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(category.getWeblog());
        wesc.setCatName(category.getName());
        if (publishedOnly) {
            wesc.setStatus(PubStatus.PUBLISHED);
        }
        return weblogEntryManager.getWeblogEntries(wesc);
    }

    private WeblogCategory setupWeblogCategory(Weblog weblog, String name) throws Exception {
        WeblogCategory testCat = new WeblogCategory(weblog, name);
        weblog.addCategory(testCat);
        weblogManager.saveWeblogCategory(testCat);
        strategy.flush();

        // query for object
        WeblogCategory cat = weblogManager.getWeblogCategory(testCat.getId());
        if (cat == null) {
            throw new IllegalStateException("error setting up weblog category");
        }
        return cat;
    }
}
