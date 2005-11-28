/*
 * Created on Aug 13, 2003
 */
package org.roller.business;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.BookmarkManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.Utilities;

/**
 * Abstract base implementation using PersistenceStrategy.
 * @author Dave Johnson
 * @author Lance Lavandowska
 */
public abstract class UserManagerImpl implements UserManager
{
    protected PersistenceStrategy mStrategy;
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserManagerImpl.class);
    
    public UserManagerImpl(PersistenceStrategy strategy)
    {
        mStrategy = strategy;
    }

    public void release()
    {
    }
            
    public PermissionsData retrievePermissions(String inviteId) 
        throws RollerException
    {
        return (PermissionsData)mStrategy.load(inviteId, PermissionsData.class);
    }
    
    //--------------------------------------------------------------- Website

    public WebsiteData retrieveWebsite(String id) throws RollerException
    {
        return (WebsiteData)mStrategy.load(id,WebsiteData.class);
    }

    /** 
     * @see org.roller.model.UserManager#storeWebsite(org.roller.pojos.WebsiteData)
     */
    public void storeWebsite(WebsiteData data) throws RollerException
    {
        mStrategy.store(data);
    }

    public void removeWebsite(String id) throws RollerException
    {
        mStrategy.remove(id,WebsiteData.class);
    }

    //------------------------------------------------------------------- User

    public UserData retrieveUser(String id) throws RollerException
    {
        return (UserData)mStrategy.load(id,UserData.class);
    }

    public void storeUser(UserData data) throws RollerException
    {
        mStrategy.store(data);
    }

    public void removeUser(String id) throws RollerException
    {
        mStrategy.remove(id,UserData.class);
    }

    //-----------------------------------------------------------------------

    public UserData getUser(String userName) throws RollerException
    {
        return getUser(userName, Boolean.TRUE);
    }

    public WebsiteData getWebsiteByHandle(String handle) throws RollerException
    {
        return getWebsiteByHandle(handle, Boolean.TRUE);
    }

    //-----------------------------------------------------------------------

    public List getUsers() throws RollerException
    {
        return getUsers(Boolean.TRUE);
    }

    //------------------------------------------------------------------------    
    /** 
     * @see org.roller.model.UserManager#retrievePage(java.lang.String)
     */
    public WeblogTemplate retrievePageReadOnly(String id) throws RollerException
    {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) return null; 

        // Hibernate has a read-only flag: LockMode.READ
        return (WeblogTemplate)mStrategy.load(id,WeblogTemplate.class);
    }

    //------------------------------------------------------------------- Role

    public RoleData retrieveRole(String id) throws RollerException
    {
        return (RoleData)mStrategy.load(id,RoleData.class);
    }

    public void storeRole(RoleData data) throws RollerException
    {
        mStrategy.store(data);
    }

    public void removeRole(String id) throws RollerException
    {
        mStrategy.remove(id,RoleData.class);
    }

    //------------------------------------------------------------------- Page

    public WeblogTemplate retrievePage(String id) throws RollerException
    {
        // Don't hit database for templates stored on disk
        if (id != null && id.endsWith(".vm")) return null; 

        return (WeblogTemplate)mStrategy.load(id,WeblogTemplate.class);
    }
 
    public void removePage(String id) throws RollerException
    {
        mStrategy.remove(id,WeblogTemplate.class);
    }

    public void removePageSafely(String id) throws RollerException
    {
        WeblogTemplate pd = retrievePageReadOnly(id);
        if (pd == null) return;

        WebsiteData wd = pd.getWebsite();
        if (pd.getId() == wd.getDefaultPageId()) {
            mLogger.error(
                   "Refusing to remove default page from website with handle: " 
                   +  wd.getHandle());
            throw new RollerException(
                   new IllegalArgumentException("Page is default page of website."));
        }
        removePage(id);        
    }

    /**
     * @see org.roller.model.UserManager#storePage(org.roller.pojos.WeblogTemplate)
     */
    public void storePage(WeblogTemplate data) throws RollerException
    {
        mStrategy.store(data);
    }
    
    public String fixPageLink(WeblogTemplate data) throws RollerException
    {
        String link = Utilities.removeHTML(data.getName());
        link = Utilities.removeNonAlphanumeric(link);

        data.setLink(link);
        mStrategy.store( data );

        return link;
    }

    /**
     * Add a new Roller user. Store new User, Role, Website, Category, and a
     * "first post" WeblogEntry in the database. Reads in files from a theme
     * directory and adds them as Pages for the User's new Website.
     * 
     * @param ud  User object representing the new user.
     * @param themeDir Directory containing the theme for this user
     */
    public void addUser(UserData ud)
        throws RollerException
    {        
        Roller mRoller = RollerFactory.getRoller();
        UserManager umgr = mRoller.getUserManager();
        if (    umgr.getUser(ud.getUserName()) != null 
             || umgr.getUser(ud.getUserName().toLowerCase()) != null) 
        {
            throw new RollerException("error.add.user.userNameInUse");
        }
        
        boolean adminUser = false;
        List users = this.getUsers();
        if (users.size() == 0) 
        {
            // Make first user an admin
            adminUser = true;
        }        
        mStrategy.store(ud);
        if (adminUser) ud.grantRole("admin");
        
        RoleData rd = new RoleData(null, ud, "editor");
        mStrategy.store(rd);
    }
    
    public WebsiteData createWebsite(
            UserData ud, 
            Map pages, 
            String handle,
            String name, 
            String description,
            String email,
            String theme, 
            String locale, 
            String timeZone) throws RollerException
    {
        Roller mRoller = RollerFactory.getRoller();
        UserManager umgr = mRoller.getUserManager();
        WeblogManager wmgr = mRoller.getWeblogManager();
        WebsiteData website = new WebsiteData(
            null,                // id
            name,                // name
            handle,              // handle
            description,         // description
            ud,                  // userId
            "dummy",             // defaultPageId
            "dummy",             // weblogDayPageId
            Boolean.TRUE,        // enableBloggerApi
            null,                // bloggerCategory
            null,                // defaultCategory
            "editor-text.jsp",   // editorPage
            "",                  // ignoreWords
            Boolean.TRUE,        // allowComments  
            Boolean.FALSE,       // emailComments
            "",                  // emailFromAddress
            Boolean.TRUE,        // isEnabled
            email,               // emailAddress
            new Date());
        website.setEditorTheme(theme);
        website.setLocale(locale);
        website.setTimeZone(timeZone);
        website.save();

        WeblogCategoryData rootCat = wmgr.createWeblogCategory(
            website, // websiteId
            null,   // parent
            "root",  // name
            "root",  // description
            null ); // image
        rootCat.save();

        String cats = RollerConfig.getProperty("newuser.categories");
        if (cats != null)
        {
            String[] splitcats = cats.split(",");
            for (int i=0; i<splitcats.length; i++)
            {
                WeblogCategoryData c = wmgr.createWeblogCategory(
                    website,         // website
                    rootCat,         // parent
                    splitcats[i],    // name
                    splitcats[i],    // description
                    null );          // image
                c.save();
            }
        }        
        website.setBloggerCategory(rootCat);
        website.setDefaultCategory(rootCat);
                
        BookmarkManager bmgr = mRoller.getBookmarkManager();                    
        FolderData root = bmgr.createFolder(
            null, "root", "root", website);
        root.save();

        Integer zero = new Integer(0);
        String blogroll = RollerConfig.getProperty("newuser.blogroll");
        if (blogroll != null)
        {
            String[] splitroll = blogroll.split(",");
            for (int i=0; i<splitroll.length; i++)
            {
                String[] rollitems = splitroll[i].split("\\|");
                if (rollitems != null && rollitems.length > 1) {
                    BookmarkData b = bmgr.createBookmark(
                        root,                // parent
                        rollitems[0],        // name
                        "",                  // description
                        rollitems[1].trim(), // url
                        null,                // feedurl
                        zero,                // weight
                        zero,                // priority
                        null);               // image
                    b.save();  
                }
            }
        }
        
        // Add user as member of website
        PermissionsData perms = new PermissionsData();
        perms.setUser(ud);
        perms.setWebsite(website);
        perms.setPending(false);
        perms.setPermissionMask(PermissionsData.ADMIN);
        perms.save();

        return website;
    }

    /**
     * Creates and stores a pending PermissionsData for user and website specified.
     */
    public PermissionsData inviteUser(WebsiteData website, 
            UserData user, short mask) throws RollerException
    {
        if (website == null) throw new RollerException("Website cannot be null");
        if (user == null) throw new RollerException("User cannot be null");        
        PermissionsData perms = new PermissionsData();
        perms.setWebsite(website);
        perms.setUser(user);
        perms.setPermissionMask(mask);
        mStrategy.store(perms);
        return perms;
    }
    
    /**
     * Remove user permissions from a website.
     */
    public void retireUser(WebsiteData website, UserData user) throws RollerException
    {
        if (website == null) throw new RollerException("Website cannot be null");
        if (user == null) throw new RollerException("User cannot be null");        
        Iterator perms = website.getPermissions().iterator();
        PermissionsData target = null;
        while (perms.hasNext())
        {
            PermissionsData pd = (PermissionsData)perms.next();
            if (pd.getUser().getId().equals(user.getId()))
            {
                target = pd;
                break;
            }
        }
        if (target == null) throw new RollerException("User not member of website");
        website.removePermission(target);
        target.remove();
    }
}
