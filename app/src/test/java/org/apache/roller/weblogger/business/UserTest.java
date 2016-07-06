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
package org.apache.roller.weblogger.business;

import java.time.Instant;
import java.util.List;

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserSearchCriteria;
import org.apache.roller.weblogger.util.Utilities;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Test User related business operations.
 */
public class UserTest extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testUserCRUD() throws Exception {
        User user;
        
        User testUser = new User();
        testUser.setId(Utilities.generateUUID());
        testUser.setUserName("testuser");
        testUser.setPassword("password");
        testUser.setScreenName("Test User Screen Name");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setDateCreated(Instant.now());
        testUser.setEnabled(Boolean.TRUE);
        testUser.setGlobalRole(GlobalRole.BLOGGER);
        
        // make sure test user does not exist
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNull(user);
        
        // add test user
        userManager.saveUser(testUser);
        String userName = testUser.getUserName();

        // make sure test user exists
        user = userManager.getUserByUserName(userName);
        assertNotNull(user);
        assertEquals(testUser, user);
        
        // modify user and save
        user.setScreenName("testtesttest");
        userManager.saveUser(user);

        // make sure changes were saved
        user = userManager.getUserByUserName(userName);
        assertNotNull(user);
        assertEquals("testtesttest", user.getScreenName());

        // remove test user
        userManager.removeUser(user);
        endSession(true);
        
        // make sure user no longer exists
        user = userManager.getUserByUserName(userName);
        assertNull(user);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testUserLookups() throws Exception {
        User user;
        SafeUser safeUser;
        
        // add test user
        User testUser = setupUser("usertestuser");
        User testUser2 = setupUser("disabledtestuser");
        testUser2.setEnabled(false);
        endSession(true);
        
        // lookup by username
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNotNull(user);
        assertEquals(testUser.getUserName(), user.getUserName());
        
        // lookup by id
        String userName = user.getUserName();
        user = userManager.getUserByUserName(userName);
        assertNotNull(user);
        assertEquals(testUser.getUserName(), user.getUserName());
        
        // lookup by getUsers() - all users
        UserSearchCriteria usc = new UserSearchCriteria();
        usc.setOffset(0);
        List<SafeUser> users = userManager.getUsers(usc);
        assertEquals(2, users.size());

        // lookup by getUsers() - enabled only
        usc.setEnabled(true);
        users = userManager.getUsers(usc);
        assertEquals(1, users.size());
        safeUser = users.get(0);
        assertNotNull(safeUser);
        assertEquals(testUser.getScreenName(), safeUser.getScreenName());

        // lookup by getUsers() - disabled only
        usc.setEnabled(false);
        users = userManager.getUsers(usc);
        assertEquals(1, users.size());
        safeUser = users.get(0);
        assertNotNull(safeUser);
        assertEquals(testUser2.getScreenName(), safeUser.getScreenName());

        // make sure disable users are not returned
        user.setEnabled(Boolean.FALSE);
        userManager.saveUser(user);
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNull(user);
        
        // remove test user
        teardownUser(testUser.getUserName());
        endSession(true);
    }
    
    
    /**
     * Test basic user role persistence ... Add, Remove
     */
    @Test
    public void testRoleCRUD() throws Exception {
        User user;
        
        // add test user
        User testUser = setupUser("roletestuser");
        endSession(true);
        
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNotNull(user);
        
        assertTrue(GlobalRole.BLOGGER.equals(user.getGlobalRole()));

        user.setGlobalRole(GlobalRole.BLOGCREATOR);
        userManager.saveUser(user);

        // check that role was switched
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNotNull(user);
        assertTrue(user.getGlobalRole() == GlobalRole.BLOGCREATOR);

        // remove test user
        teardownUser(testUser.getUserName());
        endSession(true);
    }

}
