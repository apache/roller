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

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.WeblogUserPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;


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
    
    
    public static Test suite() {
        return new TestSuite(PermissionTest.class);
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
            TestUtils.teardownUser(testUser.getId());
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
    public void testPermissionsCRUD2() throws Exception {
        
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
        mgr.revokeWeblogPermission(perm);
        TestUtils.endSession(true);
        
        // check that delete was successful
        perm = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = mgr.getWeblogPermission(testWeblog, testUser);
        assertNull(perm);
        
        // create permissions
        perm = new WeblogPermission(testWeblog, testUser,
            WeblogPermission.ADMIN + "," + WeblogPermission.POST);
        mgr.grantWeblogPermission(perm);
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
        
        // revoke those same permissions, add limited permission
        mgr.revokeWeblogPermission(perm);
        TestUtils.endSession(true);
        
        WeblogPermission draft = new WeblogPermission(testWeblog, testUser, 
            WeblogPermission.EDIT_DRAFT);
        mgr.grantWeblogPermission(draft);
        TestUtils.endSession(true);

        // check that update was successful
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
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void _testPermissionsCRUD() throws Exception {
        
        log.info("BEGIN");
        
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogUserPermission perm = null;
                
        // delete permissions
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        mgr.removePermissions(perm);
        TestUtils.endSession(true);
        
        // check that delete was successful
        perm = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNull(perm);
        
        // create permissions
        perm = new WeblogUserPermission();
        perm.setUser(testUser);
        perm.setWebsite(testWeblog);
        perm.setPending(false);
        perm.setPermissionMask(WeblogUserPermission.ADMIN);
        mgr.savePermissions(perm);
        TestUtils.endSession(true);
        
        // check that create was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = null;
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        assertEquals(WeblogUserPermission.ADMIN, perm.getPermissionMask());
        
        // update permissions
        perm.setPermissionMask(WeblogUserPermission.LIMITED);
        mgr.savePermissions(perm);
        TestUtils.endSession(true);

        // check that update was successful
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testUser = TestUtils.getManagedUser(testUser);
        perm = null;
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        assertEquals(WeblogUserPermission.LIMITED, perm.getPermissionMask());
        
        log.info("END");
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void _testPermissionsLookups() throws Exception {
        
        log.info("BEGIN");
        
        try {
            // we need a second user for this test
            User user = TestUtils.setupUser("testPermissionsLookups");
            TestUtils.endSession(true);
            
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
            WeblogUserPermission perm = null;
            List perms = null;
            
            // get all permissions for a user
            perms = mgr.getAllPermissions(TestUtils.getManagedUser(user));
            assertEquals(0, perms.size());
            perms = mgr.getAllPermissions(TestUtils.getManagedUser(testUser));
            assertEquals(1, perms.size());
            
            // get all permissions for a weblog
            perms = mgr.getAllPermissions(TestUtils.getManagedWebsite(testWeblog));
            assertEquals(1, perms.size());
            
            perm = new WeblogUserPermission();
            perm.setUser(TestUtils.getManagedUser(user));
            perm.setWebsite(TestUtils.getManagedWebsite(testWeblog));
            perm.setPending(true);
            perm.setPermissionMask(WeblogUserPermission.AUTHOR);
            mgr.savePermissions(perm);
            TestUtils.endSession(true);
            
            // get pending permissions for a user
            perms = mgr.getPendingPermissions(TestUtils.getManagedUser(testUser));
            assertEquals(0, perms.size());
            perms = mgr.getPendingPermissions(TestUtils.getManagedUser(user));
            assertEquals(1, perms.size());
            
            // get pending permissions for a weblog
            perms = mgr.getPendingPermissions(TestUtils.getManagedWebsite(testWeblog));
            assertEquals(1, perms.size());
            
            // get permissions by id
            String id = perm.getId();
            perm = null;
            perm = mgr.getPermissions(id);
            assertNotNull(perm);
            assertEquals(id, perm.getId());
            
            // get permissions for a specific user/weblog
            perm = null;
            perm = mgr.getPermissions(TestUtils.getManagedWebsite(testWeblog), TestUtils.getManagedUser(testUser));
            assertNotNull(perm);
            assertEquals(WeblogUserPermission.ADMIN, perm.getPermissionMask());
            perm = null;
            perm = mgr.getPermissions(TestUtils.getManagedWebsite(testWeblog), TestUtils.getManagedUser(user));
            assertNotNull(perm);
            assertEquals(WeblogUserPermission.AUTHOR, perm.getPermissionMask());
            assertEquals(true, perm.isPending());
            
            // cleanup
            TestUtils.teardownPermissions(perm.getId());
            TestUtils.teardownUser(user.getId());
            TestUtils.endSession(true);
        } catch(Throwable t) {
            log.error("Error running test", t);
            throw (Exception) t;
        }
        
        log.info("END");
    }


    /**
     * Tests weblog invitation process.
     */
    public void _testInvitations() throws Exception {
        
        log.info("BEGIN");
        
        // we need a second user for this test
        User user = TestUtils.setupUser("testInvitations");
        TestUtils.endSession(true);

        WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogUserPermission perm = null;
        List perms = null;

        // invite user to weblog
        perm = umgr.inviteUser(TestUtils.getManagedWebsite(testWeblog), user, WeblogUserPermission.LIMITED);
        String id = perm.getId();
        TestUtils.endSession(true);

        // accept invitation
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        user = TestUtils.getManagedUser(user);
        perm = umgr.getPermissions(testWeblog, user);
        perm.setPending(false);
        umgr.savePermissions(perm);
        TestUtils.endSession(true);

        // re-query now that we have changed things
        user = umgr.getUserByUserName(user.getUserName());
        testWeblog = wmgr.getWebsiteByHandle(testWeblog.getHandle());

        // assert that invitation list is empty
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        user = TestUtils.getManagedUser(user);
        assertTrue(umgr.getPendingPermissions(user).isEmpty());
        assertTrue(umgr.getPendingPermissions(testWeblog).isEmpty());

        // assert that user is member of weblog
        assertFalse(umgr.getPermissions(testWeblog, user).isPending());
        List weblogs = wmgr.getWebsites(TestUtils.getManagedUser(user), null, null, null, null, 0, -1);
        assertEquals(1, weblogs.size());
        assertEquals(testWeblog.getId(), ((Weblog)weblogs.get(0)).getId());

        // assert that website has user
        List users = umgr.getUsers(testWeblog, null, null, null, 0, -1);
        assertEquals(2, users.size());

        // test user can be retired from website
        umgr.retireUser(testWeblog, user);
        TestUtils.endSession(true);

        user = umgr.getUser(user.getId());
        weblogs = wmgr.getWebsites(user, null, null, null, null, 0, -1);
        assertEquals(0, weblogs.size());

        // cleanup the extra test user
        TestUtils.teardownUser(user.getId());
        TestUtils.endSession(true);
        
        log.info("END");
    }
    
}
