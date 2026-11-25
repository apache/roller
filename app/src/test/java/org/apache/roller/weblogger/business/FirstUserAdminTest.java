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
package org.apache.roller.weblogger.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * With users.firstUserAdmin on, only the site's first account becomes an
 * administrator. Accounts that exist but are disabled still count.
 */
class FirstUserAdminTest {

    private static final String NEW_USER = TestUtils.JUNIT_PREFIX + "firstadminnew";

    private String existingName;
    private final List<String> disabledHere = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        existingName = TestUtils.setupUser("firstadminexisting").getUserName();
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        UserManager mgr = users();
        for (String name : disabledHere) {
            User user = mgr.getUserByUserName(name, null);
            if (user != null) {
                user.setEnabled(Boolean.TRUE);
                mgr.saveUser(user);
            }
        }
        TestUtils.endSession(true);
        for (String name : new String[] { NEW_USER, existingName }) {
            if (mgr.getUserByUserName(name, null) != null) {
                TestUtils.teardownUser(name);
            }
        }
        TestUtils.endSession(true);
    }

    @Test
    void aNewAccountIsNotAdminWhenOnlyDisabledAccountsExist() throws Exception {
        assertTrue(WebloggerConfig.getBooleanProperty("users.firstUserAdmin"));
        UserManager mgr = users();
        for (User user : mgr.getUsers(Boolean.TRUE, null, null, 0, -1)) {
            user.setEnabled(Boolean.FALSE);
            mgr.saveUser(user);
            disabledHere.add(user.getUserName());
        }
        TestUtils.endSession(true);
        assertTrue(disabledHere.contains(existingName));

        User user = new User();
        user.setUserName(NEW_USER);
        user.setPassword("password");
        user.setScreenName("First admin test");
        user.setFullName("First admin test");
        user.setEmailAddress("firstadmin@example.com");
        user.setLocale("en_US");
        user.setTimeZone("America/New_York");
        user.setDateCreated(new Date());
        user.setEnabled(Boolean.TRUE);
        mgr.addUser(user);
        TestUtils.endSession(true);

        User saved = mgr.getUserByUserName(NEW_USER, null);
        assertNotNull(saved);
        assertTrue(mgr.hasRole("editor", saved));
        assertFalse(mgr.hasRole("admin", saved), "an earlier account exists, though disabled");
    }

    private static UserManager users() {
        return WebloggerFactory.getWeblogger().getUserManager();
    }
}
