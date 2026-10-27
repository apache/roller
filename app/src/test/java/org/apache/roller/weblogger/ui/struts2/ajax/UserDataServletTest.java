/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.roller.weblogger.ui.struts2.ajax;

import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Users who are not admins see only users whose user name matches the search.
 */
class UserDataServletTest {

    private static User user(String userName, String email) {
        User user = new User();
        user.setUserName(userName);
        user.setEmailAddress(email);
        return user;
    }

    @Test
    void matchesUserNamePrefixIgnoringCase() {
        User user = user("Siteadmin", "admin@example.com");
        assertTrue(UserDataServlet.userNameStartsWith(user, "site"));
        assertTrue(UserDataServlet.userNameStartsWith(user, ""));
        assertTrue(UserDataServlet.userNameStartsWith(user, null));
    }

    @Test
    void doesNotMatchOnEmailAddress() {
        User user = user("siteadmin", "admin@example.com");
        assertFalse(UserDataServlet.userNameStartsWith(user, "admin"));
        assertFalse(UserDataServlet.userNameStartsWith(user, "siteadmin2"));
    }
}
