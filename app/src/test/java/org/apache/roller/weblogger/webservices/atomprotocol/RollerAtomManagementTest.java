/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for the AtomPub template, category and comment
 * collections against the in-memory test database.
 */
public class RollerAtomManagementTest {

    private static final String HANDLE = "atommgmtblog";
    private static final String OTHER_HANDLE = "atommgmtother";
    private static final String ATOM_URL = "http://localhost/roller/roller-services/app";

    private User owner;
    private User author;
    private Weblog weblog;
    private Weblog otherWeblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        owner = TestUtils.setupUser("atommgmtowner");
        author = TestUtils.setupUser("atommgmtauthor");
        weblog = TestUtils.setupWeblog(HANDLE, owner);
        otherWeblog = TestUtils.setupWeblog(OTHER_HANDLE, owner);
        WebloggerFactory.getWeblogger().getUserManager().grantWeblogPermission(
                weblog, author, List.of(WeblogPermission.POST));
        setProperty("webservices.enableAtomPub", "true");
        setProperty("themes.customtheme.allowed", "true");
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownWeblog(otherWeblog.getId());
        TestUtils.teardownUser(author.getUserName());
        TestUtils.teardownUser(owner.getUserName());
        TestUtils.endSession(true);
    }

    // ------------------------------------------------------------ templates

    @Test
    public void templatesOfASharedThemeAreNotFound() throws Exception {
        assertThrows(AtomNotFoundException.class, () -> templates(owner)
                .getCollection(request("/" + HANDLE + "/templates")));
    }

    @Test
    public void templatesNeedTheAdminPermission() throws Exception {
        useCustomTheme();
        assertThrows(AtomNotAuthorizedException.class, () -> templates(author)
                .getCollection(request("/" + HANDLE + "/templates")));
    }

    @Test
    public void templatesNeedCustomThemesToBeAllowed() throws Exception {
        useCustomTheme();
        setProperty("themes.customtheme.allowed", "false");
        TestUtils.endSession(true);
        assertThrows(AtomNotFoundException.class, () -> templates(owner)
                .getCollection(request("/" + HANDLE + "/templates")));
    }

    @Test
    public void templateLifecycle() throws Exception {
        useCustomTheme();

        // create a custom template with both renditions
        AtomEntry in = new AtomEntry();
        in.setTitle("sidebar");
        in.setSummary(CollectionSupport.text("text", "The sidebar"));
        in.setContent(CollectionSupport.text("text", "<div>standard</div>"));
        in.setMobileRendition("<div>mobile</div>");
        in.setExtension("navbar", "true");
        AtomEntry created = templates(owner).postEntry(request("/" + HANDLE + "/templates"), in);
        TestUtils.endSession(true);

        String id = idFrom(created, "/template/");
        assertEquals("custom", created.getExtension("action"));
        assertEquals("sidebar", created.getExtension("link"));
        assertEquals("true", created.getExtension("navbar"));
        assertEquals("<div>mobile</div>", created.getMobileRendition());

        // read it back
        AtomEntry read = templates(owner).getEntry(request("/" + HANDLE + "/template/" + id));
        assertEquals("sidebar", read.getTitle());
        assertEquals("The sidebar", read.getSummary().getValue());
        assertEquals("<div>standard</div>", read.getContent().getValue());

        // the feed lists it
        AtomFeed feed = templates(owner).getCollection(request("/" + HANDLE + "/templates"));
        assertTrue(feed.getEntries().stream().anyMatch(e -> "sidebar".equals(e.getTitle())));

        // update both renditions; leave the metadata alone
        AtomEntry update = new AtomEntry();
        update.setContent(CollectionSupport.text("text", "<div>standard 2</div>"));
        update.setMobileRendition("<div>mobile 2</div>");
        templates(owner).putEntry(request("/" + HANDLE + "/template/" + id), update);
        TestUtils.endSession(true);

        WeblogTemplate stored = weblogManager().getTemplate(managedWeblog(), id);
        assertEquals("sidebar", stored.getName());
        assertEquals("<div>standard 2</div>",
                stored.getTemplateRendition(RenditionType.STANDARD).getTemplate());
        CustomTemplateRendition mobile = stored.getTemplateRendition(RenditionType.MOBILE);
        assertEquals("<div>mobile 2</div>", mobile.getTemplate());

        // an empty mobile rendition removes it; the standard one stays
        AtomEntry dropMobile = new AtomEntry();
        dropMobile.setMobileRendition("");
        templates(owner).putEntry(request("/" + HANDLE + "/template/" + id), dropMobile);
        TestUtils.endSession(true);
        stored = weblogManager().getTemplate(managedWeblog(), id);
        assertNull(stored.getTemplateRendition(RenditionType.MOBILE));
        assertEquals("<div>standard 2</div>",
                stored.getTemplateRendition(RenditionType.STANDARD).getTemplate());
        assertNull(templates(owner).getEntry(request("/" + HANDLE + "/template/" + id))
                .getMobileRendition());

        // a second template with the same name conflicts
        AtomEntry duplicate = new AtomEntry();
        duplicate.setTitle("sidebar");
        AtomException conflict = assertThrows(AtomException.class, () ->
                templates(owner).postEntry(request("/" + HANDLE + "/templates"), duplicate));
        assertEquals(HttpServletResponse.SC_CONFLICT, conflict.getStatus());
        TestUtils.endSession(false);

        // delete it
        templates(owner).deleteEntry(request("/" + HANDLE + "/template/" + id));
        TestUtils.endSession(true);
        assertNull(weblogManager().getTemplate(managedWeblog(), id));
    }

    @Test
    public void requiredTemplatesOfACustomThemeCannotBeRemoved() throws Exception {
        useCustomTheme();
        AtomEntry in = new AtomEntry();
        in.setTitle("main");
        in.setExtension("action", "weblog");
        in.setContent(CollectionSupport.text("text", "main page"));
        AtomEntry created = templates(owner).postEntry(request("/" + HANDLE + "/templates"), in);
        TestUtils.endSession(true);
        assertEquals("Weblog", created.getTitle());
        assertEquals("true", created.getExtension("required"));

        String id = idFrom(created, "/template/");
        AtomException refused = assertThrows(AtomException.class, () ->
                templates(owner).deleteEntry(request("/" + HANDLE + "/template/" + id)));
        assertEquals(HttpServletResponse.SC_CONFLICT, refused.getStatus());
    }

    @Test
    public void templatesOfAnotherWeblogAreNotFound() throws Exception {
        useCustomTheme();
        AtomEntry in = new AtomEntry();
        in.setTitle("mine");
        AtomEntry created = templates(owner).postEntry(request("/" + HANDLE + "/templates"), in);
        TestUtils.endSession(true);
        String id = idFrom(created, "/template/");

        Weblog other = WebloggerFactory.getWeblogger().getWeblogManager()
                .getWeblogByHandle(OTHER_HANDLE);
        other.setEditorTheme(WeblogTheme.CUSTOM);
        weblogManager().saveWeblog(other);
        TestUtils.endSession(true);

        assertThrows(AtomNotFoundException.class, () -> templates(owner)
                .getEntry(request("/" + OTHER_HANDLE + "/template/" + id)));
    }

    // ----------------------------------------------------------- categories

    @Test
    public void categoryLifecycle() throws Exception {
        AtomEntry in = new AtomEntry();
        in.setTitle("Travel");
        in.setSummary(CollectionSupport.text("text", "Trips"));
        AtomEntry created = categories(author).postEntry(request("/" + HANDLE + "/categories"), in);
        TestUtils.endSession(true);
        String id = idFrom(created, "/category/");
        assertEquals("false", created.getExtension("inUse"));

        // duplicate name conflicts
        AtomEntry duplicate = new AtomEntry();
        duplicate.setTitle("Travel");
        AtomException conflict = assertThrows(AtomException.class, () -> categories(author)
                .postEntry(request("/" + HANDLE + "/categories"), duplicate));
        assertEquals(HttpServletResponse.SC_CONFLICT, conflict.getStatus());
        TestUtils.endSession(false);

        // markup in a name is refused
        AtomEntry markup = new AtomEntry();
        markup.setTitle("<b>bold</b>");
        AtomException invalid = assertThrows(AtomException.class, () -> categories(author)
                .postEntry(request("/" + HANDLE + "/categories"), markup));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, invalid.getStatus());
        TestUtils.endSession(false);

        // rename
        AtomEntry rename = new AtomEntry();
        rename.setTitle("Journeys");
        categories(author).putEntry(request("/" + HANDLE + "/category/" + id), rename);
        TestUtils.endSession(true);
        assertEquals("Journeys", entryManager().getWeblogCategory(managedWeblog(), id).getName());

        // the category document lists it
        AtomCategories doc = categories(author)
                .getCategoriesDocument(request("/" + HANDLE + "/categories.atomcat"));
        assertTrue(doc.isFixed());
        assertTrue(doc.getCategories().stream().anyMatch(c -> "Journeys".equals(c.getTerm())));

        // delete
        categories(author).deleteEntry(request("/" + HANDLE + "/category/" + id));
        TestUtils.endSession(true);
        assertNull(entryManager().getWeblogCategory(managedWeblog(), id));
    }

    @Test
    public void categoryInUseIsRemovedOnlyWithMoveTo() throws Exception {
        WeblogCategory used = TestUtils.setupWeblogCategory(managedWeblog(), "Used");
        WeblogCategory target = TestUtils.setupWeblogCategory(managedWeblog(), "Target");
        WeblogEntry entry = TestUtils.setupWeblogEntry("inuse", used, managedWeblog(),
                TestUtils.getManagedUser(owner));
        TestUtils.endSession(true);

        AtomException refused = assertThrows(AtomException.class, () -> categories(author)
                .deleteEntry(request("/" + HANDLE + "/category/" + used.getId())));
        assertEquals(HttpServletResponse.SC_CONFLICT, refused.getStatus());
        TestUtils.endSession(false);

        categories(author).deleteEntry(request("/" + HANDLE + "/category/" + used.getId(),
                "moveTo", target.getId()));
        TestUtils.endSession(true);

        assertNull(entryManager().getWeblogCategory(managedWeblog(), used.getId()));
        assertEquals(target.getId(),
                entryManager().getWeblogEntry(entry.getId()).getCategory().getId());
    }

    @Test
    public void defaultCategoryRemovedWithMoveToHandsOverTheDefault() throws Exception {
        WeblogCategory target = TestUtils.setupWeblogCategory(managedWeblog(), "Target");
        TestUtils.endSession(true);
        String defaultId = managedWeblog().getBloggerCategory().getId();

        // the default category counts as in use
        AtomException refused = assertThrows(AtomException.class, () -> categories(author)
                .deleteEntry(request("/" + HANDLE + "/category/" + defaultId)));
        assertEquals(HttpServletResponse.SC_CONFLICT, refused.getStatus());
        TestUtils.endSession(false);

        categories(author).deleteEntry(request("/" + HANDLE + "/category/" + defaultId,
                "moveTo", target.getId()));
        TestUtils.endSession(true);

        assertEquals(target.getId(), managedWeblog().getBloggerCategory().getId());
        // the collection still lists cleanly
        AtomFeed feed = categories(author).getCollection(request("/" + HANDLE + "/categories"));
        assertTrue(feed.getEntries().stream().anyMatch(e -> "Target".equals(e.getTitle())));
    }

    @Test
    public void lastCategoryCannotBeRemoved() throws Exception {
        WeblogEntryManager wem = entryManager();
        List<WeblogCategory> cats = wem.getWeblogCategories(managedWeblog());
        WeblogCategory keep = cats.get(0);
        for (WeblogCategory cat : cats.subList(1, cats.size())) {
            categories(author).deleteEntry(request("/" + HANDLE + "/category/" + cat.getId(),
                    "moveTo", keep.getId()));
            TestUtils.endSession(true);
        }
        assertEquals(1, entryManager().getWeblogCategories(managedWeblog()).size());

        AtomException refused = assertThrows(AtomException.class, () -> categories(author)
                .deleteEntry(request("/" + HANDLE + "/category/" + keep.getId())));
        assertEquals(HttpServletResponse.SC_CONFLICT, refused.getStatus());
    }

    @Test
    public void categoriesNeedPermissionOnTheWeblog() throws Exception {
        assertThrows(AtomNotAuthorizedException.class, () -> categories(author)
                .getCollection(request("/" + OTHER_HANDLE + "/categories")));
    }

    // ------------------------------------------------------------- comments

    @Test
    public void commentModeration() throws Exception {
        WeblogEntry entry = TestUtils.setupWeblogEntry("commented", managedWeblog(),
                TestUtils.getManagedUser(owner));
        WeblogEntryComment pending = TestUtils.setupComment("pending one", entry);
        pending.setStatus(ApprovalStatus.PENDING);
        entryManager().saveComment(pending);
        WeblogEntryComment approved = TestUtils.setupComment("approved one", entry);
        TestUtils.endSession(true);

        // list only pending comments
        AtomFeed feed = comments(author).getCollection(
                request("/" + HANDLE + "/comments", "status", "pending"));
        List<String> statuses = feed.getEntries().stream()
                .map(e -> e.getExtension("status")).collect(Collectors.toList());
        assertEquals(List.of("pending"), statuses);
        AtomEntry listed = feed.getEntries().get(0);
        assertEquals(entry.getPermalink(), listed.getInReplyToRef());
        assertEquals("test", listed.getAuthors().get(0).getName());

        // approve it
        AtomEntry change = new AtomEntry();
        change.setExtension("status", "approved");
        comments(author).putEntry(request("/" + HANDLE + "/comment/" + pending.getId()), change);
        TestUtils.endSession(true);
        assertEquals(ApprovalStatus.APPROVED, entryManager().getComment(pending.getId()).getStatus());

        // an unknown status is refused
        AtomEntry bad = new AtomEntry();
        bad.setExtension("status", "maybe");
        AtomException invalid = assertThrows(AtomException.class, () -> comments(author)
                .putEntry(request("/" + HANDLE + "/comment/" + pending.getId()), bad));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, invalid.getStatus());

        // delete the other one
        comments(author).deleteEntry(request("/" + HANDLE + "/comment/" + approved.getId()));
        TestUtils.endSession(true);
        assertNull(entryManager().getComment(approved.getId()));
    }

    @Test
    public void commentsOfAnotherWeblogAreNotFound() throws Exception {
        WeblogEntry entry = TestUtils.setupWeblogEntry("elsewhere", managedOther(),
                TestUtils.getManagedUser(owner));
        WeblogEntryComment comment = TestUtils.setupComment("other", entry);
        TestUtils.endSession(true);

        assertThrows(AtomNotFoundException.class, () -> comments(author)
                .getEntry(request("/" + HANDLE + "/comment/" + comment.getId())));
    }

    // ------------------------------------------------------ service document

    @Test
    public void serviceDocumentListsTheCollectionsTheUserMayUse() throws Exception {
        List<String> authorHrefs = hrefs(author);
        assertTrue(authorHrefs.contains(ATOM_URL + "/" + HANDLE + "/categories"));
        assertTrue(authorHrefs.contains(ATOM_URL + "/" + HANDLE + "/comments"));
        assertFalse(authorHrefs.contains(ATOM_URL + "/" + HANDLE + "/templates"));

        // the owner sees templates only once the weblog uses a custom theme
        assertFalse(hrefs(owner).contains(ATOM_URL + "/" + HANDLE + "/templates"));
        useCustomTheme();
        assertTrue(hrefs(owner).contains(ATOM_URL + "/" + HANDLE + "/templates"));
        assertFalse(hrefs(author).contains(ATOM_URL + "/" + HANDLE + "/templates"));
    }

    // -------------------------------------------------------------- helpers

    private List<String> hrefs(User user) throws Exception {
        AtomServiceDoc doc = new RollerAtomService(TestUtils.getManagedUser(user), ATOM_URL)
                .getServiceDoc();
        return doc.getWorkspaces().stream()
                .flatMap(w -> w.getCollections().stream())
                .map(AtomCollection::getHref)
                .collect(Collectors.toList());
    }

    private void useCustomTheme() throws Exception {
        Weblog managed = managedWeblog();
        managed.setEditorTheme(WeblogTheme.CUSTOM);
        weblogManager().saveWeblog(managed);
        TestUtils.endSession(true);
    }

    private static void setProperty(String name, String value) throws Exception {
        PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty property = pmgr.getProperty(name);
        property.setValue(value);
        pmgr.saveProperty(property);
    }

    private TemplateCollection templates(User user) throws Exception {
        return new TemplateCollection(TestUtils.getManagedUser(user), ATOM_URL);
    }

    private CategoryCollection categories(User user) throws Exception {
        return new CategoryCollection(TestUtils.getManagedUser(user), ATOM_URL);
    }

    private CommentCollection comments(User user) throws Exception {
        return new CommentCollection(TestUtils.getManagedUser(user), ATOM_URL);
    }

    private Weblog managedWeblog() throws Exception {
        return weblogManager().getWeblogByHandle(HANDLE);
    }

    private Weblog managedOther() throws Exception {
        return weblogManager().getWeblogByHandle(OTHER_HANDLE);
    }

    private static WeblogManager weblogManager() {
        return WebloggerFactory.getWeblogger().getWeblogManager();
    }

    private static WeblogEntryManager entryManager() {
        return WebloggerFactory.getWeblogger().getWeblogEntryManager();
    }

    private static String idFrom(AtomEntry entry, String marker) {
        String edit = entry.getLinkHref("edit");
        assertNotNull(edit, "entry must have an edit link");
        return edit.substring(edit.lastIndexOf(marker) + marker.length());
    }

    private static AtomRequest request(String pathInfo) {
        return request(pathInfo, null, null);
    }

    private static AtomRequest request(String pathInfo, String param, String value) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getPathInfo()).thenReturn(pathInfo);
        if (param != null) {
            lenient().when(req.getParameter(param)).thenReturn(value);
        }
        return new AtomRequest(req, null);
    }
}
