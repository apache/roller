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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals(List.of("yourWebsites.declineMember"), new ArrayList<>(menu.getActionErrors()));
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
    void disabledAdministratorDoesNotCountAsAnotherAdministrator() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.ADMIN));
        User member = user(memberName);
        member.setEnabled(Boolean.FALSE);
        users().saveUser(member);
        TestUtils.endSession(true);

        MemberResign action = resign(ownerName);

        assertTrue(action.hasActionErrors());
        assertNotNull(permission(ownerName), "a disabled administrator can't manage the weblog");
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

    @Test
    void membershipUpdateRefusesToRemoveTheLastAdministrator() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        assertFalse(users().updateWeblogMembership(weblog(),
                Collections.singletonMap(ownerName, null)));
        TestUtils.endSession(true);
        assertNotNull(permission(ownerName));

        assertFalse(users().updateWeblogMembership(weblog(),
                Map.of(ownerName, List.of(WeblogPermission.POST))), "downgrading the last administrator");
        TestUtils.endSession(true);
        assertTrue(permission(ownerName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void membershipUpdateAppliesChangesThatKeepAnAdministrator() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        // promote the member and remove the owner in one update
        Map<String, List<String>> changes = new java.util.HashMap<>();
        changes.put(memberName, List.of(WeblogPermission.ADMIN));
        changes.put(ownerName, null);
        assertTrue(users().updateWeblogMembership(weblog(), changes));
        TestUtils.endSession(true);

        assertNull(permission(ownerName));
        assertTrue(permission(memberName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void membersPageChangesMembershipThroughTheSharedUpdate() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        Members action = spy(new Members());
        stubText(action);
        action.setAuthenticatedUser(user(ownerName));
        action.setActionWeblog(weblog());
        doAnswer(call -> null).when(action).getParameter(anyString());
        doAnswer(call -> WeblogPermission.ADMIN).when(action).getParameter("perm-" + permission(ownerName).getId());
        doAnswer(call -> WeblogPermission.ADMIN).when(action).getParameter("perm-" + permission(memberName).getId());
        action.save();
        TestUtils.endSession(true);

        assertFalse(action.hasActionErrors());
        assertTrue(permission(memberName).hasAction(WeblogPermission.ADMIN));
        assertTrue(permission(ownerName).hasAction(WeblogPermission.ADMIN));
    }

    @Test
    void disabledMemberCanBeRemoved() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        disable(memberName);

        assertTrue(users().updateWeblogMembership(weblog(),
                Collections.singletonMap(memberName, null)));
        TestUtils.endSession(true);

        assertNull(permission(memberName));
    }

    @Test
    void disabledMemberCanBeDemoted() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        disable(memberName);

        assertTrue(users().updateWeblogMembership(weblog(),
                Map.of(memberName, List.of(WeblogPermission.EDIT_DRAFT))));
        TestUtils.endSession(true);

        assertEquals(List.of(WeblogPermission.EDIT_DRAFT), permission(memberName).getActionsAsList());
    }

    @Test
    void changingAPendingInvitationKeepsItPending() throws Exception {
        users().grantWeblogPermissionPending(weblog(), user(memberName),
                List.of(WeblogPermission.POST));
        TestUtils.endSession(true);

        assertTrue(users().updateWeblogMembership(weblog(),
                Map.of(memberName, List.of(WeblogPermission.ADMIN))));
        TestUtils.endSession(true);

        WeblogPermission invitation = permission(memberName);
        assertEquals(List.of(WeblogPermission.ADMIN), invitation.getActionsAsList());
        assertTrue(invitation.isPending(), "the invitation has not been accepted");
    }

    @Test
    void nonMemberCannotResign() throws Exception {
        MemberResign action = resign(memberName);

        assertTrue(action.hasActionErrors());
        assertFalse(action.hasActionMessages(), "nobody resigned");
    }

    @Test
    void membershipUpdateRejectsAUserWithoutAMembership() throws Exception {
        Map<String, List<String>> changes = new java.util.HashMap<>();
        changes.put(ownerName, List.of(WeblogPermission.POST));
        changes.put(memberName, List.of(WeblogPermission.ADMIN));

        assertThrows(WebloggerException.class,
                () -> users().updateWeblogMembership(weblog(), changes));
        TestUtils.endSession(false);

        assertTrue(permission(ownerName).hasAction(WeblogPermission.ADMIN), "nothing was changed");
        assertNull(permission(memberName));
    }

    @Test
    void membershipUpdateForANonMemberDoesNotWaitForAnotherUpdate() throws Exception {
        CountDownLatch updating = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            // an update that has started and not yet finished
            Future<?> update = pool.submit(() -> {
                try {
                    users().updateWeblogMembership(weblog(),
                            Map.of(ownerName, List.of(WeblogPermission.ADMIN)));
                    updating.countDown();
                    return finish.await(60, TimeUnit.SECONDS);
                } finally {
                    WebloggerFactory.getWeblogger().release();
                }
            });
            assertTrue(updating.await(30, TimeUnit.SECONDS));

            Future<?> refused = pool.submit(() -> {
                try {
                    return assertThrows(WebloggerException.class, () -> users().updateWeblogMembership(
                            weblog(), Collections.singletonMap(memberName, null)));
                } finally {
                    WebloggerFactory.getWeblogger().release();
                }
            });
            refused.get(10, TimeUnit.SECONDS);
            finish.countDown();
            update.get(30, TimeUnit.SECONDS);
        } finally {
            finish.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void membersPageReportsNoChangesWhenSavingFails() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        TestUtils.endSession(true);
        // a weblog whose memberships can be read but not changed
        Weblog unsaved = new Weblog();
        unsaved.setId("membershiptest-unsaved");
        unsaved.setHandle(handle);

        Members action = spy(new Members());
        stubText(action);
        action.setAuthenticatedUser(user(ownerName));
        action.setActionWeblog(unsaved);
        doAnswer(call -> null).when(action).getParameter(anyString());
        doAnswer(call -> WeblogPermission.ADMIN).when(action).getParameter("perm-" + permission(ownerName).getId());
        doAnswer(call -> "-1").when(action).getParameter("perm-" + permission(memberName).getId());
        action.save();
        TestUtils.endSession(false);

        assertTrue(action.hasActionErrors());
        assertFalse(action.hasActionMessages(), () -> action.getActionMessages().toString());
        assertNotNull(permission(memberName), "nothing was saved");
    }

    @Test
    void membersPageSavesWhenAMemberIsDisabled() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        disable(memberName);

        Members action = spy(new Members());
        stubText(action);
        action.setAuthenticatedUser(user(ownerName));
        action.setActionWeblog(weblog());
        doAnswer(call -> null).when(action).getParameter(anyString());
        doAnswer(call -> WeblogPermission.ADMIN).when(action).getParameter("perm-" + permission(ownerName).getId());
        action.save();
        TestUtils.endSession(true);

        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
        assertTrue(permission(memberName).hasAction(WeblogPermission.POST));
    }

    @Test
    void membersPageRemovesADisabledMember() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        disable(memberName);

        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, "-1"));
        TestUtils.endSession(true);

        assertNull(permission(memberName), "the disabled member was removed");
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
    }

    @Test
    void membersPageChangesADisabledMembersRole() throws Exception {
        users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.POST));
        disable(memberName);

        Members action = membersSaving(Map.of(
                ownerName, WeblogPermission.ADMIN,
                memberName, WeblogPermission.EDIT_DRAFT));
        TestUtils.endSession(true);

        assertEquals(List.of(WeblogPermission.EDIT_DRAFT), permission(memberName).getActionsAsList());
        assertFalse(action.hasActionErrors(), () -> action.getActionErrors().toString());
    }

    @Test
    void concurrentResignationsLeaveAnAdministrator() throws Exception {
        for (int round = 0; round < 5; round++) {
            for (String name : List.of(ownerName, memberName)) {
                if (permission(name) != null) {
                    users().revokeWeblogPermission(weblog(), user(name), WeblogPermission.ALL_ACTIONS);
                }
            }
            TestUtils.endSession(true);
            users().grantWeblogPermission(weblog(), user(ownerName), List.of(WeblogPermission.ADMIN));
            users().grantWeblogPermission(weblog(), user(memberName), List.of(WeblogPermission.ADMIN));
            TestUtils.endSession(true);

            CyclicBarrier start = new CyclicBarrier(2);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                List<Future<Boolean>> results = new ArrayList<>();
                for (String name : List.of(ownerName, memberName)) {
                    results.add(pool.submit(() -> {
                        try {
                            MemberResign action = spy(new MemberResign());
                            stubText(action);
                            action.setAuthenticatedUser(user(name));
                            action.setActionWeblog(weblog());
                            action.setWeblog(handle);
                            start.await(10, TimeUnit.SECONDS);
                            action.resign();
                            return !action.hasActionErrors();
                        } finally {
                            WebloggerFactory.getWeblogger().release();
                        }
                    }));
                }
                int resigned = 0;
                for (Future<Boolean> result : results) {
                    if (result.get(60, TimeUnit.SECONDS)) {
                        resigned++;
                    }
                }
                assertEquals(1, resigned, "only one of the two administrators can resign");
            } finally {
                pool.shutdownNow();
            }
            TestUtils.endSession(true);
            int admins = 0;
            for (WeblogPermission perm : users().getWeblogPermissions(weblog())) {
                if (perm.hasAction(WeblogPermission.ADMIN)) {
                    admins++;
                }
            }
            assertEquals(1, admins, "the weblog keeps an administrator");
        }
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

    // saves the Members page with a choice for each named member, submitted
    // in the field the page renders for that member's permission
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
        return action;
    }

    private static void disable(String name) throws Exception {
        User user = user(name);
        user.setEnabled(Boolean.FALSE);
        users().saveUser(user);
        TestUtils.endSession(true);
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
