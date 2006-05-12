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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Test Weblog related business operations.
 */
public class WeblogTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogTest.class);
    
    UserData testUser = null;
    
    
    public WeblogTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(WeblogTest.class);
    }
    
    
    /**
     * All tests in this suite require a user.
     */
    public void setUp() throws Exception {
        
        try {
            testUser = TestUtils.setupUser("weblogTestUser");
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
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
    public void testWeblogCRUD() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        WebsiteData weblog = null;
        
        WebsiteData testWeblog = new WebsiteData();
        testWeblog.setName("Test Weblog");
        testWeblog.setDescription("Test Weblog");
        testWeblog.setHandle("testweblog");
        testWeblog.setEmailAddress("testweblog@dev.null");
        testWeblog.setEditorPage("editor-text.jsp");
        testWeblog.setBlacklist("");
        testWeblog.setEmailFromAddress("");
        testWeblog.setEditorTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(new java.util.Date());
        testWeblog.setCreator(testUser);
        
        // make sure test weblog does not exist
        weblog = mgr.getWebsiteByHandle(testWeblog.getHandle());
        assertNull(weblog);
        
        // add test weblog
        mgr.addWebsite(testWeblog);
        String id = testWeblog.getId();
        TestUtils.endSession(true);
        
        // make sure test weblog exists
        weblog = null;
        weblog = mgr.getWebsite(id);
        assertNotNull(weblog);
        assertEquals(testWeblog, weblog);
        
        // modify weblog and save
        weblog.setName("testtesttest");
        mgr.saveWebsite(weblog);
        TestUtils.endSession(true);
        
        // make sure changes were saved
        weblog = null;
        weblog = mgr.getWebsite(id);
        assertNotNull(weblog);
        assertEquals("testtesttest", weblog.getName());
        
        // remove test weblog
        mgr.removeWebsite(weblog);
        TestUtils.endSession(true);
        
        // make sure weblog no longer exists
        weblog = null;
        weblog = mgr.getWebsite(id);
        assertNull(weblog);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testWeblogLookups() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        WebsiteData weblog = null;
        
        // add test weblogs
        WebsiteData testWeblog1 = TestUtils.setupWeblog("testWeblog1", testUser);
        WebsiteData testWeblog2 = TestUtils.setupWeblog("testWeblog2", testUser);
        TestUtils.endSession(true);
        
        // lookup by id
        weblog = mgr.getWebsite(testWeblog1.getId());
        assertNotNull(weblog);
        assertEquals(testWeblog1.getHandle(), weblog.getHandle());
        
        // lookup by weblog handle
        weblog = null;
        weblog = mgr.getWebsiteByHandle(testWeblog1.getHandle());
        assertNotNull(weblog);
        assertEquals(testWeblog1.getHandle(), weblog.getHandle());
        
        // make sure disable weblogs are not returned
        weblog.setEnabled(Boolean.FALSE);
        mgr.saveWebsite(weblog);
        weblog = null;
        weblog = mgr.getWebsiteByHandle(testWeblog1.getHandle());
        assertNull(weblog);
        
        // restore enabled state
        weblog = mgr.getWebsiteByHandle(testWeblog1.getHandle(), Boolean.FALSE);
        weblog.setEnabled(Boolean.TRUE);
        mgr.saveWebsite(weblog);
        TestUtils.endSession(true);
        weblog = null;
        weblog = mgr.getWebsiteByHandle(testWeblog1.getHandle());
        assertNotNull(weblog);
        
        // get all weblogs for user
        weblog = null;
        List weblogs1 = mgr.getWebsites(testUser, Boolean.TRUE, Boolean.TRUE);
        assertEquals(2, weblogs1.size());
        weblog = (WebsiteData) weblogs1.get(0);
        assertNotNull(weblog);
        
        // make sure disabled weblogs are not returned
        weblog.setEnabled(Boolean.FALSE);
        mgr.saveWebsite(weblog);
        TestUtils.endSession(true);
        List weblogs2 = mgr.getWebsites(testUser, Boolean.TRUE, Boolean.TRUE);
        assertEquals(1, weblogs2.size());
        weblog = (WebsiteData) weblogs2.get(0);
        assertNotNull(weblog);
        
        // make sure inactive weblogs are not returned
        weblog.setActive(Boolean.FALSE);
        mgr.saveWebsite(weblog);
        TestUtils.endSession(true);
        List weblogs3 = mgr.getWebsites(testUser, Boolean.TRUE, Boolean.TRUE);
        assertEquals(0, weblogs3.size());
        
        // remove test weblogs
        TestUtils.teardownWeblog(testWeblog1.getId());
        TestUtils.teardownWeblog(testWeblog2.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test that we can safely remove a fully loaded weblog.
     * That means a weblog with entries, categories, bookmarks, pings, etc.
     */
    public void testRemoveLoadedWeblog() throws Exception {
        // TODO: implement testRemoveLoadedWeblog
    }
    
}
