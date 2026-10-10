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

package org.apache.roller.weblogger.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the rich text editor's menus: Summernote must write its menu icons
 * as {@code <i ...></i>}, and Roller's panel chevron must not land on the
 * editor toolbar's menu items.
 */
public class EntryEditorAssetsTest {

    private static final Path WEBAPP = Paths.get("src/main/webapp");

    @Test
    public void summernoteClosesItsIconElements() throws IOException {
        String head = read(WEBAPP.resolve("WEB-INF/jsps/tiles/head.jsp"));
        Matcher script = Pattern.compile("/webjars/(summernote/[^/]+/dist/summernote\\.min\\.js)")
                .matcher(head);
        assertTrue(script.find(), "head.jsp loads Summernote");

        String resource = "META-INF/resources/webjars/" + script.group(1);
        String js;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "the Summernote webjar named in head.jsp is on the classpath: " + resource);
            js = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Summernote 0.8 built icons as <i class="..."/>. jQuery 3.5+ keeps that
        // as an open <i>, which swallows the menu label after it.
        assertFalse(Pattern.compile("class=\"'\\+\\w+\\+'\"/>").matcher(js).find(),
                "Summernote must not write icons as self-closing <i/>");
    }

    @Test
    public void panelChevronIsOnlyOnCollapseToggles() throws IOException {
        String css = read(WEBAPP.resolve("roller-ui/styles/roller.css"));
        Matcher rule = Pattern.compile("([^{}]*\\.panel-heading\\s+a[^{}]*:after)\\s*\\{").matcher(css);
        boolean found = false;
        while (rule.find()) {
            found = true;
            assertTrue(rule.group(1).contains("[data-toggle=\"collapse\"]"),
                    "rule must be limited to collapse toggles: " + rule.group(1).trim());
        }
        assertTrue(found, "roller.css still styles the panel collapse toggles");
    }

    private static String read(Path file) throws IOException {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }
}
