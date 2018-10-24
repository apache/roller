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
package org.tightblog.business;

import java.util.List;
import java.util.Optional;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test Weblog Category related business operations.
 */
public class WeblogCategoryFunctionalityTestIT extends WebloggerTest {

    private User testUser;
    private Weblog testWeblog;
    private WeblogCategory testCat;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("categoryTestUser");
        testWeblog = setupWeblog("category-test-weblog", testUser);

        // setup several categories for testing
        setupWeblogCategory(testWeblog, "catTest-cat1");
        setupWeblogCategory(testWeblog, "catTest-cat2");

        // a simple test cat at the root level
        testCat = setupWeblogCategory(testWeblog, "catTest-testCat");
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
    }

    @Test
    public void testHasCategory() {
        // check that root has category
        assertTrue(testWeblog.hasCategory(testCat.getName()));
    }
    
    @Test
    public void testLookupCategoryById() {
        Optional<WeblogCategory> maybeCat = weblogCategoryRepository.findById(testCat.getId());
        assertTrue(maybeCat.isPresent());
        assertEquals(maybeCat.get(), testCat);
    }

    @Test
    public void testLookupAllCategoriesByWeblog() {
        List cats = weblogManager.getWeblogCategories(testWeblog);
        assertNotNull(cats);
        assertEquals(4, cats.size());
    }

    @Test
    public void testMoveWeblogCategoryContents() throws Exception {

        // add some categories and entries to test with
        WeblogCategory c1 = new WeblogCategory(testWeblog, "c1");
        testWeblog.addCategory(c1);

        WeblogCategory dest = new WeblogCategory(testWeblog, "dest");
        testWeblog.addCategory(dest);
        weblogManager.saveWeblog(testWeblog);

        testWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        setupWeblogEntry("e1", c1, PubStatus.PUBLISHED, testWeblog, testUser);
        setupWeblogEntry("e2", c1, PubStatus.DRAFT, testWeblog, testUser);

        // need to query for cats again since session was closed
        WeblogCategory fromCat = weblogCategoryRepository.findByIdOrNull(c1.getId());
        WeblogCategory toCat = weblogCategoryRepository.findByIdOrNull(dest.getId());

        // verify number of entries in each category
        assertNotNull(fromCat);
        assertNotNull(toCat);
        assertEquals(0, retrieveWeblogEntries(toCat, false).size());
        assertEquals(0, retrieveWeblogEntries(toCat, true).size());
        assertEquals(2, retrieveWeblogEntries(fromCat, false).size());
        assertEquals(1, retrieveWeblogEntries(fromCat, true).size());

        // move contents of source category c1 to destination category dest
        weblogManager.moveWeblogCategoryContents(c1, dest);

        // after move, verify number of entries in each category
        fromCat = weblogCategoryRepository.findByIdOrNull(c1.getId());
        toCat = weblogCategoryRepository.findByIdOrNull(dest.getId());

        assertNotNull(fromCat);
        assertNotNull(toCat);

        // Hierarchy is flattened under dest
        assertEquals(2, retrieveWeblogEntries(toCat, false).size());
        assertEquals(1, retrieveWeblogEntries(toCat, true).size());

        // c1 category should be empty
        assertEquals(0, retrieveWeblogEntries(fromCat, false).size());
    }

    private List<WeblogEntry> retrieveWeblogEntries(WeblogCategory category, boolean publishedOnly) {
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(category.getWeblog());
        wesc.setCategoryName(category.getName());
        if (publishedOnly) {
            wesc.setStatus(PubStatus.PUBLISHED);
        }
        return weblogEntryManager.getWeblogEntries(wesc);
    }

    private WeblogCategory setupWeblogCategory(Weblog weblog, String name) {
        WeblogCategory testCategory = new WeblogCategory(weblog, name);
        weblog.addCategory(testCategory);
        weblogManager.saveWeblog(weblog);

        // query for object
        Optional<WeblogCategory> maybeCat = weblogCategoryRepository.findById(testCategory.getId());
        if (!maybeCat.isPresent()) {
            throw new IllegalStateException("error setting up weblog category");
        }
        return maybeCat.get();
    }
}
