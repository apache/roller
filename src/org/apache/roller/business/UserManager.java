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

package org.apache.roller.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;


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
     * @throws RollerException If there is a problem.
     */
    public void addUser(UserData newUser) throws RollerException;
    
    
    /**
     * Save a user.
     *
     * @param user User to be saved.
     * @throws RollerException If there is a problem.
     */
    public void saveUser(UserData user) throws RollerException;
    
    
    /**
     * Remove a user.
     *
     * @param user User to be removed.
     * @throws RollerException If there is a problem.
     */
    public void removeUser(UserData user) throws RollerException;
    
    
    /**
     * Lookup a user by ID.
     *
     * @param id ID of user to lookup.
     * @returns UserData The user, or null if not found.
     * @throws RollerException If there is a problem.
     */
    public UserData getUser(String id) throws RollerException;
    
    
    /**
     * Lookup a user by UserName.
     *
     * This lookup is restricted to 'enabled' users by default.  So this method
     * should return null if the user is found but is not enabled.
     *
     * @param userName User Name of user to lookup.
     * @returns UserData The user, or null if not found or is disabled.
     * @throws RollerException If there is a problem.
     */
    public UserData getUserByUserName(String userName) throws RollerException;
    
    
    /**
     * Lookup a user by UserName with the given enabled status.
     *
     * @param userName User Name of user to lookup.
     * @returns UserData The user, or null if not found or doesn't match 
     *   the proper enabled status.
     * @throws RollerException If there is a problem.
     */
    public UserData getUserByUserName(String userName, Boolean enabled)
        throws RollerException;
    
    
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
     * @returns List A list of UserData objects which match the criteria.
     * @throws RollerException If there is a problem.
     */
    public List getUsers(
            WebsiteData weblog,
            Boolean enabled,
            Date    startDate,
            Date    endDate,
            int     offset,
            int     length) throws RollerException;
    
    
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
            Boolean enabled, int offset, int length) throws RollerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of users whose
     * names start with each letter.
     */
    public Map getUserNameLetterMap() throws RollerException;
    
    
    /** 
     * Get collection of users whose names begin with specified letter 
     */
    public List getUsersByLetter(char letter, int offset, int length) 
        throws RollerException;
    
    
    /**
     * Add new website, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new website.
     * @param newWebsite New website to be created, must have creator.
     */
    public void addWebsite(WebsiteData newWebsite) throws RollerException;
    
    
    /**
     * Store a single weblog.
     */
    public void saveWebsite(WebsiteData data) throws RollerException;
    
    
    /**
     * Remove website object.
     */
    public void removeWebsite(WebsiteData website) throws RollerException;
    
    
    /**
     * Get website object by name.
     */
    public WebsiteData getWebsite(String id) throws RollerException;
    
    
    /**
     * Get website specified by handle (or null if enabled website not found).
     * @param handle  Handle of website
     */
    public WebsiteData getWebsiteByHandle(String handle) throws RollerException;
    
    
    /**
     * Get website specified by handle with option to return only enabled websites.
     * @param handle  Handle of website
     */
    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled)
        throws RollerException;
    
    
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
            UserData user,
            Boolean  enabled,
            Boolean  active,
            Date     startDate,
            Date     endDate,
            int      offset,
            int      length)
            throws RollerException;
    
    
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
            throws RollerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map getWeblogHandleLetterMap() throws RollerException;
    
    
    /** 
     * Get collection of weblogs whose handles begin with specified letter 
     */
    public List getWeblogsByLetter(char letter, int offset, int length) 
        throws RollerException;
    
    
    /**
     * Save permissions object.
     */
    public void savePermissions(PermissionsData perms) throws RollerException;
    
    
    /**
     * Remove permissions object.
     */
    public void removePermissions(PermissionsData perms) throws RollerException;
    
    
    /**
     * Get permissions object by id.
     */
    public PermissionsData getPermissions(String id) throws RollerException;
    
    
    /**
     * Get pending permissions for user.
     * @param user User (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(UserData user) throws RollerException;
    
    
    /**
     * Get pending permissions for website.
     * @param website Website (not null)
     * @returns List of PermissionsData objects.
     */
    public List getPendingPermissions(WebsiteData user) throws RollerException;
    
    
    /**
     * Get permissions of user in website.
     * @param website Website (not null)
     * @param user    User (not null)
     * @return        PermissionsData object
     */
    public PermissionsData getPermissions(WebsiteData website, UserData user)
        throws RollerException;
    
    
    /**
     * Get all permissions in website
     * @param website Website (not null)
     * @return        PermissionsData object
     */
    public List getAllPermissions(WebsiteData website) throws RollerException;
    
    
    /**
     * Get all permissions of user
     * @param user User (not null)
     * @return     PermissionsData object
     */
    public List getAllPermissions(UserData user) throws RollerException;
    
    
    /**
     * Invite user to join a website with specific permissions
     * @param website Website to be joined (persistent instance)
     * @param user    User to be invited (persistent instance)
     * @param perms   Permissions mask (see statics in PermissionsData)
     * @return        New PermissionsData object, with pending=true
     */
    public PermissionsData inviteUser(WebsiteData website, UserData user, short perms)
        throws RollerException;
    
    
    /**
     * Retire user from a website
     * @param website Website to be retired from (persistent instance)
     * @param user    User to be retired (persistent instance)
     */
    public void retireUser(WebsiteData website, UserData user)
        throws RollerException;
    
    
    /**
     * Revoke role of user
     * @param roleName Name of the role to be revoked
     * @param user    User for whom the role is to be revoked
     */
    public void revokeRole(String roleName, UserData user)
        throws RollerException;

    /**
     * Store page.
     */
    public void savePage(WeblogTemplate data) throws RollerException;
    
    
    /**
     * Remove page.
     */
    public void removePage(WeblogTemplate page) throws RollerException;
    
    
    /**
     * Get page by id.
     */
    public WeblogTemplate getPage(String id) throws RollerException;
    
    
    /**
     * Get user's page by action.
     */
    public WeblogTemplate getPageByAction(WebsiteData w, String a) throws RollerException;
    
    
    /**
     * Get user's page by name.
     */
    public WeblogTemplate getPageByName(WebsiteData w, String p) throws RollerException;
    
    
    /**
     * Get website's page by link.
     */
    public WeblogTemplate getPageByLink(WebsiteData w, String p)
        throws RollerException;
    
    
    /**
     * Get website's pages
     */
    public List getPages(WebsiteData w) throws RollerException;
   
    
    /**
     * Get count of active weblogs
     */    
    public long getWeblogCount() throws RollerException;

    
    /**
     * Get count of enabled users
     */    
    public long getUserCount() throws RollerException; 
    
    
    /**
     * Release any resources held by manager.
     */
    public void release();
    
}
