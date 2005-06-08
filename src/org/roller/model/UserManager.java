
package org.roller.model;

import org.roller.RollerException;
import org.roller.pojos.PageData;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;

import java.util.Map;
import java.util.List;


/**
 * Manages storage, retrieval, and querying of user, website, and page data.
 * 
 * @author David M Johnson
 */
public interface UserManager
{
    /** Release any resources used */
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

    public UserData retrieveUser(String id)throws RollerException;
    public void storeUser( UserData data ) throws RollerException;

    public List getUserRoles(UserData user) throws RollerException;
    public RoleData retrieveRole(String id) throws RollerException;
    public void storeRole( RoleData data ) throws RollerException;
    public void removeRole( String id ) throws RollerException;

    //------------------------------------------------------------ WebsiteData
    
    /** Get website object by user name */
    public WebsiteData getWebsite(String userName) throws RollerException;
	public WebsiteData getWebsite(String userName, boolean enabledOnly) throws RollerException;

    public WebsiteData retrieveWebsite(String id) throws RollerException;
    public void storeWebsite(WebsiteData data) throws RollerException;
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

    public PageData retrievePage(String id) throws RollerException;
    public void storePage(PageData data) throws RollerException;
    public void removePage(String id) throws RollerException;

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



