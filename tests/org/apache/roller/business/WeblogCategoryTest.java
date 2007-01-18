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

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Utilities;


/**
 * Test Weblog Category related business operations.
 */
public class WeblogCategoryTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogCategoryTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    
    
    public WeblogCategoryTest() {
    }
    
    
    public static Test suite() {
        return new TestSuite(WeblogCategoryTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        log.info("Setup "+this.getClass().getName());
        
        try {
            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        log.info("Teardown "+this.getClass().getName());
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testWeblogCategoryCRUD() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogCategoryData cat = null;
        List cats = null;
        
        WeblogCategoryData root = mgr.getRootWeblogCategory(testWeblog);
        
        // make sure we are starting with 0 categories (beneath root)
        assertEquals(0, root.getWeblogCategories().size());
        
        // add a new category
        WeblogCategoryData newCat = new WeblogCategoryData(testWeblog, root, "catTestCategory", null, null);
        mgr.saveWeblogCategory(newCat);
        TestUtils.endSession(true);
        
        // make sure category was added
        cat = null;
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNotNull(cat);
        assertEquals(newCat, cat);
        
        // make sure category count increased
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
        
        // add a subcat
        WeblogCategoryData subcat = new WeblogCategoryData(testWeblog, cat, "subcatTest1", null, null);
        mgr.saveWeblogCategory(subcat);
        TestUtils.endSession(true);
        
        // check that subcat was saved and we can navigate to it
        root = mgr.getRootWeblogCategory(testWeblog);
        assertEquals(1, root.getWeblogCategories().size());
        cat = (WeblogCategoryData) root.getWeblogCategories().iterator().next();
        assertEquals("testtest", cat.getName());
        assertEquals(1, cat.getWeblogCategories().size());
        subcat = (WeblogCategoryData) cat.getWeblogCategories().iterator().next();
        assertEquals("subcatTest1", subcat.getName());
        
        // remove category, should cascade to subcat
        mgr.removeWeblogCategory(cat);
        TestUtils.endSession(true);
        
        // make sure folder and subfolder was removed
        cat = null;
        cat = mgr.getWeblogCategory(newCat.getId());
        assertNull(cat);
        cat = mgr.getWeblogCategory(subcat.getId());
        assertNull(cat);
        
        // make sure category count decreased
        root = mgr.getRootWeblogCategory(testWeblog);
        assertEquals(0, root.getWeblogCategories().size());
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    public void testWeblogCategoryLookups() throws Exception {
        
    }
    
    
    /**
     * Test WeblogCategoryData.equals() method.
     */
    public void testWeblogCategoryEquality() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        WeblogCategoryData root = mgr.getRootWeblogCategory(testWeblog);
        
        WeblogCategoryData testCat = new WeblogCategoryData(testWeblog, null, "root", "root", null);
        assertTrue(root.equals(testCat));
        
        testCat = new WeblogCategoryData(testWeblog, root, "root", "root", null);
        assertFalse(root.equals(testCat));
    }
    
    
    /**
     * Test working with category paths.
     */
    public void testWeblogCategoryPaths() throws Exception {
        
        WeblogCategoryData root = null;
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        root = mgr.getRootWeblogCategory(testWeblog);
        
        WeblogCategoryData f1 = new WeblogCategoryData(testWeblog, root, "f1", null, null);
        mgr.saveWeblogCategory(f1);
        
        WeblogCategoryData f2 = new WeblogCategoryData(testWeblog, f1, "f2", null, null);
        mgr.saveWeblogCategory(f2);
        
        WeblogCategoryData f3 = new WeblogCategoryData(testWeblog, f2, "f3", null, null);
        mgr.saveWeblogCategory(f3);
        
        TestUtils.endSession(true);
        
        // test get by path
        assertEquals("f1",
                mgr.getWeblogCategoryByPath(testWeblog, null, "f1").getName());
        
        assertEquals("f1",
                mgr.getWeblogCategoryByPath(testWeblog, null, "/f1").getName());
        
        assertEquals("f2",
                mgr.getWeblogCategoryByPath(testWeblog, null, "/f1/f2").getName());
        
        assertEquals("f3",
                mgr.getWeblogCategoryByPath(testWeblog, null, "/f1/f2/f3").getName());
        
        // test path creation
        f3 = mgr.getWeblogCategoryByPath(testWeblog, null, "/f1/f2/f3");
        String pathString = f3.getPath();
        String[] pathArray = Utilities.stringToStringArray(pathString,"/");
        assertEquals("f1", pathArray[0]);
        assertEquals("f2", pathArray[1]);
        assertEquals("f3", pathArray[2]);
    }
    
    
    /**
     * Test moving one category into another.
     */
    public void testMoveWeblogCategory() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        WeblogCategoryData root = mgr.getRootWeblogCategory(testWeblog);
        
        // add some categories and entries to test with
        WeblogCategoryData dest = new WeblogCategoryData(testWeblog, root, "c0", null, null);
        mgr.saveWeblogCategory(dest);
        
        WeblogCategoryData c1 = new WeblogCategoryData(testWeblog, root, "c1", null, null);
        mgr.saveWeblogCategory(c1);
        
        WeblogCategoryData c2 = new WeblogCategoryData(testWeblog, c1, "c2", null, null);
        mgr.saveWeblogCategory(c2);
        
        WeblogCategoryData c3 = new WeblogCategoryData(testWeblog, c2, "c3", null, null);
        mgr.saveWeblogCategory(c3);
        
        TestUtils.endSession(true);
        
        c1 = mgr.getWeblogCategory(c1.getId());
        c2 = mgr.getWeblogCategory(c2.getId());
        c3 = mgr.getWeblogCategory(c3.getId());
        dest = mgr.getWeblogCategory(dest.getId());
        
        WeblogEntryData e1 = TestUtils.setupWeblogEntry("e1", c1, testWeblog, testUser);
        WeblogEntryData e2 = TestUtils.setupWeblogEntry("e2", c2, testWeblog, testUser);
        WeblogEntryData e3 = TestUtils.setupWeblogEntry("e3", c3, testWeblog, testUser);
        
        TestUtils.endSession(true);
        
        // need to query for cats again because we closed the session
        c1 = mgr.getWeblogCategory(c1.getId());
        c2 = mgr.getWeblogCategory(c2.getId());
        c3 = mgr.getWeblogCategory(c3.getId());
        dest = mgr.getWeblogCategory(dest.getId());
        
        // verify number of entries in each category
        assertEquals(0, dest.retrieveWeblogEntries(true).size());
        assertEquals(0, dest.retrieveWeblogEntries(false).size());
        assertEquals(1, c1.retrieveWeblogEntries(false).size());
        assertEquals(3, c1.retrieveWeblogEntries(true).size());
        
        // move contents of source category c1 to destination catetory dest
        mgr.moveWeblogCategory(c1, dest);
        TestUtils.endSession(true);
        
        // after move, verify number of entries in each category
        dest = mgr.getWeblogCategory(dest.getId());
        c1 = mgr.getWeblogCategory(c1.getId());
        c2 = mgr.getWeblogCategory(c2.getId());
        c3 = mgr.getWeblogCategory(c3.getId());
        
        assertEquals(3, dest.retrieveWeblogEntries(true).size());
        assertEquals(0, dest.retrieveWeblogEntries(false).size());
        
        assertEquals(dest, c1.getParent());
        assertEquals(c1,   c2.getParent());
        assertEquals(c2,   c3.getParent());
        
        assertEquals(1, c1.retrieveWeblogEntries(false).size());
        assertEquals(1, c2.retrieveWeblogEntries(false).size());
        assertEquals(1, c3.retrieveWeblogEntries(false).size());
        
        List entries = c1.retrieveWeblogEntries(true);
        assertEquals(3, entries.size());
    }
    
    
    /**
     * Test moving entries in category to new category.
     */
    public void testMoveWeblogCategoryContents() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        WeblogCategoryData root = mgr.getRootWeblogCategory(testWeblog);
        
        // add some categories and entries to test with
        WeblogCategoryData dest = new WeblogCategoryData(testWeblog, root, "c0", null, null);
        mgr.saveWeblogCategory(dest);
        
        WeblogCategoryData c1 = new WeblogCategoryData(testWeblog, root, "c1", null, null);
        mgr.saveWeblogCategory(c1);
        
        WeblogCategoryData c2 = new WeblogCategoryData(testWeblog, c1, "c2", null, null);
        mgr.saveWeblogCategory(c2);
        
        WeblogCategoryData c3 = new WeblogCategoryData(testWeblog, c2, "c3", null, null);
        mgr.saveWeblogCategory(c3);
        
        TestUtils.endSession(true);
        
        c1 = mgr.getWeblogCategory(c1.getId());
        c2 = mgr.getWeblogCategory(c2.getId());
        c3 = mgr.getWeblogCategory(c3.getId());
        dest = mgr.getWeblogCategory(dest.getId());
        
        WeblogEntryData e1 = TestUtils.setupWeblogEntry("e1", c1, testWeblog, testUser);
        WeblogEntryData e2 = TestUtils.setupWeblogEntry("e2", c2, testWeblog, testUser);
        WeblogEntryData e3 = TestUtils.setupWeblogEntry("e3", c3, testWeblog, testUser);
        
        TestUtils.endSession(true);
        
        // need to query for cats again since session was closed
        c1 = mgr.getWeblogCategory(c1.getId());
        c2 = mgr.getWeblogCategory(c2.getId());
        c3 = mgr.getWeblogCategory(c3.getId());
        dest = mgr.getWeblogCategory(dest.getId());
        
        // verify number of entries in each category
        assertEquals(0, dest.retrieveWeblogEntries(true).size());
        assertEquals(0, dest.retrieveWeblogEntries(false).size());
        assertEquals(1, c1.retrieveWeblogEntries(false).size());
        assertEquals(3, c1.retrieveWeblogEntries(true).size());
        
        // move contents of source category c1 to destination category dest
        mgr.moveWeblogCategoryContents(c1, dest);
        mgr.saveWeblogCategory(c1);
        TestUtils.endSession(true);
        
        // after move, verify number of entries in each category
        dest = mgr.getWeblogCategory(dest.getId());
        c1 = mgr.getWeblogCategory(c1.getId());
        
        // Hierarchy is flattened under dest      
        assertEquals(3, dest.retrieveWeblogEntries(true).size());
        assertEquals(3, dest.retrieveWeblogEntries(false).size());
        
        // c1 category should be empty now
        assertEquals(0, c1.retrieveWeblogEntries(false).size());

    }
}
