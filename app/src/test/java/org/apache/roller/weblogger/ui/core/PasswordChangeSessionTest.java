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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.security.RollerLoginSuccessHandler;
import org.apache.roller.weblogger.ui.core.security.RollerPasswordEncoders;
import org.apache.roller.weblogger.ui.core.security.RollerRememberMeServices;
import org.apache.roller.weblogger.ui.core.security.RollerUserDetailsService;
import org.apache.roller.weblogger.ui.struts2.admin.CreateUserBean;
import org.apache.roller.weblogger.ui.struts2.admin.UserEdit;
import org.apache.roller.weblogger.ui.struts2.core.Profile;
import org.apache.roller.weblogger.ui.struts2.core.ProfileBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    private boolean encoderSwapped;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        savedEncoder = swapEncoder(RollerPasswordEncoders.create());
        encoderSwapped = true;
        userName = TestUtils.setupUser("sessionuser").getUserName();
        adminName = TestUtils.setupUser("sessionadmin").getUserName();
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        try {
            if (userName != null) {
                TestUtils.teardownUser(userName);
            }
            if (adminName != null) {
                TestUtils.teardownUser(adminName);
            }
            TestUtils.endSession(true);
        } finally {
            // later test classes must not run with this class's encoder
            if (encoderSwapped) {
                swapEncoder(savedEncoder);
            }
        }
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
        profile.withServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save(), () -> String.valueOf(profile.getActionErrors()));
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
    void adminChangingOwnPasswordStaysLoggedIn() throws Exception {
        Browser admin = new Browser(adminName);
        Browser other = new Browser(adminName);
        admin.request();
        other.request();
        TestUtils.endSession(true);

        admin.request();
        UserEdit action = userEdit(admin, adminName);
        action.getBean().setPassword("reset-password");
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
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
    void oidcProfileEditKeepsTheUsersOtherSessions() throws Exception {
        useHashedPasswords();
        linkOpenId(userName, "https://idp.example.com/sessionuser");
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        a.request();
        Profile profile = spy(new Profile());
        stubText(profile);
        setAuthMethod(profile, AuthMethod.OIDC);
        User self = user(userName);
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        bean.setScreenName("Renamed");
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        profile.withServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save(), () -> String.valueOf(profile.getActionErrors()));
        TestUtils.endSession(true);

        assertEquals("Renamed", user(userName).getScreenName());
        assertEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedIn(userName);
        a.assertLoggedIn(userName);
    }

    @Test
    void adminEditOfAnOidcUserKeepsTheirSessions() throws Exception {
        useHashedPasswords();
        linkOpenId(userName, "https://idp.example.com/sessionuser");
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        admin.request();
        UserEdit action = userEdit(admin, userName);
        setAuthMethod(action, AuthMethod.DB_OIDC);
        action.getBean().setFullName("Renamed User");
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
        TestUtils.endSession(true);

        assertEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedIn(userName);
    }

    @Test
    void switchingToOidcReplacesTheStoredPassword() throws Exception {
        useHashedPasswords();
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        admin.request();
        UserEdit action = userEdit(admin, userName);
        setAuthMethod(action, AuthMethod.DB_OIDC);
        action.getBean().setOpenIdUrl("https://idp.example.com/sessionuser");
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
        TestUtils.endSession(true);

        assertNotEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedOut();
    }

    /**
     * The test configuration stores passwords in plain text, and a generated
     * 255-character password doesn't fit the column that way. tearDown puts
     * the configured encoder back.
     */
    private static void useHashedPasswords() throws Exception {
        swapEncoder(new DelegatingPasswordEncoder("pbkdf2",
                Map.of("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8())));
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
        profile.withServletRequest(a.httpRequest);
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

    @Test
    void passwordChangeEndsALoginThatHasNotOpenedTheUiYet() throws Exception {
        Browser a = new Browser(userName);
        // b logged in and went to a weblog page, so Roller has not looked at
        // its session yet
        Browser b = new Browser(userName);
        a.request();
        TestUtils.endSession(true);

        changeOwnPassword(a);

        b.assertLoggedOut();
        a.assertLoggedIn(userName);
    }

    @Test
    void passwordChangeEndsARememberMeLoginThatHasNotOpenedTheUiYet() throws Exception {
        Browser a = new Browser(userName);
        Browser b = Browser.remembered(userName);
        a.request();
        TestUtils.endSession(true);

        changeOwnPassword(a);

        b.assertLoggedOut();
        a.assertLoggedIn(userName);
    }

    @Test
    void passwordChangeEndsAnOidcLoginThatHasNotOpenedTheUiYet() throws Exception {
        String issuer = "https://idp.example.com";
        linkOpenId(userName, issuer + "#sessionuser");
        // a login whose principal name is the provider's subject, not the
        // Roller user name
        OidcUser oidcUser = mock(OidcUser.class);
        when(oidcUser.getIssuer()).thenReturn(URI.create(issuer).toURL());
        when(oidcUser.getSubject()).thenReturn("sessionuser");
        OAuth2AuthenticationToken token = mock(OAuth2AuthenticationToken.class);
        when(token.getName()).thenReturn("sessionuser");
        when(token.getPrincipal()).thenReturn(oidcUser);
        Browser admin = new Browser(adminName);
        Browser b = new Browser(token);
        admin.request();
        TestUtils.endSession(true);

        saveAsAdmin(admin, bean -> bean.setPassword("reset-password"));

        b.assertLoggedOut();
    }

    @Test
    void disablingAUserEndsALoginThatHasNotOpenedTheUiYet() throws Exception {
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        TestUtils.endSession(true);

        saveAsAdmin(admin, bean -> bean.setEnabled(false));

        b.assertLoggedOut();
        admin.assertLoggedIn(adminName);
    }

    @Test
    void disablingAUserEndsASessionThatWasPassivated() throws Exception {
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);
        b.passivate();

        saveAsAdmin(admin, bean -> bean.setEnabled(false));

        b.assertLoggedOut();
    }

    @Test
    void removingAUserEndsALoginThatHasNotOpenedTheUiYet() throws Exception {
        String goneName = TestUtils.setupUser("sessiongone").getUserName();
        TestUtils.endSession(true);
        Browser b = new Browser(goneName);

        TestUtils.teardownUser(goneName);
        TestUtils.endSession(true);

        b.assertLoggedOut();
    }

    @Test
    void reencodingThePasswordAtLoginKeepsTheUsersOtherSessions() throws Exception {
        Browser opened = new Browser(userName);
        Browser notOpened = new Browser(userName);
        Browser passivated = new Browser(userName);
        opened.request();
        passivated.request();
        TestUtils.endSession(true);
        passivated.passivate();

        reencodePasswordAtLogin();
        // the login whose password check led to the re-encoding
        Browser upgrading = new Browser(userName);

        opened.assertLoggedIn(userName);
        notOpened.assertLoggedIn(userName);
        passivated.assertLoggedIn(userName);
        upgrading.assertLoggedIn(userName);
    }

    @Test
    void passwordChangeAfterAReencodingEndsTheUsersOtherSessions() throws Exception {
        Browser opened = new Browser(userName);
        Browser notOpened = new Browser(userName);
        opened.request();
        TestUtils.endSession(true);

        reencodePasswordAtLogin();
        Browser upgrading = new Browser(userName);
        changeOwnPassword(upgrading);

        opened.assertLoggedOut();
        notOpened.assertLoggedOut();
        upgrading.assertLoggedIn(userName);
    }

    /**
     * Spring Security stores a new encoding of the user's unchanged password
     * after a login checked it against an older one.
     */
    private void reencodePasswordAtLogin() throws Exception {
        String stored = user(userName).getPassword();
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername(userName).password(stored).authorities(List.of()).build();
        String reencoded = "{bcrypt}" + new BCryptPasswordEncoder(4).encode("unchanged");
        new RollerUserDetailsService().updatePassword(details, reencoded);
        TestUtils.endSession(true);
        assertEquals(reencoded, user(userName).getPassword(), "the re-encoding should be stored");
    }

    @Test
    void loggingInAgainInAStaleSessionStaysLoggedIn() throws Exception {
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);

        changeOwnPassword(a);

        // b still has its old session and logs in again with the new
        // password before making any other request
        b.login();
        b.assertLoggedIn(userName);
        a.assertLoggedIn(userName);
    }

    @Test
    void loggingInAsAnotherUserInTheSameSessionSwitchesUser() throws Exception {
        Browser b = new Browser(userName);
        assertEquals(userName, b.request().getAuthenticatedUser().getUserName());
        TestUtils.endSession(true);

        b.login(UsernamePasswordAuthenticationToken.authenticated(adminName, null, List.of()));

        b.assertLoggedIn(adminName);
    }

    @Test
    void requestOnASessionEndedByAnotherRequestIsLoggedOut() throws Exception {
        Browser b = new Browser(userName);
        b.request();
        TestUtils.endSession(true);

        b.sessionEndsDuringNextRequest();

        b.assertLoggedOut();
    }

    @Test
    void endingASessionLogsOutItsParallelRequests() throws Exception {
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);

        changeOwnPassword(a);

        // Spring gives two requests of b the login context kept in b's session
        SecurityContext shared = SecurityContextHolder.createEmptyContext();
        shared.setAuthentication(b.login);
        HttpServletRequest parallel = b.requestWith(shared);

        // the first one ends the session
        SecurityContextHolder.setContext(shared);
        RollerSession.getRollerSession(b.httpRequest);

        // the second one asks for a session afterwards, as LoadSaltFilter does
        SecurityContextHolder.setContext(shared);
        parallel.getSession(true);
        RollerSession rollerSession = RollerSession.getRollerSession(parallel);

        assertNull(rollerSession.getAuthenticatedUser(), "the parallel request should have no user");
        assertNull(shared.getAuthentication(), "the login should be cleared for every request of the session");
        a.assertLoggedIn(userName);
    }

    @Test
    void ownRequestWhilePasswordIsSavedStaysLoggedIn() throws Exception {
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);

        // what Profile.save does, with another request of a in between the
        // commit and the end of the save
        a.request();
        User self = user(userName);
        self.resetPassword("changed-password");
        RollerSession.savingPassword(a.httpRequest, self);
        WebloggerFactory.getWeblogger().getUserManager().saveUser(self);
        WebloggerFactory.getWeblogger().flush();
        a.assertLoggedIn(userName);
        RollerSession.refreshPassword(a.httpRequest, self);
        TestUtils.endSession(true);

        a.assertLoggedIn(userName);
        b.assertLoggedOut();
    }

    @Test
    void savingAProfileOnASessionEndedByAnotherRequestDoesNotFail() throws Exception {
        Browser b = new Browser(userName);
        b.request();
        TestUtils.endSession(true);

        b.sessionEndsDuringNextRequest();

        assertDoesNotThrow(() -> RollerSession.refreshPassword(b.httpRequest, user(userName)));
    }

    @Test
    void oidcProfileEditKeepsSessionsWhenTheStoredUrlEndsWithASlash() throws Exception {
        useHashedPasswords();
        linkOpenId(userName, "https://idp.example.com/sessionuser/");
        Browser a = new Browser(userName);
        Browser b = new Browser(userName);
        a.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        a.request();
        Profile profile = spy(new Profile());
        stubText(profile);
        setAuthMethod(profile, AuthMethod.OIDC);
        User self = user(userName);
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        bean.setScreenName("Renamed");
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        profile.withServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save(), () -> String.valueOf(profile.getActionErrors()));
        TestUtils.endSession(true);

        assertEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedIn(userName);
        a.assertLoggedIn(userName);
    }

    @Test
    void adminEditKeepsSessionsWhenOnlyATrailingSlashIsAdded() throws Exception {
        useHashedPasswords();
        linkOpenId(userName, "https://idp.example.com/sessionuser");
        Browser admin = new Browser(adminName);
        Browser b = new Browser(userName);
        admin.request();
        b.request();
        TestUtils.endSession(true);
        String storedPassword = user(userName).getPassword();

        admin.request();
        UserEdit action = userEdit(admin, userName);
        setAuthMethod(action, AuthMethod.DB_OIDC);
        action.getBean().setOpenIdUrl("https://idp.example.com/sessionuser/");
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
        TestUtils.endSession(true);

        assertEquals(storedPassword, user(userName).getPassword());
        b.assertLoggedIn(userName);
    }

    /** The user of this browser changes their own password on the profile page. */
    private void changeOwnPassword(Browser a) throws Exception {
        a.request();
        Profile profile = spy(new Profile());
        stubText(profile);
        User self = user(a.login.getName());
        ProfileBean bean = new ProfileBean();
        bean.copyFrom(self);
        bean.setPasswordText("changed-password");
        bean.setPasswordConfirm("changed-password");
        profile.setBean(bean);
        profile.setAuthenticatedUser(self);
        profile.withServletRequest(a.httpRequest);
        assertEquals(Profile.SUCCESS, profile.save(), () -> String.valueOf(profile.getActionErrors()));
        TestUtils.endSession(true);
    }

    private void saveAsAdmin(Browser admin, java.util.function.Consumer<CreateUserBean> change)
            throws Exception {
        admin.request();
        UserEdit action = userEdit(admin, userName);
        change.accept(action.getBean());
        assertEquals(UserEdit.SUCCESS, action.save(), () -> String.valueOf(action.getActionErrors()));
        TestUtils.endSession(true);
    }

    private UserEdit userEdit(Browser admin, String target) throws Exception {
        UserEdit action = spy(new UserEdit());
        stubText(action);
        action.setActionName("modifyUser");
        action.setAuthenticatedUser(user(adminName));
        action.withServletRequest(admin.httpRequest);
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
        Authentication login;
        HttpSession session;
        // the session holding the login was invalidated
        boolean ended;
        // a session another request invalidated after this one picked it up
        HttpSession endedUnderneath;
        // attributes of the current request
        final Map<String, Object> requestAttributes = new HashMap<>();

        Browser(String name) {
            this(UsernamePasswordAuthenticationToken.authenticated(name, null, List.of()));
        }

        Browser(Authentication authentication) {
            this(authentication, false);
        }

        private Browser(Authentication authentication, boolean remembered) {
            session = newSession();
            // keep reporting the login, so a rebuild from it would show
            when(httpRequest.getUserPrincipal()).thenAnswer(call -> login);
            when(httpRequest.getSession(false)).thenAnswer(call -> {
                if (endedUnderneath != null) {
                    HttpSession stale = endedUnderneath;
                    endedUnderneath = null;
                    return stale;
                }
                return session;
            });
            when(httpRequest.getSession(true)).thenAnswer(call -> {
                if (session == null) {
                    session = newSession();
                }
                return session;
            });
            when(httpRequest.getSession()).thenAnswer(call -> httpRequest.getSession(true));
            doAnswer(call -> requestAttributes.put(call.getArgument(0), call.getArgument(1)))
                    .when(httpRequest).setAttribute(anyString(), any());
            when(httpRequest.getAttribute(anyString()))
                    .thenAnswer(call -> requestAttributes.get(call.<String>getArgument(0)));
            if (remembered) {
                rememberMeLogin(authentication.getName());
            } else {
                login(authentication);
            }
        }

        /** A browser whose remember-me cookie logged it in. */
        static Browser remembered(String name) {
            return new Browser(UsernamePasswordAuthenticationToken.authenticated(name, null, List.of()), true);
        }

        /** Logs in again with the same account, in the session it has. */
        void login() {
            login(login);
        }

        /** Spring authenticated a login form or an OIDC sign-in. */
        void login(Authentication authentication) {
            login = authentication;
            ended = false;
            try {
                new RollerLoginSuccessHandler((request, response, auth) -> { })
                        .onAuthenticationSuccess(httpRequest, mock(HttpServletResponse.class), login);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        /** Spring accepted the browser's remember-me cookie. */
        private void rememberMeLogin(String name) {
            UserDetails details = org.springframework.security.core.userdetails.User
                    .withUsername(name).password("stored-hash").authorities(List.of()).build();
            login = new RollerRememberMeServices(userName -> details) {
                Authentication accept() {
                    return createSuccessfulAuthentication(httpRequest, details);
                }
            }.accept();
        }

        /**
         * Another request of this browser, which reads its login from the
         * given security context, as Spring's request wrapper does.
         */
        HttpServletRequest requestWith(SecurityContext context) {
            HttpServletRequest other = mock(HttpServletRequest.class);
            when(other.getUserPrincipal()).thenAnswer(call -> context.getAuthentication());
            when(other.getSession(false)).thenAnswer(call -> httpRequest.getSession(false));
            when(other.getSession(true)).thenAnswer(call -> httpRequest.getSession(true));
            when(other.getSession()).thenAnswer(call -> httpRequest.getSession(true));
            return other;
        }

        /**
         * Another request of this browser ends the session just after the
         * next request has picked it up.
         */
        void sessionEndsDuringNextRequest() {
            HttpSession current = session;
            current.invalidate();
            endedUnderneath = current;
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
            when(s.getCreationTime()).thenAnswer(call -> {
                checkValid(valid);
                return 0L;
            });
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
            requestAttributes.clear();
            if (ended && endedUnderneath == null) {
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
            assertTrue(RollerSession.isSessionEnded(httpRequest), "the request should be marked as logged out");
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
            assertFalse(RollerSession.isSessionEnded(httpRequest));
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            SecurityContextHolder.clearContext();
        }
    }
}
