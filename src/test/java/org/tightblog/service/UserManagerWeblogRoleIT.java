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
        assertEquals(WeblogRole.POST, p1.getWeblogRole());

        UserWeblogRole role;
         
        // delete weblog roles
        role = userWeblogRoleDao.findByUserAndWeblog(testUser, testWeblog);
        assertNotNull(role);
        userManager.deleteUserWeblogRole(role);

        // check that delete was successful
        role = userWeblogRoleDao.findByUserAndWeblog(testUser, testWeblog);
        assertNull(role);
        
        // create weblog roles
        userManager.grantWeblogRole(testUser, testWeblog, WeblogRole.OWNER);

        // check that create was successful
        role = userWeblogRoleDao.findByUserAndWeblog(testUser, testWeblog);
        assertNotNull(role);
        assertSame(role.getWeblogRole(), WeblogRole.OWNER);

        // revoke role
        userManager.deleteUserWeblogRole(role);

        // add only draft role
        userManager.grantWeblogRole(testUser, testWeblog, WeblogRole.EDIT_DRAFT);

        // check that user has draft weblog role only
        role = userWeblogRoleDao.findByUserAndWeblog(testUser, testWeblog);
        assertNotNull(role);
        assertSame(role.getWeblogRole(), WeblogRole.EDIT_DRAFT);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testWeblogRoleLookups() {
        // we need a second user for this test
        User user = setupUser("testWeblogRoleLookups");

        UserWeblogRole role;
        List<UserWeblogRole> roles;

        // get all weblog roles for a user
        roles = userWeblogRoleDao.findByUser(user);
        assertEquals(0, roles.size());
        roles = userWeblogRoleDao.findByUser(testUser);
        assertEquals(1, roles.size());

        // get all weblog roles for a weblog
        roles = userWeblogRoleDao.findByWeblog(testWeblog);
        assertEquals(1, roles.size());

        userManager.grantWeblogRole(user, testWeblog, WeblogRole.POST);

        // get weblog roles for a specific user/weblog
        role = userWeblogRoleDao.findByUserAndWeblog(testUser, testWeblog);
        assertNotNull(role);
        assertSame(role.getWeblogRole(), WeblogRole.OWNER);

        userManager.removeUser(user);
    }


    /**
     * Tests weblog member addition process
     */
    @Test
    public void testAddUserToWeblog() {
        // we need a second user for this test
        User user = setupUser("testInvitations");

        // invite user to weblog
        userManager.grantWeblogRole(user, testWeblog, WeblogRole.EDIT_DRAFT);

        // re-query now that we have changed things
        user = userDao.findEnabledByUserName(user.getUserName());
        testWeblog = weblogDao.findByHandleAndVisibleTrue(testWeblog.getHandle());

        // assert that user is member of weblog
        assertNotNull(userWeblogRoleDao.findByUserAndWeblog(user, testWeblog));
        List<UserWeblogRole> userRoles = userWeblogRoleDao.findByUser(user);
        assertEquals(1, userRoles.size());
        assertEquals(testWeblog.getId(), userRoles.get(0).getWeblog().getId());

        // assert that website has user
        List users = userWeblogRoleDao.findByWeblogAndStatusEnabled(testWeblog);
        assertEquals(2, users.size());

        // test user can be retired from website
        UserWeblogRole uwr = userWeblogRoleDao.findByUserAndWeblog(user, testWeblog);
        userManager.deleteUserWeblogRole(uwr);

        userRoles = userWeblogRoleDao.findByUser(user);
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
