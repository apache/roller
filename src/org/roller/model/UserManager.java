
package org.roller.model;

import java.util.List;
import org.roller.RollerException;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;


/**
 * Manages users, weblogs, permissions, and weblog pages.
 */
public interface UserManager {
    
    
    /**
     * Add new user object to Roller. User will be given the global editor role,
     * unless it's the first user, who will get the global admin role.
     *
     * @param user User object to be added, initialized with name, password, etc.
     * @throws RollerException
     */
    public void addUser(UserData newUser) throws RollerException;
    
    
    /**
     * Store a single user.
     */
    public void saveUser(UserData data) throws RollerException;
    
    
    public void removeUser(UserData user) throws RollerException;
    
    
    public UserData getUser(String id)throws RollerException;
    
    
    /** 
     * Get user object by user name (only enabled users) 
     */
    public UserData getUserByUsername(String userName) throws RollerException;
    
    
    /** 
     * Get user object by user name, optionally include dis-enabled users 
     */
    public UserData getUserByUsername(String userName, Boolean enabled) throws RollerException;
    
    
    /** 
     * Get all enabled users 
     */
    public List getUsers() throws RollerException;
    
    
    /**
     * Get all users, optionally include dis-enabled users.
     *
     * @param enabled True for enabled only, false for disabled only, null for all
     */
    public List getUsers(Boolean enabled) throws RollerException;
    
    
    /**
     * Get all users or a website.
     *
     * @param website Get all users of this website (or null for all)
     * @returns List of UserData objects.
     */
    public List getUsers(WebsiteData website, Boolean enabled) throws RollerException;
    
    
    /**
     * Returns users whose usernames or email addresses start with a string.
     * @param startsWith String to match userNames and emailAddresses against
     * @param offset     Offset into results (for paging)
     * @param length     Max to return (for paging)
     * @param enabled    True for only enalbed, false for disabled, null for all
     * @return List of (up to length) users that match startsWith string
     */
    public List getUsersStartingWith(String startsWith,
            int offset, int length, Boolean enabled) throws RollerException;
    
    
    public void addWebsite(WebsiteData newWebsite) throws RollerException;
    
    
    /**
     * Store a single weblog.
     */
    public void saveWebsite(WebsiteData data) throws RollerException;
    
    
    public void removeWebsite(WebsiteData website) throws RollerException;
    
    
    public WebsiteData getWebsite(String id) throws RollerException;
    
    
    /**
     * Get website specified by handle (or null if enabled website not found).
     *
     * @param handle  Handle of website
     */
    public WebsiteData getWebsiteByHandle(String handle) throws RollerException;
    
    
    /**
     * Get website specified by handle with option to return only enabled websites.
     *
     * @param handle  Handle of website
     */
    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled) throws RollerException;
    
    
    /**
     * Get all websites of which user is a member
     *
     * @param user    Get all websites for this user (or null for all)
     * @param enabled Get all with this enabled state (or null or all)
     * @param active  Get all with this active state (or null or all)
     * @returns List of WebsiteData objects.
     */
    public List getWebsites(UserData user, Boolean enabled, Boolean active) throws RollerException;
    
    
    public void savePermissions(PermissionsData perms) throws RollerException;
    
    
    public void removePermissions(PermissionsData perms) throws RollerException;
    
    
    public PermissionsData getPermissions(String id) throws RollerException;
    
    
    /**
     * Get pending permissions for user
     *
     * @param user User (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(UserData user) throws RollerException;
    
    
    /**
     * Get pending permissions for website
     *
     * @param website Website (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(WebsiteData user) throws RollerException;
    
    
    /**
     * Get permissions of user in website
     *
     * @param website Website (not null)
     * @param user    User (not null)
     * @return        PermissionsData object
     */
    public PermissionsData getPermissions(WebsiteData website, UserData user) throws RollerException;
    
    
    /**
     * Get all permissions in website
     *
     * @param website Website (not null)
     * @return        PermissionsData object
     */
    public List getAllPermissions(WebsiteData website) throws RollerException;
    
    
    /**
     * Get all permissions of user
     *
     * @param user User (not null)
     * @return     PermissionsData object
     */
    public List getAllPermissions(UserData user) throws RollerException;
    
    
    /**
     * Invite user to join a website with specific permissions
     *
     * @param website Website to be joined (persistent instance)
     * @param user    User to be invited (persistent instance)
     * @param perms   Permissions mask (see statics in PermissionsData)
     * @return        New PermissionsData object, with pending=true
     */
    public PermissionsData inviteUser(WebsiteData website, UserData user, short perms) throws RollerException;
    
    
    /**
     * Retire user from a website
     *
     * @param website Website to be retired from (persistent instance)
     * @param user    User to be retired (persistent instance)
     */
    public void retireUser(WebsiteData website, UserData user) throws RollerException;
    
    
    /**
     * Store page
     */
    public void savePage(WeblogTemplate data) throws RollerException;
    
    
    /**
     * Remove page
     */
    public void removePage(WeblogTemplate page) throws RollerException;
    
    
    /**
     * Get page by ID
     */
    public WeblogTemplate getPage(String id) throws RollerException;
    
    
    /** 
     * Get user's page by name 
     */
    public WeblogTemplate getPageByName(WebsiteData w, String p) throws RollerException;
    
    
    /** 
     * Get user's page by link 
     */
    public WeblogTemplate getPageByLink(WebsiteData w, String p) throws RollerException;
    
    
    /** 
     * Get users pages 
     */
    public List getPages(WebsiteData w) throws RollerException;
    
    
    public void release();
    
}



