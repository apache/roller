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
import java.util.Map;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * The aggregated front-page weblog's templates read the whole site, so only
 * global admins decide who is Admin there.
 */
class FrontPageAdminMembershipTest {

    private static final String LOCKED = "memberPermissions.frontPageAdminsGlobalOnly";

    private String ownerName;
    private String memberName;
    private String weblogId;
    private String handle;
    private String savedHandle;
    private String savedAggregated;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        User owner = TestUtils.setupUser("frontpageowner");
        User member = TestUtils.setupUser("frontpagemember");
        ownerName = owner.getUserName();
        memberName = member.getUserName();
        Weblog weblog = TestUtils.setupWeblog("frontpageadmins", owner);
        weblogId = weblog.getId();
        handle = weblog.getHandle();
        users().grantWeblogPermission(weblog, member, List.of(WeblogPermission.POST));
        // the weblog's Admin is not a global admin
        if (users().hasRole("admin", owner)) {
            users().revokeRole("admin", owner);
        }
        TestUtils.endSession(true);
        savedHandle = setProperty("site.frontpage.weblog.handle", handle);
        savedAggregated = setProperty("site.frontpage.weblog.aggregated", "true");
    }

    @AfterEach
    void tearDown() throws Exception {
        setProperty("site.frontpage.weblog.handle", savedHandle);
        setProperty("site.frontpage.weblog.aggregated", savedAggregated);
        TestUtils.teardownWeblog(weblogId);
        TestUtils.teardownUser(ownerName);
        TestUtils.teardownUser(memberName);
        TestUtils.endSession(true);
    }

    @Test
    void weblogAdminCannotPromoteAMember() throws Exception {
        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, WeblogPermission.ADMIN));
        assertEquals(List.of(LOCKED), List.copyOf(action.getActionErrors()));
        assertEquals(List.of(WeblogPermission.POST), permission(memberName).getActionsAsList());
    }

    @Test
    void weblogAdminCannotRemoveAnotherAdmin() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.ADMIN));
        TestUtils.endSession(true);
        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, "-1"));
        assertEquals(List.of(LOCKED), List.copyOf(action.getActionErrors()));
        assertTrue(permission(memberName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void weblogAdminCanStillChangeOtherRoles() throws Exception {
        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, WeblogPermission.EDIT_DRAFT));
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
        assertEquals(List.of(WeblogPermission.EDIT_DRAFT), permission(memberName).getActionsAsList());
    }

    @Test
    void globalAdminCanPromoteAMember() throws Exception {
        users().grantRole("admin", user(ownerName));
        TestUtils.endSession(true);
        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, WeblogPermission.ADMIN));
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
        assertTrue(permission(memberName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void withoutAggregationTheWeblogAdminCanPromote() throws Exception {
        setProperty("site.frontpage.weblog.aggregated", "false");
        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, WeblogPermission.ADMIN));
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
        assertTrue(permission(memberName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void weblogAdminCannotInviteAnAdmin() throws Exception {
        User invitee = TestUtils.setupUser("frontpageinvitee");
        TestUtils.endSession(true);
        try {
            MembersInvite action = inviting(invitee.getUserName(), WeblogPermission.ADMIN);
            assertEquals(List.of(LOCKED), List.copyOf(action.getActionErrors()));
            assertNull(permission(invitee.getUserName()));

            MembersInvite author = inviting(invitee.getUserName(), WeblogPermission.POST);
            assertFalse(author.hasActionErrors(), () -> author.getActionErrors().toString());
        } finally {
            // remove the invitation first, so no permission is left without its user
            if (permission(invitee.getUserName()) != null) {
                users().revokeWeblogPermission(weblog(), user(invitee.getUserName()),
                        WeblogPermission.ALL_ACTIONS);
                TestUtils.endSession(true);
            }
            TestUtils.teardownUser(invitee.getUserName());
            TestUtils.endSession(true);
        }
    }

    @Test
    void weblogAdminCannotInviteAnAdminAlongsideAnotherRole() throws Exception {
        assertInvitationRefused(true, "admin,post", "post,admin", "admin,", ",admin");
    }

    @Test
    void anInvitationOffersOnlyOneKnownRole() throws Exception {
        setProperty("site.frontpage.weblog.aggregated", "false");
        assertInvitationRefused(false, "admin,post", "editor", "", null);
    }

    @Test
    void weblogAdminCannotResignAdmin() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.ADMIN));
        TestUtils.endSession(true);
        MemberResign action = resigning(memberName);
        assertEquals(List.of(LOCKED), List.copyOf(action.getActionErrors()));
        assertTrue(permission(memberName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void authorCanStillResign() throws Exception {
        MemberResign action = resigning(memberName);
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
        assertNull(permission(memberName));
    }

    @Test
    void withoutAggregationAnAdminCanResign() throws Exception {
        setProperty("site.frontpage.weblog.aggregated", "false");
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.ADMIN));
        TestUtils.endSession(true);
        MemberResign action = resigning(memberName);
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
        assertNull(permission(memberName));
    }

    private void assertInvitationRefused(boolean locked, String... permissions) throws Exception {
        User invitee = TestUtils.setupUser("frontpageinvitee");
        TestUtils.endSession(true);
        try {
            for (String permission : permissions) {
                MembersInvite action = inviting(invitee.getUserName(), permission);
                assertEquals(List.of("inviteMember.error.unknownPermission"),
                        List.copyOf(action.getActionErrors()), String.valueOf(permission));
                assertNull(permission(invitee.getUserName()), String.valueOf(permission));
            }
            if (locked) {
                MembersInvite admin = inviting(invitee.getUserName(), WeblogPermission.ADMIN);
                assertEquals(List.of(LOCKED), List.copyOf(admin.getActionErrors()));
            }
        } finally {
            if (permission(invitee.getUserName()) != null) {
                users().revokeWeblogPermission(weblog(), user(invitee.getUserName()),
                        WeblogPermission.ALL_ACTIONS);
                TestUtils.endSession(true);
            }
            TestUtils.teardownUser(invitee.getUserName());
            TestUtils.endSession(true);
        }
    }

    private MemberResign resigning(String name) throws Exception {
        MemberResign action = spy(new MemberResign());
        stubText(action);
        action.setAuthenticatedUser(user(name));
        action.setActionWeblog(weblog());
        action.setWeblog(handle);
        action.resign();
        TestUtils.endSession(true);
        return action;
    }

    private MembersInvite inviting(String name, String permission) throws Exception {
        MembersInvite action = spy(new MembersInvite());
        stubText(action);
        action.setAuthenticatedUser(user(ownerName));
        action.setActionWeblog(weblog());
        action.setUserName(name);
        action.setPermissionString(permission);
        action.save();
        TestUtils.endSession(true);
        return action;
    }

    private Members membersSaving(Map<String, String> choices) throws Exception {
        Members action = spy(new Members());
        stubText(action);
        action.setAuthenticatedUser(user(ownerName));
        action.setActionWeblog(weblog());
        doAnswer(call -> null).when(action).getParameter(anyString());
        for (Map.Entry<String, String> choice : choices.entrySet()) {
            doAnswer(call -> choice.getValue()).when(action)
                    .getParameter("perm-" + permission(choice.getKey()).getId());
        }
        action.save();
        TestUtils.endSession(true);
        return action;
    }

    private static String setProperty(String name, String value) throws Exception {
        PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty prop = mgr.getProperty(name);
        String old = prop.getValue();
        prop.setValue(value);
        mgr.saveProperty(prop);
        TestUtils.endSession(true);
        return old;
    }

    private static void stubText(UIAction action) {
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(action)
                .getText(anyString(), anyString(), anyString());
    }

    private WeblogPermission permission(String name) throws Exception {
        return users().getWeblogPermissionIncludingPending(weblog(), user(name));
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
