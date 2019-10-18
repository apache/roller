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
package org.tightblog.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.GlobalRole;
import org.tightblog.domain.UserCredentials;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.tightblog.dao.UserCredentialsDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Security UserDetailsService implemented using Weblogger API.
 * DB auth uses this class to obtain correct DB-stored credentials
 * to compare against user input to determine if login successful.
 * The user's GlobalRole is also determined here.
 */
@Component
public class CustomUserDetailsService implements UserDetailsService {

    private UserCredentialsDao userCredentialsDao;

    private DynamicProperties dynamicProperties;

    @Autowired
    public CustomUserDetailsService(UserCredentialsDao userCredentialsDao,
                                    DynamicProperties dynamicProperties) {
        this.userCredentialsDao = userCredentialsDao;
        this.dynamicProperties = dynamicProperties;
    }

    @Value("${mfa.enabled}")
    private boolean mfaEnabled;

    /**
     * @throws UsernameNotFoundException, DataAccessException
     */
    @Override
    public UserDetails loadUserByUsername(String userName) {

        if (!dynamicProperties.isDatabaseReady()) {
            // Should only happen in case of 1st time startup, setup required
            // Thowing a "soft" exception here allows setup to proceed
            throw new UsernameNotFoundException("User info not available yet.");
        }

        UserCredentials creds = userCredentialsDao.findByUserName(userName);
        GlobalRole targetGlobalRole;
        String targetPassword;

        if (creds == null) {
            throw new UsernameNotFoundException("ERROR no user: " + userName);
        }
        targetPassword = creds.getPassword();

        // If MFA required & user hasn't a secret for their authenticator app, limit role
        // to PRE_AUTH_USER (intended to limit user to QR code scan page.)
        if (mfaEnabled && StringUtils.isBlank(creds.getMfaSecret())) {
            targetGlobalRole = GlobalRole.MISSING_MFA_SECRET;
        } else {
            targetGlobalRole = creds.getGlobalRole();
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>(1);
        authorities.add(new SimpleGrantedAuthority(targetGlobalRole.name()));
        return new org.springframework.security.core.userdetails.User(userName, targetPassword,
                true, true, true, true, authorities);
    }
}
