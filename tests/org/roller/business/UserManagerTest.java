
package org.roller.business;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.RollerException;
import org.roller.RollerTestBase;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.FolderData;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;

///////////////////////////////////////////////////////////////////////////////
/**
 * Test Roller User Management.
 */
public class UserManagerTest  extends RollerTestBase
{
    WebsiteData enabledSite = null;
    UserData    enabledUser = null;
    String      enabledUserName = "enabledUser";

    WebsiteData disabledSite = null;
    UserData    disabledUser = null;
    String      disabledUserName = "disabledUser";

    //------------------------------------------------------------------------
    public UserManagerTest()
    {
        super();
    }

    //------------------------------------------------------------------------
    public UserManagerTest(String name)
    {
        super(name);
    }

    //------------------------------------------------------------------------
    public static Test suite()
    {
        return new TestSuite(UserManagerTest.class);
    }

    //------------------------------------------------------------------------
    public static void main(String args[])
    {
        junit.textui.TestRunner.run(UserManagerTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();

        getRoller().begin(UserData.SYSTEM_USER);
        UserManager umgr = getRoller().getUserManager();

        enabledUser = createUser(enabledUserName,
                                 "password",
                                 "EnabledUser",
                                 "enabledUser@example.com");
        enabledUser.setEnabled(Boolean.TRUE);
        enabledSite = ((WebsiteData)umgr.getWebsites(enabledUser, null, null).get(0));
        enabledSite.setEnabled(Boolean.TRUE);       

        disabledUser = createUser(disabledUserName,
                                 "password",
                                 "DisabledUser",
                                 "disabledUser@example.com");
                
        disabledUser.setEnabled(Boolean.FALSE);
        disabledSite = ((WebsiteData)umgr.getWebsites(disabledUser, null, null).get(0));
        disabledSite.setEnabled(Boolean.FALSE);  

        getRoller().commit();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();

        getRoller().begin(UserData.SYSTEM_USER);
        UserManager umgr = getRoller().getUserManager();

        enabledSite = umgr.retrieveWebsite(enabledSite.getId());
        enabledSite.remove();
        
        enabledUser = umgr.retrieveUser(enabledUser.getId());
        enabledUser.remove();

        disabledSite = umgr.retrieveWebsite(disabledSite.getId());
        disabledSite.remove();
        
        disabledUser = umgr.retrieveUser(disabledUser.getId());
        disabledUser.remove();

        getRoller().commit();
    }

    //------------------------------------------------------------------------
    /** Tests storage and removal of user. */
    public void testUserStorage() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();
        UserData stored = null;

        getRoller().begin(UserData.SYSTEM_USER);
        stored = new UserData(
            null,
            "testUserStorage2",
            "password2",
            "TestUser2",
            "testuser2@example.com",
            "en_US_WIN", "America/Los_Angeles",
            new java.util.Date(), Boolean.TRUE);
        umgr.storeUser(stored);
        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        UserData retrieved = umgr.retrieveUser(stored.getId());
        assertEquals(stored, retrieved);
        getRoller().release();

        getRoller().begin(UserData.SYSTEM_USER);
        retrieved = umgr.retrieveUser(stored.getId());
        retrieved.remove();
        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        assertNull(umgr.retrieveUser(stored.getId()));
        getRoller().release();
    }

    //------------------------------------------------------------------------
    /** Tests storage and removal of website. */
    public void testWebsiteStorage() throws RollerException
    {
        UserData user = null;
        WebsiteData stored = null;
        UserManager umgr = getRoller().getUserManager();

        getRoller().begin(UserData.SYSTEM_USER);

        user = new UserData(
            null,
            "testWebsiteStorage3",
            "password3",
            "TestUser3",
            "testuser3@example.com",
            "en_US_WIN", "America/Los_Angeles",
            new java.util.Date(), Boolean.TRUE);
        umgr.storeUser(user);
        
        stored = new WebsiteData();
        stored.setName("testsite3");
        stored.setHandle("testsite3");
        stored.setDescription("Testsite3");
        stored.setCreator(user);
        stored.setDefaultPageId("dpid");
        stored.setWeblogDayPageId("ddid");
        stored.setEnableBloggerApi(Boolean.FALSE);
        stored.setEditorPage("edit-weblog.jsp");
        stored.setAllowComments(Boolean.TRUE);
        stored.setEmailComments(Boolean.FALSE);
        stored.setEmailAddress("test@test.com");
        stored.setEnabled(Boolean.TRUE);
        stored.setEditorTheme("theme");
        stored.setDateCreated(new Date());
        stored.setBlacklist("ignore");
        stored.setDefaultAllowComments(Boolean.TRUE);
        stored.setDefaultCommentDays(7);
        stored.setModerateComments(Boolean.FALSE);
        umgr.storeWebsite(stored);
        
        WeblogCategoryData rootCategory = getRoller().getWeblogManager()
            .createWeblogCategory(stored, null, "root", "root", "");
        rootCategory.save();

        stored.setBloggerCategory(rootCategory);
        stored.setDefaultCategory(rootCategory);
        
        FolderData rootFolder = getRoller().getBookmarkManager()
            .createFolder(null, "root", "root", stored);
        rootFolder.save();

        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        WebsiteData retrieved = umgr.retrieveWebsite(stored.getId());
        assertEquals(stored, retrieved);
        retrieved.remove();
        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        user = umgr.retrieveUser(user.getId());
        user.remove();
        getRoller().commit();

        getRoller().begin(UserData.SYSTEM_USER);
        assertNull(umgr.retrieveUser(user.getId()));
        assertNull(umgr.retrieveWebsite(stored.getId()));
        getRoller().rollback();
    }

