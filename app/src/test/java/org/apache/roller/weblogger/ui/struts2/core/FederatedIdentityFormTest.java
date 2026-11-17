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
package org.apache.roller.weblogger.ui.struts2.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * The federated identity (issuer#subject) stored in User.openIdUrl decides
 * which account an OIDC login opens, so only Roller's OIDC login code sets it.
 * The profile and registration forms keep whatever is stored, whatever the
 * request carries.
 */
class FederatedIdentityFormTest {

    private static final String IDENTITY = "https://idp.example.com#subject-1";

    private final Map<String, String> savedProperties = new HashMap<>();
    private String userName;
    private PasswordEncoder savedEncoder;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        // Profile gives OIDC users a 255 character random password, which
        // neither bcrypt (72 byte limit) nor "{noop}" plus the password (past
        // the 255 character column) can store, so store a short stand-in.
        savedEncoder = swapEncoder(new PasswordEncoder() {
            @Override
            public String encode(CharSequence raw) {
                return "{test}" + Integer.toHexString(raw.toString().hashCode());
            }

            @Override
            public boolean matches(CharSequence raw, String encoded) {
                return encode(raw).equals(encoded);
            }
        });
        userName = TestUtils.setupUser("federatedform").getUserName();
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        TestUtils.teardownUser(userName);
        User registered = WebloggerFactory.getWeblogger().getUserManager()
                .getUserByUserName("junitregistered", null);
        if (registered != null) {
            TestUtils.teardownUser(registered.getUserName());
        }
        TestUtils.endSession(true);
        swapEncoder(savedEncoder);
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
    void profileSaveDoesNotSetTheIdentity() throws Exception {
        Profile profile = profile();
        profile.getBean().setOpenIdUrl(IDENTITY);

        assertEquals(UIAction.SUCCESS, profile.save(), () -> profile.getActionErrors().toString());
        TestUtils.endSession(true);

        assertNull(storedIdentity(userName));
    }

    @Test
    void profileSaveDoesNotChangeTheIdentity() throws Exception {
        setStoredIdentity(IDENTITY);
        Profile profile = profile();
        profile.getBean().setOpenIdUrl("https://other.example.com#subject-2");

        assertEquals(UIAction.SUCCESS, profile.save(), () -> profile.getActionErrors().toString());
        TestUtils.endSession(true);

        assertEquals(IDENTITY, storedIdentity(userName));
    }

    @Test
    void oidcUserSavesProfileWithoutPostingTheIdentity() throws Exception {
        setProperty("authentication.method", "oidc");
        setStoredIdentity(IDENTITY);
        Profile profile = profile();
        profile.getBean().setOpenIdUrl(null);
        profile.getBean().setFullName("Renamed");

        assertEquals(UIAction.SUCCESS, profile.save(), () -> profile.getActionErrors().toString());
        TestUtils.endSession(true);

        assertEquals(IDENTITY, storedIdentity(userName));
        assertEquals("Renamed", user(userName).getFullName());
    }

    @Test
    void dbOidcUserSavesProfileWithoutPostingTheIdentity() throws Exception {
        setProperty("authentication.method", "db-oidc");
        setStoredIdentity(IDENTITY);
        Profile profile = profile();
        profile.getBean().setOpenIdUrl(null);

        assertEquals(UIAction.SUCCESS, profile.save(), () -> profile.getActionErrors().toString());
        TestUtils.endSession(true);

        assertEquals(IDENTITY, storedIdentity(userName));
    }

    @Test
    void registrationDoesNotSetTheIdentity() throws Exception {
        boolean completed = setBootstrapCompleted(true);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));
        try (MockedStatic<WebloggerRuntimeConfig> runtime =
                     mockStatic(WebloggerRuntimeConfig.class, CALLS_REAL_METHODS)) {
            runtime.when(() -> WebloggerRuntimeConfig.getBooleanProperty("users.registration.enabled"))
                    .thenReturn(true);
            runtime.when(() -> WebloggerRuntimeConfig.getBooleanProperty("user.account.email.activation"))
                    .thenReturn(false);

            Register register = spy(new Register());
            stubText(register);
            register.withServletRequest(request);
            ProfileBean bean = register.getBean();
            bean.setUserName("junitregistered");
            bean.setScreenName("registered");
            bean.setFullName("Registered User");
            bean.setEmailAddress("registered@dev.null");
            bean.setLocale("en_US");
            bean.setTimeZone("America/Denver");
            bean.setPasswordText("registered-password");
            bean.setPasswordConfirm("registered-password");
            bean.setOpenIdUrl(IDENTITY);

            assertEquals(UIAction.SUCCESS, register.save(), () -> register.getActionErrors().toString());
            TestUtils.endSession(true);
        } finally {
            setBootstrapCompleted(completed);
        }

        User registered = user("junitregistered");
        assertNotNull(registered);
        assertNull(registered.getOpenIdUrl());
    }

    private Profile profile() throws Exception {
        Profile profile = spy(new Profile());
        stubText(profile);
        User self = user(userName);
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        return profile;
    }

    private void setStoredIdentity(String identity) throws Exception {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        User user = mgr.getUserByUserName(userName, null);
        user.setOpenIdUrl(identity);
        mgr.saveUser(user);
        TestUtils.endSession(true);
    }

    private static String storedIdentity(String name) throws Exception {
        return user(name).getOpenIdUrl();
    }

    private static User user(String name) throws Exception {
        return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(name, null);
    }

    private static void stubText(UIAction action) {
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
    }

    private static PasswordEncoder swapEncoder(PasswordEncoder encoder) throws Exception {
        Field field = RollerContext.class.getDeclaredField("encoder");
        field.setAccessible(true);
        PasswordEncoder previous = (PasswordEncoder) field.get(null);
        field.set(null, encoder);
        return previous;
    }

    private static boolean setBootstrapCompleted(boolean value) throws Exception {
        Field field = BootstrapSecurity.class.getDeclaredField("completed");
        field.setAccessible(true);
        boolean previous = field.getBoolean(null);
        field.setBoolean(null, value);
        return previous;
    }

    private void setProperty(String key, String value) throws ReflectiveOperationException {
        Properties config = config();
        savedProperties.putIfAbsent(key, config.getProperty(key));
        config.setProperty(key, value);
    }

    private static Properties config() throws ReflectiveOperationException {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        return (Properties) field.get(null);
    }
}
