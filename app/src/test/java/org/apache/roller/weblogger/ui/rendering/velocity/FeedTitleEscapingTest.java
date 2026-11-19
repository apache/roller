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
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An RSS title is plain text, so the feed macros must turn the entities in an
 * entry title, which is HTML and may have been through the sanitizer, back
 * into text and XML-escape them once.
 */
public class FeedTitleEscapingTest {

    /** Entry title, and the text the RSS title must contain. */
    private static final String[][] TITLES = {
        {"Tom & Jerry", "Tom & Jerry"},
        {HTMLSanitizer.sanitize("Tom & \"Jerry\""), "Tom & \"Jerry\""},
        {"AT&amp;T &lt;b&gt;", "AT&T <b>"},
    };

    public static class TitledEntry extends FeedAuthorEscapingTest.StubEntry {
        private final String title;

        TitledEntry(String title) {
            super(null);
            this.title = title;
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    public static class TitledComment extends FeedAuthorEscapingTest.StubComment {
        private final String title;

        TitledComment(String title) {
            super("Reader");
            this.title = title;
        }

        @Override
        public FeedAuthorEscapingTest.StubEntry getWeblogEntry() {
            return new TitledEntry(title);
        }
    }

    private static String rssTitle(String macro, String title) throws Exception {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", "src/main/webapp/WEB-INF/velocity");
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);

        VelocityContext ctx = new VelocityContext();
        ctx.put("entries", List.of(new TitledEntry(title)));
        ctx.put("comments", List.of(new TitledComment(title)));
        ctx.put("utils", new UtilitiesModel());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#parse('feeds.vm')"
                + "<rss xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
                + " xmlns:atom=\"http://www.w3.org/2005/Atom\"><channel>" + macro + "</channel></rss>");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document feed = factory.newDocumentBuilder().parse(new InputSource(new StringReader(out.toString())));
        return feed.getElementsByTagName("title").item(0).getTextContent();
    }

    @Test
    public void rssEntryTitleIsText() throws Exception {
        for (String[] title : TITLES) {
            assertEquals(title[1], rssTitle("#showEntriesRSS20($entries)", title[0]));
        }
    }

    @Test
    public void rssCommentTitleIsText() throws Exception {
        for (String[] title : TITLES) {
            assertEquals("Re: " + title[1], rssTitle("#showCommentsRSS20($comments)", title[0]));
        }
    }
}