    public void testAddRemoveUser() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();

        // Add a user

        getRoller().begin(UserData.SYSTEM_USER);
        UserData user = new UserData(
            null,
            "testAddRemoveUser",
            "password4",
            "TestUser4",
            "testuser4@example.com",
            "en_US_WIN", "America/Los_Angeles",
            new java.util.Date(), Boolean.TRUE);
        Map pages = new HashMap();
        pages.put("Weblog","Weblog page content");
        pages.put("_day","Day page content");
        pages.put("css","CSS page content");
        umgr.addUser(user);
        umgr.createWebsite(user, pages, 
                user.getUserName(), user.getUserName(), user.getUserName(), 
                "dummy@example.com","basic", "en_US_WIN", "America/Los_Angeles");
        getRoller().commit();

        // Verify that user has all the goodies
        getRoller().begin(UserData.SYSTEM_USER);
        UserData user1 = umgr.retrieveUser(user.getId());
        assertNotNull(user1);

        WebsiteData website = (WebsiteData)umgr.getWebsites(user, null, null).get(0);
        assertNotNull(website);

        FolderData root = getRoller().getBookmarkManager().getRootFolder(website);
        assertNotNull(root);

        List pages1 = getRoller().getUserManager().getPages(website);
        // new registrations require a theme, so no pages are created -- Allen G
        assertEquals(0, pages1.size());
        getRoller().rollback();

        // Remove the website and user
        getRoller().begin(UserData.SYSTEM_USER);
        website = umgr.retrieveWebsite(website.getId());
        website.remove();
        user = umgr.retrieveUser(user.getId());
        user.remove();
        getRoller().commit();

        // Verify that user was completely deleted
        getRoller().begin(UserData.SYSTEM_USER);
        UserData user2 = umgr.retrieveUser(user.getId());
        assertNull(user2);

        assertNull(getRoller().getBookmarkManager().retrieveFolder(root.getId()));

