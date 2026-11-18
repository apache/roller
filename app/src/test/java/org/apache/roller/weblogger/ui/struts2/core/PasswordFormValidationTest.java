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

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Passwords that the encoder can't take get a form error, and a password
 * generated for an OIDC user is never put in the form.
 */
class PasswordFormValidationTest {

    // 20 emoji, 80 bytes in UTF-8
    private static final String TOO_LONG = "😀".repeat(20);
    // 72 bytes in UTF-8
    private static final String LONGEST = "a".repeat(72);

    @Test
    void profileRejectsPasswordTooLongToEncode() {
        Profile action = new Profile() {
            @Override
            public String getText(String key) {
                return key;
            }
        };
        action.getBean().setPasswordText(TOO_LONG);
        action.getBean().setPasswordConfirm(TOO_LONG);
        action.myValidate();
        assertTrue(action.getActionErrors().contains("userRegister.error.passwordTooLong"));
    }

    @Test
    void profileAcceptsLongestPassword() {
        Profile action = new Profile() {
            @Override
            public String getText(String key) {
                return key;
            }
        };
        action.getBean().setPasswordText(LONGEST);
        action.getBean().setPasswordConfirm(LONGEST);
        action.myValidate();
        assertFalse(action.getActionErrors().contains("userRegister.error.passwordTooLong"));
    }

    @Test
    void registerRejectsPasswordTooLongToEncode() throws Exception {
        Register action = register(AuthMethod.ROLLERDB);
        action.getBean().setUserName("emoji");
        action.getBean().setPasswordText(TOO_LONG);
        action.getBean().setPasswordConfirm(TOO_LONG);
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            action.myValidate();
        }
        assertTrue(action.getActionErrors().contains("userRegister.error.passwordTooLong"));
    }

    @Test
    void registerKeepsGeneratedPasswordOutOfTheForm() throws Exception {
        Register action = register(AuthMethod.DB_OIDC);
        action.getBean().setUserName("taken");
        action.getBean().setOpenIdUrl("https://idp.example.com#taken");
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            action.myValidate();
        }
        // the form is shown again with an error, and must not carry the password
        assertTrue(action.getActionErrors().contains("error.add.user.userNameInUse"));
        assertNull(action.getBean().getPasswordText());
        assertNull(action.getBean().getPasswordConfirm());
        assertNull(action.getBean().getPassword());
    }

    private static Register register(AuthMethod authMethod) throws Exception {
        Register action = new Register() {
            @Override
            public String getText(String key) {
                return key;
            }
        };
        Field field = Register.class.getDeclaredField("authMethod");
        field.setAccessible(true);
        field.set(action, authMethod);
        return action;
    }

    private static MockedStatic<WebloggerFactory> mockUserManager() throws Exception {
        UserManager userManager = mock(UserManager.class);
        when(userManager.getUserByUserName("taken", null))
                .thenReturn(new org.apache.roller.weblogger.pojos.User());
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getUserManager()).thenReturn(userManager);
        MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        return factory;
    }
}
