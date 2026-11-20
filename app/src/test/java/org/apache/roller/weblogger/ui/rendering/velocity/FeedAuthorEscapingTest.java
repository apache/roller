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

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.wrapper.UserWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryCommentWrapper;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Entry, comment and media file authors in the built-in feeds are plain text,
 * so the feed macros must turn the HTML-escaped names from {@link UserWrapper}
 * and {@link WeblogEntryCommentWrapper} back into text and XML-escape them
 * once.
 */
public class FeedAuthorEscapingTest {

    /** Stored screen name, and the text the feed must contain. */
    private static final String[][] NAMES = {
        // stored as typed (trusted admins)
        {"<script>alert(1)</script>", "<script>alert(1)</script>"},
        {"Tom & Jerry", "Tom & Jerry"},
        {"José \"Pepe\" O'Brien", "José \"Pepe\" O'Brien"},
        // quotes and angle brackets encoded by the sanitizer (untrusted admins)
        {"Tom & &quot;Jerry&quot; &lt;3", "Tom & \"Jerry\" <3"},
        // an entity typed as part of the name
        {"Tom &amp; Jerry", "Tom &amp; Jerry"},
    };

    /** Comment author name as submitted, and the text the feed must contain. */
    private static final String[][] COMMENT_NAMES = {
        {"A & B", "A & B"},
        {"Tom &amp; Jerry", "Tom &amp; Jerry"},
        {"<b>Bold</b>", "<b>Bold</b>"},
        {"José \"Pepe\" O'Brien", "José \"Pepe\" O'Brien"},
    };

    /** Minimal stand-ins for the model objects the feed macros read. */
    public static class StubEntry {
        private final UserWrapper creator;

        StubEntry(UserWrapper creator) {
            this.creator = creator;
        }

        public String getPermalink() {
            return "https://example.org/blog/entry/post";
        }

        public String getTitle() {
            return "Post";
        }

        public UserWrapper getCreator() {
            return creator;
        }

        public Date getPubTime() {
            return new Date(0);
        }

        public Date getUpdateTime() {
            return new Date(0);
        }

        public StubCategory getCategory() {
            return new StubCategory();
        }

        public List<Object> getTags() {
            return Collections.emptyList();
        }

        public String getText() {
            return "Text";
        }

        public String getTransformedText() {
            return "Text";
        }

        public String findEntryAttribute(String name) {
            return null;
        }
    }

    /** Comment stand-in that takes its name from the real comment wrapper. */
    public static class StubComment {
        private final WeblogEntryCommentWrapper wrapper;

        StubComment(String name) {
            WeblogEntryComment comment = new WeblogEntryComment();
            comment.setName(name);
            this.wrapper = WeblogEntryCommentWrapper.wrap(comment, null);
        }

        public String getName() {
            return wrapper.getName();
        }

        public StubEntry getWeblogEntry() {
            return new StubEntry(null);
        }

        public String getTimestamp() {
            return "0";
        }

        public Date getPostTime() {
            return new Date(0);
        }

        public String getContent() {
            return "Comment";
        }
    }

    /** Media file stand-in; media files expose the creator's User directly. */
    public static class StubFile {
        private final User creator;

        StubFile(User creator) {
            this.creator = creator;
        }

        public String getPermalink() {
            return "https://example.org/blog/mediaresource/file";
        }

        public String getName() {
            return "file.png";
        }

        public User getCreator() {
            return creator;
        }

        public Date getDateUploaded() {
            return new Date(0);
        }

        public Date getLastUpdated() {
            return new Date(0);
        }

        public List<Object> getTags() {
            return Collections.emptyList();
        }

        public String getDescription() {
            return null;
        }

        public String getContentType() {
            return "image/png";
        }
    }

    public static class StubCategory {
        public String getName() {
            return "General";
        }
    }

    private static String render(String macro, String name) {
        User user = mock(User.class);
        when(user.getScreenName()).thenReturn(name);
        return render(macro, name, user);
    }

    private static String render(String macro, String name, User fileCreator) {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", "src/main/webapp/WEB-INF/velocity");
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);

