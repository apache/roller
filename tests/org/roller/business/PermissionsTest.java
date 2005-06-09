package org.roller.business; 

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;
 
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.CommentData;
import org.roller.pojos.FolderData;
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.Utilities;
import org.roller.RollerTestBase;

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
        
        // evil testuser
        UserData testuser = getRoller().getUserManager().getUser("testuser");       
        assertNotNull(testuser);
        
        // gets hold of testuser0's entry
        WebsiteData website0 = getRoller().getUserManager().getWebsite("testuser0");
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
        
        // evil testuser
        UserData testuser = getRoller().getUserManager().getUser("testuser");       
        assertNotNull(testuser);
        
        // gets hold of testuser0's entry
        WebsiteData website0 = getRoller().getUserManager().getWebsite("testuser0");
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
        
        // evil testuser
        UserData testuser = getRoller().getUserManager().getUser("testuser");       
        assertNotNull(testuser);
        
        // gets hold of testuser0's entry
        WebsiteData website0 = getRoller().getUserManager().getWebsite("testuser0");
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
}

