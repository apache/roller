/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.ui.core.security;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.WebloggerStaticConfig.AuthMethod;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.UserCredentials;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security UserDetailsService implemented using Weblogger API.
 * Configured in security.xml to be used by both DB and LDAP authentication.
 * DB auth uses this class to obtain correct DB-stored credentials to compare against
 * user input to determine if login successful.
 * For LDAP, auth against directory already successful if this class entered, only
 * purpose of this class is to report Global Role of user.
 */
public class CustomUserDetailsService implements UserDetailsService {
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    /**
     * @throws UsernameNotFoundException, DataAccessException
     */
    @Override
    public UserDetails loadUserByUsername(String userName) {

        if (!WebloggerContext.isBootstrapped()) {
            // Should only happen in case of 1st time startup, setup required
            // Thowing a "soft" exception here allows setup to proceed
            throw new UsernameNotFoundException("User info not available yet.");
        }

        boolean usingDBAuth = (WebloggerStaticConfig.getAuthMethod() == AuthMethod.DB);
        UserCredentials creds = userManager.getCredentialsByUserName(userName);
        GlobalRole targetGlobalRole;
        String targetPassword;

        if (usingDBAuth) {
            if (creds == null) {
                throw new UsernameNotFoundException("ERROR no user: " + userName);
            }
            targetPassword = creds.getPassword();
            targetGlobalRole = creds.getGlobalRole();
        } else {
            // for LDAP, password unused, auth already occurred
            targetPassword = "";
            if (userManager.getUserCount() == 0) {
                targetGlobalRole = GlobalRole.ADMIN;
            } else {
                targetGlobalRole = (creds != null) ? creds.getGlobalRole() :
                        persistenceStrategy.getWebloggerProperties().isUsersCreateBlogs() ?
                                GlobalRole.BLOGCREATOR : GlobalRole.BLOGGER;
            }
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(1);
        authorities.add(new SimpleGrantedAuthority(targetGlobalRole.name()));
        return new org.springframework.security.core.userdetails.User(userName, targetPassword,
                true, true, true, true, authorities);
    }
}
