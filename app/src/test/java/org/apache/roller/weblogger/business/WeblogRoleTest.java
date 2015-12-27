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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test WeblogRole related business operations.
 */
public class WeblogRoleTest extends WebloggerTest {
    public static Log log = LogFactory.getLog(WeblogRoleTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        try {
            testUser = setupUser("permsTestUser");
            testWeblog = setupWeblog("permsTestWeblog", testUser);
            endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in setup", ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            teardownUser(testUser.getUserName());
            teardownWeblog(testWeblog.getId());
            endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tear down", ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testUserWeblogRoleCRUD() throws Exception {
        UserWeblogRole p1 = new UserWeblogRole(testUser.getUserName(), testWeblog.getId(), WeblogRole.POST);
        assertTrue(p1.getWeblogRole() == WeblogRole.POST);

        UserWeblogRole role;
         
        // delete weblog roles
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNotNull(role);
        userManager.revokeWeblogRole(testUser.getUserName(), testWeblog.getId());
        endSession(true);
        
        // check that delete was successful
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNull(role);
        
        // create weblog roles
        userManager.grantWeblogRole(testUser.getUserName(), testWeblog.getId(), WeblogRole.OWNER);
        endSession(true);
        
        // check that create was successful
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNotNull(role);
        assertTrue(role.getWeblogRole() == WeblogRole.OWNER);
        endSession(true);
        
        // revoke role
        userManager.revokeWeblogRole(role.getUserName(), role.getWeblogId());
        endSession(true);
        
        // add only draft role
        userManager.grantWeblogRole(testUser.getUserName(), testWeblog.getId(), WeblogRole.EDIT_DRAFT);
        endSession(true);

        // check that user has draft weblog role only
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNotNull(role);
        assertTrue(role.getWeblogRole() == WeblogRole.EDIT_DRAFT);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testWeblogRoleLookups() throws Exception {
        // we need a second user for this test
        User user = setupUser("testWeblogRoleLookups");
        endSession(true);
        user = getManagedUser(user);

        UserWeblogRole perm;
        List<UserWeblogRole> perms;

        // get all weblog roles for a user
        perms = userManager.getWeblogRolesIncludingPending(getManagedUser(user));
        assertEquals(0, perms.size());
        perms = userManager.getWeblogRoles(getManagedUser(testUser));
        assertEquals(1, perms.size());
        assertFalse(perms.get(0).isPending());

        // get all weblog roles for a weblog
        perms = userManager.getWeblogRoles(getManagedWeblog(testWeblog));
        assertEquals(1, perms.size());

        userManager.grantPendingWeblogRole(user, testWeblog, WeblogRole.POST);
        endSession(true);

        // confirm weblog role is pending
        user = getManagedUser(user);
        assertTrue(userManager.getWeblogRolesIncludingPending(user).get(0).isPending());

        // get pending weblog roles for a weblog
        perms = userManager.getPendingWeblogRoles(getManagedWeblog(testWeblog));
        assertEquals(1, perms.size());            

        // get weblog roles for a specific user/weblog
        perm = userManager.getWeblogRole(
                getManagedUser(testUser), getManagedWeblog(testWeblog)
        );
        assertNotNull(perm);
        assertTrue(perm.getWeblogRole() == WeblogRole.OWNER);

        // pending weblog roles should not be visible
        perm = userManager.getWeblogRole(
                getManagedUser(user), getManagedWeblog(testWeblog)
        );
        assertNull(perm);

        // cleanup
        teardownUser(user.getUserName());
        endSession(true);
    }


    /**
     * Tests weblog invitation process.
     */
    @Test
    public void testInvitations() throws Exception {
        // we need a second user for this test
        User user = setupUser("testInvitations");
        endSession(true);

        // invite user to weblog
        userManager.grantPendingWeblogRole(user, testWeblog, WeblogRole.EDIT_DRAFT);
        endSession(true);
        assertTrue(userManager.getWeblogRolesIncludingPending(user).get(0).isPending());

        // accept invitation
        userManager.acceptWeblogInvitation(user, testWeblog);
        endSession(true);

        // re-query now that we have changed things
        user = userManager.getUserByUserName(user.getUserName());
        testWeblog = weblogManager.getWeblogByHandle(testWeblog.getHandle());

        // assert that invitation list is empty
        testWeblog = getManagedWeblog(testWeblog);
        assertFalse(userManager.getWeblogRoles(user).get(0).isPending());
        assertTrue(userManager.getPendingWeblogRoles(testWeblog).isEmpty());

        // assert that user is member of weblog
        assertNotNull(userManager.getWeblogRole(user, testWeblog));
        List weblogs = weblogManager.getUserWeblogs(getManagedUser(user), true);
        assertEquals(1, weblogs.size());
        assertEquals(testWeblog.getId(), ((Weblog)weblogs.get(0)).getId());

        // assert that website has user
        List users = weblogManager.getWeblogUsers(testWeblog, true);
        assertEquals(2, users.size());

        // test user can be retired from website
        userManager.revokeWeblogRole(user.getUserName(), testWeblog.getId());
        endSession(true);

        //user = userManager.getUser(user.getId());
        weblogs = weblogManager.getUserWeblogs(user, true);
        assertEquals(0, weblogs.size());

        // cleanup the extra test user
        teardownUser(user.getUserName());
        endSession(true);
    }
    
    
    /**
     * Tests weblog invitation process.
     */
    @Test
    public void testGlobalAdminHasPostWeblogRoleCheck() throws Exception {
        assertTrue(userManager.checkWeblogRole(testUser, testWeblog, WeblogRole.POST));
        
        // we need a second user for this test
        User adminUser = setupUser("adminUser");
        adminUser.setGlobalRole(GlobalRole.ADMIN);
        endSession(true);

        // because adminUser is a global admin, they should have POST perm
        assertTrue(userManager.checkWeblogRole(adminUser, testWeblog, WeblogRole.POST));

        // cleanup the extra test user
        teardownUser(adminUser.getUserName());
        endSession(true);
    }
}
