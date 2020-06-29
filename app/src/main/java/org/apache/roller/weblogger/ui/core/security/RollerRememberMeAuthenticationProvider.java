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

package org.apache.roller.weblogger.ui.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;


public class RollerRememberMeAuthenticationProvider extends RememberMeAuthenticationProvider {
    private static final Log log = LogFactory.getLog(RollerRememberMeAuthenticationProvider.class);


    public RollerRememberMeAuthenticationProvider() {
        
        super(WebloggerConfig.getProperty("rememberme.key", "springRocks"));
        
        log.debug("initializing: RollerRememberMeAuthenticationProvider");

        if (WebloggerConfig.getBooleanProperty("rememberme.enabled") && "springRocks".equals(getKey())) {
            throw new RuntimeException(
                "If remember-me is to be enabled, rememberme.key must be specified in the roller " +
                "properties file. Make sure it is a secret and make sure it is NOT springRocks");
        }

        log.debug("initialized: RollerRememberMeAuthenticationProvider with key: " + getKey());
    }
}


