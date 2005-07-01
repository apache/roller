/*
 * Created on Aug 13, 2003
 */
package org.roller.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.BookmarkManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserCookieData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.RandomGUID;
import org.roller.util.Utilities;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.roller.model.RollerFactory;

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

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
    /** 
     * This method is a hotspot, it is called on every page request.
     */
    public WebsiteData getWebsite(String userName) throws RollerException
    {
        return getWebsite(userName, true);
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
        return getUser(userName, true);
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -     
    public UserData getUser(String userName, boolean enabledOnly) throws RollerException
    {
        if (userName==null )
            throw new RollerException("userName is null");
        
        WebsiteData website = getWebsite(userName, enabledOnly);
        if (website != null)
        {
            return website.getUser();
        }
        return null;
    }

    //-----------------------------------------------------------------------

    public List getUsers() throws RollerException
    {
        return getUsers(true);
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
            mLogger.error("Refusing to remove default page from website of: " +  wd.getUser().getUserName());
            throw new RollerException(new IllegalArgumentException("Page is default page of website."));
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
    public void addUser(UserData ud, Map pages, String theme, 
                        String locale, String timezone)
        throws RollerException
    {        
        Roller mRoller = RollerFactory.getRoller();
        UserManager umgr = mRoller.getUserManager();
        WeblogManager wmgr = mRoller.getWeblogManager();
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
        
        RoleData rd = new RoleData(null, ud, "editor");
        mStrategy.store(rd);
        
        //
        // CREATE WEBSITE AND CATEGORIES FOR USER
        //
        
        WebsiteData website = new WebsiteData(null,
            ud.getFullName()+"'s Weblog", // name
            ud.getFullName()+"'s Weblog", // description
            ud,                // userId
            "dummy",           // defaultPageId
            "dummy",           // weblogDayPageId
            Boolean.TRUE,      // enableBloggerApi
            null,                // bloggerCategory
            null,                // defaultCategory
            "editor-text.jsp", // editorPage
            "",                // ignoreWords
            Boolean.TRUE,      // allowComments  
            Boolean.FALSE,     // emailComments
            "",                // emailFromAddress
            Boolean.TRUE);     // isEnabled
        website.setEditorTheme(theme);
        website.setLocale(locale);
        website.setTimezone(timezone);
        website.save();

        WeblogCategoryData rootCat = wmgr.createWeblogCategory(
            website, // websiteId
            null,   // parent
            "root",  // name
            "root",  // description
            null ); // image
        rootCat.save();
        
        WeblogCategoryData generalCat = wmgr.createWeblogCategory(
            website,         // websiteId
            rootCat,
            "General",       // name
            "General",       // description
            null );         // image
        generalCat.save();
            
        WeblogCategoryData javaCat = wmgr.createWeblogCategory(
            website,         // websiteId
            rootCat,
            "Java",          // name
            "Java",          // description
            null );          // image
        javaCat.save();
            
        WeblogCategoryData musicCat = wmgr.createWeblogCategory(
            website,         // websiteId
            rootCat,
            "Music",         // name
            "Music",         // description
            null );         // image
        musicCat.save();
        
        website.setBloggerCategory(rootCat);
        website.setDefaultCategory(rootCat);
        
        Integer zero = new Integer(0);
        
        BookmarkManager bmgr = mRoller.getBookmarkManager();
                    
        FolderData root = bmgr.createFolder(
            null, "root", "root", website);
        root.save();

        FolderData blogroll = bmgr.createFolder(
            root, "Blogroll", "Blogroll", website);
        blogroll.save();

        BookmarkData b1 = bmgr.createBookmark(
            blogroll, "Dave Johnson", "",
            "http://rollerweblogger.org/page/roller",
            "http://rollerweblogger.org/rss/roller",
            zero, zero, null);
        b1.save();

        BookmarkData b2 = bmgr.createBookmark(
            blogroll, "Matt Raible", "",
            "http://raibledesigns.com/page/rd",
            "http://raibledesigns.com/rss/rd",
            zero, zero, null);
        b2.save();

        BookmarkData b3 = bmgr.createBookmark(
            blogroll, "Lance Lavandowska", "",
            "http://brainopolis.dnsalias.com/roller/page/lance/",
            "http://brainopolis.dnsalias.com/roller/rss/lance/",
            zero, zero, null);
        b3.save();
        
        
        FolderData news = bmgr.createFolder(
            root, "News", "News", website);
        news.save();

        BookmarkData b5 = bmgr.createBookmark(
            news, "CNN", "",
            "http://www.cnn.com",
            "",
            zero, zero, null);
        b5.save();

        BookmarkData b6 = bmgr.createBookmark(
            news, "NY Times", "", 
           "http://nytimes.com",
           "",
            zero, zero, null);
        b6.save();

        //
        // READ THEME FILES AND CREATE PAGES FOR USER
        //
        Iterator iter = pages.keySet().iterator();
        while ( iter.hasNext() )
        {
            String pageName = (String) iter.next();
            String sb = (String)pages.get( pageName );
              
            // Store each Velocity template as a page
            WeblogTemplate pd = new WeblogTemplate( null,
                website,         // website
                pageName,        // name
                pageName,        // description
                pageName,        // link
                sb,              // template
                new Date()       // updateTime                
            );
            mStrategy.store(pd);
            
            if ( pd.getName().equals("Weblog") )
            {  
                website.setDefaultPageId(pd.getId());                 
            }
            else if ( pd.getName().equals("_day") )
            {
                website.setWeblogDayPageId(pd.getId());                 
            }                
        }
        
        if (adminUser) ud.grantRole("admin");
        
        // Save website with blogger cat id, defauld page id and day id
        mStrategy.store(website); 
    }
    
    /**
     * @see org.roller.model.UserManager#createLoginCookie(java.lang.String)
     */
    public String createLoginCookie(String username) throws RollerException 
    {
        UserCookieData cookie = new UserCookieData();
        cookie.setUsername(username);

        return saveLoginCookie(cookie);
    }

    /**
     * Convenience method to set a unique cookie id and save to database
     * 
     * @param cookie
     * @return
     * @throws Exception
     */
    protected String saveLoginCookie(UserCookieData cookie) throws RollerException 
    {
        cookie.setCookieId(new RandomGUID().toString());
        cookie.save();

        String cookieString = null;
        try {
            cookieString = Utilities.encodeString(cookie.getUsername() + "|" +
            		       cookie.getCookieId());
        } catch (IOException io) {
        	mLogger.warn("Failed to encode rememberMe cookieString");
            mLogger.warn(io.getMessage());  
            cookieString = cookie.getUsername() + "|" + cookie.getCookieId();
        }
        return cookieString;
    }
}