        User user = mock(User.class);
        when(user.getScreenName()).thenReturn(name);
        VelocityContext ctx = new VelocityContext();
        ctx.put("entries", List.of(new StubEntry(UserWrapper.wrap(user))));
        ctx.put("comments", List.of(new StubComment(name)));
        ctx.put("files", List.of(new StubFile(fileCreator)));
        ctx.put("utils", new UtilitiesModel());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#parse('feeds.vm')" + macro);
        return out.toString();
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    @Test
    public void atomAuthorIsText() throws Exception {
        for (String[] name : NAMES) {
            String xml = render("<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                    + "#showEntriesAtom10($entries)</feed>", name[0]);
            Document feed = parse(xml);
            assertEquals(name[1], feed.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "name")
                    .item(0).getTextContent(), xml);
        }
    }

    @Test
    public void rssCreatorIsText() throws Exception {
        for (String[] name : NAMES) {
            String xml = render("<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                    + " xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel>"
                    + "#showEntriesRSS20($entries)</channel></rss>", name[0]);
            Document feed = parse(xml);
            assertEquals(name[1], feed.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
                    .item(0).getTextContent(), xml);
        }
    }

    @Test
    public void atomCommentAuthorIsText() throws Exception {
        for (String[] name : COMMENT_NAMES) {
            String xml = render("<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                    + "#showCommentsAtom10($comments)</feed>", name[0]);
            Document feed = parse(xml);
            assertEquals(name[1], feed.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "name")
                    .item(0).getTextContent(), xml);
        }
    }

    @Test
    public void rssCommentCreatorIsText() throws Exception {
        for (String[] name : COMMENT_NAMES) {
            String xml = render("<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                    + " xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel>"
                    + "#showCommentsRSS20($comments)</channel></rss>", name[0]);
            Document feed = parse(xml);
            assertEquals(name[1], feed.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
                    .item(0).getTextContent(), xml);
        }
    }

    @Test
    public void atomMediaFileAuthorIsText() throws Exception {
        for (String[] name : NAMES) {
            String xml = render("<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                    + "#showFilesAtom10($files)</feed>", name[0]);
            Document feed = parse(xml);
            assertEquals(name[1], feed.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "name")
                    .item(0).getTextContent(), xml);
        }
    }

    /**
     * Trusted admins' names are stored as typed, so an entity in one is text
     * in every feed author, except for the quotes, angle brackets and spaces
     * that the sanitizer stores encoded.
     */
    @Test
    public void trustedAdminNamesAreText() throws Exception {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getBooleanProperty("weblogAdminsUntrusted")).thenReturn(false);
            for (String name : new String[] {"Tom &amp; Jerry", "&eacute; &#169;"}) {
                String atom = render("<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                        + "#showEntriesAtom10($entries)#showFilesAtom10($files)</feed>", name);
                NodeList authors = parse(atom).getElementsByTagNameNS("http://www.w3.org/2005/Atom", "name");
                assertEquals(2, authors.getLength(), atom);
                assertEquals(name, authors.item(0).getTextContent(), atom);
                assertEquals(name, authors.item(1).getTextContent(), atom);

                String rss = render("<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                        + " xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel>"
                        + "#showEntriesRSS20($entries)</channel></rss>", name);
                assertEquals(name, parse(rss).getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
                        .item(0).getTextContent(), rss);
            }
        }
    }

    /** A comment saved without a name has an empty author, not template source. */
    @Test
    public void commentWithoutNameHasEmptyAuthor() throws Exception {
        String atom = render("<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                + "#showCommentsAtom10($comments)</feed>", null);
        assertEquals("", parse(atom).getElementsByTagNameNS("http://www.w3.org/2005/Atom", "name")
                .item(0).getTextContent(), atom);

        String rss = render("<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                + " xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel>"
                + "#showCommentsRSS20($comments)</channel></rss>", null);
        assertEquals("", parse(rss).getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "creator")
                .item(0).getTextContent(), rss);
    }

    /** A media file whose creator can't be found has an empty author. */
    @Test
    public void mediaFileWithoutCreatorHasEmptyAuthor() throws Exception {
        String xml = render("<feed xmlns=\"http://www.w3.org/2005/Atom\">"
                + "#showFilesAtom10($files)</feed>", "name", null);
        assertEquals("", parse(xml).getElementsByTagNameNS("http://www.w3.org/2005/Atom", "name")
                .item(0).getTextContent(), xml);
    }
}
