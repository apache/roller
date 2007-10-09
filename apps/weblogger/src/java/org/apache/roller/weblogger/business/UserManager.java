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
import org.apache.roller.weblogger.pojos.RollerPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;


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
     * Get count of enabled users
     */    
    public long getUserCount() throws WebloggerException; 
    
    
    /**
     * get a user by activation code
     * @param activationCode
     * @return
     * @throws WebloggerException
     */
    public User getUserByActivationCode(String activationCode) 
            throws WebloggerException;
    
          
    //------------------------------------------------------------ user queries
    
    
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
    
        
    //-------------------------------------------------------- permissions CRUD

    
    /**
     * Return true if user has permission specified.
     */
    public boolean checkPermission(RollerPermission perm, User user) 
            throws WebloggerException;
    
    
    /**
     * Grant to user specific actions in a weblog.
     * (will create new permission record if none already exists)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param actions Actions to be granted
     */
    public void grantWeblogPermission(Weblog weblog, User user, List<String> actions)
            throws WebloggerException;

    
    /**
     * Grant to user specific actions in a weblog, but pending confirmation.
     * (will create new permission record if none already exists)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param actions Actions to be granted
     */
    public void grantWeblogPermissionPending(Weblog weblog, User user, List<String> actions)
            throws WebloggerException;

    
    /**
     * Confirm user's permission within specified weblog or throw exception if no pending permission exists.
     * (changes state of permsission record to pending = true)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param actions Actions to be granted
     */
    public void confirmWeblogPermission(Weblog weblog, User user)
            throws WebloggerException;

    
    /**
     * Decline permissions within specified weblog or throw exception if no pending permission exists.
     * (removes permission record)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param actions Actions to be granted
     */
    public void declineWeblogPermission(Weblog weblog, User user)
            throws WebloggerException;

    
    /**
     * Revoke from user specific actions in a weblog.
     * (if resulting permission has empty removes permission record)
     * @param weblog  Weblog to grant permissions in
     * @param user    User to grant permissions to
     * @param actions Actions to be granted
     */
    public void revokeWeblogPermission(Weblog weblog, User user, List<String> actions)
            throws WebloggerException;

    
    /**
     * Get all of user's weblog permissions.
     */
    public List<WeblogPermission> getWeblogPermissions(User user) 
            throws WebloggerException;
    
    
    /**
     * Get all of user's pending weblog permissions.
     */
    public List<WeblogPermission> getWeblogPermissionsPending(User user) 
            throws WebloggerException;
    
    
    /**
     * Get all permissions associated with a weblog.
     */
    public List<WeblogPermission> getWeblogPermissions(Weblog weblog) 
            throws WebloggerException;
    
    
    /**
     * Get all pending permissions associated with a weblog.
     */
    public List<WeblogPermission> getWeblogPermissionsPending(Weblog weblog) 
            throws WebloggerException;
    
    
    /**
     * Get user's permission within a weblog or null if none.
     */
    public WeblogPermission getWeblogPermission(Weblog weblog, User user) 
            throws WebloggerException;        
    
    
    //--------------------------------------------------------------- role CRUD

    
    /**
     * Grant role to user.
     */
    public void grantRole(String roleName, User user) throws WebloggerException;
    
    
    /**
     * Revoke role from user.
     */
    public void revokeRole(String roleName, User user) throws WebloggerException;

        
    /**
     * Returns true if user has role specified, should be used only for testing.
     * @deprecated User checkPermission() instead.
     */
    public boolean hasRole(String roleName, User user) throws WebloggerException;
    
    
    /**
     * Get roles associated with user, should be used only for testing.
     * Get all roles associated with user.
     * @deprecated User checkPermission() instead.
     */
    public List<String> getRoles(User user) throws WebloggerException;

    
    /**
     * Release any resources held by manager.
     */
    public void release();   
}