        List pages2 = getRoller().getUserManager().getPages(website);
        assertEquals(0, pages2.size());
        getRoller().rollback();
    }

    public void testGetWebsite() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();
        
        // can get testuser0 who is enabled
        assertTrue(umgr.getWebsites(
                umgr.getUser(enabledUserName), Boolean.TRUE, null).size() > 0);

        // can't get testuser1, who is disabled
        disabledUser = umgr.retrieveUser(disabledUser.getId());
        assertTrue(umgr.getWebsites(
                disabledUser, Boolean.TRUE, null).size() == 0);

        // can get testuser1 with enabledOnly flag set to false
        assertTrue(umgr.getWebsites(
                umgr.getUser(disabledUserName), Boolean.FALSE, null).size() == 1);
    }

    public void testGetUser() throws RollerException
    {
        // can get testuser0 who is enabled
        getRoller().begin(UserData.SYSTEM_USER);
        
        UserData user = getRoller().getUserManager().getUser(enabledUserName);
        assertNotNull(user);
        
        // can't get testuser1, who is disabled
        assertNull(getRoller().getUserManager().getUser(disabledUserName));

        // can get testuser1 with enabledOnly flag set to false
        assertNotNull(getRoller().getUserManager().getUser(disabledUserName, null));
        
        getRoller().release();
    }

    public void testGetUsers() throws RollerException
    {
        // There are users
        int userCountEnabled = getRoller().getUserManager().getUsers().size();
        assertTrue(userCountEnabled > 0);

        // At least one user is disabled
        int userCountAll = getRoller().getUserManager().getUsers(null).size();
        assertTrue(userCountAll > userCountEnabled);
    }

    public void testUserRoles() throws Exception {
        
        UserData user = null;
        
        // check existing roles within a session
        getRoller().begin(UserData.SYSTEM_USER);        
        user = getRoller().getUserManager().getUser(enabledUserName);
        assertNotNull(user);
        assertNotNull(user.getRoles());
        assertEquals(1, user.getRoles().size()); 
        getRoller().release();
        
        // test hasRole within a session
        getRoller().begin(UserData.SYSTEM_USER);        
        user = getRoller().getUserManager().getUser(enabledUserName);
        assertTrue(user.hasRole("editor"));
        getRoller().release();
        
        // and without a session
        assertTrue(user.hasRole("editor"));

        // test revokeRole within session
        getRoller().begin(UserData.SYSTEM_USER);
        user = getRoller().getUserManager().getUser(enabledUserName);
        user.revokeRole("editor");
        assertFalse(user.hasRole("editor"));
        getRoller().commit();
        
        // role has been removed after commit?
        assertFalse(user.hasRole("editor"));
        
        // restore role
        getRoller().begin(UserData.SYSTEM_USER);
        user = getRoller().getUserManager().getUser(enabledUserName);
        user.grantRole("editor");
        assertTrue(user.hasRole("editor"));
        getRoller().commit();

        assertTrue(user.hasRole("editor"));
    }
    
    /** Each website should be able to return a page named Weblog */
    public void testGetPageByName() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();
        WebsiteData wd0 = (WebsiteData)umgr.getWebsites(
                umgr.getUser(enabledUserName), null, null).get(0);
        assertNotNull(wd0.getPageByName("Weblog"));
    }

    /** Each website should be able to return a page with link=Weblog */
    public void testGetPageByLink() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();
        WebsiteData wd0 = (WebsiteData)umgr.getWebsites(
                umgr.getUser(enabledUserName), null, null).get(0);
        assertNotNull(wd0.getPageByLink("Weblog"));
    }

    public void testGetPages() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();
        WebsiteData wd0 = (WebsiteData)umgr.getWebsites(
                umgr.getUser(enabledUserName), null, null).get(0);
        assertEquals(5, wd0.getPages().size());
    }

    public void hide_testUpdateIfNeeded() throws Exception
    {
        UserManager umgr = getRoller().getUserManager();
        WeblogManager wmgr = getRoller().getWeblogManager();
        PersistenceStrategy pstrategy = getRoller().getPersistenceStrategy();

        // create cats without a root
        getRoller().begin(UserData.SYSTEM_USER);

        WebsiteData website = umgr.retrieveWebsite(mWebsite.getId());
        WeblogCategoryData origRoot = wmgr.getRootWeblogCategory(mWebsite);
        website.setBloggerCategory(null);
        website.setDefaultCategory(null);

        if (null != origRoot) origRoot.remove();

        WeblogCategoryData cat1 = wmgr.createWeblogCategory(
            website, null, "cat1 name", "cat1 desc", null);
        pstrategy.store(cat1);

        WeblogCategoryData cat2 = wmgr.createWeblogCategory(
            website, null, "cat2 name", "cat2 desc", null);
        pstrategy.store(cat2);

        WeblogCategoryData cat3 = wmgr.createWeblogCategory(
            website, null, "cat3 name", "cat3 desc", null);
        pstrategy.store(cat3);

        getRoller().commit();

        // upgrade site

        // We need a database connection and the hibernate.properties file
        // is easier to parse than the Castor database.xml file.
        Properties hibernateProperties = new Properties();
        hibernateProperties.load(new FileInputStream("hibernate.properties"));
        String driverClass = hibernateProperties.getProperty("hibernate.connection.driver_class");
        String connectionUrl = hibernateProperties.getProperty("hibernate.connection.url");
        Class.forName(driverClass);
        Connection con = DriverManager.getConnection(connectionUrl);

        getRoller().upgradeDatabase(con);

        // verify that upgrade created a root and assigned it to cats
        getRoller().begin(UserData.SYSTEM_USER);
        WeblogCategoryData root = wmgr.getRootWeblogCategory(mWebsite);
        assertNotNull(root);

        cat1 = wmgr.retrieveWeblogCategory(cat1.getId());
        assertEquals(root, cat1.getParent());

        cat2 = wmgr.retrieveWeblogCategory(cat2.getId());
        assertEquals(root, cat2.getParent());

        cat3 = wmgr.retrieveWeblogCategory(cat3.getId());
        assertEquals(root, cat3.getParent());

        getRoller().release();
    }


}
