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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.rometools.propono.atom.common.rome.AppModule;
import com.rometools.propono.atom.common.rome.AppModuleImpl;
import com.rometools.propono.atom.server.AtomNotAuthorizedException;
import com.rometools.propono.atom.server.AtomRequest;
import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.synd.SyndPerson;

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
        AtomRequest req = mock(AtomRequest.class);
        lenient().when(req.getPathInfo()).thenReturn(pathInfo);
        lenient().when(req.getContentType()).thenReturn("application/atom+xml");
        return req;
    }

    private Entry atomEntry(String title, boolean draft, Date published) {
        Entry entry = new Entry();
        entry.setTitle(title);
        Content content = new Content();
        content.setType("html");
        content.setValue("<p>" + title + "</p>");
        entry.setContents(Collections.singletonList(content));
        AppModule control = new AppModuleImpl();
        control.setDraft(draft);
        List<Module> modules = new ArrayList<>();
        modules.add(control);
        entry.setModules(modules);
        entry.setPublished(published);
        Category tag = new Category();
        tag.setTerm("scheduledtag");
        List<Category> categories = new ArrayList<>();
        categories.add(tag);
        entry.setCategories(categories);
        return entry;
    }

    private static String editId(Entry entry) {
        for (Link link : entry.getOtherLinks()) {
            if ("edit".equals(link.getRel())) {
                String edit = link.getHref();
                return edit.substring(edit.lastIndexOf("/entry/") + "/entry/".length());
            }
        }
        throw new AssertionError("no edit link");
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
        Entry created = ecol.postEntry(request("/" + HANDLE + "/entries"),
                atomEntry("future", false, new Date(System.currentTimeMillis() + DAY_MS)));
        String id = editId(created);

        assertEquals(PubStatus.SCHEDULED, reload(id).getStatus());
        assertEquals(0, tagCount("scheduledtag"));

        goLive(id);
        assertEquals(PubStatus.PUBLISHED, reload(id).getStatus());
        assertEquals(1, tagCount("scheduledtag"));
    }

    @Test
    public void republishingAnEntryInTheFutureRemovesItsTagsUntilItGoesLive() throws Exception {
        EntryCollection ecol = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL);
        Entry created = ecol.postEntry(request("/" + HANDLE + "/entries"),
                atomEntry("now", false, null));
        String id = editId(created);
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

    private Entry atomEntry(String title, boolean draft, Date published,
            String... tags) {
        Entry entry = atomEntry(title, draft, published);
        List<Category> categories = new ArrayList<>();
        for (String name : tags) {
            Category tag = new Category();
            tag.setTerm(name);
            categories.add(tag);
        }
        entry.setCategories(categories);
        return entry;
    }

    private String post(Entry entry) throws Exception {
        Entry created = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL)
                .postEntry(request("/" + HANDLE + "/entries"), entry);
        TestUtils.endSession(true);
        return editId(created);
    }

    private void put(String id, Entry entry) throws Exception {
        new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL)
                .putEntry(request("/" + HANDLE + "/entry/" + id), entry);
        TestUtils.endSession(true);
    }

    @Test
    public void unpublishingAnEntryWhileRemovingATagUncountsAllItsOldTags() throws Exception {
        String id = post(atomEntry("now", false, null, "keeptag", "droptag"));
        assertEquals(1, tagCount("keeptag"));
        assertEquals(1, tagCount("droptag"));

        put(id, atomEntry("draft", true, null, "keeptag"));
        assertEquals(PubStatus.DRAFT, reload(id).getStatus());
        assertEquals(0, tagCount("keeptag"));
        assertEquals(0, tagCount("droptag"));

        put(id, atomEntry("again", false, null, "keeptag"));
        assertEquals(1, tagCount("keeptag"));
        assertEquals(0, tagCount("droptag"));
    }

    @Test
    public void savingAsDraftWhileChangingTagsUncountsTheOldTags() throws Exception {
        String id = post(atomEntry("now", false, null, "atag"));
        assertEquals(1, tagCount("atag"));

        put(id, atomEntry("draft", true, null, "btag"));
        assertEquals(PubStatus.DRAFT, reload(id).getStatus());
        assertEquals(0, tagCount("atag"));
        assertEquals(0, tagCount("btag"));
    }

    @Test
    public void schedulingAnEntryWhileChangingItsTagsUncountsTheOldTags() throws Exception {
        String id = post(atomEntry("now", false, null, "oldtag"));
        assertEquals(1, tagCount("oldtag"));

        put(id, atomEntry("later", false,
                new Date(System.currentTimeMillis() + DAY_MS), "newtag"));
        assertEquals(PubStatus.SCHEDULED, reload(id).getStatus());
        assertEquals(0, tagCount("oldtag"));
        assertEquals(0, tagCount("newtag"));

        goLive(id);
        assertEquals(0, tagCount("oldtag"));
        assertEquals(1, tagCount("newtag"));
    }

    @Test
    public void publishingADraftWhileChangingItsTagsCountsOnlyTheNewTags() throws Exception {
        String id = post(atomEntry("draft", true, null, "drafttag"));
        assertEquals(0, tagCount("drafttag"));

        put(id, atomEntry("now", false, null, "livetag"));
        assertEquals(PubStatus.PUBLISHED, reload(id).getStatus());
        assertEquals(0, tagCount("drafttag"));
        assertEquals(1, tagCount("livetag"));
    }

    @Test
    public void changingThePublishedTagsCountsEachChangeOnce() throws Exception {
        String id = post(atomEntry("now", false, null, "firsttag", "secondtag"));

        put(id, atomEntry("still live", false, null, "secondtag", "thirdtag"));
        assertEquals(0, tagCount("firsttag"));
        assertEquals(1, tagCount("secondtag"));
        assertEquals(1, tagCount("thirdtag"));
    }

    /** An entry read with GET and sent back unchanged with PUT stays scheduled. */
    @Test
    public void scheduledEntryStaysScheduledThroughGetAndPut() throws Exception {
        String id = post(atomEntry("later", false,
                new Date(System.currentTimeMillis() + DAY_MS), "scheduledtag"));
        assertEquals(PubStatus.SCHEDULED, reload(id).getStatus());

        String path = "/" + HANDLE + "/entry/" + id;
        Entry fetched = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL)
                .getEntry(request(path));
        AppModule control = (AppModule) fetched.getModule(AppModule.URI);
        assertEquals(false, control.getDraft(), "a scheduled entry is not a draft");
        put(id, fetched);
        assertEquals(PubStatus.SCHEDULED, reload(id).getStatus());
        assertEquals(0, tagCount("scheduledtag"));
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

        Entry seenByAuthor = new EntryCollection(
                TestUtils.getManagedUser(author), ATOM_URL).getEntry(request(path));
        assertEquals(owner.getUserName(), firstAuthor(seenByAuthor).getName());
        assertNull(firstAuthor(seenByAuthor).getEmail());

        Feed feed = new EntryCollection(TestUtils.getManagedUser(author), ATOM_URL)
                .getCollection(request("/" + HANDLE + "/entries"));
        assertNotNull(feed.getEntries());
        feed.getEntries().forEach(e -> assertNull(firstAuthor(e).getEmail()));

        Entry seenByOwner = new EntryCollection(
                TestUtils.getManagedUser(owner), ATOM_URL).getEntry(request(path));
        assertEquals("TestUser@dev.null", firstAuthor(seenByOwner).getEmail());
    }

    private static SyndPerson firstAuthor(Entry entry) {
        return entry.getAuthors().get(0);
    }
}
