/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 *  under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.  For additional information regarding
 *  copyright in this work, please see the NOTICE file in the top level
 *  directory of this distribution.
 */
package org.apache.roller.weblogger.business;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.jdom2.input.JDOMParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the OPML bookmark import's handling of document type declarations:
 * documents that carry one are refused, while ordinary OPML still imports.
 * Also checks that imported links are kept only when they are web links.
 */
public class BookmarkImportParsingTest {

    private static final Log log = LogFactory.getLog(BookmarkImportParsingTest.class);

    private User testUser = null;
    private Weblog testWeblog = null;
    private final String folderName = "ZZZ_import_parsing_ZZZ";

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        testUser = TestUtils.setupUser("importParsingTestUser");
        testWeblog = TestUtils.setupWeblog("importParsingTestWeblog", testUser);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tearDown", ex);
        }
    }

    private BookmarkManager bookmarkManager() {
        return WebloggerFactory.getWeblogger().getBookmarkManager();
    }

    /** @return the bookmarks imported into the test folder, empty if none */
    private java.util.List<WeblogBookmark> importedBookmarks() throws Exception {
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        WeblogBookmarkFolder folder = bookmarkManager().getFolder(testWeblog, folderName);
        if (folder == null) {
            return java.util.Collections.emptyList();
        }
        return folder.retrieveBookmarks();
    }

    private void assertDoctypeRejected(String opml) throws Exception {
        Exception failure = null;
        try {
            bookmarkManager().importBookmarks(
                    TestUtils.getManagedWebsite(testWeblog), folderName, opml);
        } catch (Exception expected) {
            failure = expected;
        } finally {
            TestUtils.endSession(true);
        }
        assertTrue(failure instanceof org.apache.roller.weblogger.WebloggerException,
                "DOCTYPE input must produce a WebloggerException: " + failure);
        assertTrue(failure.getMessage().contains("document type declarations"),
                "the import error should explain the rejected input: " + failure.getMessage());
        Throwable rootCause = ((org.apache.roller.weblogger.WebloggerException) failure)
                .getRootCause();
        assertTrue(rootCause instanceof JDOMParseException,
                "the parse cause should be retained: " + rootCause);
    }

    /** Ordinary OPML, with no declarations in it, must still import. */
    @Test
    public void ordinaryOpmlStillImports() throws Exception {
        String opml;
        try (InputStream in = getClass().getResourceAsStream("/bookmarks.opml")) {
            assertTrue(in != null, "the ordinary OPML fixture must be on the classpath");
            opml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try {
            bookmarkManager().importBookmarks(TestUtils.getManagedWebsite(testWeblog),
                    folderName, opml);
        } finally {
            TestUtils.endSession(true);
        }

        assertFalse(importedBookmarks().isEmpty(),
                "ordinary OPML no longer imports any bookmarks");
    }

    /** The rejection must not depend on where the DOCTYPE points. */
    @Test
    public void aDoctypeAloneIsEnoughToBeRefused() throws Exception {
        String opml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE opml [<!ELEMENT opml ANY>]>"
                + "<opml version=\"1.1\"><head><title>t</title></head><body>"
                + "<outline text=\"harmless\" type=\"link\" url=\"http://example.test/\"/>"
                + "</body></opml>";

        assertDoctypeRejected(opml);

        assertTrue(importedBookmarks().isEmpty(),
                "a document carrying a DOCTYPE was still imported");
    }

    /** Only absolute http and https links are imported. */
    @Test
    public void importKeepsOnlyWebLinks() throws Exception {
        String opml = "<?xml version=\"1.0\"?><opml version=\"1.1\"><head><title>t</title></head><body>"
                + "<outline text=\"web\" htmlUrl=\"https://example.com/a\" xmlUrl=\"http://example.com/feed\"/>"
                + "<outline text=\"ftp\" htmlUrl=\"ftp://example.com/a\"/>"
                + "<outline text=\"mail\" htmlUrl=\"mailto:reader@example.com\"/>"
                + "<outline text=\"relative\" htmlUrl=\"/a/b\"/>"
                + "<outline text=\"spaces\" htmlUrl=\"http://example.com/a b\"/>"
                + "<outline text=\"feed\" htmlUrl=\"https://example.com/b\" xmlUrl=\"file:///etc/feed\"/>"
                + "</body></opml>";
        bookmarkManager().importBookmarks(TestUtils.getManagedWebsite(testWeblog), folderName, opml);
        TestUtils.endSession(true);

        java.util.Map<String, WeblogBookmark> byName = new java.util.HashMap<>();
        for (WeblogBookmark b : importedBookmarks()) {
            byName.put(b.getName(), b);
        }
        assertEquals(java.util.Set.of("web", "feed"), byName.keySet());
        assertEquals("https://example.com/a", byName.get("web").getUrl());
        assertEquals("http://example.com/feed", byName.get("web").getFeedUrl());
        assertEquals("https://example.com/b", byName.get("feed").getUrl());
        assertNull(byName.get("feed").getFeedUrl());
    }
}
