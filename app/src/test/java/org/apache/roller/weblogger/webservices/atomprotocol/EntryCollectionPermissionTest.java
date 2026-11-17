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
package org.apache.roller.weblogger.webservices.atomprotocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.runnable.ScheduledEntriesTask;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Entry status, tag counts and author details as seen through
 * {@link EntryCollection} by weblog members with different permissions.
 */
public class EntryCollectionPermissionTest {

    private static final String HANDLE = "atompermblog";
    private static final String ATOM_URL = "http://localhost/roller/roller-services/app";
    private static final long DAY_MS = 24L * 60 * 60 * 1000;

    private User owner;
    private User author;
    private User limited;
    private Weblog weblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        owner = TestUtils.setupUser("atompermowner");
        author = TestUtils.setupUser("atompermauthor");
        limited = TestUtils.setupUser("atompermlimited");
        UserManager userManager = WebloggerFactory.getWeblogger().getUserManager();
        for (User user : List.of(owner, author, limited)) {
            if (userManager.hasRole("admin", user)) {
                userManager.revokeRole("admin", user);
            }
        }
        TestUtils.endSession(true);
        weblog = TestUtils.setupWeblog(HANDLE, owner);
        userManager = WebloggerFactory.getWeblogger().getUserManager();
        userManager.grantWeblogPermission(weblog, author,
                List.of(WeblogPermission.POST));
        userManager.grantWeblogPermission(weblog, limited,
                List.of(WeblogPermission.EDIT_DRAFT));

        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty enableAtomPub = pmgr.getProperty("webservices.enableAtomPub");
        enableAtomPub.setValue("true");
        pmgr.saveProperty(enableAtomPub);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(owner.getUserName());
        TestUtils.teardownUser(author.getUserName());
        TestUtils.teardownUser(limited.getUserName());
        TestUtils.endSession(true);
    }

    private AtomRequest request(String pathInfo) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getPathInfo()).thenReturn(pathInfo);
        lenient().when(req.getContentType()).thenReturn("application/atom+xml");
        return new AtomRequest(req, null);
    }

    private AtomEntry atomEntry(String title, boolean draft, Date published) {
        AtomEntry entry = new AtomEntry();
        entry.setTitle(title);
        AtomContent content = new AtomContent();
        content.setType("html");
        content.setValue("<p>" + title + "</p>");
        entry.setContent(content);
        entry.setDraft(draft);
        entry.setPublished(published);
        AtomCategory tag = new AtomCategory();
        tag.setTerm("scheduledtag");
        entry.getCategories().add(tag);
        return entry;
    }

    private WeblogEntry ownerEntry(String anchor, PubStatus status) throws Exception {
        WeblogEntry entry = TestUtils.setupWeblogEntry(anchor,
                TestUtils.getManagedWebsite(weblog).getWeblogCategories().iterator().next(),
                status, TestUtils.getManagedWebsite(weblog), TestUtils.getManagedUser(owner));
        String id = entry.getId();
        TestUtils.endSession(true);
        return WebloggerFactory.getWeblogger().getWeblogEntryManager().getWeblogEntry(id);
    }

    private WeblogEntry reload(String id) throws Exception {
        TestUtils.endSession(true);
        return WebloggerFactory.getWeblogger().getWeblogEntryManager().getWeblogEntry(id);
    }

    private int tagCount(String name) throws Exception {
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        List<TagStat> tags = mgr.getTags(TestUtils.getManagedWebsite(weblog), null, null, 0, -1);
        return tags.stream().filter(t -> name.equals(t.getName()))
                .mapToInt(TagStat::getCount).sum();
    }

    @Test
    public void limitedMemberCannotPublishADraftOrPendingEntry() throws Exception {
        for (PubStatus status : new PubStatus[] {PubStatus.DRAFT, PubStatus.PENDING}) {
            WeblogEntry entry = ownerEntry("limited-" + status.name().toLowerCase(), status);
            String id = entry.getId();
            String path = "/" + HANDLE + "/entry/" + id;

            EntryCollection limitedCol = new EntryCollection(
                    TestUtils.getManagedUser(limited), ATOM_URL);
            assertThrows(AtomNotAuthorizedException.class,
                    () -> limitedCol.putEntry(request(path), atomEntry("published", false, null)));
            assertEquals(status, reload(id).getStatus());

            // a Limited member may still edit the entry as a draft
            new EntryCollection(TestUtils.getManagedUser(limited), ATOM_URL)
                    .putEntry(request(path), atomEntry("still a draft", true, null));
            WeblogEntry edited = reload(id);
            assertEquals("still a draft", edited.getTitle());
            assertEquals(PubStatus.DRAFT, edited.getStatus());

            // an author may publish it
            new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL)
                    .putEntry(request(path), atomEntry("published", false, null));
            assertEquals(PubStatus.PUBLISHED, reload(id).getStatus());
        }
    }

    @Test
    public void scheduledEntryTagsAreCountedOnceWhenTheEntryGoesLive() throws Exception {
        EntryCollection ecol = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL);
        AtomEntry created = ecol.postEntry(request("/" + HANDLE + "/entries"),
                atomEntry("future", false, new Date(System.currentTimeMillis() + DAY_MS)));
        String edit = created.getLinkHref("edit");
        String id = edit.substring(edit.lastIndexOf("/entry/") + "/entry/".length());

        assertEquals(PubStatus.SCHEDULED, reload(id).getStatus());
        assertEquals(0, tagCount("scheduledtag"));

        goLive(id);
        assertEquals(PubStatus.PUBLISHED, reload(id).getStatus());
        assertEquals(1, tagCount("scheduledtag"));
    }

    @Test
    public void republishingAnEntryInTheFutureRemovesItsTagsUntilItGoesLive() throws Exception {
        EntryCollection ecol = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL);
        AtomEntry created = ecol.postEntry(request("/" + HANDLE + "/entries"),
                atomEntry("now", false, null));
        String edit = created.getLinkHref("edit");
        String id = edit.substring(edit.lastIndexOf("/entry/") + "/entry/".length());
        TestUtils.endSession(true);
        assertEquals(1, tagCount("scheduledtag"));

        new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL).putEntry(
                request("/" + HANDLE + "/entry/" + id),
                atomEntry("later", false, new Date(System.currentTimeMillis() + DAY_MS)));
        assertEquals(PubStatus.SCHEDULED, reload(id).getStatus());
        assertEquals(0, tagCount("scheduledtag"));

        goLive(id);
        assertEquals(1, tagCount("scheduledtag"));
    }

    private void goLive(String id) throws Exception {
        // move the publish time into the past, then let the scheduler promote it
        WeblogEntry entry = reload(id);
        entry.setPubTime(new java.sql.Timestamp(System.currentTimeMillis() - DAY_MS));
        TestUtils.endSession(true);
        new ScheduledEntriesTask().runTask();
        TestUtils.endSession(true);
    }

    @Test
    public void entryAuthorEmailIsShownOnlyToTheCreator() throws Exception {
        WeblogEntry entry = ownerEntry("owner-entry", PubStatus.PUBLISHED);
        String path = "/" + HANDLE + "/entry/" + entry.getId();

        AtomEntry seenByAuthor = new EntryCollection(
                TestUtils.getManagedUser(author), ATOM_URL).getEntry(request(path));
        assertEquals(owner.getUserName(), seenByAuthor.getAuthors().get(0).getName());
        assertNull(seenByAuthor.getAuthors().get(0).getEmail());

        AtomFeed feed = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL)
                .getCollection(request("/" + HANDLE + "/entries"));
        assertNotNull(feed.getEntries());
        feed.getEntries().forEach(e -> assertNull(e.getAuthors().get(0).getEmail()));

        AtomEntry seenByOwner = new EntryCollection(
                TestUtils.getManagedUser(owner), ATOM_URL).getEntry(request(path));
        assertEquals("TestUser@dev.null", seenByOwner.getAuthors().get(0).getEmail());
    }
}
