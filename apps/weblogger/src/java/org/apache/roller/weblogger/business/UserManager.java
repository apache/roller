/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Manages users, weblogs, permissions, and weblog pages.
 */
public interface UserManager {
    
    /**
     * Add a new user.
     * 
     * This method is used to provide supplemental data to new user accounts,
     * such as adding the proper roles for the user.  This method should see
     * if the new user is the first user and give that user the admin role if so.
     *
     * @param newUser User object to be added.
     * @throws WebloggerException If there is a problem.
     */
    public void addUser(User newUser) throws WebloggerException;
    
    
    /**
     * Save a user.
     *
     * @param user User to be saved.
     * @throws WebloggerException If there is a problem.
     */
    public void saveUser(User user) throws WebloggerException;
    
    
    /**
     * Remove a user.
     *
     * @param user User to be removed.
     * @throws WebloggerException If there is a problem.
     */
    public void removeUser(User user) throws WebloggerException;
    
    
    /**
     * Lookup a user by ID.
     * 
     * @param id ID of user to lookup.
     * @returns UsUserhe user, or null if not found.
     * @throws WebloggerException If there is a problem.
     */
    public User getUser(String id) throws WebloggerException;
    
    
    /**
     * Lookup a user by UserName.
     * 
     * This lookup is restricted to 'enabled' users by default.  So this method
     * should return null if the user is found but is not enabled.
     * 
     * @param userName User Name of user to lookup.
     * @returns UsUserhe user, or null if not found or is disabled.
     * @throws WebloggerException If there is a problem.
     */
    public User getUserByUserName(String userName) throws WebloggerException;
    
    
    /**
     * Lookup a user by UserName with the given enabled status.
     * 
     * @param userName User Name of user to lookup.
     * @returns UsUserhe user, or null if not found or doesn't match 
     *   the proper enabled status.
     * @throws WebloggerException If there is a problem.
     */
    public User getUserByUserName(String userName, Boolean enabled)
        throws WebloggerException;
    
    
    /**
     * Lookup a group of users.
     * 
     * The lookup may be constrained to users with a certain enabled status,
     * to users created within a certain date range, and the results can be
     * confined to a certain offset & length for paging abilities.
     * 
     * @param weblog Confine results to users with permission to a certain weblog.
     * @param enabled True for enabled only, False for disabled only (or null for all)
     * @param startDate Restrict to those created after startDate (or null for all)
     * @param endDate Restrict to those created before startDate (or null for all)
     * @param offset The index of the first result to return.
     * @param length The number of results to return.
     * @returns List A list of UserDatUsers which match the criteria.
     * @throws WebloggerException If there is a problem.
     */
    public List getUsers(
            Weblog weblog,
            Boolean enabled,
            Date    startDate,
            Date    endDate,
            int     offset,
            int     length) throws WebloggerException;
    
    
    /**
     * Lookup users whose usernames or email addresses start with a string.
     *
     * @param startsWith String to match userNames and emailAddresses against
     * @param offset     Offset into results (for paging)
     * @param length     Max to return (for paging)
     * @param enabled    True for only enalbed, false for disabled, null for all
     * @return List of (up to length) users that match startsWith string
     */
    public List getUsersStartingWith(String startsWith,
            Boolean enabled, int offset, int length) throws WebloggerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of users whose
     * names start with each letter.
     */
    public Map getUserNameLetterMap() throws WebloggerException;
    
    
    /** 
     * Get collection of users whose names begin with specified letter 
     */
    public List getUsersByLetter(char letter, int offset, int length) 
        throws WebloggerException;
    
    
    /**
     * Add new website, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new website.
     * @param newWebsite New website to be created, must have creator.
     */
    public void addWebsite(Weblog newWebsite) throws WebloggerException;
    
    
    /**
     * Store a single weblog.
     */
    public void saveWebsite(Weblog data) throws WebloggerException;
    
    
    /**
     * Remove website object.
     */
    public void removeWebsite(Weblog website) throws WebloggerException;
    
    
    /**
     * Get website object by name.
     */
    public Weblog getWebsite(String id) throws WebloggerException;
    
    
    /**
     * Get website specified by handle (or null if enabled website not found).
     * @param handle  Handle of website
     */
    public Weblog getWebsiteByHandle(String handle) throws WebloggerException;
    
    
    /**
     * Get website specified by handle with option to return only enabled websites.
     * @param handle  Handle of website
     */
    public Weblog getWebsiteByHandle(String handle, Boolean enabled)
        throws WebloggerException;
    
    
    /**
     * Get websites optionally restricted by user, enabled and active status.
     * @param user    Get all websites for this user (or null for all)
     * @param offset  Offset into results (for paging)
     * @param len     Maximum number of results to return (for paging)
     * @param enabled Get all with this enabled state (or null or all)
     * @param active  Get all with this active state (or null or all)
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @returns List of WebsiteData objects.
     */
    public List getWebsites(
            User user,
            Boolean  enabled,
            Boolean  active,
            Date     startDate,
            Date     endDate,
            int      offset,
            int      length)
            throws WebloggerException;
    
    
    /**
     * Get websites ordered by descending number of comments.
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging)
     * @returns List of WebsiteData objects.
     */
    public List getMostCommentedWebsites(
            Date startDate,
            Date endDate,
            int  offset,
            int  length)
            throws WebloggerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map getWeblogHandleLetterMap() throws WebloggerException;
    
    
    /** 
     * Get collection of weblogs whose handles begin with specified letter 
     */
    public List getWeblogsByLetter(char letter, int offset, int length) 
        throws WebloggerException;
    
    
    /**
     * Save permissions object.
     */
    public void savePermissions(WeblogPermission perms) throws WebloggerException;
    
    
    /**
     * Remove permissions object.
     */
    public void removePermissions(WeblogPermission perms) throws WebloggerException;
    
    
    /**
     * Get permissions object by id.
     */
    public WeblogPermission getPermissions(String id) throws WebloggerException;
    
    
    /**
     * Get pending permissions for user.
     * @param user User (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(User user) throws WebloggerException;
    
    
    /**
     * Get pending permissions for website.
     * @param website Website (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(Weblog user) throws WebloggerException;
    
    
    /**
     * Get permissions of user in website.
     * @param website Website (not null)
     * @param user    User (not null)
     * @return        PermissionsData object
     */
    public WeblogPermission getPermissions(Weblog website, User user)
        throws WebloggerException;
    
    
    /**
     * Get all permissions in website
     * @param website Website (not null)
     * @return        PermissionsData object
     */
    public List getAllPermissions(Weblog website) throws WebloggerException;
    
    
    /**
     * Get all permissions of user
     * @param user User (not null)
     * @return     PermissionsData object
     */
    public List getAllPermissions(User user) throws WebloggerException;
    
    
    /**
     * Invite user to join a website with specific permissions
     * @param website Website to be joined (persistent instance)
     * @param user    User to be invited (persistent instance)
     * @param perms   Permissions mask (see statics in PermissionsData)
     * @return        New PermissionsData object, with pending=true
     */
    public WeblogPermission inviteUser(Weblog website, User user, short perms)
        throws WebloggerException;
    
    
    /**
     * Retire user from a website
     * @param website Website to be retired from (persistent instance)
     * @param user    User to be retired (persistent instance)
     */
    public void retireUser(Weblog website, User user)
        throws WebloggerException;
    
    
    /**
     * Revoke role of user
     * @param roleName Name of the role to be revoked
     * @param user    User for whom the role is to be revoked
     */
    public void revokeRole(String roleName, User user)
        throws WebloggerException;

