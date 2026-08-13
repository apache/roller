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
 */
package org.apache.roller.weblogger.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public enum AuthMethod {
    ROLLERDB("db"),
    LDAP("ldap"),
    OIDC("oidc"),
    DB_OIDC("db-oidc"),
    CMA("cma");

    private final String propertyName;

    AuthMethod(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    private static final Log log = LogFactory.getLog(AuthMethod.class);
    private static boolean warnedAboutOpenId;

    public static AuthMethod getAuthMethod(String propertyName) {
        // OpenID 2.0 was replaced by OIDC; accept the old property values so
        // an upgraded install boots instead of failing on every request
        if ("openid".equals(propertyName) || "db-openid".equals(propertyName)) {
            AuthMethod replacement = "openid".equals(propertyName) ? OIDC : DB_OIDC;
            if (!warnedAboutOpenId) {
                warnedAboutOpenId = true;
                log.warn("authentication.method=" + propertyName + " is no longer supported and is "
                        + "treated as " + replacement.getPropertyName() + "; update the property and "
                        + "configure an oidc.{id}.* provider registration");
            }
            return replacement;
        }
        for (AuthMethod test : AuthMethod.values()) {
            if (test.getPropertyName().equals(propertyName)) {
                return test;
            }
        }
        throw new IllegalArgumentException("Unknown authentication.method property value: "
                + propertyName + " defined in Roller properties file.");
    }

}
