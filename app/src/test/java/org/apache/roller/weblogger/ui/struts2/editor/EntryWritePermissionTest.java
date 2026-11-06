/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.Collections;
import java.util.Locale;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The entry actions admit a Limited member, so each one has to ask the entry
 * whether that member may work on it.
 */
class EntryWritePermissionTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<CacheManager> cache;
    private WeblogEntryManager entries;
    private UserManager users;
    private Weblog weblog;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        entries = mock(WeblogEntryManager.class);
        users = mock(UserManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getWeblogEntryManager()).thenReturn(entries);
        when(weblogger.getUserManager()).thenReturn(users);
        when(weblogger.getIndexManager()).thenReturn(mock(IndexManager.class));
        factory = mockStatic(WebloggerFactory.class);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
        cache = mockStatic(CacheManager.class);

        weblog = mock(Weblog.class);
        user = mock(User.class);
        when(user.getUserName()).thenReturn("member");
    }

    @AfterEach
    void tearDown() {
        cache.close();
        factory.close();
    }

    @Test
    void limitedMemberCannotRemoveALiveEntry() throws Exception {
        memberHas(WeblogPermission.EDIT_DRAFT);
        for (PubStatus status : new PubStatus[]{PubStatus.PUBLISHED, PubStatus.SCHEDULED}) {
            EntryRemove action = removeAction(entry(status));

            assertEquals(UIAction.DENIED, action.execute());
            assertEquals(UIAction.DENIED, action.remove());
        }
        verify(entries, never()).removeWeblogEntry(any());
    }

    @Test
    void limitedMemberCanRemoveADraftOrPendingEntry() throws Exception {
        memberHas(WeblogPermission.EDIT_DRAFT);
        for (PubStatus status : new PubStatus[]{PubStatus.DRAFT, PubStatus.PENDING}) {
            assertEquals(UIAction.SUCCESS, removeAction(entry(status)).remove());
        }
        verify(entries, times(2)).removeWeblogEntry(any());
    }

    @Test
    void authorCanRemoveAPublishedEntry() throws Exception {
        memberHas(WeblogPermission.POST);
        WeblogEntry entry = entry(PubStatus.PUBLISHED);

        assertEquals(UIAction.SUCCESS, removeAction(entry).remove());
        verify(entries).removeWeblogEntry(entry);
    }

    @Test
    void removeLooksTheEntryUpAgainWhateverWasPosted() throws Exception {
        memberHas(WeblogPermission.EDIT_DRAFT);
        EntryRemove action = action(new EntryRemove());
        action.setRemoveEntry(entry(PubStatus.DRAFT));

        action.myPrepare();

        assertEquals(UIAction.ERROR, action.remove());
        verify(entries, never()).removeWeblogEntry(any());
    }

    @Test
    void limitedMemberCannotOpenOrSaveAPublishedEntry() throws Exception {
        memberHas(WeblogPermission.EDIT_DRAFT);
        WeblogEntry entry = entry(PubStatus.PUBLISHED);

        assertEquals(UIAction.DENIED, editAction(entry).execute());
        assertEquals(UIAction.DENIED, editAction(entry).firstSave());
        assertEquals(UIAction.DENIED, editAction(entry).saveDraft());
        assertEquals(UIAction.DENIED, editAction(entry).publish());

        assertEquals(PubStatus.PUBLISHED, entry.getStatus());
        verify(entries, never()).saveWeblogEntry(any());
    }

    @Test
    void failedValidationShowsNoEntryToALimitedMember() throws Exception {
        // Struts returns INPUT without calling saveDraft() or publish() when
        // a field fails validation; the page then shows getEntry().
        memberHas(WeblogPermission.EDIT_DRAFT);

        assertNull(editAction(entry(PubStatus.PUBLISHED)).getEntry());
        assertNull(editAction(entry(PubStatus.SCHEDULED)).getEntry());
    }

    @Test
    void limitedMemberCanOpenADraft() throws Exception {
        memberHas(WeblogPermission.EDIT_DRAFT);

        assertEquals(UIAction.INPUT, editAction(entry(PubStatus.DRAFT)).execute());
    }

    @Test
    void authorCanOpenAPublishedEntry() throws Exception {
        memberHas(WeblogPermission.POST);

        assertEquals(UIAction.INPUT, editAction(entry(PubStatus.PUBLISHED)).execute());
    }

    private void memberHas(String action) throws Exception {
        WeblogPermission permission = new WeblogPermission(
                weblog, user, Collections.singletonList(action));
        when(users.getWeblogPermission(weblog, user)).thenReturn(permission);
    }

    private WeblogEntry entry(PubStatus status) throws Exception {
        WeblogEntry entry = new WeblogEntry();
        entry.setId("entry-1");
        entry.setTitle("title");
        entry.setWebsite(weblog);
        entry.setStatus(status);
        entry.setCategory(mock(WeblogCategory.class));
        when(entries.getWeblogEntry(weblog, "entry-1")).thenReturn(entry);
        return entry;
    }

    private EntryRemove removeAction(WeblogEntry entry) {
        EntryRemove action = action(new EntryRemove());
        action.setRemoveId(entry.getId());
        action.myPrepare();
        return action;
    }

    private EntryEdit editAction(WeblogEntry entry) {
        EntryEdit action = action(new EntryEdit());
        action.setActionName("entryEdit");
        action.getBean().setId(entry.getId());
        doReturn(Locale.ENGLISH).when(action).getLocale();
        action.myPrepare();
        return action;
    }

    private <T extends UIAction> T action(T instance) {
        T action = spy(instance);
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(action)
                .getText(anyString(), anyString(), anyString());
        action.setActionWeblog(weblog);
        action.setAuthenticatedUser(user);
        return action;
    }
}
