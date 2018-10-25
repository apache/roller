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
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tightblog.pojos.WeblogRole;

import static org.junit.Assert.*;

/**
 * Test Weblog related business operations.
 */
public class WeblogTestIT extends WebloggerTest {
    User testUser;
    
    /**
     * All tests in this suite require a user.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("weblogTestUser");
    }

    @After
    public void tearDown() throws Exception {
        teardownUser(testUser.getId());
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testWeblogCRUD() throws Exception {
        Weblog weblog;

        Weblog testWeblog = new Weblog();
        testWeblog.setName("Test Weblog");
        testWeblog.setTagline("Test Weblog");
        testWeblog.setHandle("testweblog");
        testWeblog.setEditFormat(Weblog.EditFormat.HTML);
        testWeblog.setBlacklist("");
        testWeblog.setTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(Instant.now());
        testWeblog.setCreator(testUser);

        // make sure test weblog does not exist
        weblog = weblogRepository.findByHandleAndVisibleTrue(testWeblog.getHandle());
        assertNull(weblog);

        // add test weblog
        weblogManager.addWeblog(testWeblog);
        String id = testWeblog.getId();

        // make sure test weblog exists
        weblog = weblogRepository.findById(id).orElse(null);
        assertNotNull(weblog);
        assertEquals(testWeblog, weblog);

        // modify weblog and save
        weblog.setName("testtesttest");
        weblogManager.saveWeblog(weblog);

        // make sure changes were saved
        weblog = weblogRepository.findById(id).orElse(null);
        assertNotNull(weblog);
        assertEquals("testtesttest", weblog.getName());

        // remove test weblog
        weblogManager.removeWeblog(weblog);

        // make sure weblog no longer exists
        weblog = weblogRepository.findById(id).orElse(null);
        assertNull(weblog);
    }
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testWeblogLookups() throws Exception {
        Weblog testWeblog1 = null;
        Weblog testWeblog2 = null;
        try {
            Weblog weblog;

            // start with no permissions
            userWeblogRoleRepository.deleteByUser(testUser);

            List<UserWeblogRole> userRoles = userWeblogRoleRepository.findByUserAndPendingFalse(testUser);
            assertEquals(0, userRoles.size());

            // add test weblogs
            testWeblog1 = setupWeblog("test-weblog1", testUser);
            testWeblog2 = setupWeblog("test-weblog2", testUser);

            // lookup by id
            weblog = weblogRepository.findById(testWeblog1.getId()).orElse(null);
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // lookup by weblog handle
            weblog = weblogRepository.findByHandleAndVisibleTrue(testWeblog1.getHandle());
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // make sure disabled weblogs are not returned
            weblog.setVisible(Boolean.FALSE);
            weblogManager.saveWeblog(weblog);
            weblog = weblogRepository.findByHandleAndVisibleTrue(testWeblog1.getHandle());
            assertNull(weblog);
            
            // restore visible state
            weblog = weblogRepository.findByHandle(testWeblog1.getHandle());
            weblog.setVisible(Boolean.TRUE);
            weblogManager.saveWeblog(weblog);
            weblog = weblogRepository.findByHandleAndVisibleTrue(testWeblog1.getHandle());
            assertNotNull(weblog);
            
            userManager.grantWeblogRole(testUser, testWeblog1, WeblogRole.EDIT_DRAFT, true);

            // get all weblogs for user
            userRoles = userWeblogRoleRepository.findByUserAndPendingFalse(testUser);
            assertEquals(1, userRoles.size());

            userRoles = userWeblogRoleRepository.findByUser(testUser);
            assertEquals(2, userRoles.size());

        } finally {
            if (testWeblog1 != null) {
                teardownWeblog(testWeblog1.getId());
            }
            if (testWeblog2 != null) {
                teardownWeblog(testWeblog2.getId());
            }
        }
    }
}
