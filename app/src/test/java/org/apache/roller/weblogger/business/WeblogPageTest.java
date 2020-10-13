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
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test Weblog Page related business operations.
 */
public class WeblogPageTest  {
    
    public static Log log = LogFactory.getLog(WeblogPageTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    WeblogTemplate testPage = null;

    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
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
        testPage.setAction(ComponentType.WEBLOG);
        testPage.setName("testTemplate");
        testPage.setDescription("Test Weblog Template");
        testPage.setLink("testTemp");
        testPage.setLastModified(new java.util.Date());
        testPage.setWeblog(TestUtils.getManagedWebsite(testWeblog));
    }

    @AfterEach
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
    @Test
    public void testTemplateCRUD() throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogTemplate template;
        
        // create template
        mgr.saveTemplate(testPage);
        TestUtils.endSession(true);
        
        // check that create was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        template = mgr.getTemplateByName(testWeblog, testPage.getName());
        assertNotNull(template);

        // update template
        template.setName("testtesttest");
        mgr.saveTemplate(template);
        TestUtils.endSession(true);
        
        // check that update was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        template = mgr.getTemplateByName(testWeblog, "testtesttest");
        assertNotNull(template);

        // delete template
        mgr.removeTemplate(template);
        TestUtils.endSession(true);
        
        // check that delete was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        template = mgr.getTemplateByName(testWeblog, testPage.getName());
        assertNull(template);
    }
    
    
    /**
     * Test lookup mechanisms ... id, name, link, weblog
     */
    @Test
    public void testPermissionsLookups() throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogTemplate page;
        
        // create page
        mgr.saveTemplate(testPage);
        String id = testPage.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        page = mgr.getTemplate(id);
        assertNotNull(page);

        // lookup by action
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        page = mgr.getTemplateByAction(testWeblog, testPage.getAction());
        assertNotNull(page);

        // lookup by name
        page = mgr.getTemplateByName(testWeblog, testPage.getName());
        assertNotNull(page);

        // lookup by link
        page = mgr.getTemplateByLink(testWeblog, testPage.getLink());
        assertNotNull(page);

        // lookup all pages for weblog
        List pages = mgr.getTemplates(testWeblog);
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        // delete page
        mgr.removeTemplate(page);
        TestUtils.endSession(true);
    }
    
}
