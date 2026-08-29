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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.business;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers what the OPML bookmark import will resolve while parsing.
 *
 * <p>The OPML document is supplied by a weblog administrator, a role Roller
 * treats as untrusted. The parser must therefore take the document as data:
 * the declarations in it name resources, and naming a resource must not cause
 * the server to go and read it.
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

    private void tryImport(String opml) {
        try {
            bookmarkManager().importBookmarks(
                    TestUtils.getManagedWebsite(testWeblog), folderName, opml);
            TestUtils.endSession(true);
        } catch (Exception expected) {
            // A refusal to parse is one acceptable outcome; the assertions in
            // each test say what must be true either way.
            log.debug("import raised: " + expected);
        }
    }

    /**
     * A declaration naming a local file must not put that file's contents into
     * the imported data.
     *
     * <p>The reference has to sit in element content rather than an attribute
     * value, which XML does not allow it in, and the file has to hold markup
     * the importer will walk. That is the shape that stores what it read.
     */
    @Test
    public void aFileNamedByTheDocumentIsNotReadIntoBookmarks() throws Exception {
        Path secret = Files.createTempFile("roller-import-probe", ".xml");
        Files.write(secret, ("<outline text=\"PROBE-CONTENT-9f3a\" type=\"link\" "
                + "url=\"http://probe.invalid/\"/>").getBytes(StandardCharsets.UTF_8));

        String opml = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE opml [<!ENTITY probe SYSTEM \""
                + secret.toUri() + "\">]>"
                + "<opml version=\"1.1\"><head><title>t</title></head><body>"
                + "&probe;"
                + "<outline text=\"normal\" type=\"link\" url=\"http://example.test/\"/>"
                + "</body></opml>";

        tryImport(opml);

        for (WeblogBookmark bookmark : importedBookmarks()) {
            String name = String.valueOf(bookmark.getName());
            String desc = String.valueOf(bookmark.getDescription());
            assertFalse(name.contains("PROBE-CONTENT") || desc.contains("PROBE-CONTENT"),
                    "file contents named by the document reached a bookmark: "
                            + name + " / " + desc);
        }

        Files.deleteIfExists(secret);
    }

    /**
     * A declaration naming an http resource must not cause the server to
     * request it. Asserted against a listener that counts connections.
     */
    @Test
    public void anHttpResourceNamedByTheDocumentIsNotRequested() throws Exception {
        AtomicInteger connections = new AtomicInteger();
        try (ServerSocket listener = new ServerSocket(0)) {
            listener.setSoTimeout(2000);
            Thread accepting = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try (Socket s = listener.accept()) {
                        connections.incrementAndGet();
                    } catch (Exception stop) {
                        return;
                    }
                }
            });
            accepting.setDaemon(true);
            accepting.start();

            String url = "http://127.0.0.1:" + listener.getLocalPort() + "/probe.dtd";
            String opml = "<?xml version=\"1.0\"?>"
                    + "<!DOCTYPE opml SYSTEM \"" + url + "\">"
                    + "<opml version=\"1.1\"><head><title>t</title></head><body>"
                    + "<outline text=\"x\" type=\"link\" url=\"http://example.test/\"/>"
                    + "</body></opml>";

            tryImport(opml);
            Thread.sleep(300);
            accepting.interrupt();

            assertEquals(0, connections.get(),
                    "the import requested a resource named by the document");
        }
    }

    /** Ordinary OPML, with no declarations in it, must still import. */
    @Test
    public void ordinaryOpmlStillImports() throws Exception {
        byte[] opml = Files.readAllBytes(
                new File("src/test/resources/bookmarks.opml").toPath());
        bookmarkManager().importBookmarks(TestUtils.getManagedWebsite(testWeblog),
                folderName, new String(opml, StandardCharsets.UTF_8));
        TestUtils.endSession(true);

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

        tryImport(opml);

        assertTrue(importedBookmarks().isEmpty(),
                "a document carrying a DOCTYPE was still imported");
    }
}
