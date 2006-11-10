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
/*
 * WeblogCategoryTest.java
 *
 * Created on April 13, 2006, 10:07 PM
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
        
        // we need to know how many categories we start the test with
        int numCats = mgr.getRootWeblogCategory(testWeblog).getWeblogCategories().size();
        
        // add a new category
        WeblogCategoryData newCat = new WeblogCategoryData();
        newCat.setName("catTestCategory");
        newCat.setParent(testWeblog.getDefaultCategory());
        newCat.setWebsite(testWeblog);
        mgr.saveWeblogCategory(newCat);
        String id = newCat.getId();
        TestUtils.endSession(true);
        
        // make sure category was added
        cat = null;
        cat = mgr.getWeblogCategory(id);
        assertNotNull(cat);
        assertEquals(newCat, cat);
        
        // make sure category count increased
        testWeblog = RollerFactory.getRoller().getUserManager().getWebsite(testWeblog.getId());
        assertEquals(numCats+1, mgr.getRootWeblogCategory(testWeblog).getWeblogCategories().size());
        
        // update category
        cat.setName("testtest");
        mgr.saveWeblogCategory(cat);
        TestUtils.endSession(true);
        
        // verify category was updated
        cat = null;
        cat = mgr.getWeblogCategory(id);
        assertNotNull(cat);
        assertEquals("testtest", cat.getName());
        
        // remove category
        mgr.removeWeblogCategory(cat);
        TestUtils.endSession(true);
        
        // make sure category was removed
        cat = null;
        mgr.getWeblogCategory(id);
        assertNull(cat);
        
        // make sure category count decreased
        testWeblog = RollerFactory.getRoller().getUserManager().getWebsite(testWeblog.getId());
        assertEquals(numCats, mgr.getRootWeblogCategory(testWeblog).getWeblogCategories().size());
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    public void testWeblogCategoryLookups() throws Exception {
        
    }
    
    
    public void testWeblogCategoryPaths() throws Exception {
        
        WeblogCategoryData root = null;
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        root = mgr.getRootWeblogCategory(testWeblog);
        
        WeblogCategoryData f1 = new WeblogCategoryData();
        f1.setName("f1");
        f1.setParent(root);
        f1.setWebsite(testWeblog);
        mgr.saveWeblogCategory(f1);
        
        WeblogCategoryData f2 = new WeblogCategoryData();
        f2.setName("f2");
        f2.setParent(f1);
        f2.setWebsite(testWeblog);
        mgr.saveWeblogCategory(f2);
        
        WeblogCategoryData f3 = new WeblogCategoryData();
        f3.setName("f3");
        f3.setParent(f2);
        f3.setWebsite(testWeblog);
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
    
    
    public void testMoveWeblogCategory() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        // add some categories and entries to test with
        WeblogCategoryData dest = new WeblogCategoryData();
        dest.setName("c0");
        dest.setParent(mgr.getRootWeblogCategory(testWeblog));
        dest.setWebsite(testWeblog);
        mgr.saveWeblogCategory(dest);
        
        WeblogCategoryData c1 = new WeblogCategoryData();
        c1.setName("c1");
        c1.setParent(mgr.getRootWeblogCategory(testWeblog));
        c1.setWebsite(testWeblog);
        mgr.saveWeblogCategory(c1);
        
        WeblogCategoryData c2 = new WeblogCategoryData();
        c2.setName("c2");
        c2.setParent(c1);
        c2.setWebsite(testWeblog);
        mgr.saveWeblogCategory(c2);
        
        WeblogCategoryData c3 = new WeblogCategoryData();
        c3.setName("c3");
        c3.setParent(c2);
        c3.setWebsite(testWeblog);
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
        c1.setParent(dest);
        mgr.saveWeblogCategory(c1);
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
    
    public void testMoveWeblogCategoryContents() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        // add some categories and entries to test with
        WeblogCategoryData dest = new WeblogCategoryData();
        dest.setName("c0");
        dest.setParent(mgr.getRootWeblogCategory(testWeblog));
        dest.setWebsite(testWeblog);
        mgr.saveWeblogCategory(dest);
        
        WeblogCategoryData c1 = new WeblogCategoryData();
        c1.setName("c1");
        c1.setParent(mgr.getRootWeblogCategory(testWeblog));
        c1.setWebsite(testWeblog);
        mgr.saveWeblogCategory(c1);
        
        WeblogCategoryData c2 = new WeblogCategoryData();
        c2.setName("c2");
        c2.setParent(c1);
        c2.setWebsite(testWeblog);
        mgr.saveWeblogCategory(c2);
        
        WeblogCategoryData c3 = new WeblogCategoryData();
        c3.setName("c3");
        c3.setParent(c2);
        c3.setWebsite(testWeblog);
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
