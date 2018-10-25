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
package org.tightblog.business;

import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;

/**
 * Interface to User and WeblogRole management.
 */
public interface UserManager {

    /**
     * Remove a user.
     *
     * @param user User to be removed.
     */
    void removeUser(User user);

    /**
     * Update user password.  This method takes an unencrypted password
     * and will encrypt it prior to storing in database.
     *
     * @param userId      internal ID of user
     * @param newPassword unencrypted password.
     */
    void updateCredentials(String userId, String newPassword);

    /**
     * Check user's rights for a specified weblog
     *
     * @param user   User whose role is being checked
     * @param weblog Target weblog of the role
     * @param role   Minimum WeblogRole being checked for
     * @return true if user is a global Admin, or has given WeblogRole or a more powerful one
     */
    boolean checkWeblogRole(User user, Weblog weblog, WeblogRole role);

    /**
     * Check user's rights given username and weblog handle.  Convenience
     * overload for callers having the weblog but not the user object.
     *
     * @param username     Username whose role is being checked
     * @param weblog Target weblog of the role
     * @param role         Minimum WeblogRole being checked for
     * @return false if user cannot be identified from the passed in username,
     * otherwise whatever checkWeblogRole(User, Weblog, WeblogRole) would return.
     */
    boolean checkWeblogRole(String username, Weblog weblog, WeblogRole role);

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
     * Generate a URL for the user's Google Authenticator code (for MFA)
     * @param user User to obtain QA code for
     * @return url Image URL for the QR code
     */
    String generateMFAQRUrl(User user);
}
