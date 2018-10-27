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
package org.tightblog.business;

import java.time.Instant;
import java.util.List;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.User;
import org.tightblog.pojos.UserStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test User related business operations.
 */
public class UserTestIT extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        userRepository.deleteAll();
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
        user = userRepository.findEnabledByUserName(testUser.getUserName());
        assertNull(user);
        
        // add test user
        userRepository.saveAndFlush(testUser);
        String userName = testUser.getUserName();

        // make sure test user exists
        user = userRepository.findEnabledByUserName(userName);
        assertNotNull(user);
        assertEquals(testUser, user);
        
        // modify user and save
        user.setScreenName("testtesttest");
        userRepository.saveAndFlush(user);

        // make sure changes were saved
        user = userRepository.findEnabledByUserName(userName);
        assertNotNull(user);
        assertEquals("testtesttest", user.getScreenName());

        // remove test user
        userManager.removeUser(user);

        // make sure user no longer exists
        user = userRepository.findEnabledByUserName(userName);
        assertNull(user);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testUserLookups() throws Exception {
        User user1;
        User user2;

        // add test users
        User testUser = setupUser("usertestuser");
        User testUser2 = setupUser("disabledtestuser");
        testUser2.setStatus(UserStatus.DISABLED);
        userRepository.saveAndFlush(testUser2);

        // lookup by username
        user1 = userRepository.findEnabledByUserName(testUser.getUserName());
        assertNotNull(user1);
        assertEquals(testUser.getUserName(), user1.getUserName());
        
        // lookup by id
        String userName = user1.getUserName();
        user1 = userRepository.findEnabledByUserName(userName);
        assertNotNull(user1);
        assertEquals(testUser.getUserName(), user1.getUserName());
        
        // lookup by getUsers() - all users
        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());

        // lookup by getUsers() - enabled only
        users = userRepository.findByStatusEnabled();
        assertEquals(1, users.size());
        user1 = users.get(0);
        assertNotNull(user1);
        assertEquals(testUser.getScreenName(), user1.getScreenName());

        // make sure disable users are not returned
        user1.setStatus(UserStatus.DISABLED);
        userRepository.saveAndFlush(user1);
        user1 = userRepository.findEnabledByUserName(testUser.getUserName());
        assertNull(user1);
        
        userManager.removeUser(testUser);
    }
    
    
    /**
     * Test basic user role persistence ... Add, Remove
     */
    @Test
    public void testRoleCRUD() throws Exception {
        User user;
        
        // add test user
        User testUser = setupUser("roletestuser");

        user = userRepository.findEnabledByUserName(testUser.getUserName());
        assertNotNull(user);
        
        assertTrue(GlobalRole.BLOGGER.equals(user.getGlobalRole()));

        user.setGlobalRole(GlobalRole.BLOGCREATOR);
        userRepository.saveAndFlush(user);

        // check that role was switched
        user = userRepository.findEnabledByUserName(testUser.getUserName());
        assertNotNull(user);
        assertTrue(user.getGlobalRole() == GlobalRole.BLOGCREATOR);

        userManager.removeUser(testUser);
    }

}
