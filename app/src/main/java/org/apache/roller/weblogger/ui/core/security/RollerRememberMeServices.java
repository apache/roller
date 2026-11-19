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
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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
     * Calculates the digital signature to be put in the cookie. Default value is
     * SHA-512 ("username:tokenExpiryTime:password:key")
     *
     * If LDAP is enabled then a configurable dummy password is used in the calculation.
     */
    @Override
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {

        boolean usingLDAP = WebloggerConfig.getAuthMethod() == AuthMethod.LDAP;
        if (usingLDAP) {
            log.debug("LDAP is enabled; using dummy password in remember me signature.");

            // for LDAP we don't store its password in the roller_users table,
            // just an string indicating external auth method being used.
            password = RollerPasswordEncoders.externalLoginMarker();
        }

        String data = username + ":" + tokenExpiryTime + ":" + password + ":" + getKey();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Required by Spec.", e);
        }

        return new String(Hex.encode(digest.digest(data.getBytes())));
    }
}
