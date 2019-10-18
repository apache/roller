/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.User;
import org.tightblog.domain.UserStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test User related business operations.
 */
public class UserManagerIT extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        userDao.deleteAll();
    }
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testUserCRUD() {
        User user;
        
        User testUser = new User();
        testUser.setUserName("testuser");
        testUser.setScreenName("Test User Screen Name");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setDateCreated(Instant.now());
        testUser.setStatus(UserStatus.ENABLED);
        testUser.setGlobalRole(GlobalRole.BLOGGER);
        
        // make sure test user does not exist
        user = userDao.findEnabledByUserName(testUser.getUserName());
        assertNull(user);
        userDao.evictUser(testUser);
        
        // add test user
        userDao.saveAndFlush(testUser);
        String userName = testUser.getUserName();

        // make sure test user exists
        user = userDao.findEnabledByUserName(userName);
        assertNotNull(user);
        assertEquals(testUser, user);
        
        // modify user and save
        user.setScreenName("testtesttest");
        userDao.saveAndFlush(user);

        // make sure changes were saved
        user = userDao.findEnabledByUserName(userName);
        assertNotNull(user);
        assertEquals("testtesttest", user.getScreenName());

        // remove test user
        userManager.removeUser(user);

        // make sure user no longer exists
        user = userDao.findEnabledByUserName(userName);
        assertNull(user);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testUserLookups() {
        User user1;

        // add test users
        User testUser = setupUser("usertestuser");
        User testUser2 = setupUser("disabledtestuser");
        testUser2.setStatus(UserStatus.DISABLED);
        userDao.saveAndFlush(testUser2);

        // lookup by username
        user1 = userDao.findEnabledByUserName(testUser.getUserName());
        assertNotNull(user1);
        assertEquals(testUser.getUserName(), user1.getUserName());
        userDao.evictUser(testUser);

        // lookup by getUsers() - all users
        List<User> users = userDao.findAll();
        assertEquals(2, users.size());

        // lookup by getUsers() - enabled only
        users = userDao.findByStatusEnabled();
        assertEquals(1, users.size());
        user1 = users.get(0);
        assertNotNull(user1);
        assertEquals(testUser.getScreenName(), user1.getScreenName());

        // make sure disable users are not returned
        user1.setStatus(UserStatus.DISABLED);
        userDao.saveAndFlush(user1);
        user1 = userDao.findEnabledByUserName(testUser.getUserName());
        assertNull(user1);
        
        userManager.removeUser(testUser);
    }
    
    
    /**
     * Test basic user role persistence ... Add, Remove
     */
    @Test
    public void testRoleCRUD() {
        User user;
        User testUser = setupUser("roletestuser");
        user = userDao.findEnabledByUserName(testUser.getUserName());
        assertNotNull(user);
        assertEquals(GlobalRole.BLOGGER, user.getGlobalRole());
        user.setGlobalRole(GlobalRole.BLOGCREATOR);
        userDao.saveAndFlush(user);

        // check that role was switched
        user = userDao.findEnabledByUserName(testUser.getUserName());
        assertNotNull(user);
        assertEquals(GlobalRole.BLOGCREATOR, user.getGlobalRole());
        userManager.removeUser(testUser);
    }
}
