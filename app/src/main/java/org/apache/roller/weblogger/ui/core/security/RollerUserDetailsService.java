/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.core.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Spring Security UserDetailsService implemented using Weblogger API.
 */
public class RollerUserDetailsService implements UserDetailsService, UserDetailsPasswordService {
    private static Log log = LogFactory.getLog(RollerUserDetailsService.class);
    
    /**
     * @throws UsernameNotFoundException, DataAccessException
     */
    @Override
    public UserDetails loadUserByUsername(String userName) {
        // hiding the login form is not an authentication control: refuse
        // password lookups server side when only OIDC login is configured
        if (WebloggerConfig.getAuthMethod() == AuthMethod.OIDC) {
            throw new UsernameNotFoundException(
                    "form login is disabled: authentication.method is oidc");
        }

        Weblogger roller;
        try {
            roller = WebloggerFactory.getWeblogger();
        } catch (Exception e) {
            // Should only happen in case of 1st time startup, setup required
            log.debug("Ignorable error getting Roller instance", e);
            // Thowing a "soft" exception here allows setup to proceed
            throw new UsernameNotFoundException("User info not available yet.");
        }
        try {
            UserManager umgr = roller.getUserManager();
            User userData;
            try {
                userData = umgr.getUserByUserName(userName);
            } catch (WebloggerException ex) {
                throw new DataRetrievalFailureException("ERROR in user lookup", ex);
            }
            if (userData == null) {
                throw new UsernameNotFoundException("ERROR no user: " + userName);
            }
            List<SimpleGrantedAuthority> authorities = getAuthorities(userData, umgr);
            return new org.springframework.security.core.userdetails.User(userData.getUserName(), userData.getPassword(),
                    true, true, true, true, authorities);
        } catch (WebloggerException ex) {
            throw new DataAccessResourceFailureException("ERROR: fetching roles", ex);
        }
        

    }
        
    /**
     * Stores a password that Spring Security re-encoded after a successful
     * login, so passwords stored with an older encoding (for example the one
     * named by passwds.encryption.lazyUpgradeFrom) move to
     * passwds.encryption.algorithm. Nothing is stored when the re-encoding
     * is plain text ({noop}), which is what passwds.encryption.enabled=false
     * or passwds.encryption.algorithm=noop produce, or when the stored
     * password is no longer the one the login checked.
     */
    @Override
    public UserDetails updatePassword(UserDetails user, String newPassword) {
        if (newPassword == null || newPassword.startsWith("{noop}")) {
            return user;
        }
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            // read the user afresh: the password may have been changed since
            // this login read it, and that change has to win
            roller.release();
            UserManager umgr = roller.getUserManager();
            User userData = umgr.getUserByUserName(user.getUsername());
            if (userData == null || !Objects.equals(userData.getPassword(), user.getPassword())) {
                log.info("Kept the stored password of " + user.getUsername()
                        + ", which changed while they logged in");
                return user;
            }
            userData.setPassword(newPassword);
            umgr.saveUser(userData);
            roller.flush();
        } catch (WebloggerException ex) {
            log.warn("Could not store the re-encoded password of " + user.getUsername(), ex);
            return user;
        }
        log.info("Stored the re-encoded password of " + user.getUsername());
        return org.springframework.security.core.userdetails.User.withUserDetails(user)
                .password(newPassword).build();
    }

     private List<SimpleGrantedAuthority> getAuthorities(User userData, UserManager umgr) throws WebloggerException {
         List<String> roles = umgr.getRoles(userData);
         List<SimpleGrantedAuthority> authorities = new ArrayList<>(roles.size());
         for (String role : roles) {
             authorities.add(new SimpleGrantedAuthority(role));
         }
         return authorities;
     }
    
}
