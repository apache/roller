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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.List;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.core.MainMenu;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * Declining an invitation removes only a pending invitation, and a weblog's
 * last administrator cannot resign from it.
 */
class WeblogMembershipTest {

    private String ownerName;
    private String memberName;
    private String weblogId;
    private String handle;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        User owner = TestUtils.setupUser("membershipowner");
        User member = TestUtils.setupUser("membershipmember");
        ownerName = owner.getUserName();
        memberName = member.getUserName();
        // the creator becomes the weblog's administrator
        Weblog weblog = TestUtils.setupWeblog("membershiptest", owner);
        weblogId = weblog.getId();
        handle = weblog.getHandle();
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        // removing a weblog leaves pending invitations, which would carry over
        // to the next test's weblog of the same handle
        for (String name : List.of(ownerName, memberName)) {
            if (permission(name) != null) {
                users().revokeWeblogPermission(weblog(), user(name), WeblogPermission.ALL_ACTIONS);
            }
        }
        TestUtils.endSession(true);
        TestUtils.teardownWeblog(weblogId);
        TestUtils.teardownUser(ownerName);
        TestUtils.teardownUser(memberName);
        TestUtils.endSession(true);
    }

    @Test
    void decliningAPendingInvitationRemovesIt() throws Exception {
        users().grantWeblogPermissionPending(weblog(), user(memberName),
                List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        decline(memberName);

        assertNull(permission(memberName));
    }

    @Test
    void decliningKeepsAnAcceptedMembership() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        MainMenu menu = decline(memberName);

        assertNotNull(permission(memberName), "an accepted membership is not an invitation");
        assertTrue(menu.hasActionErrors());
    }

    @Test
    void soleAdministratorCannotDeclineTheirOwnMembership() throws Exception {
        decline(ownerName);

        assertNotNull(permission(ownerName));
    }

    @Test
    void lastAdministratorCannotResign() throws Exception {
        MemberResign action = resign(ownerName);

        assertTrue(action.hasActionErrors());
        assertNotNull(permission(ownerName), "the weblog keeps its last administrator");
    }

    @Test
    void pendingAdministratorDoesNotCountAsAnotherAdministrator() throws Exception {
        users().grantWeblogPermissionPending(weblog(), user(memberName),
                List.of(WeblogPermission.ADMIN));
        TestUtils.endSession(true);

        MemberResign action = resign(ownerName);

        assertTrue(action.hasActionErrors());
        assertNotNull(permission(ownerName));
    }

    @Test
    void administratorCanResignWhenAnotherRemains() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.ADMIN));
        TestUtils.endSession(true);

        MemberResign action = resign(ownerName);

        assertFalse(action.hasActionErrors());
        assertNull(permission(ownerName));
    }

    @Test
    void memberCanResign() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        MemberResign action = resign(memberName);

        assertFalse(action.hasActionErrors());
        assertNull(permission(memberName));
        assertNotNull(permission(ownerName));
    }

    private MainMenu decline(String name) throws Exception {
        MainMenu menu = spy(new MainMenu());
        stubText(menu);
        menu.setAuthenticatedUser(user(name));
        menu.setInviteId(weblogId);
        menu.decline();
        TestUtils.endSession(true);
        return menu;
    }

    private MemberResign resign(String name) throws Exception {
        MemberResign action = spy(new MemberResign());
        stubText(action);
        action.setAuthenticatedUser(user(name));
        action.setActionWeblog(weblog());
        action.setWeblog(handle);
        action.resign();
        TestUtils.endSession(true);
        return action;
    }

    private WeblogPermission permission(String name) throws Exception {
        return users().getWeblogPermissionIncludingPending(weblog(), user(name));
    }

    private static void stubText(UIAction action) {
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(action)
                .getText(anyString(), anyString(), anyString());
    }

    private static UserManager users() {
        return WebloggerFactory.getWeblogger().getUserManager();
    }

    private Weblog weblog() throws Exception {
        return WebloggerFactory.getWeblogger().getWeblogManager().getWeblog(weblogId);
    }

    private static User user(String name) throws Exception {
        return users().getUserByUserName(name, null);
    }
}
