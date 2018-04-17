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

import java.time.Instant;
import java.util.List;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.Template.ComponentType;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test Weblog Page related business operations.
 */
public class WeblogPageTest extends WebloggerTest {

    private User testUser;
    private Weblog testWeblog;
    private WeblogTemplate testPage;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("wtTestUser");
        testWeblog = setupWeblog("wtTestWeblog", testUser);
        endSession(true);

        testPage = new WeblogTemplate();
        testPage.setRole(ComponentType.WEBLOG);
        testPage.setName("testTemplate");
        testPage.setDescription("Test Weblog Template");
        testPage.setRelativePath("testTemp");
        testPage.setLastModified(Instant.now());
        testPage.setWeblog(getManagedWeblog(testWeblog));
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
        testPage = null;
    }
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testTemplateCRUD() throws Exception {
        WeblogTemplate template;
        
        // create template
        weblogManager.saveTemplate(testPage);
        endSession(true);
        
        // check that create was successful
        testWeblog = getManagedWeblog(testWeblog);
        template = weblogManager.getTemplateByName(testWeblog, testPage.getName());
        assertNotNull(template);

        // update template
        template.setName("testtesttest");
        weblogManager.saveTemplate(template);
        endSession(true);
        
        // check that update was successful
        testWeblog = getManagedWeblog(testWeblog);
        template = weblogManager.getTemplateByName(testWeblog, "testtesttest");
        assertNotNull(template);

        // delete template
        weblogManager.removeTemplate(template);
        endSession(true);
        
        // check that delete was successful
        testWeblog = getManagedWeblog(testWeblog);
        template = weblogManager.getTemplateByName(testWeblog, testPage.getName());
        assertNull(template);
    }
    
    
    /**
     * Test lookup mechanisms ... id, name, link, weblog
     */
    @Test
    public void testPermissionsLookups() throws Exception {
        WeblogTemplate page;
        
        // create page
        weblogManager.saveTemplate(testPage);
        String id = testPage.getId();
        endSession(true);
        
        // lookup by id
        page = weblogManager.getTemplate(id);
        assertNotNull(page);

        // lookup by action
        testWeblog = getManagedWeblog(testWeblog);
        page = weblogManager.getTemplateByAction(testWeblog, testPage.getRole());
        assertNotNull(page);

        // lookup by name
        page = weblogManager.getTemplateByName(testWeblog, testPage.getName());
        assertNotNull(page);

        // lookup by link
        page = weblogManager.getTemplateByPath(testWeblog, testPage.getRelativePath());
        assertNotNull(page);

        // lookup all pages for weblog
        List pages = weblogManager.getTemplates(testWeblog);
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        // delete page
        weblogManager.removeTemplate(page);
        endSession(true);
    }
    
}
