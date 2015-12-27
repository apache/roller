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
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Interface to User and WeblogRole management.
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
     * @param activationCode activate code from email
     * @return User object
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
    

    //-------------------------------------------------------- WeblogRoles CRUD

    
    /**
     * Check user's rights for a specified weblog
     * @param user    User whose role is being checked
     * @param weblog  Target weblog of the role
     * @param role    Minimum WeblogRole being checked for
     * @return true if user has WeblogRole or a more powerful one
     */
    boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role);
    
    
    /**
     * Grant user specific WeblogRole for a weblog.  Optimized to use unique identifiers
     * instead of User and Weblog objects, as the latter not always available without an
     * additional DB call.
     *
     * @param userName  Username to grant weblog role to
     * @param weblogId  Weblog Id being granted access to
     * @param role    WeblogRole to grant
     */
    void grantWeblogRole(String userName, String weblogId, WeblogRole role)
            throws WebloggerException;

    
    /**
     * Grant user a specific WeblogRole for a weblog, but pending user's acceptance of it
     * @param user    User to grant weblog role to
     * @param weblog  Weblog being granted access to
     * @param role    WeblogRole to grant
     */
    void grantPendingWeblogRole(User user, Weblog weblog, WeblogRole role)
            throws WebloggerException;

    
    /**
     * Confirm user's participation with the specified weblog or throw exception if no pending invitation exists.
     * (changes state of WeblogRole record to pending = false)
     * @param user    User granted invitation
     * @param weblog  Weblog granted invitation to
     */
    void acceptWeblogInvitation(User user, Weblog weblog)
            throws WebloggerException;

    
    /**
     * Decline participation within specified weblog or throw exception if no pending invitation exists.
     * (removes WeblogRole record)
     * @param user    User granted invitation
     * @param weblog  Weblog granted invitation to
     */
    void declineWeblogInvitation(User user, Weblog weblog)
            throws WebloggerException;

    
    /**
     * Revoke from user his WeblogRole for a given weblog.  Optimized to use unique identifiers
     * instead of User and Weblog objects, as the latter not always available without an
     * additional DB call.
     * @param userName  Username to remove WeblogRole from
     * @param weblogId  Weblog ID to revoke WeblogRole from
     */
    void revokeWeblogRole(String userName, String weblogId)
            throws WebloggerException;

    
    /**
     * Get all of user's WeblogRoles.
     */
    List<UserWeblogRole> getWeblogRoles(User user)
            throws WebloggerException;
    

    List<UserWeblogRole> getWeblogRolesIncludingPending(User user) throws WebloggerException;

    /**
     * Get all active User WeblogRoles associated with a weblog.
     */
    List<UserWeblogRole> getWeblogRoles(Weblog weblog)
            throws WebloggerException;

    /**
     * Get all pending User WeblogRoles associated with a weblog.
     */
    List<UserWeblogRole> getPendingWeblogRoles(Weblog weblog)
            throws WebloggerException;

    /**
     * Get all User WeblogRoles (pending or actual) for a weblog.
     */
    List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog)
            throws WebloggerException;


    /**
     * Get user's WeblogRole within a weblog or null if none.
     */
    UserWeblogRole getWeblogRole(User user, Weblog weblog)
            throws WebloggerException;

    /**
     * Get user's WeblogRole (pending or actual) for a weblog
     */
    UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog)
            throws WebloggerException;

}
