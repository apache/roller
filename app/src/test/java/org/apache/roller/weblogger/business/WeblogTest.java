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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test Weblog related business operations.
 */
public class WeblogTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogTest.class);
    
    User testUser = null;
    
    
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
        
        log.info("BEGIN");
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("weblogTestUser");
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        log.info("END");
    }
    
    public void tearDown() throws Exception {
        
        log.info("BEGIN");
        
        try {
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testWeblogCRUD() throws Exception {
        
        log.info("BEGIN");
        
        try {
        
            WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

            Weblog weblog = null;

            Weblog testWeblog = new Weblog();
            testUser = TestUtils.getManagedUser(testUser);
            testWeblog.setName("Test Weblog");
            testWeblog.setTagline("Test Weblog");
            testWeblog.setHandle("testweblog");
            testWeblog.setEmailAddress("testweblog@dev.null");
            testWeblog.setEditorPage("editor-text.jsp");
            testWeblog.setBlacklist("");
            testWeblog.setEditorTheme("basic");
            testWeblog.setLocale("en_US");
            testWeblog.setTimeZone("America/Los_Angeles");
            testWeblog.setDateCreated(new java.util.Date());
            testWeblog.setCreatorUserName(testUser.getUserName());

            // make sure test weblog does not exist
            weblog = mgr.getWeblogByHandle(testWeblog.getHandle());
            assertNull(weblog);

            // add test weblog
            mgr.addWeblog(testWeblog);
            String id = testWeblog.getId();
            TestUtils.endSession(true);

            // make sure test weblog exists
            weblog = null;
            weblog = mgr.getWeblog(id);
            assertNotNull(weblog);
            assertEquals(testWeblog, weblog);

            // modify weblog and save
            weblog.setName("testtesttest");
            mgr.saveWeblog(weblog);
            TestUtils.endSession(true);

            // make sure changes were saved
            weblog = null;
            weblog = mgr.getWeblog(id);
            assertNotNull(weblog);
            assertEquals("testtesttest", weblog.getName());

            // remove test weblog
            mgr.removeWeblog(weblog);
            TestUtils.endSession(true);

            // make sure weblog no longer exists
            weblog = null;
            weblog = mgr.getWeblog(id);
            assertNull(weblog);
        
        } catch(Throwable t) {
            log.error("Exception running test", t);
            throw (Exception) t;
        }
        log.info("END");
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testWeblogLookups() throws Exception {
        
        log.info("BEGIN");
        Weblog testWeblog1 = null;
        Weblog testWeblog2 = null;
        try {
            WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

            Weblog weblog = null;
            
            // add test weblogs
            testWeblog1 = TestUtils.setupWeblog("testWeblog1", testUser);
            testWeblog2 = TestUtils.setupWeblog("testWeblog2", testUser);
            TestUtils.endSession(true);
            
            // lookup by id
            weblog = mgr.getWeblog(testWeblog1.getId());
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // lookup by weblog handle
            weblog = null;
            weblog = mgr.getWeblogByHandle(testWeblog1.getHandle());
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // make sure disabled weblogs are not returned
            weblog.setVisible(Boolean.FALSE);
            mgr.saveWeblog(weblog);
            TestUtils.endSession(true);
            weblog = null;
            weblog = mgr.getWeblogByHandle(testWeblog1.getHandle());
            assertNull(weblog);
            
            // restore visible state
            weblog = mgr.getWeblogByHandle(testWeblog1.getHandle(), Boolean.FALSE);
            weblog.setVisible(Boolean.TRUE);
            mgr.saveWeblog(weblog);
            TestUtils.endSession(true);
            weblog = null;
            weblog = mgr.getWeblogByHandle(testWeblog1.getHandle());
            assertNotNull(weblog);
            
            // get all weblogs for user
            weblog = null;
            List weblogs1 = mgr.getUserWeblogs(TestUtils.getManagedUser(testUser), true);
            assertEquals(2, weblogs1.size());
            weblog = (Weblog) weblogs1.get(0);
            assertNotNull(weblog);           
            
            // make sure disabled weblogs are not returned
            weblog.setVisible(Boolean.FALSE);
            mgr.saveWeblog(weblog);
            TestUtils.endSession(true);
            List weblogs2 = mgr.getUserWeblogs(TestUtils.getManagedUser(testUser), true);
            assertEquals(1, weblogs2.size());
            weblog = (Weblog) weblogs2.get(0);
            assertNotNull(weblog);
            
            // make sure inactive weblogs are not returned
            weblog.setActive(Boolean.FALSE);
            mgr.saveWeblog(weblog);
            TestUtils.endSession(true);
            List weblogs3 = mgr.getUserWeblogs(TestUtils.getManagedUser(testUser), true);
            assertEquals(0, weblogs3.size());
            
        } finally {
            TestUtils.teardownWeblog(testWeblog1.getId());
            TestUtils.teardownWeblog(testWeblog2.getId());
            TestUtils.endSession(true);
        }
        
        log.info("END");
    }
    
}

