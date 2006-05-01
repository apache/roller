
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
import org.apache.roller.pojos.UserData;


/**
 * Test User related business operations.
 */
public class UserTest extends TestCase {
    
    public static Log log = LogFactory.getLog(UserTest.class);
    
    
    public UserTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(UserTest.class);
    }
    
    
    public void setUp() throws Exception {
    }
    
    public void tearDown() throws Exception {
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testUserCRUD() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        UserData user = null;
        
        UserData testUser = new UserData();
        testUser.setUserName("testUser");
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setTimeZone("America/Los_Angeles");
        testUser.setDateCreated(new java.util.Date());
        testUser.setEnabled(Boolean.TRUE);
        
        // make sure test user does not exist
        user = mgr.getUserByUsername(testUser.getUserName());
        assertNull(user);
        
        // add test user
        mgr.addUser(testUser);
        String id = testUser.getId();
        TestUtils.endSession(true);
        
        // make sure test user exists
        user = null;
        user = mgr.getUser(id);
        assertNotNull(user);
        assertEquals(testUser, user);
        
        // modify user and save
        user.setFullName("testtesttest");
        mgr.saveUser(user);
        TestUtils.endSession(true);
        
        // make sure changes were saved
        user = null;
        user = mgr.getUser(id);
        assertNotNull(user);
        assertEquals("testtesttest", user.getFullName());
        
        // remove test user
        mgr.removeUser(user);
        TestUtils.endSession(true);
        
        // make sure user no longer exists
        user = null;
        user = mgr.getUser(id);
        assertNull(user);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testUserLookups() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        UserData user = null;
        
        // add test user
        UserData testUser = TestUtils.setupUser("userTestUser");
        TestUtils.endSession(true);
        
        // lookup by username
        user = mgr.getUserByUsername(testUser.getUserName());
        assertNotNull(user);
        assertEquals(testUser.getUserName(), user.getUserName());
        
        // lookup by id
        String id = user.getId();
        user = null;
        user = mgr.getUser(id);
        assertNotNull(user);
        assertEquals(testUser.getUserName(), user.getUserName());
        
        // lookup by UserName (part)
        user = null;
        List users1 = mgr.getUsersStartingWith(testUser.getUserName().substring(0, 3), 0, 1, Boolean.TRUE);
        assertEquals(1, users1.size());
        user = (UserData) users1.get(0);
        assertNotNull(user);
        assertEquals(testUser.getUserName(), user.getUserName());
        
        // lookup by Email (part)
        user = null;
        List users2 = mgr.getUsersStartingWith(testUser.getEmailAddress().substring(0, 3), 0, 1, Boolean.TRUE);
        assertEquals(1, users2.size());
        user = (UserData) users2.get(0);
        assertNotNull(user);
        assertEquals(testUser.getUserName(), user.getUserName());
        
        // make sure disable users are not returned
        user.setEnabled(Boolean.FALSE);
        mgr.saveUser(user);
        user = null;
        user = mgr.getUserByUsername(testUser.getUserName());
        assertNull(user);
        
        // remove test user
        TestUtils.teardownUser(testUser.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test basic user role persistence ... Add, Remove
     */
    public void testRoleCRUD() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        UserData user = null;
        
        // add test user
        UserData testUser = TestUtils.setupUser("roleTestUser");
        TestUtils.endSession(true);
        
        // verify user has 2 roles, admin & editor
        user = mgr.getUserByUsername(testUser.getUserName());
        assertNotNull(user);
        assertEquals(2, user.getRoles().size());
        assertTrue(user.hasRole("editor"));
        assertTrue(user.hasRole("admin"));
        
        // remove role
        user.revokeRole("admin");
        mgr.saveUser(user);
        TestUtils.endSession(true);
        
        // check that role was removed
        user = null;
        user = mgr.getUserByUsername(testUser.getUserName());
        assertNotNull(user);
        assertEquals(1, user.getRoles().size());
        assertTrue(user.hasRole("editor"));
        assertFalse(user.hasRole("admin"));
        
        // add role
        user.grantRole("admin");
        mgr.saveUser(user);
        TestUtils.endSession(true);
        
        // check that role was added
        user = null;
        user = mgr.getUserByUsername(testUser.getUserName());
        assertNotNull(user);
        assertEquals(2, user.getRoles().size());
        assertTrue(user.hasRole("editor"));
        assertTrue(user.hasRole("admin"));
        
        // remove test user
        TestUtils.teardownUser(testUser.getId());
        TestUtils.endSession(true);
    }

    
    /**
     * Test ability to remove a user with a full set of data.
     */
    public void testRemoveLoadedUser() throws Exception {
        // TODO: implement testRemoveLoadedUser
    }
    
}
