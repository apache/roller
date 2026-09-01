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
package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders the bundled frontpage blog-directory template against the real
 * Velocity engine and asserts how it treats the caller-supplied
 * <code>letter</code> parameter.
 *
 * <p>The template is reached anonymously, so the parameter is untrusted. The
 * contract is that only a value which normalizes to one of the directory's own
 * A-Z keys is used, and that anything else falls back to the complete directory
 * without the rejected value appearing in the response in any form — raw,
 * HTML-encoded, or URL-encoded.
 */
public class FrontpageDirectoryRenderingTest {

    private static final String THEME_DIR = "src/main/webapp/themes/frontpage";
    private static final String TEMPLATE = "_blogdirectory.vm";

    private static VelocityEngine engine;

    @BeforeAll
    public static void setUpEngine() {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", THEME_DIR);
        engine = new VelocityEngine();
        engine.init(props);
    }

    /** Minimal stand-ins for the model objects the template reads. */
    public static class StubModel {
        private final String letter;
        private final String weblog;
        StubModel(String letter) { this(letter, null); }
        StubModel(String letter, String weblog) {
            this.letter = letter;
            this.weblog = weblog;
        }
        public String getRequestParameter(String name) {
            if ("letter".equals(name)) { return letter; }
            return "weblog".equals(name) ? weblog : null;
        }
    }

    public static class StubPager {
        public List<Object> getItems() { return new ArrayList<>(); }
        public String prevLink() { return null; }
        public String nextLink() { return null; }
        public String prevName() { return null; }
        public String nextName() { return null; }
    }

    public static class StubSite {
        public Map<String, Long> getWeblogHandleLetterMap() {
            Map<String, Long> map = new LinkedHashMap<>();
            for (char c = 'A'; c <= 'Z'; c++) {
                map.put(String.valueOf(c), 1L);
            }
            return map;
        }
        public StubPager getWeblogsByLetterPager(String letter, int offset, int length) {
            return new StubPager();
        }
        public StubWeblog getWeblog(String handle) {
            if ("alice".equals(handle) || "2blog".equals(handle)) {
                return new StubWeblog(handle);
            }
            return null;
        }
        public List<Object> getNewWeblogs(int since, int maxResults) {
            return new ArrayList<>();
        }
    }

    public static class StubWeblog {
        private final String handle;
        StubWeblog(String handle) { this.handle = handle; }
        public String getHandle() { return handle; }
    }

    public static class StubUrl {
        public String getAbsoluteSite() { return "http://example.test"; }
        public String getHome() { return "/"; }
        public String page(String name) { return "/page/" + name; }
    }

    private String render(String letterParam) throws Exception {
        VelocityContext ctx = new VelocityContext();
        ctx.put("model", new StubModel(letterParam));
        ctx.put("site", new StubSite());
        ctx.put("utils", new UtilitiesModel());
        ctx.put("url", new StubUrl());
        ctx.put("pageLength", 30);
        StringWriter out = new StringWriter();
        engine.mergeTemplate(TEMPLATE, "UTF-8", ctx, out);
        return out.toString();
    }

    @Test
    public void missingLetterRendersCompleteDirectory() throws Exception {
        String html = render(null);
        assertTrue(html.contains("All weblogs"),
                "a missing letter must render the complete directory:\n" + html);
        assertFalse(html.contains("Weblogs starting with"),
                "a missing letter must not render a filtered heading");
    }

    @Test
    public void validUppercaseLetterIsAccepted() throws Exception {
        String html = render("A");
        assertTrue(html.contains("Weblogs starting with A"),
                "a valid key must be accepted:\n" + html);
    }

    @Test
    public void lowercaseLetterNormalizesToTheSameGroup() throws Exception {
        assertTrue(render("a").contains("Weblogs starting with A"),
                "lowercase input must normalize to the uppercase key");
    }

    /**
     * Every value that is not a single A-Z key must be discarded outright and
     * must not be echoed, raw or encoded.
     */
    @Test
    public void invalidValuesFallBackAndAreNotEchoed() throws Exception {
        String[] rejected = {
                "AB",                       // multi-character
                "1",                        // numeric
                "!",                        // punctuation
                "é",                   // non-ASCII
                "<script>alert(1)</script>", // script payload
                "\" onmouseover=\"alert(1)", // attribute-breaking payload
                "A<b>",                     // valid prefix, invalid remainder
        };
        for (String value : rejected) {
            String html = render(value);
            assertTrue(html.contains("All weblogs"),
                    "rejected value [" + value + "] must fall back to the complete "
                            + "directory:\n" + html);
            // Assert against the heading directly. A bare contains(value) would
            // match incidentally: single characters such as "1" occur naturally
            // in the rendered letter counts.
            assertFalse(html.contains("Weblogs starting with"),
                    "rejected value [" + value + "] produced a filtered heading:\n" + html);
            assertFalse(html.contains("<script") || html.contains("&lt;script"),
                    "rejected value [" + value + "] reached the page, raw or encoded:\n" + html);
            assertFalse(html.contains("onmouseover"),
                    "rejected value [" + value + "] leaked an event handler:\n" + html);
        }
    }

    private String renderDirectory(String weblogParam) throws Exception {
        VelocityContext ctx = new VelocityContext();
        ctx.put("model", new StubModel(null, weblogParam));
        ctx.put("site", new StubSite());
        ctx.put("utils", new UtilitiesModel());
        ctx.put("url", new StubUrl());
        ctx.put("config", new Object() {
            public String getSiteName() { return "Example"; }
            public String getSiteDescription() { return "Description"; }
        });
        StringWriter out = new StringWriter();
        String source = "#macro(includeTemplate $weblog $template)#end\n"
                + "#parse('directory.vm')";
        engine.evaluate(ctx, out, "directory-test", source);
        return out.toString();
    }

    @Test
    public void directoryTemplateValidatesTheWeblogParameter() throws Exception {
        String unknown = renderDirectory("<script>alert(1)</script>");
        assertFalse(unknown.contains("Back to blog directory"),
                "invalid handles must not reach the weblog lookup or profile link");

        String alpha = renderDirectory("alice");
        assertTrue(alpha.contains("href=\"?letter=A\""),
                "alphabetic resolved handles must link to their normalized directory group:\n"
                        + alpha);

        String numeric = renderDirectory("2blog");
        assertTrue(numeric.contains("href=\"?\""),
                "numeric resolved handles must omit the letter filter:\n" + numeric);
    }

    /**
     * Guards the test itself: if the template stopped rendering, or the theme
     * moved, every assertion above would pass or fail for the wrong reason.
     */
    @Test
    public void templateActuallyRenders() throws Exception {
        String html = render("A");
        assertTrue(html.contains("blogdirectory"),
                "expected the directory table to render:\n" + html);
        assertTrue(html.contains("letterMap"),
                "expected the A-Z letter map to render:\n" + html);
    }
}
