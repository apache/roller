package org.roller.business; 

import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.RollerPermissionsException;
import org.roller.RollerTestBase;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.PageData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

/**
 * Test Roller Weblog Management.
 */
public class PermissionsTest extends RollerTestBase 
{    
    public PermissionsTest(String name) 
    {
        super(name);
    }
    
    public static void main(String args[]) 
    {
        junit.textui.TestRunner.run(PermissionsTest.class);
    }
    
    public static Test suite() 
    {
        return new TestSuite(PermissionsTest.class);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        setUpTestWeblogs();
    }
        
    public void tearDown() throws Exception
    {
        super.tearDown();
        tearDownTestWeblogs();
    }
    
    public void testWeblogEntryPermissions() throws Exception
    {
        getRoller().begin(UserData.ANONYMOUS_USER);
        UserManager umgr = getRoller().getUserManager();
        
        // evil testuser
        UserData testuser = umgr.getUser("testuser");       
        assertNotNull(testuser);
        
        // gets hold of testuser0's entry
        UserData testuser0 = umgr.getUser("testuser0");
        WebsiteData website0 = (WebsiteData)umgr.getWebsites(testuser0, null).get(0);
        assertNotNull(website0);
        List entries = getRoller().getWeblogManager().getWeblogEntries(
                website0,
                null,       // start
                new Date(), // end
                null,       // cat
                WeblogManager.ALL,
                new Integer(1));
        WeblogEntryData entry = (WeblogEntryData)entries.get(0);
        assertNotNull(entry);
        
        // and tries to save it
        getRoller().setUser(testuser);
        boolean denied = false; 
        try 
        {
            entry.save();
        }
        catch (RollerPermissionsException e)
        {
            // permission denied!
            denied = true;
        }       
        assertTrue(denied);
        getRoller().rollback();
    }
    
    public void testBookmarkPermissions() throws Exception
    {
        getRoller().begin(UserData.ANONYMOUS_USER);
        UserManager umgr = getRoller().getUserManager();
        
        // evil testuser
        UserData testuser = umgr.getUser("testuser");       
        assertNotNull(testuser);
        
        // gets hold of testuser0's entry
        UserData testuser0 = umgr.getUser("testuser0");
        WebsiteData website0 = (WebsiteData)umgr.getWebsites(testuser0, null).get(0);
        assertNotNull(website0);
        List folders = getRoller().getBookmarkManager().getAllFolders(website0);
        FolderData root = (FolderData)folders.get(0);
        FolderData folder = (FolderData)root.getFolders().get(0);
        
        BookmarkData bookmark = (BookmarkData)(folder.getBookmarks().iterator().next());
        assertNotNull(bookmark);
        
        // and tries to save it
        getRoller().setUser(testuser);
        boolean denied = false; 
        try 
        {
            bookmark.save();
        }
        catch (RollerPermissionsException e)
        {
            // permission denied!
            denied = true;
        }       
        assertTrue(denied);
        
        getRoller().setUser(testuser);
        denied = false; 
        try 
        {
            folder.save();
        }
        catch (RollerPermissionsException e)
        {
            // permission denied!
            denied = true;
        }       
        assertTrue(denied);
       
        getRoller().rollback();
    }

    public void testPagePermissions() throws Exception
    {
        getRoller().begin(UserData.ANONYMOUS_USER);
        UserManager umgr = getRoller().getUserManager();
        
        // evil testuser
        UserData testuser = umgr.getUser("testuser");       
        assertNotNull(testuser);
        
        // gets hold of testuser0's entry
        UserData testuser0 = umgr.getUser("testuser0");
        WebsiteData website0 = (WebsiteData)umgr.getWebsites(testuser0, null).get(0);
        assertNotNull(website0);
        PageData page = (PageData)getRoller().getUserManager().getPages(website0).get(0);
        assertNotNull(page);
        
        // and tries to save it
        getRoller().setUser(testuser);
        boolean denied = false; 
        try 
        {
            page.save();
        }
        catch (RollerPermissionsException e)
        {
            // permission denied!
            denied = true;
        }       
        assertTrue(denied);
        
        
        // and tries to save it
        getRoller().setUser(testuser);
        denied = false; 
        try 
        {
            website0.save();
        }
        catch (RollerPermissionsException e)
        {
            // permission denied!
            denied = true;
        }       
        assertTrue(denied);

        
        getRoller().rollback();
    }

    /*
     Disabling this test for now because it won't work.
    public void testConfigPermissions() throws Exception
    {
        getRoller().begin(UserData.ANONYMOUS_USER);
        
        // evil testuser0
        UserData testuser0 = getRoller().getUserManager().getUser("testuser0");       
        assertNotNull(testuser0);
        
        // gets hold of testuser's (an admin) entry
        WebsiteData website = getRoller().getUserManager().getWebsite("testuser");
        assertNotNull(website);
        
        Map config = getRoller().getPropertiesManager().getProperties();
        assertNotNull(config);
        
        // and tries to save it
        getRoller().setUser(testuser0);
        boolean denied = false; 
        try 
        {
            getRoller().getPropertiesManager().store(config);
        }
        catch (RollerPermissionsException e)
        {
            // permission denied!
            denied = true;
        }       
        assertTrue(denied);
        getRoller().rollback();
    }
    */
    
