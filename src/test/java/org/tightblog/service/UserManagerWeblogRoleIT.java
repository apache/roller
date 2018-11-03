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

import java.util.List;

import org.tightblog.WebloggerTest;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.UserWeblogRole;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test WeblogRole related business operations.
 */
public class UserManagerWeblogRoleIT extends WebloggerTest {
    private User testUser;
    private Weblog testWeblog;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("roleTestUser");
        testWeblog = setupWeblog("role-test-weblog", testUser);
    }

    @After
    public void tearDown() {
        weblogManager.removeWeblog(testWeblog);
        userManager.removeUser(testUser);
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testUserWeblogRoleCRUD() {
        UserWeblogRole p1 = new UserWeblogRole(testUser, testWeblog, WeblogRole.POST);
        assertTrue(p1.getWeblogRole() == WeblogRole.POST);

        UserWeblogRole role;
         
        // delete weblog roles
        role = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(testUser, testWeblog);
        assertNotNull(role);
        userWeblogRoleRepository.delete(role);

        // check that delete was successful
        role = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(testUser, testWeblog);
        assertNull(role);
        
        // create weblog roles
        userManager.grantWeblogRole(testUser, testWeblog, WeblogRole.OWNER, false);

        // check that create was successful
        role = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(testUser, testWeblog);
        assertNotNull(role);
        assertSame(role.getWeblogRole(), WeblogRole.OWNER);

        // revoke role
        userWeblogRoleRepository.delete(role);

        // add only draft role
        userManager.grantWeblogRole(testUser, testWeblog, WeblogRole.EDIT_DRAFT, false);

        // check that user has draft weblog role only
        role = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(testUser, testWeblog);
        assertNotNull(role);
        assertSame(role.getWeblogRole(), WeblogRole.EDIT_DRAFT);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testWeblogRoleLookups() throws Exception {
        // we need a second user for this test
        User user = setupUser("testWeblogRoleLookups");

        UserWeblogRole role;
        List<UserWeblogRole> roles;

        // get all weblog roles for a user
        roles = userWeblogRoleRepository.findByUser(user);
        assertEquals(0, roles.size());
        roles = userWeblogRoleRepository.findByUserAndPendingFalse(testUser);
        assertEquals(1, roles.size());
        assertFalse(roles.get(0).isPending());

        // get all weblog roles for a weblog
        roles = userWeblogRoleRepository.findByWeblogAndPendingFalse(testWeblog);
        assertEquals(1, roles.size());

        userManager.grantWeblogRole(user, testWeblog, WeblogRole.POST, true);

        // confirm weblog role is pending
        assertTrue(userWeblogRoleRepository.findByUser(user).get(0).isPending());

        // get pending weblog roles for a weblog
        roles = userWeblogRoleRepository.findByWeblogAndPendingTrue(testWeblog);
        assertEquals(1, roles.size());

        // get weblog roles for a specific user/weblog
        role = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(testUser, testWeblog);
        assertNotNull(role);
        assertSame(role.getWeblogRole(), WeblogRole.OWNER);

        // pending weblog roles should not be visible
        role = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(user, testWeblog);
        assertNull(role);

        userManager.removeUser(user);
    }


    /**
     * Tests weblog invitation process.
     */
    @Test
    public void testInvitations() throws Exception {
        // we need a second user for this test
        User user = setupUser("testInvitations");

        // invite user to weblog
        userManager.grantWeblogRole(user, testWeblog, WeblogRole.EDIT_DRAFT, true);
        List<UserWeblogRole> uwrList = userWeblogRoleRepository.findByUser(user);
        assertTrue(uwrList.get(0).isPending());

        // accept invitation
        uwrList.get(0).setPending(false);
        userWeblogRoleRepository.saveAndFlush(uwrList.get(0));

        // re-query now that we have changed things
        user = userRepository.findEnabledByUserName(user.getUserName());
        testWeblog = weblogRepository.findByHandleAndVisibleTrue(testWeblog.getHandle());

        // assert that invitation list is empty
        assertFalse(userWeblogRoleRepository.findByUserAndPendingFalse(user).get(0).isPending());
        assertTrue(userWeblogRoleRepository.findByWeblogAndPendingTrue(testWeblog).isEmpty());

        // assert that user is member of weblog
        assertNotNull(userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(user, testWeblog));
        List<UserWeblogRole> userRoles = userWeblogRoleRepository.findByUserAndPendingFalse(user);
        assertEquals(1, userRoles.size());
        assertEquals(testWeblog.getId(), userRoles.get(0).getWeblog().getId());

        // assert that website has user
        List users = weblogManager.getWeblogUsers(testWeblog);
        assertEquals(2, users.size());

        // test user can be retired from website
        UserWeblogRole uwr = userWeblogRoleRepository.findByUserAndWeblogAndPendingFalse(user, testWeblog);
        userWeblogRoleRepository.delete(uwr);

        userRoles = userWeblogRoleRepository.findByUserAndPendingFalse(user);
        assertEquals(0, userRoles.size());

        userManager.removeUser(user);
    }
    
    
    /**
     * Tests weblog invitation process.
     */
    @Test
    public void testGlobalAdminHasPostWeblogRoleCheck() {
        User adminUser = setupUser("adminUser");
        adminUser.setGlobalRole(GlobalRole.ADMIN);

        // because adminUser is a global admin, they should have POST perm
        assertTrue(userManager.checkWeblogRole(adminUser, testWeblog, WeblogRole.POST));

        userManager.removeUser(adminUser);
    }
}
