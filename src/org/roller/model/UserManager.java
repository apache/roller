
package org.roller.model;

import org.roller.RollerException;
import org.roller.pojos.PageData;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;

import java.io.Serializable;
import java.util.Map;
import java.util.List;


/**
 * Manages storage, retrieval, and querying of user, website, and page data.
 * 
 * @author David M Johnson
 */
public interface UserManager extends Serializable
{
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
    //--------------------------------------------------------------- UserData
    
    /** Get all enabled users */
    public List getUsers() throws RollerException;
    
	/** Get all users, optionally include dis-enabled users */
	public List getUsers(boolean enabledOnly) throws RollerException;

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

    /**
     * Get user by ID
     */
    public UserData retrieveUser(String id)throws RollerException;
    /**
     * Store user.
     */
    public void storeUser( UserData data ) throws RollerException;

    /**
     * Get all user roles.
     */
    public List getUserRoles(UserData user) throws RollerException;
    /**
     * Get role by ID
     */
    public RoleData retrieveRole(String id) throws RollerException;
    /**
     * Store role.
     */
    public void storeRole( RoleData data ) throws RollerException;
    /**
     * Remove role by ID.
     */
    public void removeRole( String id ) throws RollerException;

    //------------------------------------------------------------ WebsiteData
    
    /** Get website object by user name */
    public WebsiteData getWebsite(String userName) throws RollerException;
	/**
	 * Get website by username.
	 * @param userName Username of website's owner
	 * @param enabledOnly Only return enabled websites.
	 * @throws org.roller.RollerException 
	 * @return 
	 */
	public WebsiteData getWebsite(String userName, boolean enabledOnly) throws RollerException;

    /**
     * Get website by ID
     */
    public WebsiteData retrieveWebsite(String id) throws RollerException;
    /**
     * Store website
     */
    public void storeWebsite(WebsiteData data) throws RollerException;
    /**
     * Remove website by ID.
     */
    public void removeWebsite(String id) throws RollerException;

    //--------------------------------------------------------------- PageData
    
    /** Get user's page by name */
    public PageData getPageByName(WebsiteData w, String p) throws RollerException;

    /** Get user's page by link */
    public PageData getPageByLink(WebsiteData w, String p) throws RollerException;

    /** Fix page link using page name */
    public String fixPageLink(PageData data) throws RollerException;

    /** Get users pages */
    public List getPages(WebsiteData w) throws RollerException;

    /**
     * Get page by ID
     */
    public PageData retrievePage(String id) throws RollerException;
    /**
     * Store page
     */
    public void storePage(PageData data) throws RollerException;
    /**
     * Remove page by ID
     */
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



