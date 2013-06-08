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
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test Weblog Page related business operations.
 */
public class WeblogPageTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogPageTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    WeblogTemplate testPage = null;
    
    
    public WeblogPageTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(WeblogPageTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("wtTestUser");
            testWeblog = TestUtils.setupWeblog("wtTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        testPage = new WeblogTemplate();
        testPage.setAction(WeblogTemplate.ACTION_WEBLOG);
        testPage.setName("testTemplate");
        testPage.setDescription("Test Weblog Template");
        testPage.setLink("testTemp");
        testPage.setContents("a test weblog template.");
        testPage.setLastModified(new java.util.Date());
        testPage.setWebsite(TestUtils.getManagedWebsite(testWeblog));
        testPage.setTemplateLanguage("velocity");
        testPage.setType("standard");
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        testPage = null;
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testTemplateCRUD() throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogTemplate template = null;
        
        // create template
        mgr.savePage(testPage);
        TestUtils.endSession(true);
        
        // check that create was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        template = null;
        template = mgr.getPageByName(testWeblog, testPage.getName());
        assertNotNull(template);
        assertEquals(testPage.getContents(), template.getContents());
        
        // update template
        template.setName("testtesttest");
        mgr.savePage(template);
        TestUtils.endSession(true);
        
        // check that update was successful
        template = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        template = mgr.getPageByName(testWeblog, "testtesttest");
        assertNotNull(template);
        assertEquals(testPage.getContents(), template.getContents());
        
        // delete template
        mgr.removePage(template);
        TestUtils.endSession(true);
        
        // check that delete was successful
        template = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        template = mgr.getPageByName(testWeblog, testPage.getName());
        assertNull(template);
    }
    
    
    /**
     * Test lookup mechanisms ... id, name, link, weblog
     */
    public void testPermissionsLookups() throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogTemplate page = null;
        
        // create page
        mgr.savePage(testPage);
        String id = testPage.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        page = mgr.getPage(id);
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup by action
        page = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        page = mgr.getPageByAction(testWeblog, testPage.getAction());
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup by name
        page = null;
        page = mgr.getPageByName(testWeblog, testPage.getName());
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup by link
        page = null;
        page = mgr.getPageByLink(testWeblog, testPage.getLink());
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup all pages for weblog
        List pages = mgr.getPages(testWeblog);
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        // delete page
        mgr.removePage(page);
        TestUtils.endSession(true);
    }
    
}
