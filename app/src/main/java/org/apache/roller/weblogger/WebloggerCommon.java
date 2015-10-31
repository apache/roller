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
package org.apache.roller.weblogger;

import java.util.UUID;

public final class WebloggerCommon {

    public static final String FORMAT_6CHARS = "yyyyMM";
    public static final String FORMAT_8CHARS = "yyyyMMdd";

    public static final int PERCENT_100 = 100;

    public static final int ONE_KB_IN_BYTES = 1024;
    public static final int FOUR_KB_IN_BYTES = 4096;
    public static final int EIGHT_KB_IN_BYTES = 8192;
    public static final int TWENTYFOUR_KB_IN_BYTES = 24576;
    public static final int ONE_MB_IN_BYTES = ONE_KB_IN_BYTES * ONE_KB_IN_BYTES;
    public static final int TEXTWIDTH_255 = 255;

    private WebloggerCommon() {
        // never instantiable
        throw new AssertionError();
    }

    /**
     * Generate a new UUID.
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public enum AuthMethod {
        ROLLERDB("db"),
        LDAP("ldap"),
        OPENID("openid");

        private final String propertyName;

        AuthMethod(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public static AuthMethod getAuthMethod(String propertyName) {
            for (AuthMethod test : AuthMethod.values()) {
                if (test.getPropertyName().equals(propertyName)) {
                    return test;
                }
            }
            throw new IllegalArgumentException("Unknown authentication.method property value: "
                    + propertyName + " defined in Roller properties file.");
        }

    }
}
