/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.webservices.xmlrpc;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.xmlrpc.XmlRpcException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration coverage for the Blogger and MetaWeblog permission matrix.
 */
public class XMLRPCWeblogPermissionTest {

    private static final String PASSWORD = "password";

    private final BloggerAPIHandler blogger = new BloggerAPIHandler();
    private final MetaWeblogAPIHandler metaWeblog = new MetaWeblogAPIHandler();

    private User owner;
    private User author;
    private User limited;
    private User outsider;
    private Weblog weblog;
    private Weblog otherWeblog;
    private WeblogTemplate otherTemplate;
    private WeblogEntry draftEntry;
    private WeblogEntry secondDraftEntry;
    private WeblogEntry pendingEntry;
    private WeblogEntry publishedEntry;
    private WeblogEntry deleteEntry;
    private String oldXmlRpcValue;
    private Object oldPasswordEncoder;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        installTestPasswordEncoder();

        Weblogger roller = WebloggerFactory.getWeblogger();
        PropertiesManager propertiesManager = roller.getPropertiesManager();
        RuntimeConfigProperty xmlRpcProperty = propertiesManager
                .getProperty("webservices.enableXmlRpc");
        oldXmlRpcValue = xmlRpcProperty.getValue();
        xmlRpcProperty.setValue("true");
        propertiesManager.saveProperty(xmlRpcProperty);

        owner = TestUtils.setupUser("xmlrpcOwner");
        author = TestUtils.setupUser("xmlrpcAuthor");
        limited = TestUtils.setupUser("xmlrpcLimited");
        outsider = TestUtils.setupUser("xmlrpcOutsider");

        weblog = TestUtils.setupWeblog("xmlrpcPermissionWeblog", owner);
        otherWeblog = TestUtils.setupWeblog("xmlrpcOtherWeblog", owner);
        enableXmlRpc(weblog);
        enableXmlRpc(otherWeblog);

        UserManager userManager = roller.getUserManager();
        userManager.grantWeblogPermission(weblog, author,
                Collections.singletonList(WeblogPermission.POST));
        userManager.grantWeblogPermission(weblog, limited,
                Collections.singletonList(WeblogPermission.EDIT_DRAFT));

        createMainTemplate(weblog, "main-template-code");
        otherTemplate = createMainTemplate(otherWeblog, "other-template-code");
        TestUtils.endSession(true);

        draftEntry = TestUtils.setupWeblogEntry("xmlrpc-draft",
                weblog.getWeblogCategories().iterator().next(), PubStatus.DRAFT,
                weblog, owner);
        secondDraftEntry = TestUtils.setupWeblogEntry("xmlrpc-second-draft",
                weblog.getWeblogCategories().iterator().next(), PubStatus.DRAFT,
                weblog, owner);
        pendingEntry = TestUtils.setupWeblogEntry("xmlrpc-pending",
                weblog.getWeblogCategories().iterator().next(), PubStatus.PENDING,
                weblog, owner);
        publishedEntry = TestUtils.setupWeblogEntry("xmlrpc-published",
                weblog.getWeblogCategories().iterator().next(), PubStatus.PUBLISHED,
                weblog, owner);
        deleteEntry = TestUtils.setupWeblogEntry("xmlrpc-delete",
                weblog.getWeblogCategories().iterator().next(), PubStatus.DRAFT,
                weblog, owner);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownWeblog(otherWeblog.getId());
        TestUtils.teardownUser(owner.getUserName());
        TestUtils.teardownUser(author.getUserName());
        TestUtils.teardownUser(limited.getUserName());
        TestUtils.teardownUser(outsider.getUserName());

