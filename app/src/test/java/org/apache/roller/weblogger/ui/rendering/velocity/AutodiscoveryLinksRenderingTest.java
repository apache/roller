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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders #showAutodiscoveryLinks from the real weblog.vm and checks how the
 * requested tag names appear in the feed link titles.
 */
public class AutodiscoveryLinksRenderingTest {

    private static VelocityEngine engine;

    @BeforeAll
    public static void setUpEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", "src/main/webapp/WEB-INF/velocity");
        engine = new VelocityEngine();
        engine.init(props);
    }

    public static class StubWeblog {
        public boolean getEnableBloggerApi() { return false; }
        public String getName() { return "Blog"; }
    }

    public static class StubModel {
        private final List<String> tags;
        StubModel(List<String> tags) { this.tags = tags; }
        public boolean getSearchResults() { return false; }
        public List<String> getTags() { return tags; }
        public Object getWeblogCategory() { return null; }
        public StubWeblog getWeblog() { return new StubWeblog(); }
    }

    public static class StubFeeds {
        public String getAtom() { return "/feed/atom"; }
        public String getRss() { return "/feed/rss"; }
        public String atomByTags(List<String> tags, boolean excerpts) { return "/feed/atom?tags"; }
        public String rssByTags(List<String> tags, boolean excerpts) { return "/feed/rss?tags"; }
    }

    public static class StubFeed {
        public StubFeeds getEntries() { return new StubFeeds(); }
        public StubFeeds getComments() { return new StubFeeds(); }
    }

    public static class StubUrl {
        public StubFeed getFeed() { return new StubFeed(); }
        public String getRsd() { return "/rsd"; }
        public String getAbsoluteSite() { return "http://example.test"; }
        public String getOpensearch() { return "/opensearch"; }
    }

    private static String render(String... tags) {
        VelocityContext ctx = new VelocityContext();
        StubModel model = new StubModel(Arrays.asList(tags));
        ctx.put("model", model);
        ctx.put("weblog", model.getWeblog());
        ctx.put("url", new StubUrl());
        ctx.put("utils", new UtilitiesModel());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#parse('weblog.vm')#showAutodiscoveryLinks($weblog)");
        return out.toString();
    }

    @Test
    public void tagNamesAppearInTheFeedTitles() {
        String html = render("java", "roller");
        assertTrue(html.contains("title=\"Entries tagged [java, roller] (Atom)\""), html);
        assertTrue(html.contains("title=\"Entries tagged [java, roller] (RSS)\""), html);
    }

    @Test
    public void tagNamesAreHtmlEncodedInTheFeedTitles() {
        String html = render("a\"b<c>&d");
        assertTrue(html.contains("title=\"Entries tagged [a&quot;b&lt;c&gt;&amp;d] (Atom)\""), html);
        assertFalse(html.contains("a\"b<c>"), html);
    }
}
