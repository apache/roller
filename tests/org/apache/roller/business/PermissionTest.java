package org.apache.roller.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Test User/Weblog Permissions related business operations.
 */
public class PermissionTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PermissionTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    
    
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
        try {
            testUser = TestUtils.setupUser("permsTestUser");
            testWeblog = TestUtils.setupWeblog("permsTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testPermissionsCRUD() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        PermissionsData perm = null;
        
        // delete permissions
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        mgr.removePermissions(perm);
        TestUtils.endSession(true);
        
        // check that delete was successful
        perm = null;
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNull(perm);
        
        // create permissions
        perm = new PermissionsData();
        perm.setUser(testUser);
        perm.setWebsite(testWeblog);
        perm.setPending(false);
        perm.setPermissionMask(PermissionsData.ADMIN);
        mgr.savePermissions(perm);
        TestUtils.endSession(true);
        
        // check that create was successful
        perm = null;
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        assertEquals(PermissionsData.ADMIN, perm.getPermissionMask());
        
        // update permissions
        perm.setPermissionMask(PermissionsData.LIMITED);
        mgr.savePermissions(perm);
        TestUtils.endSession(true);
        
        // check that update was successful
        perm = null;
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        assertEquals(PermissionsData.LIMITED, perm.getPermissionMask());
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testPermissionsLookups() throws Exception {
        
        // we need a second user for this test
        UserData user = TestUtils.setupUser("foofoo");
        TestUtils.endSession(true);
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        PermissionsData perm = null;
        List perms = null;
        
        // get all permissions for a user
        perms = mgr.getAllPermissions(user);
        assertEquals(0, perms.size());
        perms = mgr.getAllPermissions(testUser);
        assertEquals(1, perms.size());
        
        // get all permissions for a weblog
        perms = mgr.getAllPermissions(testWeblog);
        assertEquals(1, perms.size());
        
        perm = new PermissionsData();
        perm.setUser(user);
        perm.setWebsite(testWeblog);
        perm.setPending(true);
        perm.setPermissionMask(PermissionsData.AUTHOR);
        mgr.savePermissions(perm);
        
        // get pending permissions for a user
        perms = mgr.getPendingPermissions(testUser);
        assertEquals(0, perms.size());
        perms = mgr.getPendingPermissions(user);
        assertEquals(1, perms.size());
        
        // get pending permissions for a weblog
        perms = mgr.getPendingPermissions(testWeblog);
        assertEquals(1, perms.size());
        
        // get permissions by id
        String id = perm.getId();
        perm = null;
        perm = mgr.getPermissions(id);
        assertNotNull(perm);
        assertEquals(id, perm.getId());
        
        // get permissions for a specific user/weblog
        perm = null;
        perm = mgr.getPermissions(testWeblog, testUser);
        assertNotNull(perm);
        assertEquals(PermissionsData.ADMIN, perm.getPermissionMask());
        perm = null;
        perm = mgr.getPermissions(testWeblog, user);
        assertNotNull(perm);
        assertEquals(PermissionsData.AUTHOR, perm.getPermissionMask());
        assertEquals(true, perm.isPending());
        
        // cleanup the extra test user
        TestUtils.teardownUser(user.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Tests weblog invitation process.
     */
    public void testInvitations() throws Exception {
        
        // we need a second user for this test
        UserData user = TestUtils.setupUser("foobee");
        TestUtils.endSession(true);
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        PermissionsData perm = null;
        List perms = null;
        
        // invite user to weblog
        perm = mgr.inviteUser(testWeblog, user, PermissionsData.LIMITED);
        String id = perm.getId();
        TestUtils.endSession(true);
        
        // accept invitation
        perm = mgr.getPermissions(testWeblog, user);
        perm.setPending(false);
        mgr.savePermissions(perm);
        TestUtils.endSession(true);
        
        // re-query now that we have changed things
        user = mgr.getUserByUsername(user.getUserName());
        testWeblog = mgr.getWebsiteByHandle(testWeblog.getHandle());
        
        // assert that invitation list is empty
        assertTrue(mgr.getPendingPermissions(user).isEmpty());
        assertTrue(mgr.getPendingPermissions(testWeblog).isEmpty());
        
        // assert that user is member of weblog
        assertFalse(mgr.getPermissions(testWeblog, user).isPending());
        List weblogs = mgr.getWebsites(user, null, null);
        assertEquals(1, weblogs.size());
        assertEquals(testWeblog.getId(), ((WebsiteData)weblogs.get(0)).getId());
        
        // assert that website has user
        List users = mgr.getUsers(testWeblog, null);
        assertEquals(2, users.size());
        
        // test user can be retired from website
        mgr.retireUser(testWeblog, user);
        TestUtils.endSession(true);
        
        user = mgr.getUser(user.getId());
        weblogs = mgr.getWebsites(user, null, null);
        assertEquals(0, weblogs.size());
        
        // cleanup the extra test user
        TestUtils.teardownUser(user.getId());
        TestUtils.endSession(true);
    }
    
}

