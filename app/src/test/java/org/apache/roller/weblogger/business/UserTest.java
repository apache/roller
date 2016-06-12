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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
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
        testUser.setId(WebloggerCommon.generateUUID());
        testUser.setUserName("testuser");
        testUser.setPassword("password");
        testUser.setScreenName("Test User Screen Name");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setDateCreated(new Timestamp(new Date().getTime()));
        testUser.setEnabled(Boolean.TRUE);
        testUser.setGlobalRole(GlobalRole.BLOGGER);
        
        // make sure test user does not exist
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNull(user);
        
        // add test user
        userManager.saveUser(testUser);
        String userName = testUser.getUserName();
        endSession(true);
        
        // make sure test user exists
        user = userManager.getUserByUserName(userName);
        assertNotNull(user);
        assertEquals(testUser, user);
        
        // modify user and save
        user.setScreenName("testtesttest");
        userManager.saveUser(user);
        endSession(true);
        
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
        
        // lookup by UserName (part)
        List<SafeUser> users1 = userManager.getUsers(testUser.getUserName().substring(0, 3), Boolean.TRUE, 0, 1);
        assertEquals(1, users1.size());
        safeUser = users1.get(0);
        assertNotNull(safeUser);
        assertEquals(testUser.getScreenName(), safeUser.getScreenName());
        
        // lookup by Email (part)
        List<SafeUser> users2 = userManager.getUsers(testUser.getEmailAddress().substring(0, 3), Boolean.TRUE, 0, 1);
        assertEquals(1, users2.size());
        safeUser = users2.get(0);
        assertNotNull(safeUser);
        assertEquals(testUser.getScreenName(), safeUser.getScreenName());

        // make sure disable users are not returned
        user.setEnabled(Boolean.FALSE);
        userManager.saveUser(user);
        endSession(true);
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
        
        assertTrue(GlobalRole.ADMIN.equals(user.getGlobalRole()));

        user.setGlobalRole(GlobalRole.LOGIN);
        userManager.saveUser(user);
        endSession(true);
        
        // check that role was switched
        user = userManager.getUserByUserName(testUser.getUserName());
        assertNotNull(user);
        assertTrue(user.getGlobalRole() == GlobalRole.LOGIN);

        // remove test user
        teardownUser(testUser.getUserName());
        endSession(true);
    }

}
