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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogRole;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface to user, role and permissions management.
 */
public interface UserManager {
    
    //--------------------------------------------------------------- user CRUD    
    
    /**
     * Add a new user.
     * 
     * This method is used to provide supplemental data to new user accounts,
     * such as adding the proper roles for the user.  This method should see if
     * the new user is the first user and give that user the admin role if so.
     *
     * @param newUser User object to be added.
     * @throws WebloggerException If there is a problem.
     */
    void addUser(User newUser) throws WebloggerException;
    
    
    /**
     * Save a user.
     *
     * @param user User to be saved.
     * @throws WebloggerException If there is a problem.
     */
    void saveUser(User user) throws WebloggerException;
    
    
    /**
     * Remove a user.
     *
     * @param user User to be removed.
     * @throws WebloggerException If there is a problem.
     */
    void removeUser(User user) throws WebloggerException;
    
    
    /**
     * Get count of enabled users
     */    
    long getUserCount() throws WebloggerException;
    
    
    /**
     * get a user by activation code
     * @param activationCode
     * @return
     * @throws WebloggerException
     */
    User getUserByActivationCode(String activationCode)
            throws WebloggerException;
    
          
    //------------------------------------------------------------ user queries

    /**
     * Retrieve a user by its internal identifier id.
     *
     * @param id the id of the user to retrieve.
     * @return the user object with specified id or null if not found
     * @throws WebloggerException
     */
    User getUser(String id) throws WebloggerException;

    /**
     * Lookup a user by UserName.
     * 
     * This lookup is restricted to 'enabled' users by default.  So this method
     * will return null if the user is found but is not enabled.
     * 
     * @param userName User Name of user to lookup.
     * @return The user, or null if not found or not enabled.
     * @throws WebloggerException If there is a problem.
     */
    User getUserByUserName(String userName) throws WebloggerException;
    
    /**
     * Lookup a user by UserName with the given enabled status.
     * 
     * @param userName User Name of user to lookup.
     * @param enabled True if user is enabled, false otherwise.
     * @return The user, or null if not found or of the proper enabled status.
     * @throws WebloggerException If there is a problem.
     */
    User getUserByUserName(String userName, Boolean enabled)
        throws WebloggerException;

    /**
     * Lookup a user by Open ID URL.
     *
     * This lookup is restricted to 'enabled' users by default.  So this method
     * will return null if the user is found but is not enabled.
     *
     * @param openIdUrl OpenIdUrl of user to lookup.
     * @return The user, or null if not found or not enabled.
     * @throws WebloggerException If there is a problem.
     */
    User getUserByOpenIdUrl(String openIdUrl)
            throws WebloggerException;

    /**
     * Lookup a group of users.
     * 
     * The lookup may be constrained to users with a certain enabled status,
     * to users created within a certain date range, and the results can be
     * confined to a certain offset & length for paging abilities.
     * 
     * @param enabled True for enabled only, False for disabled only (or null for all)
     * @param startDate Restrict to those created after startDate (or null for all)
     * @param endDate Restrict to those created before startDate (or null for all)
     * @param offset The index of the first result to return.
     * @param length The number of results to return.
     * @return List A list of UserDatUsers which match the criteria.
     * @throws WebloggerException If there is a problem.
     */
    List<User> getUsers(
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
    List<User> getUsersStartingWith(String startsWith,
            Boolean enabled, int offset, int length) throws WebloggerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing Longs reflecting the number of users whose
     * names start with each letter.
     */
    Map<String, Long> getUserNameLetterMap() throws WebloggerException;
    
    
    /** 
     * Get collection of users whose names begin with specified letter 
     */
    List<User> getUsersByLetter(char letter, int offset, int length)
        throws WebloggerException;
    

    //-------------------------------------------------------- permissions CRUD

    
    /**
     * Return true if user has permission specified.
     */
    boolean checkPermission(WeblogPermission perm, User user)
            throws WebloggerException;
    
    
    /**
     * Grant to user specific WeblogRole for a weblog
     * (will create new permission record if none already exists)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param weblogRole Role to grant user
     */
    void grantWeblogRole(Weblog weblog, User user, WeblogRole weblogRole)
            throws WebloggerException;

    
    /**
     * Grant to user a specific WeblogRole for a weblog, but pending confirmation.
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param role    WeblogRole to grant
     */
    void grantPendingWeblogRole(Weblog weblog, User user, WeblogRole role)
            throws WebloggerException;

    
    /**
     * Confirm user's permission within specified weblog or throw exception if no pending permission exists.
     * (changes state of permission record to pending = true)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     */
    void confirmWeblogPermission(Weblog weblog, User user)
            throws WebloggerException;

    
    /**
     * Decline permissions within specified weblog or throw exception if no pending permission exists.
     * (removes permission record)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     */
    void declineWeblogPermission(Weblog weblog, User user)
            throws WebloggerException;

    
    /**
     * Revoke from user his WeblogRole for a given weblog.
     * @param weblog  Weblog to revoke WeblogRole from
     * @param user    User to remove WeblogRole from
     */
    void revokeWeblogRole(Weblog weblog, User user)
            throws WebloggerException;

    
    /**
     * Get all of user's weblog permissions.
     */
    List<WeblogPermission> getWeblogPermissions(User user)
            throws WebloggerException;
    
    
    /**
     * Get all of user's pending weblog permissions.
     */
    List<WeblogPermission> getPendingWeblogPermissions(User user)
            throws WebloggerException;

    /**
     * Get all active permissions associated with a weblog.
     */
    List<WeblogPermission> getWeblogPermissions(Weblog weblog)
            throws WebloggerException;

    /**
     * Get all pending permissions associated with a weblog.
     */
    List<WeblogPermission> getPendingWeblogPermissions(Weblog weblog)
            throws WebloggerException;

    /**
     * Get all permissions (pending or actual) for a weblog.
     */
    List<WeblogPermission> getWeblogPermissionsIncludingPending(Weblog weblog)
            throws WebloggerException;


    /**
     * Get user's permission within a weblog or null if none.
     */
    WeblogPermission getWeblogPermission(Weblog weblog, User user)
            throws WebloggerException;

    /**
     * Get user's permission (pending or actual) for a weblog
     */
    WeblogPermission getWeblogPermissionIncludingPending(Weblog weblog, User user)
            throws WebloggerException;


    //--------------------------------------------------------------- role CRUD

    /**
     * Returns GlobalRole assigned to a user.  Useful if wish to retrieve
     * the latest DB-stored role value prior to allowing a certain
     * action.
     */
    GlobalRole getGlobalRole(User user) throws WebloggerException;

    /**
     * Convenience method to check if a user has a role of GlobalRole.ADMIN
     */
    boolean isGlobalAdmin(User user) throws WebloggerException;

    /**
     * Convenience method to check if a user has a role equal to or more
     * powerful than a specified one
     */
    public boolean hasEffectiveGlobalRole(User user, GlobalRole roleToCheck) throws WebloggerException;

    /**
     * Release any resources held by manager.
     */
    void release();
}



