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

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tag names are printed by the shared macro library and by bundled themes.
 * They must be HTML-escaped wherever they are printed as text.
 */
public class TagNameRenderingTest {

    private static final String WEBAPP = "src/main/webapp";

    /** Stand-in for a tag as the templates see it. */
    public static class StubTag {
        private final String name;
        StubTag(String name) { this.name = name; }
        public String getName() { return name; }
    }

    public static class StubEntry {
        private final List<StubTag> tags = new ArrayList<>();
        public List<StubTag> getTags() { return tags; }
    }

    public static class StubUrl {
        public String tag(String name) { return "/tags/x"; }
    }

    @Test
    public void showEntryTagsEscapesTagNames() throws Exception {
        Properties props = new Properties();
        props.setProperty("resource.loaders", "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", WEBAPP + "/WEB-INF/velocity");
        props.setProperty("velocimacro.library.path", "weblog.vm");
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);

        StubEntry entry = new StubEntry();
        entry.getTags().add(new StubTag("a<b>&c"));
        VelocityContext ctx = new VelocityContext();
        ctx.put("entry", entry);
        ctx.put("url", new StubUrl());
        ctx.put("utils", new UtilitiesModel());
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "test", "#showEntryTags($entry)");
        String html = out.toString();

        assertTrue(html.contains(">a&lt;b&gt;&amp;c</a>"), html);
        assertFalse(html.contains("<b>"), html);
    }

    /**
     * Every bundled template that prints a tag name as element text must
     * escape it.
     */
    @Test
    public void bundledTemplatesEscapeTagNameText() throws IOException {
        Pattern raw = Pattern.compile(">\\s*\\$!?\\{?tag\\.name\\}?\\s*<");
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Paths.get(WEBAPP))) {
            files.filter(p -> p.toString().endsWith(".vm")).forEach(p -> {
                try {
                    Matcher m = raw.matcher(new String(Files.readAllBytes(p), StandardCharsets.UTF_8));
                    if (m.find()) {
                        offenders.add(p.toString());
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
        }
        assertTrue(offenders.isEmpty(), "unescaped tag names in " + offenders);
    }
}
