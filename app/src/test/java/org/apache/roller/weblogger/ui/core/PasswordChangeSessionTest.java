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

package org.apache.roller.weblogger.ui.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.struts2.admin.CreateUserBean;
import org.apache.roller.weblogger.ui.struts2.admin.UserEdit;
import org.apache.roller.weblogger.ui.struts2.core.Profile;
import org.apache.roller.weblogger.ui.struts2.core.ProfileBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * A user logged in from two browsers changes their password in one of them, or
 * an administrator resets or disables the account. The other browser's session
 * ends on its next request, and the session that made the change stays logged
 * in.
 */
class PasswordChangeSessionTest {

    private String userName;
    private String adminName;
    private PasswordEncoder savedEncoder;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        savedEncoder = swapEncoder(new DelegatingPasswordEncoder("bcrypt",
                Map.of("bcrypt", new BCryptPasswordEncoder(4))));
        userName = TestUtils.setupUser("sessionuser").getUserName();
        adminName = TestUtils.setupUser("sessionadmin").getUserName();
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        TestUtils.teardownUser(userName);
        TestUtils.teardownUser(adminName);
        TestUtils.endSession(true);
        swapEncoder(savedEncoder);
    }

    @Test
    void changingOwnPasswordEndsTheUsersOtherSessions() throws Exception {
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        assertEquals(userName, a.request().getAuthenticatedUser().getUserName());
        assertEquals(userName, b.request().getAuthenticatedUser().getUserName());
        TestUtils.endSession(true);

        a.request();
        Profile profile = spy(new Profile());
        stubText(profile);
        User self = user(userName);
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        bean.setPasswordText("changed-password");
        bean.setPasswordConfirm("changed-password");
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        profile.setServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save());
        TestUtils.endSession(true);

        b.assertLoggedOut();
        a.assertLoggedIn(userName);
    }

    @Test
    void adminPasswordResetEndsTheUsersSessions() throws Exception {
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        assertEquals(userName, b.request().getAuthenticatedUser().getUserName());
        TestUtils.endSession(true);

        saveAsAdmin(admin, bean -> bean.setPassword("reset-password"));

        b.assertLoggedOut();
        admin.assertLoggedIn(adminName);
    }

    @Test
    void disablingAUserEndsTheirSessions() throws Exception {
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        assertEquals(userName, b.request().getAuthenticatedUser().getUserName());
        TestUtils.endSession(true);

        saveAsAdmin(admin, bean -> bean.setEnabled(false));

        b.assertLoggedOut();
        admin.assertLoggedIn(adminName);
    }

    @Test
    void disablingAUserEndsTheirOpenIdSessions() throws Exception {
        // OpenID 2.0 logins carry the user's OpenID URL as the principal name
        String openIdUrl = "https://openid.example.com/sessionuser";
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        User u = user(userName);
        u.setOpenIdUrl(openIdUrl);
        mgr.saveUser(u);
        TestUtils.endSession(true);

        Browser admin = new Browser(adminName);
        Browser b = new Browser(openIdUrl + "/");
        admin.request();
        assertEquals(userName, b.request().getAuthenticatedUser().getUserName());
        TestUtils.endSession(true);

        saveAsAdmin(admin, bean -> bean.setEnabled(false));

        b.assertLoggedOut();
        admin.assertLoggedIn(adminName);
    }

    @Test
    void adminChangingOwnPasswordStaysLoggedIn() throws Exception {
        Browser admin = new Browser(adminName);
        Browser other = new Browser(adminName);
        admin.request();
        other.request();
        TestUtils.endSession(true);

        admin.request();
        UserEdit action = userEdit(admin, adminName);
        action.getBean().setPassword("reset-password");
        assertEquals(UserEdit.SUCCESS, action.save());
        TestUtils.endSession(true);

        other.assertLoggedOut();
        admin.assertLoggedIn(adminName);
    }

    @Test
    void savingWithoutAPasswordChangeKeepsOtherSessions() throws Exception {
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);

        saveAsAdmin(admin, bean -> bean.setFullName("Renamed User"));

        b.assertLoggedIn(userName);
    }

    @Test
    void openIdProfileEditKeepsTheUsersOtherSessions() throws Exception {
        linkOpenId(userName, "https://openid.example.com/sessionuser");
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        a.request();
        Profile profile = spy(new Profile());
        stubText(profile);
        setAuthMethod(profile, AuthMethod.OPENID);
        User self = user(userName);
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        bean.setScreenName("Renamed");
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        profile.setServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save(), () -> String.valueOf(profile.getActionErrors()));
        TestUtils.endSession(true);

        assertEquals("Renamed", user(userName).getScreenName());
        assertEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedIn(userName);
        a.assertLoggedIn(userName);
    }

    @Test
    void adminEditOfAnOpenIdUserKeepsTheirSessions() throws Exception {
        linkOpenId(userName, "https://openid.example.com/sessionuser");
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        admin.request();
        UserEdit action = userEdit(admin, userName);
        setAuthMethod(action, AuthMethod.DB_OPENID);
        action.getBean().setFullName("Renamed User");
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
        TestUtils.endSession(true);

        assertEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedIn(userName);
    }

    @Test
    void switchingToOpenIdReplacesTheStoredPassword() throws Exception {
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        admin.request();
        UserEdit action = userEdit(admin, userName);
        setAuthMethod(action, AuthMethod.DB_OPENID);
        action.getBean().setOpenIdUrl("https://openid.example.com/sessionuser");
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
        TestUtils.endSession(true);

        assertNotEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedOut();
    }

    private static void linkOpenId(String name, String openIdUrl) throws Exception {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        User u = mgr.getUserByUserName(name, null);
        u.setOpenIdUrl(openIdUrl);
        mgr.saveUser(u);
        TestUtils.endSession(true);
    }

    private static void setAuthMethod(Object action, AuthMethod method) throws Exception {
        // the action is a Mockito spy, a subclass of the real action
        Field field = findField(action.getClass(), "authMethod");
        field.setAccessible(true);
        field.set(action, method);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // keep looking in the superclass
            }
        }
        throw new NoSuchFieldException(name);
    }

    @Test
    void passwordChangeEndsASessionThatWasPassivated() throws Exception {
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);

        // the container saves b's session and restores it later, which drops
        // the Roller session but keeps the login
        b.passivate();

        a.request();
        Profile profile = spy(new Profile());
        stubText(profile);
        User self = user(userName);
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        bean.setPasswordText("changed-password");
        bean.setPasswordConfirm("changed-password");
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        profile.setServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save());
        TestUtils.endSession(true);

        b.assertLoggedOut();
        // the session that changed the password keeps working after a restore
        a.passivate();
        a.assertLoggedIn(userName);
    }

    @Test
    void passivatedSessionStaysLoggedInWithoutAPasswordChange() throws Exception {
        Browser b = new Browser(userName);
        b.request();
        TestUtils.endSession(true);

        b.passivate();

        b.assertLoggedIn(userName);
    }

    private void saveAsAdmin(Browser admin, java.util.function.Consumer<CreateUserBean> change)
            throws Exception {
        admin.request();
        UserEdit action = userEdit(admin, userName);
        change.accept(action.getBean());
        assertEquals(UserEdit.SUCCESS, action.save());
        TestUtils.endSession(true);
    }

    private UserEdit userEdit(Browser admin, String target) throws Exception {
        UserEdit action = spy(new UserEdit());
        stubText(action);
        action.setActionName("modifyUser");
        action.setAuthenticatedUser(user(adminName));
        action.setServletRequest(admin.httpRequest);
        action.getBean().setId(user(target).getId());
        action.myPrepare();
        action.getBean().copyFrom(user(target));
        action.getBean().setPassword(null);
        return action;
    }

    private static void stubText(org.apache.roller.weblogger.ui.struts2.util.UIAction action) {
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
    }

    private static User user(String name) throws Exception {
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
        return mgr.getUserByUserName(name, null);
    }

    private static PasswordEncoder swapEncoder(PasswordEncoder encoder) throws Exception {
        Field field = RollerContext.class.getDeclaredField("encoder");
        field.setAccessible(true);
        PasswordEncoder previous = (PasswordEncoder) field.get(null);
        field.set(null, encoder);
        return previous;
    }

    /**
     * One browser: an HTTP session and the Spring login stored in it. Each
     * request puts that login in the SecurityContextHolder, as Spring does,
     * while the session holding it is still valid.
     */
    private static final class Browser {
        final HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        final Authentication login;
        HttpSession session;
        // the session holding the login was invalidated
        boolean ended;

        Browser(String name) {
            login = UsernamePasswordAuthenticationToken.authenticated(name, null, List.of());
            session = newSession();
            // keep reporting the original login, so a rebuild from it would show
            when(httpRequest.getUserPrincipal()).thenReturn(login);
            when(httpRequest.getSession(false)).thenAnswer(call -> session);
            when(httpRequest.getSession(true)).thenAnswer(call -> {
                if (session == null) {
                    session = newSession();
                }
                return session;
            });
            when(httpRequest.getSession()).thenAnswer(call -> httpRequest.getSession(true));
        }

        private HttpSession newSession() {
            Map<String, Object> attributes = new HashMap<>();
            boolean[] valid = {true};
            HttpSession s = mock(HttpSession.class);
            when(s.getAttribute(anyString())).thenAnswer(call -> {
                checkValid(valid);
                return attributes.get(call.<String>getArgument(0));
            });
            doAnswer(call -> {
                checkValid(valid);
                attributes.put(call.getArgument(0), call.getArgument(1));
                return null;
            }).when(s).setAttribute(anyString(), any());
            doAnswer(call -> {
                checkValid(valid);
                attributes.remove(call.<String>getArgument(0));
                return null;
            }).when(s).removeAttribute(anyString());
            doAnswer(call -> {
                checkValid(valid);
                valid[0] = false;
                attributes.clear();
                ended = true;
                if (session == s) {
                    session = null;
                }
                return null;
            }).when(s).invalidate();
            return s;
        }

        private static void checkValid(boolean[] valid) {
            if (!valid[0]) {
                throw new IllegalStateException("session invalidated");
            }
        }

        /** The container saves this session and restores it later. */
        void passivate() {
            new RollerSession().sessionWillPassivate(new HttpSessionEvent(session));
        }

        RollerSession request() {
            if (ended) {
                SecurityContextHolder.clearContext();
            } else {
                SecurityContextHolder.getContext().setAuthentication(login);
            }
            return RollerSession.getRollerSession(httpRequest);
        }

        void assertLoggedOut() {
            RollerSession rollerSession = request();
            assertNull(rollerSession == null ? null : rollerSession.getAuthenticatedUser(),
                    "the session should no longer have a user");
            assertTrue(ended, "the HTTP session should be invalidated");
            assertNull(SecurityContextHolder.getContext().getAuthentication(),
                    "the login should be cleared from this request");
            SecurityContextHolder.clearContext();
        }

        void assertLoggedIn(String name) {
            RollerSession rollerSession = request();
            assertNotNull(rollerSession);
            assertNotNull(rollerSession.getAuthenticatedUser(), "the session should keep its user");
            assertEquals(name, rollerSession.getAuthenticatedUser().getUserName());
            assertFalse(ended, "the HTTP session should stay valid");
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            SecurityContextHolder.clearContext();
        }
    }
}
