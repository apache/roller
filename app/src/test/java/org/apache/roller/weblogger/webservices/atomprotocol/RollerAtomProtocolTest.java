/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.weblogger.webservices.atomprotocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that exercise the AtomPub server handlers against a real
 * (in-memory Derby) Roller, driving the full create / retrieve / update / delete
 * lifecycle through {@link EntryCollection}, {@link MediaCollection} and
 * {@link RollerAtomService}. The HTTP transport and BASIC authentication are not
 * exercised here (they require the Spring web context); those are covered by an
 * over-the-wire exerciser such as APE run against a deployed instance.
 */
public class RollerAtomProtocolTest {

    private static final String HANDLE = "atomtestblog";
    private static final String ATOM_URL = "http://localhost/roller/roller-services/app";

    private User testUser;
    private Weblog testWeblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        testUser = TestUtils.setupUser("atomtestuser");
        testWeblog = TestUtils.setupWeblog(HANDLE, testUser);

        // RollerAtomService requires AtomPub to be enabled; media tests need uploads enabled
        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty enableAtomPub = pmgr.getProperty("webservices.enableAtomPub");
        enableAtomPub.setValue("true");
        pmgr.saveProperty(enableAtomPub);
        RuntimeConfigProperty enableUploads = pmgr.getProperty("uploads.enabled");
        enableUploads.setValue("true");
        pmgr.saveProperty(enableUploads);

        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
        TestUtils.endSession(true);
    }

    private User managedUser() throws Exception {
        return TestUtils.getManagedUser(testUser);
    }

    /** Build an AtomRequest backed by a mocked HttpServletRequest. */
    private AtomRequest request(String pathInfo, String contentType, String slug, byte[] body) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getPathInfo()).thenReturn(pathInfo);
        lenient().when(req.getContentType()).thenReturn(contentType);
        lenient().when(req.getHeader("Slug")).thenReturn(slug);
        return new AtomRequest(req, body);
    }

    private AtomEntry sampleEntry(String title, String body, boolean draft) {
        AtomEntry entry = new AtomEntry();
        entry.setTitle(title);
        AtomContent content = new AtomContent();
        content.setType("html");
        content.setValue(body);
        entry.setContent(content);
        entry.setDraft(draft);
        // a free-form tag (no scheme); the weblog category will default
        AtomCategory tag = new AtomCategory();
        tag.setTerm("testing");
        entry.getCategories().add(tag);
        return entry;
    }

    private String entryIdFromEditLink(AtomEntry entry) {
        String edit = entry.getLinkHref("edit");
        assertNotNull(edit, "created entry must have an edit link");
        return edit.substring(edit.lastIndexOf("/entry/") + "/entry/".length());
    }

    @Test
    public void testEntryLifecycle() throws Exception {
        // ---- create ----
        EntryCollection ecol = new EntryCollection(managedUser(), ATOM_URL);
        AtomEntry created = ecol.postEntry(
                request("/" + HANDLE + "/entries", "application/atom+xml", null, null),
                sampleEntry("First Post", "<p>Hello AtomPub</p>", false));

        assertNotNull(created.getId());
        assertEquals("First Post", created.getTitle());
        assertFalse(created.isDraft());
        String entryId = entryIdFromEditLink(created);
        TestUtils.endSession(true);

        // ---- it actually persisted ----
        WeblogEntryManager wem = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntry persisted = wem.getWeblogEntry(entryId);
        assertNotNull(persisted);
        assertEquals("First Post", persisted.getTitle());
        assertEquals("<p>Hello AtomPub</p>", persisted.getText());
        assertEquals(PubStatus.PUBLISHED, persisted.getStatus());
        assertTrue(persisted.getTags().stream().anyMatch(t -> "testing".equals(t.getName())));
        TestUtils.endSession(true);

        // ---- retrieve single entry ----
        ecol = new EntryCollection(managedUser(), ATOM_URL);
        AtomEntry fetched = ecol.getEntry(request("/" + HANDLE + "/entry/" + entryId, null, null, null));
        assertEquals("First Post", fetched.getTitle());
        assertEquals("<p>Hello AtomPub</p>", fetched.getContent().getValue());
        TestUtils.endSession(true);

        // ---- retrieve collection ----
        ecol = new EntryCollection(managedUser(), ATOM_URL);
        AtomFeed feed = ecol.getCollection(request("/" + HANDLE + "/entries", null, null, null));
        assertTrue(feed.getEntries().stream().anyMatch(e -> "First Post".equals(e.getTitle())));
        TestUtils.endSession(true);

        // ---- update (and flip to draft) ----
        ecol = new EntryCollection(managedUser(), ATOM_URL);
        ecol.putEntry(request("/" + HANDLE + "/entry/" + entryId, "application/atom+xml", null, null),
                sampleEntry("First Post (edited)", "<p>Edited body</p>", true));
        TestUtils.endSession(true);

        wem = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntry updated = wem.getWeblogEntry(entryId);
        assertEquals("First Post (edited)", updated.getTitle());
        assertEquals("<p>Edited body</p>", updated.getText());
        assertEquals(PubStatus.DRAFT, updated.getStatus());
        TestUtils.endSession(true);

        // ---- delete ----
        ecol = new EntryCollection(managedUser(), ATOM_URL);
        ecol.deleteEntry(request("/" + HANDLE + "/entry/" + entryId, null, null, null));
        TestUtils.endSession(true);

        wem = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        assertNull(wem.getWeblogEntry(entryId));
    }

    @Test
    public void testServiceDocument() throws Exception {
        AtomServiceDoc service = new RollerAtomService(managedUser(), ATOM_URL).getServiceDoc();

        assertFalse(service.getWorkspaces().isEmpty());
        AtomWorkspace workspace = service.getWorkspaces().get(0);

        // entries collection present, pointing at this weblog
        AtomCollection entries = workspace.getCollections().stream()
                .filter(c -> c.getHref() != null && c.getHref().endsWith("/" + HANDLE + "/entries"))
                .findFirst().orElse(null);
        assertNotNull(entries, "service doc should expose an entries collection");
        assertTrue(entries.getAccepts().contains("application/atom+xml;type=entry"));
        // a fixed categories block (weblog categories) plus a free-form one
        assertEquals(2, entries.getCategories().size());
        assertTrue(entries.getCategories().stream().anyMatch(AtomCategories::isFixed));

        // the document serializes to well-formed XML
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        new AtomWriter().writeServiceDoc(out, service);
        assertTrue(out.size() > 0);
    }

    @Test
    public void testMediaUpload() throws Exception {
        byte[] bytes = "fake-png-bytes".getBytes(StandardCharsets.UTF_8);

        // a named upload directory, the way the service document advertises them
        MediaFileManager mfm = WebloggerFactory.getWeblogger().getMediaFileManager();
        Weblog weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(HANDLE);
        mfm.createMediaFileDirectory(weblog, "atomuploads");
        TestUtils.endSession(true);

        // ---- upload ----
        MediaCollection mcol = new MediaCollection(managedUser(), ATOM_URL);
        AtomEntry mediaIn = new AtomEntry();
        AtomContent content = new AtomContent();
        content.setType("image/png");
        mediaIn.setContent(content);
        mediaIn.setTitle("snapshot");

        AtomEntry created = mcol.postMedia(
                request("/" + HANDLE + "/resources/atomuploads", "image/png", "snapshot", bytes),
                mediaIn);

        assertNotNull(created.getLinkHref("edit"));
        assertNotNull(created.getLinkHref("edit-media"));
        String fileName = created.getTitle();
        TestUtils.endSession(true);

        // ---- it persisted into the named media directory ----
        mfm = WebloggerFactory.getWeblogger().getMediaFileManager();
        weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(HANDLE);
        MediaFileDirectory dir = mfm.getMediaFileDirectoryByName(weblog, "atomuploads");
        assertNotNull(dir, "named upload directory should exist");
        MediaFile stored = dir.getMediaFiles().stream()
                .filter(mf -> mf.getName().equals(fileName))
                .findFirst().orElse(null);
        assertNotNull(stored, "uploaded media file should be persisted");
        assertEquals("image/png", stored.getContentType());
    }
}
