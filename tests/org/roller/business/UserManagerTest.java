
package org.roller.business;

import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.FolderData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;


///////////////////////////////////////////////////////////////////////////////
/**
 * Test Roller User Management.
 */
public class UserManagerTest  extends RollerTestBase
{
    UserData enabledUser = null;
    UserData disabledUser = null;
    String enabledUserName = "enabledUser";
    String disabledUserName = "disabledUser";

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
    protected void setUp() throws Exception
    {
        super.setUp();

        //getRoller().begin();
        UserManager umgr = getRoller().getUserManager();

        enabledUser = createUser(umgr,
                                 enabledUserName,
                                 "password",
                                 "EnabledUser",
                                 "enabledUser@example.com"
        );

        disabledUser = createUser(umgr,
                                 disabledUserName,
                                 "password",
                                 "DisabledUser",
                                 "disabledUser@example.com"
        );
        umgr.getWebsite(disabledUserName,false).setIsEnabled(Boolean.FALSE);

        getRoller().commit();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();

        //getRoller().begin();
        UserManager umgr = getRoller().getUserManager();

        enabledUser = umgr.retrieveUser(enabledUser.getId());
        enabledUser.remove();

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

        //getRoller().begin();
        stored = new UserData(
            null,
            "testUserStorage",
            "password2",
            "TestUser2",
            "testuser2@example.com",
            new java.util.Date());
        umgr.storeUser(stored);
        getRoller().commit();

        //getRoller().begin();
        UserData retrieved = umgr.retrieveUser(stored.getId());
        assertEquals(stored, retrieved);
        getRoller().release();

        //getRoller().begin();
        retrieved = umgr.retrieveUser(stored.getId());
        retrieved.remove();
        getRoller().commit();

        //getRoller().begin();
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

        //getRoller().begin();

        user = new UserData(
            null,
            "testWebsiteStorage",
            "password3",
            "TestUser3",
            "testuser3@example.com",
            new java.util.Date());
        umgr.storeUser( user );

        stored = new WebsiteData(
            null,
            "testsite",
            "Testsite",
            user,
            "dpid",
            "wpid",
            Boolean.FALSE,
            null,
            null,
            "edit-weblog.jsp",
            "ignore",
            Boolean.TRUE,
            Boolean.FALSE,
            null,
            Boolean.TRUE);
        umgr.storeWebsite(stored);

        FolderData rootFolder = getRoller().getBookmarkManager()
            .createFolder(null, "root", "root", stored);
        rootFolder.save();

        WeblogCategoryData rootCategory = getRoller().getWeblogManager()
            .createWeblogCategory(stored, null, "root", "root", "");
        rootCategory.save();

        stored.setBloggerCategory(rootCategory);
        stored.setDefaultCategory(rootCategory);
        stored.save();

        getRoller().commit();

        //getRoller().begin();
        WebsiteData retrieved = umgr.retrieveWebsite(stored.getId());
        assertEquals(stored, retrieved);
        getRoller().release();

        //getRoller().begin();
        user = umgr.retrieveUser(user.getId());
        user.remove();
        getRoller().commit();

        assertNull(umgr.retrieveUser(user.getId()));
        assertNull(umgr.retrieveWebsite(stored.getId()));
    }

    public void testAddRemoveUser() throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();

        // Add a user

        //getRoller().begin();
        UserData user = new UserData(
            null,
            "testAddRemoveUser",
            "password4",
            "TestUser4",
            "testuser4@example.com",
            new java.util.Date());
        Map pages = new HashMap();
        pages.put("Weblog","Weblog page content");
        pages.put("_day","Day page content");
        pages.put("css","CSS page content");
        umgr.addUser(user, pages, "basic", "en_US_WIN", "America/Los_Angeles");
        getRoller().commit();

        // Verify that user has all the goodies

        UserData user1 = umgr.retrieveUser(user.getId());
        assertNotNull(user1);

        WebsiteData website = umgr.getWebsite(user.getUserName());
        assertNotNull(website);

        FolderData root = getRoller().getBookmarkManager().getRootFolder(website);
        assertNotNull(root);

        List pages1 = getRoller().getUserManager().getPages(website);
        assertEquals(3, pages1.size());

        // Remove the user

        //getRoller().begin();
        user = umgr.retrieveUser(user.getId());
        user.remove();
        getRoller().commit();

        // Verify that user was completely deleted

        UserData user2 = umgr.retrieveUser(user.getId());
        assertNull(user2);

        WebsiteData website2 = umgr.getWebsite(user.getUserName());
        assertNull(website2);

        assertNull(getRoller().getBookmarkManager().retrieveFolder(root.getId()));

        List pages2 = getRoller().getUserManager().getPages(website);
        assertEquals(0, pages2.size());
    }

    public void testGetWebsite() throws RollerException
    {
        // can get testuser0 who is enabled
        assertNotNull(getRoller().getUserManager().getWebsite(enabledUserName));

        // can't get testuser1, who is disabled
        assertNull(getRoller().getUserManager().getWebsite(disabledUserName));

        // can get testuser1 with enabledOnly flag set to false
        assertNotNull(getRoller().getUserManager().getWebsite(disabledUserName,false));
    }

    public void testGetUser() throws RollerException
    {
        // can get testuser0 who is enabled
        assertNotNull(getRoller().getUserManager().getUser(enabledUserName));

        // can't get testuser1, who is disabled
        assertNull(getRoller().getUserManager().getUser(disabledUserName));

        // can get testuser1 with enabledOnly flag set to false
        assertNotNull(getRoller().getUserManager().getUser(disabledUserName,false));
    }

    public void testGetUsers() throws RollerException
    {
        // There are users
        int userCountEnabled = getRoller().getUserManager().getUsers().size();
        assertTrue(userCountEnabled > 0);

        // At least one user is disabled
        int userCountAll = getRoller().getUserManager().getUsers(false).size();
        assertTrue(userCountAll > userCountEnabled);
    }

    public void testGetPageByName() throws RollerException
    {
        WebsiteData wd0 = getRoller().getUserManager().getWebsite(enabledUserName);
        assertNotNull(getRoller().getUserManager().getPageByName(wd0,"Weblog"));
    }

    public void testGetPageByLink() throws RollerException
    {
        WebsiteData wd0 = getRoller().getUserManager().getWebsite(enabledUserName);
        assertNotNull(getRoller().getUserManager().getPageByLink(wd0,"Weblog"));
    }

    public void testGetPages() throws RollerException
    {
        // testuser0 is enabled and has 3 pages
        WebsiteData wd0 = getRoller().getUserManager().getWebsite(enabledUserName);
        assertEquals(3, getRoller().getUserManager().getPages(wd0).size());
    }

    public void hide_testUpdateIfNeeded() throws Exception
    {
        UserManager umgr = getRoller().getUserManager();
        WeblogManager wmgr = getRoller().getWeblogManager();
        PersistenceStrategy pstrategy = getRoller().getPersistenceStrategy();

        // create cats without a root
        //getRoller().begin();

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
        //getRoller().begin();
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
