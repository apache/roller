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

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders parts of the Basic Mobile theme, and the mobile macros it uses from
 * the real weblog.vm, to check they work on a page that loads no jQuery.
 */
public class BasicMobileThemeRenderingTest {

    private static final String THEME_DIR = "src/main/webapp/themes/basicmobile";

    private static VelocityEngine engine;

    @BeforeAll
    public static void setUpEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path",
                "src/main/webapp/WEB-INF/velocity," + THEME_DIR);
        engine = new VelocityEngine();
        engine.init(props);
    }

    public static class StubWeblog {
        private final String stylesheet;
        StubWeblog(String stylesheet) { this.stylesheet = stylesheet; }
        public String getStylesheet() { return stylesheet; }
        public String getName() { return "Blog"; }
    }

    public static class StubModel {
        private final boolean permalink;
        private final StubWeblog weblog;
        StubModel(boolean permalink, String stylesheet) {
            this.permalink = permalink;
            this.weblog = new StubWeblog(stylesheet);
        }
        public boolean getPermalink() { return permalink; }
        public StubWeblog getWeblog() { return weblog; }
        public String getTerm() { return null; }
    }

    public static class StubUrl {
        public String getSearch() { return "/blog/search"; }
        public String editEntry(String anchor) { return "/edit/" + anchor; }
    }

    public static class StubText {
        public String get(String key) { return key; }
    }

    public static class StubUtils {
        public boolean isEmpty(String str) { return str == null || str.trim().isEmpty(); }
        public String formatDate(Date date, String format) { return "date"; }
        public boolean isUserAuthorizedToAuthor(Object weblog) { return false; }
    }

    public static class StubNamed {
        public String getName() { return "General"; }
        public String getScreenName() { return "Author"; }
    }

    /** An entry whose text and summary are plain, already transformed strings. */
    public static class StubEntry {
        private final String text;
        private final String summary;
        StubEntry(String text, String summary) {
            this.text = text;
            this.summary = summary;
        }
        public String getTitle() { return "Title"; }
        public String getAnchor() { return "title"; }
        public String getPermalink() { return "/blog/entry/title"; }
        public int getCommentCount() { return 0; }
        public boolean getCommentsStillAllowed() { return false; }
        public Object getWebsite() { return null; }
        public Date getPubTime() { return new Date(0); }
        public StubNamed getCreator() { return new StubNamed(); }
        public StubNamed getCategory() { return new StubNamed(); }
        public String getSummary() { return summary; }
        public String getText() { return text; }
        public String getTransformedText() { return text; }
        public String getTransformedSummary() { return summary; }
        // as WeblogEntry.getDisplayContent(): text when there is some
        public String getDisplayContent() { return text != null ? text : summary; }
    }

    private static String render(StubModel model, List<StubEntry> entries, String template) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("model", model);
        ctx.put("weblog", model.getWeblog());
        ctx.put("url", new StubUrl());
        ctx.put("text", new StubText());
        ctx.put("utils", new StubUtils());
        ctx.put("day", new Date(0));
        ctx.put("entries", entries);
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#parse('weblog.vm')" + template);
        return out.toString();
    }

    private static String renderDay(boolean permalink, StubEntry entry) {
        return render(new StubModel(permalink, "/blog/page/basic-custom.css"),
                Collections.singletonList(entry), "#parse('_day-mobile.vm')");
    }

    private static int count(String haystack, String needle) {
        int count = 0;
        for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) {
            count++;
        }
        return count;
    }

    /** The stylesheet link of a mobile page template, rendered on its own. */
    private static String renderStylesheetLink(String templateName, String stylesheet)
            throws Exception {
        String link = null;
        for (String line : Files.readAllLines(Paths.get(THEME_DIR, templateName),
                StandardCharsets.UTF_8)) {
            if (line.contains("rel=\"stylesheet\"")) {
                link = line;
            }
        }
        assertTrue(link != null, templateName + " has no stylesheet link");
        return render(new StubModel(false, stylesheet),
                Collections.<StubEntry>emptyList(), link).trim();
    }

    @Test
    public void emptySearchOpensTheAlertWithoutJQuery() {
        String html = render(new StubModel(false, "/blog/page/basic-custom.css"),
                Collections.<StubEntry>emptyList(),
                "#showMobileSearchForm($weblog false)");
        assertTrue(html.contains("function validateSearch(form)"), html);
        assertFalse(html.contains("$("), html);
        assertFalse(html.contains(".popup("), html);
        assertTrue(html.contains("data-role=\"popup\" id=\"searchDialog\""), html);
        assertTrue(html.contains("location.hash = \"searchDialog\""), html);
    }

    @Test
    public void permalinkShowsAnEntryWithoutSummaryOnce() {
        String html = renderDay(true, new StubEntry("ENTRY-BODY", null));
        assertEquals(1, count(html, "ENTRY-BODY"), html);
    }

    @Test
    public void permalinkShowsTheSummaryAboveTheTextOfAnEntryWithBoth() {
        String html = renderDay(true, new StubEntry("ENTRY-BODY", "ENTRY-SUMMARY"));
        assertEquals(1, count(html, "ENTRY-SUMMARY"), html);
        assertEquals(1, count(html, "ENTRY-BODY"), html);
        assertTrue(html.indexOf("ENTRY-SUMMARY") < html.indexOf("ENTRY-BODY"), html);
    }

    @Test
    public void permalinkShowsASummaryWithoutTextOnce() {
        String html = renderDay(true, new StubEntry(null, "ENTRY-SUMMARY"));
        assertEquals(1, count(html, "ENTRY-SUMMARY"), html);
    }

    @Test
    public void listShowsTheTextOfAnEntryWithoutSummary() {
        String html = renderDay(false, new StubEntry("ENTRY-BODY", null));
        assertEquals(1, count(html, "ENTRY-BODY"), html);
        assertTrue(html.contains("<a href=\"/blog/entry/title\""), html);
    }

    @Test
    public void listShowsTheSummaryOfAnEntryThatHasOne() {
        String html = renderDay(false, new StubEntry("ENTRY-BODY", "ENTRY-SUMMARY"));
        assertEquals(1, count(html, "ENTRY-SUMMARY"), html);
        assertEquals(0, count(html, "ENTRY-BODY"), html);
    }

    @Test
    public void mobileStylesheetLinkAddsDeviceType() throws Exception {
        for (String template : new String[] {
                "weblog-mobile.vm", "permalink-mobile.vm", "searchresults-mobile.vm"}) {
            assertEquals("<link rel=\"stylesheet\" "
                            + "href=\"/blog/page/basic-custom.css?deviceType=mobile\">",
                    renderStylesheetLink(template, "/blog/page/basic-custom.css"), template);
        }
    }

    @Test
    public void mobileStylesheetLinkKeepsAnExistingQueryString() throws Exception {
        String preview = "/roller-ui/authoring/preview/blog/page/basic-custom.css?theme=basicmobile";
        for (String template : new String[] {
                "weblog-mobile.vm", "permalink-mobile.vm", "searchresults-mobile.vm"}) {
            assertEquals("<link rel=\"stylesheet\" href=\"" + preview
                            + "&amp;deviceType=mobile\">",
                    renderStylesheetLink(template, preview), template);
        }
    }
}
