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

import java.util.List;
import org.tightblog.WebloggerTest;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserWeblogRole;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test WeblogRole related business operations.
 */
public class WeblogRoleTestIT extends WebloggerTest {
    private User testUser;
    private Weblog testWeblog;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("permsTestUser");
        testWeblog = setupWeblog("permsTestWeblog", testUser);
        endSession(true);
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testUserWeblogRoleCRUD() throws Exception {
        UserWeblogRole p1 = new UserWeblogRole(testUser, testWeblog, WeblogRole.POST);
        assertTrue(p1.getWeblogRole() == WeblogRole.POST);

        UserWeblogRole role;
         
        // delete weblog roles
        testWeblog = getManagedWeblog(testWeblog);
        testUser = getManagedUser(testUser);
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNotNull(role);
        userManager.revokeWeblogRole(role);
        endSession(true);
        
        // check that delete was successful
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNull(role);
        
        // create weblog roles
        userManager.grantWeblogRole(testUser, testWeblog, WeblogRole.OWNER, false);
        endSession(true);
        
        // check that create was successful
        role = userManager.getWeblogRole(testUser, testWeblog);
        assertNotNull(role);
        assertTrue(role.getWeblogRole() == WeblogRole.OWNER);
        endSession(true);
        
        // revoke role
        userManager.revokeWeblogRole(role);
        endSession(true);
        
        // add only draft role
        userManager.grantWeblogRole(testUser, testWeblog, WeblogRole.EDIT_DRAFT, false);
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

        userManager.grantWeblogRole(user, testWeblog, WeblogRole.POST, true);
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
        teardownUser(user.getId());
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
        userManager.grantWeblogRole(user, testWeblog, WeblogRole.EDIT_DRAFT, true);
        endSession(true);
        List<UserWeblogRole> uwrList = userManager.getWeblogRolesIncludingPending(user);
        assertTrue(uwrList.get(0).isPending());

        // accept invitation
        userManager.acceptWeblogInvitation(uwrList.get(0));
        endSession(true);

        // re-query now that we have changed things
        user = userManager.getEnabledUserByUserName(user.getUserName());
        testWeblog = weblogManager.getWeblogByHandle(testWeblog.getHandle());

        // assert that invitation list is empty
        testWeblog = getManagedWeblog(testWeblog);
        assertFalse(userManager.getWeblogRoles(user).get(0).isPending());
        assertTrue(userManager.getPendingWeblogRoles(testWeblog).isEmpty());

        // assert that user is member of weblog
        assertNotNull(userManager.getWeblogRole(user, testWeblog));
        List<UserWeblogRole> userRoles = userManager.getWeblogRoles(user);
        assertEquals(1, userRoles.size());
        assertEquals(testWeblog.getId(), userRoles.get(0).getWeblog().getId());

        // assert that website has user
        List users = weblogManager.getWeblogUsers(testWeblog);
        assertEquals(2, users.size());

        // test user can be retired from website
        UserWeblogRole uwr = userManager.getWeblogRole(user, testWeblog);
        userManager.revokeWeblogRole(uwr);
        endSession(true);

        userRoles = userManager.getWeblogRoles(user);
        assertEquals(0, userRoles.size());

        // cleanup the extra test user
        teardownUser(user.getId());
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
        teardownUser(adminUser.getId());
        endSession(true);
    }
}
