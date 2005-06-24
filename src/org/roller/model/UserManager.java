
package org.roller.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.roller.RollerException;
import org.roller.pojos.PageData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;


/**
 * Manages storage, retrieval, and querying of user, website, and page data.
 * 
 * @author David M Johnson
 */
public interface UserManager extends Serializable
{
    /** 
     * Get user by name
     * @param name Username of user
     */
    //public UserData getUserByName(String name);

    /**
     * Get all users or a website.
     * @param website Get all users of this website (or null for all)
     * @returns List of UserData objects.
     */
    public List getUsers(WebsiteData website, Boolean enabled) 
        throws RollerException;
    
    /** 
     * Get all websites of which user is a member
     * @param user    Get all websites for this user (or null for all)
     * @param enabled Get all with this enabled state (or null or all)
     * @returns List of WebsiteData objects.
     */
    public List getWebsites(UserData user, Boolean enabled)
        throws RollerException;

    /** 
     * Get pending permissions for user
     * @param user User (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(UserData user) throws RollerException;
    
    /** 
     * Get pending permissions for website
     * @param website Website (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(WebsiteData user) throws RollerException;
    
    /**
     * Get permissions of user in website
     * @param website Website (not null)
     * @param user    User (not null)
     * @return        PermissionsData object
     */
    public PermissionsData getPermissions(
            WebsiteData website, UserData user) throws RollerException;
    
    /**
     * Invite user to join a website with specific permissions
     * @param website Website to be joined (persistent instance) 
     * @param user    User to be invited (persistent instance)
     * @param perms   Permissions mask (see statics in PermissionsData)
     * @return        New PermissionsData object, with pending=true
     */
    public PermissionsData inviteUser(
            WebsiteData website, UserData user, short perms) throws RollerException;

    /**
     * Retire user from a website
     * @param website Website to be retired from (persistent instance) 
     * @param user    User to be retired (persistent instance)
     */
    public void retireUser(
            WebsiteData website, UserData user) throws RollerException;

    /** Release any resources used */
    public void release();
    
    //--------------------------------------------------------------- UserData
    
    /** Get all enabled users */
    public List getUsers() throws RollerException;
    
	/** 
     * Get all users, optionally include dis-enabled users.
     * @param enabled True for enabled only, false for disabled only, null for all
     */
	public List getUsers(Boolean enabled) throws RollerException;

    /** Get user object by user name (only enabled users) */
    public UserData getUser( String userName ) throws RollerException;

	/** Get user object by user name, optionally include dis-enabled users */
	public UserData getUser( String userName, boolean enabledOnly ) throws RollerException;

    /** Add a new user with pages, bookmarks, folders...
     * @param user New user object to be added to database
     * @param themeDir Directory containing theme for user
     */
    public void addUser( UserData user, Map page, String theme,
            String locale, String timezone)
        throws RollerException;

    public UserData retrieveUser(String id)throws RollerException;
    public void storeUser( UserData data ) throws RollerException;

    public List getUserRoles(UserData user) throws RollerException;
    public RoleData retrieveRole(String id) throws RollerException;
    public void storeRole( RoleData data ) throws RollerException;
    public void removeRole( String id ) throws RollerException;

    //------------------------------------------------------------ WebsiteData
    
    /** 
     * Get website specified by handle (or null if enabled website not found).
     * @param handle  Handle of website
     */
    public WebsiteData getWebsiteByHandle(String handle) 
        throws RollerException;

    /** 
     * Get website specified by handle with option to return only enabled websites.
     * @param handle  Handle of website
     */
    public WebsiteData getWebsiteByHandle(String handle, boolean enabled) 
        throws RollerException;

    public WebsiteData retrieveWebsite(String id) throws RollerException;
    public void storeWebsite(WebsiteData data) throws RollerException;

    //--------------------------------------------------------------- PageData
    
    /** Get user's page by name */
    public PageData getPageByName(WebsiteData w, String p) throws RollerException;

    /** Get user's page by link */
    public PageData getPageByLink(WebsiteData w, String p) throws RollerException;

    /** Fix page link using page name */
    public String fixPageLink(PageData data) throws RollerException;

    /** Get users pages */
    public List getPages(WebsiteData w) throws RollerException;

    public PageData retrievePage(String id) throws RollerException;
    public void storePage(PageData data) throws RollerException;
    public void removePage(String id) throws RollerException;


    /**
     * Remove page safely.  This will throw an exception on attempts to remove mandatory website pages such as the
     * website's default page.
     * @param id  id of the page to be removed
     * @throws RollerException with root cause <code>IllegalArgumentException</code> if the page id is that of
     * a page that will not be removed by this method.
     */
    public void removePageSafely(String id) throws RollerException;

	/**
	 * Retrieve the Page in read-only mode (does hibernate support this?).
	 */
	public PageData retrievePageReadOnly(String id) throws RollerException;
    
    /**
     * Validates a user based on a cookie value.  If successful, it returns
     * a new cookie String.  If not, then it returns null.
     * 
     * @param value (in format username|guid)
     * @return indicator that this is a valid login
     * @throws Exception
     */
    public String checkLoginCookie(String value) throws RollerException;
 
    /**
     * Creates a cookie string using a username - designed for use when
     * a user logs in and wants to be remembered.
     * 
     * @param username
     * @return String to put in a cookie for remembering user
     * @throws Exception
     */
    public String createLoginCookie(String username) throws RollerException;
    
    /**
     * Deletes all cookies for user.
     * @param username
     */
    public void removeLoginCookies(String username) throws RollerException;

    /**
     * Remove website(s) associated with user.
     */
    public void removeUserWebsites(UserData data) throws RollerException;

    /**
     * Remove contents of website.
     */
    public void removeWebsiteContents(WebsiteData data) throws RollerException;
}



