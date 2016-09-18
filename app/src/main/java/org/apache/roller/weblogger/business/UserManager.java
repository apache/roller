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

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserCredentials;
import org.apache.roller.weblogger.pojos.UserSearchCriteria;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;

import java.util.List;

/**
 * Interface to User and WeblogRole management.
 */
public interface UserManager {

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
     */
    void removeUser(User user);

    /**
     * Get count of enabled users, returning long type as that is what the
     * JPA COUNT aggregate returns (http://stackoverflow.com/a/3574441/1207540)
     */
    long getUserCount();

    /**
     * Retrieve a user by its internal identifier id.
     *
     * @param id the id of the user to retrieve.
     * @return the user object with specified id or null if not found
     */
    User getUser(String id);

    /**
     * Lookup an enabled user by UserName.
     * <p>
     * This lookup is restricted to 'enabled' users by default.  So this method
     * will return null if the user is found but is not enabled.
     *
     * @param userName User Name of user to lookup.
     * @return The user, or null if not found or not enabled.
     */
    User getEnabledUserByUserName(String userName);

    /**
     * Get user credentials by user name.  Only enabled users are retrievable.
     *
     * @param userName User Name of user to lookup.
     * @return The user, or null if not found or not enabled.
     */
    UserCredentials getCredentialsByUserName(String userName);

    /**
     * Update user password.  This method takes an unencrypted password
     * and will encrypt it prior to storing in database.
     *
     * @param userId      internal ID of user
     * @param newPassword unencrypted password.
     */
    void updateCredentials(String userId, String newPassword);

    /**
     * Lookup users based on supplied criteria
     *
     * @param criteria UserSearchCriteria object indicating search parameters
     * @return list of User objects matching desired criteria
     */
    List<User> getUsers(UserSearchCriteria criteria);

    /**
     * Check user's rights for a specified weblog
     *
     * @param user   User whose role is being checked
     * @param weblog Target weblog of the role
     * @param role   Minimum WeblogRole being checked for
     * @return true if user has WeblogRole or a more powerful one
     */
    boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role);

    /**
     * Check user's rights given username and weblog handle.  Convenience
     * overload for callers not having the User and/or Weblog objects.
     *
     * @param username     Username whose role is being checked
     * @param weblogHandle target weblog handle of the role
     * @param role         Minimum WeblogRole being checked for
     * @return true if user has WeblogRole or more powerful, or is global admin.
     * false if not or if either username and/or weblogHandle can't be found.
     */
    boolean checkWeblogRole(String username, String weblogHandle, WeblogRole role);

    /**
     * Retrieve a user by its internal identifier id.
     *
     * @param id the id of the user weblog role object to retrieve.
     * @return the user object with specified id or null if not found
     */
    UserWeblogRole getUserWeblogRole(String id);

    /**
     * Get user's WeblogRole within a weblog or null if none.
     *
     * @param user   User whose role is being checked
     * @param weblog Target weblog of the role
     * @return UserWeblogRole indicating user's role with weblog or null if no permission
     */
    UserWeblogRole getWeblogRole(User user, Weblog weblog);

    /**
     * Grant user specific WeblogRole for a weblog.
     *
     * @param user    User to grant weblog role to
     * @param weblog  Weblog being granted access to
     * @param role    WeblogRole to grant
     * @param pending Whether grantee approve the role before it becomes effective
     */
    void grantWeblogRole(User user, Weblog weblog, WeblogRole role, boolean pending);

    /**
     * Confirm user's participation with the specified weblog or throw exception if no pending invitation exists.
     * (changes state of WeblogRole record to pending = false)
     *
     * @param userWeblogRole pending UserWeblogRole object containing user and weblog combination
     */
    void acceptWeblogInvitation(UserWeblogRole userWeblogRole);

    /**
     * Revoke from user his WeblogRole for a given weblog.
     *
     * @param userWeblogRole UserWeblogRole object containing user and weblog combination to remove
     */
    void revokeWeblogRole(UserWeblogRole userWeblogRole);

    /**
     * Get user's non-pending WeblogRoles.
     */
    List<UserWeblogRole> getWeblogRoles(User user);

    /**
     * Get all of user's WeblogRoles.
     */
    List<UserWeblogRole> getWeblogRolesIncludingPending(User user);

    /**
     * Get all non-pending User WeblogRoles associated with a weblog.
     */
    List<UserWeblogRole> getWeblogRoles(Weblog weblog);

    /**
     * Get all pending User WeblogRoles associated with a weblog.
     */
    List<UserWeblogRole> getPendingWeblogRoles(Weblog weblog);

    /**
     * Get all User WeblogRoles (pending or actual) for a weblog.
     */
    List<UserWeblogRole> getWeblogRolesIncludingPending(Weblog weblog);

    /**
     * Get user's WeblogRole (pending or actual) for a weblog
     */
    UserWeblogRole getWeblogRoleIncludingPending(User user, Weblog weblog);

}