    /**
     * Store page.
     */
    public void savePage(WeblogTemplate data) throws WebloggerException;
    
    
    /**
     * Remove page.
     */
    public void removePage(WeblogTemplate page) throws WebloggerException;
    
    
    /**
     * Get page by id.
     */
    public WeblogTemplate getPage(String id) throws WebloggerException;
    
    
    /**
     * Get user's page by action.
     */
    public WeblogTemplate getPageByAction(Weblog w, String a) throws WebloggerException;
    
    
    /**
     * Get user's page by name.
     */
    public WeblogTemplate getPageByName(Weblog w, String p) throws WebloggerException;
    
    
    /**
     * Get website's page by link.
     */
    public WeblogTemplate getPageByLink(Weblog w, String p)
        throws WebloggerException;
    
    
    /**
     * Get website's pages
     */
    public List getPages(Weblog w) throws WebloggerException;
   
    
    /**
     * Get count of active weblogs
     */    
    public long getWeblogCount() throws WebloggerException;

    
    /**
     * Get count of enabled users
     */    
    public long getUserCount() throws WebloggerException; 
    
    
    /**
     * Release any resources held by manager.
     */
    public void release();
    
    
    /**
     * get a user by activation code
     * @param activationCode
     * @return
     * @throws WebloggerException
     */
    public User getUserByActivationCode(String activationCode) throws WebloggerException;
    
    /**
     * get a user by password request code
     * @param passwordRequestCode
     * @return
     * @throws WebloggerException
     */
    //public User getUserByPasswordRequestCode(String passwordRequestCode) throws WebloggerException;


}
