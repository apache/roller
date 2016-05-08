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
import org.apache.roller.weblogger.pojos.SafeUser;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.menu.Menu;

import java.util.List;

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
     */
    void addUser(User newUser);
    
    
    /**
     * Save a user.
     *
     * @param user User to be saved.
     */
    void saveUser(User user);
    
    
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
    long getUserCount();
    
    
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
     */
    User getUser(String id);

    /**
     * Retrieve a SafeUser object by its internal identifier id.
     *
     * @param id the id of the user to retrieve.
     * @return the SafeUser object with specified id or null if not found
     * @throws WebloggerException
     */
    SafeUser getSafeUser(String id) throws WebloggerException;

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

    User getUserByScreenName(String screenName);

    /**
     * Lookup users whose usernames or email addresses start with a string.
     *
     * @param startsWith String to match screenNames and emailAddresses against (null for all)
     * @param enabled    True if user is enabled, false disabled, null if either OK.
     * @param offset     Offset into results (for paging)
     * @param length     Max to return (for paging)
     * @param enabled    True for only enalbed, false for disabled, null for all
     * @return List of (up to length) users that match startsWith string
     */
    List<SafeUser> getUsers(String startsWith,
            Boolean enabled, int offset, int length) throws WebloggerException;
    
    
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
     * Check user's rights given username and weblog handle.  Convenience
     * overload for callers not having the User and/or Weblog objects.
     * @param username    Username whose role is being checked
     * @param weblogHandle target weblog handle of the role
     * @param role    Minimum WeblogRole being checked for
     * @return true if user has WeblogRole or more powerful, or is global admin.
     *         false if not or if either username and/or weblogHandle can't be found.
     */
    boolean checkWeblogRole(String username, String weblogHandle, WeblogRole role);

    /**
     * Get user's WeblogRole within a weblog or null if none.
     * @param user    User whose role is being checked
     * @param weblog  Target weblog of the role
     * @return UserWeblogRole indicating user's role with weblog or null if no permission
     */
    UserWeblogRole getWeblogRole(User user, Weblog weblog);

    /**
     * Get user's WeblogRole within a weblog or null if none.  Convenience
     * version of getWeblogRole(User, Weblog) for callers lacking the User and/or
     * Weblog objects.
     *
     * @param username    Username whose role is being checked
     * @param weblogHandle target weblog handle of the role
     * @return UserWeblogRole indicating user's role with weblog or null if no permission
     * @throws WebloggerException If exceptions occurred during processing.
     */
    UserWeblogRole getWeblogRole(String username, String weblogHandle) throws WebloggerException;


    /**
     * Grant user specific WeblogRole for a weblog.
     * @param user    User to grant weblog role to
     * @param weblog  Weblog being granted access to
     * @param role    WeblogRole to grant
     */
    void grantWeblogRole(User user, Weblog weblog, WeblogRole role);

    /**
     * Grant user specific WeblogRole for a weblog.
     * @param userId    User to grant weblog role to
     * @param weblog  Weblog being granted access to
     * @param role    WeblogRole to grant
     */
    void grantWeblogRole(String userId, Weblog weblog, WeblogRole role);


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
    void acceptWeblogInvitation(User user, Weblog weblog);
    
    /**
     * Decline participation within specified weblog or throw exception if no pending invitation exists.
     * (removes WeblogRole record)
     * @param user    User granted invitation
     * @param weblog  Weblog granted invitation to
     */
    void declineWeblogInvitation(User user, Weblog weblog);

    
    /**
     * Revoke from user his WeblogRole for a given weblog.
     * @param user  User to remove WeblogRole from
     * @param weblog  Weblog to revoke WeblogRole from
     */
    void revokeWeblogRole(User user, Weblog weblog);

    
    /**
     * Get all of user's WeblogRoles.
     */
    List<UserWeblogRole> getWeblogRoles(User user);

    List<UserWeblogRole> getWeblogRolesIncludingPending(User user) throws WebloggerException;

    /**
     * Get all active User WeblogRoles associated with a weblog.
     */
    List<UserWeblogRole> getWeblogRoles(Weblog weblog);

    /**
     * Get all pending User WeblogRoles associated with a weblog.
     */
    List<UserWeblogRole> getPendingWeblogRoles(Weblog weblog)
            throws WebloggerException;

    /**
     * Get all User WeblogRoles (pending or actual) for a weblog.
     */
    List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog);


    /**
     * Get user's WeblogRole (pending or actual) for a weblog
     */
    UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog)
            throws WebloggerException;

    /**
     * Return the Editor menu for the given username, weblog handle, and current
     * action.
     */
    Menu getEditorMenu(String username, String weblogHandle);

}
