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
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.struts2.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.struts2.admin.UserEdit;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Accounts that log in through LDAP or the servlet container store
 * users.passwords.externalAuthValue instead of a password, and that value never
 * matches at login. So nobody can choose it as a Roller password.
 */
class ExternalLoginMarkerPasswordTest {

    private static final String RESERVED = "error.add.user.reservedPassword";

    private final Map<String, String> savedProperties = new HashMap<>();
    private MockedStatic<WebloggerFactory> factory;
    private UserManager users;

    @BeforeEach
    void setUp() throws Exception {
        setProperty("authentication.method", "db");
        setProperty("users.passwords.externalAuthValue", "<externalAuth>");
        factory = mockStatic(WebloggerFactory.class);
        users = mock(UserManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getUserManager()).thenReturn(users);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
    }

    @AfterEach
    void tearDown() throws Exception {
        factory.close();
        Properties config = config();
        savedProperties.forEach((k, v) -> {
            if (v == null) {
                config.remove(k);
            } else {
                config.setProperty(k, v);
            }
        });
    }

    @Test
    void registrationRejectsTheMarker() {
        Register action = withText(new Register());
        action.getBean().setUserName("newuser");
        action.getBean().setPasswordText("<externalAuth>");
        action.getBean().setPasswordConfirm("<externalAuth>");

        action.myValidate();

        assertTrue(action.getActionErrors().contains(RESERVED));
    }

    @Test
    void registrationRejectsTheDefaultMarkerUnderAnotherConfiguredValue() throws Exception {
        setProperty("users.passwords.externalAuthValue", "<ldap>");
        for (String reserved : new String[] {"<ldap>", "<externalAuth>"}) {
            Register action = withText(new Register());
            action.getBean().setUserName("newuser");
            action.getBean().setPasswordText(reserved);
            action.getBean().setPasswordConfirm(reserved);

            action.myValidate();

            assertTrue(action.getActionErrors().contains(RESERVED), reserved);
        }
    }

    @Test
    void registrationAcceptsAnOrdinaryPassword() {
        Register action = withText(new Register());
        action.getBean().setUserName("newuser");
        action.getBean().setPasswordText("an ordinary password");
        action.getBean().setPasswordConfirm("an ordinary password");

        action.myValidate();

        assertFalse(action.hasActionErrors());
    }

    @Test
    void registrationWithoutAPasswordDoesNotFail() throws Exception {
        // neither a password nor an OpenID URL is typed
        setProperty("authentication.method", "db-oidc");
        Register action = withText(new Register());
        action.getBean().setUserName("newuser");

        assertDoesNotThrow(action::myValidate);
    }

    @Test
    void theProfileRejectsTheMarker() throws Exception {
        User user = new User();
        user.setUserName("dbuser");
        user.setPassword("{bcrypt}existing");
        Profile action = withText(new Profile());
        action.setAuthenticatedUser(user);
        action.getBean().setPasswordText("<externalAuth>");
        action.getBean().setPasswordConfirm("<externalAuth>");

        assertEquals(UIAction.INPUT, action.save());

        assertTrue(action.getActionErrors().contains(RESERVED));
        assertEquals("{bcrypt}existing", user.getPassword());
        verify(users, never()).saveUser(any());
    }

    @Test
    void anAdministratorCannotGiveAUserTheMarker() throws Exception {
        User admin = new User();
        admin.setUserName("admin");
        User user = new User();
        user.setId("user-1");
        user.setUserName("dbuser");
        user.setPassword("{bcrypt}existing");
        when(users.getUser("user-1")).thenReturn(user);
        UserEdit action = withText(new UserEdit());
        action.setActionName("modifyUser");
        action.setAuthenticatedUser(admin);
        action.getBean().setId("user-1");
        action.myPrepare();
        action.getBean().setPassword("<externalAuth>");

        assertEquals(UIAction.INPUT, action.save());

        assertTrue(action.getActionErrors().contains(RESERVED));
        assertEquals("{bcrypt}existing", user.getPassword());
        verify(users, never()).saveUser(any());
    }

    @Test
    void anAdministratorCannotCreateAUserWithTheMarker() throws Exception {
        UserEdit action = withText(new UserEdit());
        User admin = new User();
        admin.setUserName("admin");
        action.setActionName("createUser");
        action.setAuthenticatedUser(admin);
        action.myPrepare();
        action.getBean().setUserName("newuser");
        action.getBean().setPassword("<externalAuth>");

        assertEquals(UIAction.INPUT, action.save());

        assertTrue(action.getActionErrors().contains(RESERVED));
        verify(users, never()).addUser(any());
    }

    private static <T extends UIAction> T withText(T action) {
        T spied = spy(action);
        doAnswer(call -> call.getArgument(0)).when(spied).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(spied).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(spied).getText(anyString(), anyString(), anyString());
        return spied;
    }

    private void setProperty(String key, String value) throws ReflectiveOperationException {
        Properties config = config();
        // keep the first value seen, even when it is null (not set)
        if (!savedProperties.containsKey(key)) {
            savedProperties.put(key, config.getProperty(key));
        }
        config.setProperty(key, value);
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }
}
