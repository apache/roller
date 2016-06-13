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

import java.time.LocalDateTime;
import java.util.List;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test Weblog related business operations.
 */
public class WeblogTest extends WebloggerTest {
    User testUser = null;
    
    /**
     * All tests in this suite require a user.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("weblogTestUser");
        endSession(true);
    }

    @After
    public void tearDown() throws Exception {
        teardownUser(testUser.getUserName());
        endSession(true);
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testWeblogCRUD() throws Exception {
        Weblog weblog;

        Weblog testWeblog = new Weblog();
        testUser = getManagedUser(testUser);
        testWeblog.setName("Test Weblog");
        testWeblog.setTagline("Test Weblog");
        testWeblog.setHandle("testweblog");
        testWeblog.setEditorPage("editor-text.jsp");
        testWeblog.setBlacklist("");
        testWeblog.setTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(LocalDateTime.now());
        testWeblog.setCreatorId(testUser.getId());

        // make sure test weblog does not exist
        weblog = weblogManager.getWeblogByHandle(testWeblog.getHandle());
        assertNull(weblog);

        // add test weblog
        weblogManager.addWeblog(testWeblog);
        String id = testWeblog.getId();
        endSession(true);

        // make sure test weblog exists
        weblog = weblogManager.getWeblog(id);
        assertNotNull(weblog);
        assertEquals(testWeblog, weblog);

        // modify weblog and save
        weblog.setName("testtesttest");
        weblogManager.saveWeblog(weblog);
        endSession(true);

        // make sure changes were saved
        weblog = weblogManager.getWeblog(id);
        assertNotNull(weblog);
        assertEquals("testtesttest", weblog.getName());

        // remove test weblog
        weblogManager.removeWeblog(weblog);
        endSession(true);

        // make sure weblog no longer exists
        weblog = weblogManager.getWeblog(id);
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
            
            // add test weblogs
            testWeblog1 = setupWeblog("testWeblog1", testUser);
            testWeblog2 = setupWeblog("testWeblog2", testUser);
            endSession(true);
            
            // lookup by id
            weblog = weblogManager.getWeblog(testWeblog1.getId());
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // lookup by weblog handle
            weblog = weblogManager.getWeblogByHandle(testWeblog1.getHandle());
            assertNotNull(weblog);
            assertEquals(testWeblog1.getHandle(), weblog.getHandle());
            
            // make sure disabled weblogs are not returned
            weblog.setVisible(Boolean.FALSE);
            weblogManager.saveWeblog(weblog);
            endSession(true);
            weblog = weblogManager.getWeblogByHandle(testWeblog1.getHandle());
            assertNull(weblog);
            
            // restore visible state
            weblog = weblogManager.getWeblogByHandle(testWeblog1.getHandle(), Boolean.FALSE);
            weblog.setVisible(Boolean.TRUE);
            weblogManager.saveWeblog(weblog);
            endSession(true);
            weblog = weblogManager.getWeblogByHandle(testWeblog1.getHandle());
            assertNotNull(weblog);
            
            // get all weblogs for user
            List<UserWeblogRole> userRoles = userManager.getWeblogRoles(testUser);
            assertEquals(2, userRoles.size());
            weblog = userRoles.get(0).getWeblog();
            assertNotNull(weblog);

        } finally {
            if (testWeblog1 != null) {
                teardownWeblog(testWeblog1.getId());
            }
            if (testWeblog2 != null) {
                teardownWeblog(testWeblog2.getId());
            }
            endSession(true);
        }
    }
}
