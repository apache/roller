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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;

import java.util.List;

/**
 * Test User/Weblog Permissions related business operations.
 */
public class PermissionTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PermissionTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;

    public PermissionTest(String name) {
        super(name);
    }

    /**
     * All tests in this suite require a user and a weblog.
     */
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
    public void testPermissionsCRUD() throws Exception {
        
        log.info("BEGIN");
        
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        
        UserWeblogRole p1 = new UserWeblogRole(testWeblog, testUser,
            WeblogRole.POST);
        assertTrue(p1.getWeblogRole() == WeblogRole.POST);

        UserWeblogRole perm = null;
         
        // delete permissions
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = mgr.getWeblogRole(testUser, testWeblog);
        assertNotNull(perm);
        mgr.revokeWeblogRole(testUser, testWeblog);
        TestUtils.endSession(true);
        
        // check that delete was successful
        perm = mgr.getWeblogRole(testUser, testWeblog);
        assertNull(perm);
        
        // create permissions
        mgr.grantWeblogRole(testUser, testWeblog, WeblogRole.OWNER);
        TestUtils.endSession(true);
        
        // check that create was successful
        perm = mgr.getWeblogRole(testUser, testWeblog);
        assertNotNull(perm);
        assertTrue(perm.getWeblogRole() == WeblogRole.OWNER);
        TestUtils.endSession(true);
        
        // revoke role
        mgr.revokeWeblogRole(perm.getUser(), perm.getWeblog());
        TestUtils.endSession(true);
        
        // add only draft role
        mgr.grantWeblogRole(testUser, testWeblog, WeblogRole.EDIT_DRAFT);
        TestUtils.endSession(true);

        // check that user has draft permission only
        perm = mgr.getWeblogRole(testUser, testWeblog);
        assertNotNull(perm);
        assertTrue(perm.getWeblogRole() == WeblogRole.EDIT_DRAFT);

        log.info("END");
    }  
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testPermissionsLookups() throws Exception {
        
        log.info("BEGIN");
        
        // we need a second user for this test
        User user = TestUtils.setupUser("testPermissionsLookups");
        TestUtils.endSession(true);

        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        UserWeblogRole perm = null;
        List<UserWeblogRole> perms = null;

        // get all permissions for a user
        perms = mgr.getWeblogRoles(TestUtils.getManagedUser(user));
        assertEquals(0, perms.size());
        perms = mgr.getWeblogRoles(TestUtils.getManagedUser(testUser));
        assertEquals(1, perms.size());

        // get all permissions for a weblog
        perms = mgr.getWeblogRoles(TestUtils.getManagedWebsite(testWeblog));
        assertEquals(1, perms.size());

        mgr.grantPendingWeblogRole(user, testWeblog, WeblogRole.POST);
        TestUtils.endSession(true);

        // get pending permissions for a user
        perms = mgr.getPendingWeblogRoles(TestUtils.getManagedUser(testUser));
        assertEquals(0, perms.size());
        perms = mgr.getPendingWeblogRoles(TestUtils.getManagedUser(user));
        assertEquals(1, perms.size());

        // get pending permissions for a weblog
        perms = mgr.getPendingWeblogRoles(TestUtils.getManagedWebsite(testWeblog));
        assertEquals(1, perms.size());            

        // get permissions for a specific user/weblog
        perm = null;
        perm = mgr.getWeblogRole(
                TestUtils.getManagedUser(testUser), TestUtils.getManagedWebsite(testWeblog)
        );
        assertNotNull(perm);
        assertTrue(perm.getWeblogRole() == WeblogRole.OWNER);

        // pending permissions should not be visible
        perm = null;
        perm = mgr.getWeblogRole(
                TestUtils.getManagedUser(user), TestUtils.getManagedWebsite(testWeblog)
        );
        assertNull(perm);
        
        List<UserWeblogRole> pendings = mgr.getPendingWeblogRoles(user);

        // cleanup
        TestUtils.teardownPermissions(pendings.get(0));
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
        
        log.info("END");
    }


    /**
     * Tests weblog invitation process.
     */
    public void testInvitations() throws Exception {
        
        log.info("BEGIN");
        
        // we need a second user for this test
        User user = TestUtils.setupUser("testInvitations");
        TestUtils.endSession(true);

        WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        UserWeblogRole perm = null;
        List perms = null;

        // invite user to weblog
        umgr.grantPendingWeblogRole(user, testWeblog, WeblogRole.EDIT_DRAFT);
        TestUtils.endSession(true);

        // accept invitation
        umgr.acceptWeblogInvitation(user, testWeblog);
        TestUtils.endSession(true);

        // re-query now that we have changed things
        user = umgr.getUserByUserName(user.getUserName());
        testWeblog = wmgr.getWeblogByHandle(testWeblog.getHandle());

        // assert that invitation list is empty
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        user = TestUtils.getManagedUser(user);
        assertTrue(umgr.getPendingWeblogRoles(user).isEmpty());
        assertTrue(umgr.getPendingWeblogRoles(testWeblog).isEmpty());

        // assert that user is member of weblog
        assertNotNull(umgr.getWeblogRole(user, testWeblog));
        List weblogs = wmgr.getUserWeblogs(TestUtils.getManagedUser(user), true);
        assertEquals(1, weblogs.size());
        assertEquals(testWeblog.getId(), ((Weblog)weblogs.get(0)).getId());

        // assert that website has user
        List users = wmgr.getWeblogUsers(testWeblog, true);
        assertEquals(2, users.size());

        // test user can be retired from website
        umgr.revokeWeblogRole(user, testWeblog);
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
    public void testPermissionChecks() throws Exception {
        
        log.info("BEGIN");
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        assertTrue(umgr.checkWeblogRole(testUser, testWeblog, WeblogRole.POST));
        
        // we need a second user for this test
        User adminUser = TestUtils.setupUser("adminUser");
        adminUser.setGlobalRole(GlobalRole.ADMIN);
        TestUtils.endSession(true);

        // because adminUser is a global admin, they should have POST perm
        assertTrue(umgr.checkWeblogRole(adminUser, testWeblog, WeblogRole.POST));

        // cleanup the extra test user
        TestUtils.teardownUser(adminUser.getUserName());
        TestUtils.endSession(true);
        log.info("END");
    }
}