        PropertiesManager propertiesManager = WebloggerFactory.getWeblogger()
                .getPropertiesManager();
        RuntimeConfigProperty xmlRpcProperty = propertiesManager
                .getProperty("webservices.enableXmlRpc");
        xmlRpcProperty.setValue(oldXmlRpcValue);
        propertiesManager.saveProperty(xmlRpcProperty);
        TestUtils.endSession(true);
        restorePasswordEncoder();
    }

    @Test
    public void testAuthenticationAndWeblogDiscovery() throws Exception {
        @SuppressWarnings("unchecked")
        Hashtable<String, String> userInfo = (Hashtable<String, String>) blogger
                .getUserInfo("", outsider.getUserName(), PASSWORD);
        assertEquals(outsider.getUserName(), userInfo.get("userid"));

        @SuppressWarnings("unchecked")
        Vector<Object> ownerBlogs = (Vector<Object>) blogger.getUsersBlogs("",
                owner.getUserName(), PASSWORD);
        assertEquals(2, ownerBlogs.size());

        Weblog managedOtherWeblog = WebloggerFactory.getWeblogger()
                .getWeblogManager().getWeblog(otherWeblog.getId());
        managedOtherWeblog.setEnableBloggerApi(false);
        WebloggerFactory.getWeblogger().getWeblogManager()
                .saveWeblog(managedOtherWeblog);
        TestUtils.endSession(true);

        @SuppressWarnings("unchecked")
        Vector<Object> enabledOwnerBlogs = (Vector<Object>) blogger.getUsersBlogs(
                "", owner.getUserName(), PASSWORD);
        assertEquals(1, enabledOwnerBlogs.size());
        assertEquals(weblog.getHandle(), postField(enabledOwnerBlogs.get(0),
                "blogid"));

        @SuppressWarnings("unchecked")
        Vector<Object> outsiderBlogs = (Vector<Object>) blogger.getUsersBlogs("",
                outsider.getUserName(), PASSWORD);
        assertTrue(outsiderBlogs.isEmpty());

        assertThrows(XmlRpcException.class, () -> blogger.getUserInfo("",
                outsider.getUserName(), "wrong-password"));

        User managedOutsider = WebloggerFactory.getWeblogger().getUserManager()
                .getUserByUserName(outsider.getUserName());
        managedOutsider.setEnabled(false);
        WebloggerFactory.getWeblogger().getUserManager().saveUser(managedOutsider);
        TestUtils.endSession(true);
        assertThrows(XmlRpcException.class, () -> blogger.getUserInfo("",
                outsider.getUserName(), PASSWORD));
    }

    @Test
    public void testGlobalXmlRpcSettingIsRequired() throws Exception {
        PropertiesManager propertiesManager = WebloggerFactory.getWeblogger()
                .getPropertiesManager();
        RuntimeConfigProperty xmlRpcProperty = propertiesManager
                .getProperty("webservices.enableXmlRpc");
        xmlRpcProperty.setValue("false");
        propertiesManager.saveProperty(xmlRpcProperty);

        assertThrows(XmlRpcException.class, () -> blogger.getUserInfo("",
                owner.getUserName(), PASSWORD));
    }

    @Test
    public void testWeblogOperationPermissionMatrix() throws Exception {
        String ownerName = owner.getUserName();
        String authorName = author.getUserName();
        String limitedName = limited.getUserName();
        String outsiderName = outsider.getUserName();
        String handle = weblog.getHandle();

        assertEquals("main-template-code", blogger.getTemplate("", handle,
                ownerName, PASSWORD, "main"));
        assertTrue(blogger.setTemplate("", handle, ownerName, PASSWORD,
                "updated-template-code", "main"));
        assertEquals("updated-template-code", blogger.getTemplate("", handle,
                ownerName, PASSWORD, "main"));
        assertThrows(XmlRpcException.class, () -> blogger.getTemplate("", handle,
                authorName, PASSWORD, "main"));
        assertThrows(XmlRpcException.class, () -> blogger.setTemplate("", handle,
                authorName, PASSWORD, "unapproved", "main"));

        // Supplying another weblog's template ID must not escape the requested weblog.
        XmlRpcException foreignTemplate = assertThrows(XmlRpcException.class,
                () -> blogger.getTemplate("", handle, ownerName, PASSWORD,
                        otherTemplate.getId()));
        XmlRpcException missingTemplate = assertThrows(XmlRpcException.class,
                () -> blogger.getTemplate("", handle, ownerName, PASSWORD,
                        "missing-template"));
        assertEquals(missingTemplate.code, foreignTemplate.code);
        assertEquals(missingTemplate.getMessage(), foreignTemplate.getMessage());

        assertNotNull(metaWeblog.getCategories(handle, limitedName, PASSWORD));
        assertThrows(XmlRpcException.class, () -> metaWeblog.getCategories(handle,
                outsiderName, PASSWORD));

        assertNotNull(blogger.newPost("", handle, limitedName, PASSWORD,
                "draft from limited member", false));
        assertThrows(XmlRpcException.class, () -> blogger.newPost("", handle,
                limitedName, PASSWORD, "published by limited member", true));
        assertNotNull(blogger.newPost("", handle, authorName, PASSWORD,
                "published by author", true));

        Hashtable<String, Object> draft = postStruct("meta draft");
        assertNotNull(metaWeblog.newPost(handle, limitedName, PASSWORD, draft,
                false));
        assertThrows(XmlRpcException.class, () -> metaWeblog.newPost(handle,
                limitedName, PASSWORD, postStruct("meta published"), true));

        // Invalid media input stops after authorization without writing a file.
        Hashtable<String, Object> media = new Hashtable<>();
        XmlRpcException authorMediaError = assertThrows(XmlRpcException.class,
                () -> metaWeblog.newMediaObject(handle, authorName, PASSWORD,
                        media));
        assertEquals(BaseAPIHandler.UNKNOWN_EXCEPTION, authorMediaError.code);
        XmlRpcException limitedMediaError = assertThrows(XmlRpcException.class,
                () -> metaWeblog.newMediaObject(handle, limitedName, PASSWORD,
                        media));
        assertTrue(limitedMediaError.code != BaseAPIHandler.UNKNOWN_EXCEPTION);
    }

    @Test
    public void testEntryOperationPermissionMatrix() throws Exception {
        String authorName = author.getUserName();
        String limitedName = limited.getUserName();
        String outsiderName = outsider.getUserName();

        assertTrue(blogger.editPost("", draftEntry.getId(), limitedName,
                PASSWORD, "limited draft edit", false));
        XmlRpcException bloggerPublishDenied = assertThrows(XmlRpcException.class,
                () -> blogger.editPost("",
                secondDraftEntry.getId(), limitedName, PASSWORD,
                "limited publish", true));
        assertTrue(bloggerPublishDenied.code != BaseAPIHandler.INVALID_POSTID);
        assertTrue(blogger.editPost("", secondDraftEntry.getId(), authorName,
                PASSWORD, "author publish", true));
        assertThrows(XmlRpcException.class, () -> blogger.editPost("",
                draftEntry.getId(), outsiderName, PASSWORD, "foreign edit", false));

        assertNotNull(metaWeblog.getPost(draftEntry.getId(), limitedName, PASSWORD));
        assertThrows(XmlRpcException.class, () -> metaWeblog.getPost(
                publishedEntry.getId(), limitedName, PASSWORD));
        assertNotNull(metaWeblog.getPost(publishedEntry.getId(), authorName,
                PASSWORD));

        assertTrue(metaWeblog.editPost(draftEntry.getId(), limitedName, PASSWORD,
                postStruct("limited meta edit"), false));
        XmlRpcException metaPublishDenied = assertThrows(XmlRpcException.class,
                () -> metaWeblog.editPost(
                draftEntry.getId(), limitedName, PASSWORD,
                postStruct("limited meta publish"), true));
        assertTrue(metaPublishDenied.code != BaseAPIHandler.INVALID_POSTID);

        assertFalse(blogger.deletePost("", publishedEntry.getId(), outsiderName,
                PASSWORD, false));
        assertTrue(blogger.deletePost("", deleteEntry.getId(), limitedName,
                PASSWORD, false));

        XmlRpcException missing = assertThrows(XmlRpcException.class,
                () -> metaWeblog.getPost("missing-entry", outsiderName, PASSWORD));
        XmlRpcException inaccessible = assertThrows(XmlRpcException.class,
                () -> metaWeblog.getPost(publishedEntry.getId(), outsiderName,
                        PASSWORD));
        assertEquals(missing.code, inaccessible.code);
        assertEquals(missing.getMessage(), inaccessible.getMessage());
    }

    @Test
    public void testRecentPostsAreFilteredForLimitedMembers() throws Exception {
        String handle = weblog.getHandle();
        String limitedName = limited.getUserName();
        String authorName = author.getUserName();

        @SuppressWarnings("unchecked")
        Vector<Object> bloggerLimited = (Vector<Object>) blogger.getRecentPosts(
                "", handle, limitedName, PASSWORD, 50);
        assertContainsPost(bloggerLimited, draftEntry.getId());
        assertContainsPost(bloggerLimited, pendingEntry.getId());
        assertDoesNotContainPost(bloggerLimited, publishedEntry.getId());

        @SuppressWarnings("unchecked")
        Vector<Object> metaLimited = (Vector<Object>) metaWeblog.getRecentPosts(
                handle, limitedName, PASSWORD, 50);
        assertContainsPost(metaLimited, draftEntry.getId());
        assertContainsPost(metaLimited, pendingEntry.getId());
        assertDoesNotContainPost(metaLimited, publishedEntry.getId());

        @SuppressWarnings("unchecked")
        Vector<Object> metaAuthor = (Vector<Object>) metaWeblog.getRecentPosts(
                handle, authorName, PASSWORD, 50);
        assertContainsPost(metaAuthor, publishedEntry.getId());

        addNewerPublishedEntries(205);

        @SuppressWarnings("unchecked")
        Vector<Object> bloggerAfterPublished =
                (Vector<Object>) blogger.getRecentPosts(
                        "", handle, limitedName, PASSWORD, 50);
        assertContainsPost(bloggerAfterPublished, draftEntry.getId());

        @SuppressWarnings("unchecked")
        Vector<Object> metaAfterPublished =
                (Vector<Object>) metaWeblog.getRecentPosts(
                        handle, limitedName, PASSWORD, 50);
        assertContainsPost(metaAfterPublished, draftEntry.getId());

        @SuppressWarnings("unchecked")
        Vector<Object> bloggerBounded = (Vector<Object>) blogger.getRecentPosts(
                "", handle, authorName, PASSWORD, 1000000);
        assertEquals(BloggerAPIHandler.DRAFT_SCAN_CAP, bloggerBounded.size());

        @SuppressWarnings("unchecked")
        Vector<Object> metaBounded = (Vector<Object>) metaWeblog.getRecentPosts(
                handle, authorName, PASSWORD, 1000000);
        assertEquals(BloggerAPIHandler.DRAFT_SCAN_CAP, metaBounded.size());

        @SuppressWarnings("unchecked")
        Vector<Object> bloggerLegacy = (Vector<Object>) blogger.getRecentPosts(
                "", handle, limitedName, PASSWORD, 0);
        assertContainsPost(bloggerLegacy, draftEntry.getId());

        @SuppressWarnings("unchecked")
        Vector<Object> metaLegacy = (Vector<Object>) metaWeblog.getRecentPosts(
                handle, limitedName, PASSWORD, -1);
        assertContainsPost(metaLegacy, draftEntry.getId());

        assertThrows(XmlRpcException.class, () -> blogger.getRecentPosts("",
                handle, outsider.getUserName(), PASSWORD, 50));
        assertThrows(XmlRpcException.class, () -> metaWeblog.getRecentPosts(
                handle, outsider.getUserName(), PASSWORD, 50));
    }

    @Test
    public void testDisabledWeblogAndApiAreRejected() throws Exception {
        WeblogManager weblogManager = WebloggerFactory.getWeblogger()
                .getWeblogManager();
        Weblog managedWeblog = weblogManager.getWeblog(weblog.getId());
        managedWeblog.setEnableBloggerApi(false);
        weblogManager.saveWeblog(managedWeblog);
        TestUtils.endSession(true);

        assertThrows(XmlRpcException.class, () -> metaWeblog.getCategories(
                weblog.getHandle(), owner.getUserName(), PASSWORD));

        managedWeblog = WebloggerFactory.getWeblogger().getWeblogManager()
                .getWeblog(weblog.getId());
        managedWeblog.setEnableBloggerApi(true);
        managedWeblog.setVisible(true);
        managedWeblog.setActive(false);
        WebloggerFactory.getWeblogger().getWeblogManager()
                .saveWeblog(managedWeblog);
        TestUtils.endSession(true);

        assertNotNull(metaWeblog.getCategories(
                weblog.getHandle(), owner.getUserName(), PASSWORD));

        managedWeblog = WebloggerFactory.getWeblogger().getWeblogManager()
                .getWeblog(weblog.getId());
        managedWeblog.setEnableBloggerApi(true);
        managedWeblog.setVisible(false);
        WebloggerFactory.getWeblogger().getWeblogManager()
                .saveWeblog(managedWeblog);
        TestUtils.endSession(true);

        assertThrows(XmlRpcException.class, () -> metaWeblog.getCategories(
                weblog.getHandle(), owner.getUserName(), PASSWORD));
    }

    private void enableXmlRpc(Weblog target) throws Exception {
        target.setEnableBloggerApi(true);
        WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(target);
    }

    private void addNewerPublishedEntries(int count) throws Exception {
        Weblogger roller = WebloggerFactory.getWeblogger();
        WeblogEntryManager entryManager = roller.getWeblogEntryManager();
        Weblog managedWeblog = roller.getWeblogManager().getWeblog(weblog.getId());
        User managedOwner = roller.getUserManager()
                .getUserByUserName(owner.getUserName());
        WeblogCategory category = managedWeblog.getWeblogCategories()
                .iterator().next();

        WeblogEntry managedDraft = entryManager.getWeblogEntry(draftEntry.getId());
        Timestamp oldTime = new Timestamp(946684800000L);
        managedDraft.setPubTime(oldTime);
        managedDraft.setUpdateTime(oldTime);
        entryManager.saveWeblogEntry(managedDraft);

        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            WeblogEntry entry = new WeblogEntry();
            entry.setTitle("newer published " + i);
            entry.setText("newer published entry");
            entry.setAnchor("xmlrpc-newer-published-" + i);
            entry.setPubTime(new Timestamp(now - count + i));
            entry.setUpdateTime(new Timestamp(now - count + i));
            entry.setStatus(PubStatus.PUBLISHED);
            entry.setWebsite(managedWeblog);
            entry.setCreatorUserName(managedOwner.getUserName());
            entry.setCategory(category);
            entryManager.saveWeblogEntry(entry);
        }
        roller.flush();
        TestUtils.endSession(true);
    }

    @SuppressWarnings("deprecation")
    private void installTestPasswordEncoder() throws Exception {
        Field encoderField = RollerContext.class.getDeclaredField("encoder");
        encoderField.setAccessible(true);
        oldPasswordEncoder = encoderField.get(null);

        PasswordEncoder noOp = NoOpPasswordEncoder.getInstance();
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(null, noOp);
        encoders.put("noop", noOp);
        encoderField.set(null, new DelegatingPasswordEncoder("noop", encoders));
    }

    private void restorePasswordEncoder() throws Exception {
        Field encoderField = RollerContext.class.getDeclaredField("encoder");
        encoderField.setAccessible(true);
        encoderField.set(null, oldPasswordEncoder);
    }

    private WeblogTemplate createMainTemplate(Weblog target, String code)
            throws Exception {
        WeblogTemplate template = new WeblogTemplate();
        template.setAction(ComponentType.WEBLOG);
        template.setName("Weblog");
        template.setDescription("Test weblog template");
        template.setLink("Weblog");
        template.setLastModified(new Date());
        template.setWeblog(target);

        WeblogManager weblogManager = WebloggerFactory.getWeblogger()
                .getWeblogManager();
        weblogManager.saveTemplate(template);
        CustomTemplateRendition rendition = new CustomTemplateRendition(
                template, RenditionType.STANDARD);
        rendition.setTemplate(code);
        rendition.setTemplateLanguage(TemplateLanguage.VELOCITY);
        weblogManager.saveTemplateRendition(rendition);
        return template;
    }

    private Hashtable<String, Object> postStruct(String title) {
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("title", title);
        struct.put("description", title + " body");
        return struct;
    }

    private void assertContainsPost(List<Object> posts, String postId) {
        assertTrue(posts.stream().map(this::postId).anyMatch(postId::equals));
    }

    private void assertDoesNotContainPost(List<Object> posts, String postId) {
        assertFalse(posts.stream().map(this::postId).anyMatch(postId::equals));
    }

    @SuppressWarnings("unchecked")
    private String postId(Object post) {
        return ((Hashtable<String, Object>) post).get("postid").toString();
    }

    @SuppressWarnings("unchecked")
    private String postField(Object post, String field) {
        return ((Hashtable<String, String>) post).get(field);
    }
}
