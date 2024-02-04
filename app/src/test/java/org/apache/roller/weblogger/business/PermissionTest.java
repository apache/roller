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
*/
package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test User/Weblog Permissions related business operations.
 */
public class PermissionTest  {
    
    public static Log log = LogFactory.getLog(PermissionTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
    public void setUp() throws Exception {
        
        log.info("BEGIN");
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("permsTestUser");
            testWeblog = TestUtils.setupWeblog("permsTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in setup", ex);
            throw new Exception("Test setup failed", ex);
        }
        
        log.info("END");
    }

    @AfterEach
    public void tearDown() throws Exception {
        
        log.info("BEGIN");
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tear down", ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        log.info("END");
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testPermissionsCRUD() throws Exception {
        
        log.info("BEGIN");
        
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        
        WeblogPermission p1 = new WeblogPermission(testWeblog, testUser, 
            WeblogPermission.ADMIN + "," + WeblogPermission.POST);
        assertTrue(p1.hasAction(WeblogPermission.POST));
        assertTrue(p1.hasAction(WeblogPermission.ADMIN));
        assertEquals(2, p1.getActionsAsList().size());
       
        WeblogPermission p2 = new WeblogPermission(testWeblog, testUser, 
            WeblogPermission.EDIT_DRAFT);
        p1.addActions(p2);
        assertEquals(3, p1.getActionsAsList().size());
        
        
        WeblogPermission perm = null;
         
        // delete permissions
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = mgr.getWeblogPermission(testWeblog, testUser);
        assertNotNull(perm);
        mgr.revokeWeblogPermission(testWeblog, testUser, WeblogPermission.ALL_ACTIONS);
        TestUtils.endSession(true);
        
        // check that delete was successful
        perm = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = mgr.getWeblogPermission(testWeblog, testUser);
        assertNull(perm);
        
        // create permissions
        List<String> actions = new ArrayList<>();
        actions.add(WeblogPermission.ADMIN);
        actions.add(WeblogPermission.POST);
        mgr.grantWeblogPermission(testWeblog, testUser, actions);
        TestUtils.endSession(true);
        
        // check that create was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = null;
        perm = mgr.getWeblogPermission(testWeblog, testUser);
        assertNotNull(perm);
        assertTrue(perm.hasAction(WeblogPermission.POST));
        assertTrue(perm.hasAction(WeblogPermission.ADMIN));
        TestUtils.endSession(true);
        
        // revoke those same permissions
        mgr.revokeWeblogPermission(perm.getWeblog(), perm.getUser(), WeblogPermission.ALL_ACTIONS);
        TestUtils.endSession(true);
        
        // add only draft permission
        mgr.grantWeblogPermission(testWeblog, testUser, 
                Collections.singletonList(WeblogPermission.EDIT_DRAFT));
        TestUtils.endSession(true);

        // check that user has draft permisson only
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = null;
        perm = mgr.getWeblogPermission(testWeblog, testUser);
        assertNotNull(perm);
        assertTrue(perm.hasAction(WeblogPermission.EDIT_DRAFT));
        assertFalse(perm.hasAction(WeblogPermission.POST));
        assertFalse(perm.hasAction(WeblogPermission.ADMIN));
        
        log.info("END");
    }  
    
    
    /**
     * Test lookup mechanisms.
     */
    @Test
    public void testPermissionsLookups() throws Exception {
        
        log.info("BEGIN");
        
        // we need a second user for this test
        User user = TestUtils.setupUser("testPermissionsLookups");
        TestUtils.endSession(true);

        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogPermission perm = null;
        List<WeblogPermission> perms = null;

        // get all permissions for a user
        perms = mgr.getWeblogPermissions(TestUtils.getManagedUser(user));
        assertEquals(0, perms.size());
        perms = mgr.getWeblogPermissions(TestUtils.getManagedUser(testUser));
        assertEquals(1, perms.size());

        // get all permissions for a weblog
        perms = mgr.getWeblogPermissions(TestUtils.getManagedWebsite(testWeblog));
        assertEquals(1, perms.size());

        List<String> actions = new ArrayList<>();
        actions.add(WeblogPermission.POST);
        mgr.grantWeblogPermissionPending(testWeblog, user, actions);
        TestUtils.endSession(true);

        // get pending permissions for a user
        perms = mgr.getPendingWeblogPermissions(TestUtils.getManagedUser(testUser));
        assertEquals(0, perms.size());
        perms = mgr.getPendingWeblogPermissions(TestUtils.getManagedUser(user));
        assertEquals(1, perms.size());

        // get pending permissions for a weblog
        perms = mgr.getPendingWeblogPermissions(TestUtils.getManagedWebsite(testWeblog));
        assertEquals(1, perms.size());            

        // get permissions for a specific user/weblog
        perm = null;
        perm = mgr.getWeblogPermission(
                TestUtils.getManagedWebsite(testWeblog), 
                TestUtils.getManagedUser(testUser));
        assertNotNull(perm);
        assertTrue(perm.hasAction(WeblogPermission.ADMIN));

        // pending permissions should not be visible
        perm = null;
        perm = mgr.getWeblogPermission(
                TestUtils.getManagedWebsite(testWeblog), 
                TestUtils.getManagedUser(user));
        assertNull(perm);
        
        List<WeblogPermission> pendings = mgr.getPendingWeblogPermissions(user);

        // cleanup
        TestUtils.teardownPermissions(pendings.get(0));
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
        
        log.info("END");
    }


    /**
     * Tests weblog invitation process.
     */
    @Test
    public void testInvitations() throws Exception {
        
        log.info("BEGIN");
        
        // we need a second user for this test
        User user = TestUtils.setupUser("testInvitations");
        TestUtils.endSession(true);

        WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();

        // invite user to weblog
        List<String> actions = new ArrayList<>();
        actions.add(WeblogPermission.EDIT_DRAFT);
        umgr.grantWeblogPermissionPending(testWeblog, user, actions);
        TestUtils.endSession(true);

        // accept invitation
        umgr.confirmWeblogPermission(testWeblog, user);
        TestUtils.endSession(true);

        // re-query now that we have changed things
        user = umgr.getUserByUserName(user.getUserName());
        testWeblog = wmgr.getWeblogByHandle(testWeblog.getHandle());

        // assert that invitation list is empty
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        user = TestUtils.getManagedUser(user);
        assertTrue(umgr.getPendingWeblogPermissions(user).isEmpty());
        assertTrue(umgr.getPendingWeblogPermissions(testWeblog).isEmpty());

        // assert that user is member of weblog
        assertNotNull(umgr.getWeblogPermission(testWeblog, user));
        List<Weblog> weblogs = wmgr.getUserWeblogs(TestUtils.getManagedUser(user), true);
        assertEquals(1, weblogs.size());
        assertEquals(testWeblog.getId(), weblogs.get(0).getId());

        // assert that website has user
        List<User> users = wmgr.getWeblogUsers(testWeblog, true);
        assertEquals(2, users.size());

        // test user can be retired from website
        umgr.revokeWeblogPermission(testWeblog, user, WeblogPermission.ALL_ACTIONS);
        TestUtils.endSession(true);

        //user = umgr.getUser(user.getId());
        weblogs = wmgr.getUserWeblogs(user, true);
        assertEquals(0, weblogs.size());

        // cleanup the extra test user
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
        
        log.info("END");
    }
    
    
    /**
     * Tests weblog invitation process.
     */
    @Test
    public void testPermissionChecks() throws Exception {
        
        log.info("BEGIN");
       
        WeblogPermission perm = 
            new WeblogPermission(testWeblog, testUser, WeblogPermission.POST);
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        assertTrue(umgr.checkPermission(perm, testUser));
        
        // we need a second user for this test
        User adminUser = TestUtils.setupUser("adminUser");
        umgr.grantRole("admin", adminUser);
        TestUtils.endSession(true);

        // because adminUser is a global admin, they should have POST perm
        WeblogPermission perm2 = 
            new WeblogPermission(testWeblog, testUser, WeblogPermission.POST);
        assertTrue(umgr.checkPermission(perm, testUser));

        // cleanup the extra test user
        TestUtils.teardownUser(adminUser.getUserName());
        TestUtils.endSession(true);
        log.info("END");
    }
}
