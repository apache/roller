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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final String MARKER = "users.passwords.externalAuthValue";

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
    void profileRejectsPasswordTooLongToEncodeWithOpenIdUrl() {
        Profile action = new Profile() {
            @Override
            public String getText(String key) {
                return key;
            }
        };
        action.getBean().setOpenIdUrl("https://idp.example.com#emoji");
        action.getBean().setPasswordText(TOO_LONG);
        action.getBean().setPasswordConfirm(TOO_LONG);
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            action.myValidate();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
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
    void registerReportsATakenUserNameWithATooLongPassword() throws Exception {
        Register action = register(AuthMethod.ROLLERDB);
        action.getBean().setUserName("taken");
        action.getBean().setPasswordText(TOO_LONG);
        action.getBean().setPasswordConfirm(TOO_LONG + "x");
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            action.myValidate();
        }
        assertTrue(action.getActionErrors().contains("userRegister.error.passwordTooLong"));
        assertTrue(action.getActionErrors().contains("userRegister.error.mismatchedPasswords"));
        assertTrue(action.getActionErrors().contains("error.add.user.userNameInUse"));
    }

    @Test
    void registerRequiresAPasswordInDatabaseMode() throws Exception {
        Register action = register(AuthMethod.ROLLERDB);
        action.getBean().setUserName("nopassword");
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            action.myValidate();
        }
        assertEquals(List.of("error.add.user.missingPassword"), List.copyOf(action.getActionErrors()));
        assertTrue(messages().containsKey("error.add.user.missingPassword"));
    }

    @Test
    void registerRequiresAPasswordWithoutAnOpenIdUrl() throws Exception {
        Register action = register(AuthMethod.DB_OIDC);
        action.getBean().setUserName("nopassword");
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            action.myValidate();
        }
        assertEquals(List.of("error.add.user.missingPassword"), List.copyOf(action.getActionErrors()));
    }

    @Test
    void registerDoesNotMeasureThePlaceholderOfAnExternalLogin() throws Exception {
        Properties config = config();
        String saved = config.getProperty(MARKER);
        // longer than a typed password may be
        config.setProperty(MARKER, "x".repeat(80));
        try (MockedStatic<WebloggerFactory> factory = mockUserManager()) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getUserPrincipal()).thenReturn(() -> "cmauser");
            Register action = new Register() {
                @Override
                public String getText(String key) {
                    return key;
                }

                @Override
                protected HttpServletRequest getServletRequest() {
                    return request;
                }
            };
            setAuthMethod(action, AuthMethod.CMA);

            action.myValidate();

            assertEquals("cmauser", action.getBean().getUserName());
            assertFalse(action.hasActionErrors(), action.getActionErrors().toString());
        } finally {
            if (saved == null) {
                config.remove(MARKER);
            } else {
                config.setProperty(MARKER, saved);
            }
        }
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
        setAuthMethod(action, authMethod);
        return action;
    }

    private static void setAuthMethod(Register action, AuthMethod authMethod) throws Exception {
        Field field = Register.class.getDeclaredField("authMethod");
        field.setAccessible(true);
        field.set(action, authMethod);
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }

    private static Properties messages() throws Exception {
        Properties messages = new Properties();
        try (InputStream in = Register.class.getResourceAsStream("/ApplicationResources.properties")) {
            messages.load(in);
        }
        return messages;
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
