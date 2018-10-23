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

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class WeblogCategoryCRUDTestIT extends WebloggerTest {

    User testUser;
    Weblog testWeblog;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("categoryCRUDTestUser");
        testWeblog = setupWeblog("category-crud-test-weblog", testUser);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
    }

    /**
     * Test WeblogCategory.equals() method.
     */
    @Test
    public void testWeblogCategoryEquality() {
        WeblogCategory testCat = new WeblogCategory(testWeblog, null);
        WeblogCategory testCat2 = new WeblogCategory(testWeblog, "root2");
        assertNotEquals(testCat2, testCat);
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testBasicCRUD() throws Exception {

        // make sure we are starting with 1 categories
        assertEquals(1, testWeblog.getWeblogCategories().size());

        // add a new category
        WeblogCategory newCat = new WeblogCategory(testWeblog, "catTestCategory");
        testWeblog.addCategory(newCat);
        weblogRepository.saveAndFlush(testWeblog);

        // make sure category was added
        WeblogCategory catFromDb = weblogCategoryRepository.findById(newCat.getId()).orElse(null);
        assertNotNull(catFromDb);
        assertEquals(newCat, catFromDb);
        
        // make sure category count increased
        testWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // update category
        Optional<WeblogCategory> maybeTest = testWeblog.getWeblogCategories().stream()
                .filter(wc -> wc.getId().equals(newCat.getId())).findFirst();
        assertTrue(maybeTest.isPresent());
        maybeTest.get().setName("testtest");
        weblogRepository.saveAndFlush(testWeblog);

        // verify category was updated
        catFromDb = weblogCategoryRepository.findById(newCat.getId()).orElse(null);
        assertNotNull(catFromDb);
        assertEquals("testtest", catFromDb.getName());
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // remove category
        testWeblog.getWeblogCategories().remove(catFromDb);
        weblogManager.saveWeblog(testWeblog);

        // make sure cat was removed
        catFromDb = weblogCategoryRepository.findById(newCat.getId()).orElse(null);
        assertNull(catFromDb);
        
        // make sure category count decreased
        testWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        assertEquals(1, testWeblog.getWeblogCategories().size());
    }
    
    
    /**
     * Make sure that deleting a category deletes all child categories.
     */
    @Test
    public void testCategoryCascadingDelete() throws Exception {
        // add a category above default one
        WeblogCategory testCat = new WeblogCategory(testWeblog, "SampleCategory");
        testWeblog.addCategory(testCat);
        weblogManager.saveWeblog(testWeblog);

        // check that testCat can be retrieved
        testWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());

        assertEquals(2, testWeblog.getWeblogCategories().size());
        testCat = testWeblog.getWeblogCategories().get(1);
        assertEquals("SampleCategory", testCat.getName());

        // now delete category and subcats should be deleted by cascade
        testWeblog.getWeblogCategories().remove(testCat);
        weblogManager.saveWeblog(testWeblog);

        // verify cascading delete succeeded
        testWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        assertEquals(1, testWeblog.getWeblogCategories().size());
        assertNull(weblogCategoryRepository.findByWeblogAndName(testWeblog, "SampleCategory"));
    }
    
}
