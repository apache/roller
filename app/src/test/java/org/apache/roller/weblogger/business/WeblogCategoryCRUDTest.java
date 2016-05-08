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

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


public class WeblogCategoryCRUDTest extends WebloggerTest {

    User testUser = null;
    Weblog testWeblog = null;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("categoryCRUDTestUser");
        testWeblog = setupWeblog("categoryCRUDTestWeblog", testUser);
        endSession(true);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getUserName());
        endSession(true);
    }
    
    
    /**
     * Test WeblogCategory.equals() method.
     */
    @Test
    public void testWeblogCategoryEquality() throws Exception {
        testWeblog = getManagedWeblog(testWeblog);

        WeblogCategory testCat = new WeblogCategory(testWeblog, null);
        WeblogCategory testCat2 = new WeblogCategory(testWeblog, "root2");
        assertFalse(testCat2.equals(testCat));
        weblogManager.removeWeblogCategory(testCat);
        weblogManager.removeWeblogCategory(testCat2);
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testBasicCRUD() throws Exception {
        WeblogCategory cat;

        // root category is always available
        testWeblog = getManagedWeblog(testWeblog);

        // make sure we are starting with 1 categories
        assertEquals(1, testWeblog.getWeblogCategories().size());

        // add a new category
        WeblogCategory newCat = new WeblogCategory(testWeblog, "catTestCategory");
        testWeblog.addCategory(newCat);
        weblogManager.saveWeblogCategory(newCat);
        endSession(true);
        
        // make sure category was added
        cat = weblogManager.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals(newCat, cat);
        
        // make sure category count increased
        testWeblog = getManagedWeblog(testWeblog);
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // update category
        cat.setName("testtest");
        weblogManager.saveWeblogCategory(cat);
        endSession(true);

        // verify category was updated
        cat = weblogManager.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals("testtest", cat.getName());
        assertEquals(2, testWeblog.getWeblogCategories().size());

        // remove category
        weblogManager.removeWeblogCategory(cat);
        endSession(true);

        // make sure cat was removed
        cat = weblogManager.getWeblogCategory(newCat.getId());
        assertNull(cat);
        
        // make sure category count decreased
        testWeblog = getManagedWeblog(testWeblog);
        assertEquals(1, testWeblog.getWeblogCategories().size());
    }
    
    
    /**
     * Make sure that deleting a category deletes all child categories.
     */
    @Test
    public void testCategoryCascadingDelete() throws Exception {
        // root category is always available
        testWeblog = getManagedWeblog(testWeblog);

        // add a category above default one
        WeblogCategory testCat = new WeblogCategory(testWeblog, "SampleCategory");
        testWeblog.addCategory(testCat);
        weblogManager.saveWeblogCategory(testCat);
        endSession(true);
        
        // check that testCat can be retrieved
        testWeblog = getManagedWeblog(testWeblog);

        assertEquals(2, testWeblog.getWeblogCategories().size());
        testCat = testWeblog.getWeblogCategories().get(1);
        assertEquals("SampleCategory", testCat.getName());

        // now delete category and subcats should be deleted by cascade
        weblogManager.removeWeblogCategory(testCat);
        endSession(true);
        
        // verify cascading delete succeeded
        testWeblog = getManagedWeblog(testWeblog);
        assertEquals(1, testWeblog.getWeblogCategories().size());
        assertNull(weblogManager.getWeblogCategoryByName(getManagedWeblog(testWeblog), "SampleCategory"));
    }
    
}