    /**
     * Tests permissions object creation and invitations, specifically:
     * <ul>
     * <li> user can be invited to website </li>
     * <li> can get list of invitations (pending permissions) for user </li>
     * <li> can accept invitation by setting pending to false</li>
     * <li> can get list of permissions from user </li>
     * <li> can get list of permissions from website </li>
     * <li> users can be removed from website</li>
     * </ul>
     */
    public void testInvitations()
    {
        try 
        {
            UserManager umgr = getRoller().getUserManager();
            String userName = "testuser0";
            String permsId;
            
            // test invite user to website
            getRoller().begin();
            {
                UserData tuser = umgr.getUser(userName);       
                WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);
                PermissionsData perms = umgr.inviteUser(
                        tsite, tuser, PermissionsData.LIMITED);
                permsId = perms.getId();
            }
            getRoller().commit();  
            
            // test user accepts invitation
            getRoller().begin();
            {
                UserData tuser = umgr.getUser(userName);       
                WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);
                
                // can get pending permission object
                PermissionsData perms = umgr.getPermissions(tsite, tuser);
                assertNotNull(perms);
                assertTrue(perms.isPending());

                // can get pending permission object via user
                List invitations = umgr.getPendingPermissions(tuser);
                PermissionsData pending = (PermissionsData)invitations.get(0);
                assertTrue(pending.getId().equals(permsId));

                // can get pending permission object via website
                invitations = umgr.getPendingPermissions(tsite);
                pending = (PermissionsData)invitations.get(0);
                assertTrue(pending.getId().equals(permsId));

                // accept invitation
                pending.setPending(false);
                pending.save();
            }
            getRoller().commit();
            
            // test user is member of website, website has member user
            getRoller().begin();
            {
                UserData tuser = umgr.getUser(userName);       
                WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);

                // assert that invitation list is empty
                assertTrue(umgr.getPendingPermissions(tuser).isEmpty());
                assertTrue(umgr.getPendingPermissions(tsite).isEmpty());

                // assert that user is member of website
                assertFalse(umgr.getPermissions(tsite, tuser).isPending());
                
                // assert that user has website
                List websites = umgr.getWebsites(tuser, null);
                assertEquals( tsite.getId(), 
                              ((WebsiteData)websites.get(0)).getId());
                
                // assert that website has user
                List users = umgr.getUsers(tsite, null);
                assertEquals( tuser.getId(),
                              ((UserData)users.get(0)).getId());
            }
            getRoller().commit();    
            
            // test user can be retired from website
            getRoller().begin();
            {
                UserData tuser = umgr.getUser(userName);       
                WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);
                umgr.retireUser(tsite, tuser);
            }
            getRoller().commit();
            getRoller().begin();
            {
                UserData tuser = umgr.getUser(userName);       
                WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);                
                List websites = umgr.getWebsites(tuser, null);
                assertEquals(0, websites.size());
                List users = umgr.getUsers(tsite, null);
                assertEquals(0, users.size());
            }
            getRoller().commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }               
    }
    
    /**
     * Test permissions removal and related cascades, specifically:
     * <ul>
     * <li> when user is deleted, permissions are deleted too </li>
     * <li> when website is deleted, permissions are deleted too </li>
     * </ul>
     */
     public void testPermissionsRemoval() throws Exception
     {
         try 
         {
             UserManager umgr = getRoller().getUserManager();
             String userName = "testuser0";
             String permsId = null;
             
             // Create add user to website
             getRoller().begin();
             {
                 UserData tuser = umgr.getUser(userName);       
                 WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);                
                 PermissionsData perms = new PermissionsData();
                 perms.setUser(tuser);
                 perms.setWebsite(tsite);
                 perms.save();
                 permsId = perms.getId();
             }
             getRoller().commit();    
             
             // delete website
             getRoller().begin(UserData.SYSTEM_USER);
             {
                 UserData tuser = umgr.getUser(userName);       
                 WebsiteData tsite = (WebsiteData)umgr.getWebsites(tuser, null).get(0);
                 tsite.remove();                 
             }
             getRoller().commit(); 
             
             // ensure that permission was deleted too
             getRoller().begin();
             {
                 assertNull(getRoller().getPersistenceStrategy().load(
                         permsId, PermissionsData.class));                 
             }
             getRoller().commit();    
         }
         catch (Exception e)
         {
             e.printStackTrace();
             fail();
         }               
     }
}

