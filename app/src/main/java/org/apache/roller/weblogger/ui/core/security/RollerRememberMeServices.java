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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class RollerRememberMeServices extends TokenBasedRememberMeServices {

    private static final Log log = LogFactory.getLog(RollerRememberMeServices.class);

    public RollerRememberMeServices(UserDetailsService userDetailsService) {
        
        super(WebloggerConfig.getProperty("rememberme.key", "springRocks"), userDetailsService);
        
        log.debug("initializing: RollerRememberMeServices");

        if (WebloggerConfig.getBooleanProperty("rememberme.enabled") && "springRocks".equals(getKey())) {
            throw new RuntimeException(
                "If remember-me is to be enabled, rememberme.key must be specified in the roller " +
                    "properties file. Make sure it is a secret and make sure it is NOT springRocks");
        }

        log.debug("initialized: RollerRememberMeServices with key");
    }

    /**
     * Signs the cookie with the Roller password of the user. With LDAP, the
     * password Roller stores for directory users is a placeholder, so the
     * configured placeholder is used for every user.
     */
    @Override
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password,
            RememberMeTokenAlgorithm algorithm) {
        if (WebloggerConfig.getAuthMethod() == AuthMethod.LDAP) {
            password = WebloggerConfig.getProperty("users.passwords.externalAuthValue", "<externalAuth>");
        }
        return super.makeTokenSignature(tokenExpiryTime, username, password, algorithm);
    }

    /**
     * A directory user who hasn't registered with Roller yet logs in without
     * a remember-me cookie, since there is no Roller account to check it
     * against later.
     */
    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication successfulAuthentication) {
        try {
            super.onLoginSuccess(request, response, successfulAuthentication);
        } catch (UsernameNotFoundException e) {
            log.debug("No remember-me cookie for " + successfulAuthentication.getName()
                    + ", who has no Roller account yet");
        }
    }
}
