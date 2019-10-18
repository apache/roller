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
package org.tightblog.service;

import java.time.Instant;
import java.util.List;

import org.tightblog.WebloggerTest;
import org.tightblog.domain.Template.Role;
import org.tightblog.domain.User;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test Weblog Page related business operations.
 */
public class ThemeManagerIT extends WebloggerTest {

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
        testWeblog = setupWeblog("wt-test-weblog", testUser);

        testPage = new WeblogTemplate();
        testPage.setRole(Role.WEBLOG);
        testPage.setName("testTemplate");
        testPage.setDescription("Test Weblog Template");
        testPage.setLastModified(Instant.now());
        testPage.setWeblog(testWeblog);
    }
    
    @After
    public void tearDown() {
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
        testPage = null;
    }
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testTemplateCRUD() {
        WeblogTemplate template;
        
        // create template
        weblogTemplateDao.save(testPage);

        // check that create was successful
        template = weblogTemplateDao.findByWeblogAndName(testWeblog, testPage.getName());
        assertNotNull(template);

        // update template
        template.setName("testtesttest");
        weblogTemplateDao.save(template);

        // check that update was successful
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        template = weblogTemplateDao.findByWeblogAndName(testWeblog, "testtesttest");
        assertNotNull(template);

        // delete template
        weblogTemplateDao.delete(template);
        weblogManager.evictWeblogTemplateCaches(template.getWeblog(), testPage.getName(), template.getRole());

        // check that delete was successful
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        template = weblogTemplateDao.findByWeblogAndName(testWeblog, testPage.getName());
        assertNull(template);
    }
    
    
    /**
     * Test lookup mechanisms ... id, name, link, weblog
     */
    @Test
    public void testPermissionsLookups() {
        WeblogTemplate page;
        
        // create page
        weblogTemplateDao.save(testPage);
        String id = testPage.getId();

        // lookup by id
        page = weblogTemplateDao.findById(id).orElse(null);
        assertNotNull(page);

        // lookup by action
        testWeblog = weblogDao.findByIdOrNull(testWeblog.getId());
        page = weblogTemplateDao.findByWeblogAndRole(testWeblog, testPage.getRole());
        assertNotNull(page);

        // lookup by name
        page = weblogTemplateDao.findByWeblogAndName(testWeblog, testPage.getName());
        assertNotNull(page);

        // lookup all pages for weblog
        List pages = weblogTemplateDao.getWeblogTemplateMetadata(testWeblog);
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        // delete page
        weblogTemplateDao.delete(page);
    }
    
}
