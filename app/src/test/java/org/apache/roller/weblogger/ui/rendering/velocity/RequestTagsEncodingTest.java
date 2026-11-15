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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tags a page is asked for come from the request, so every template that
 * prints them must HTML-encode them.
 */
public class RequestTagsEncodingTest {

    /** A loop over the requested tags; group 1 is the loop variable. */
    private static final Pattern TAG_LOOP =
            Pattern.compile("#foreach\\(\\$(\\w+) in \\$model\\.tags\\)");

    /** The requested tags printed whole, as in "$model.tags". */
    private static final Pattern WHOLE_TAGS =
            Pattern.compile("\\$!?\\{?model\\.tags(?:\\.toString\\(\\))?\\}?(?![.\\w(])");

    /**
     * The app module's directory, found from where this class was compiled to
     * (target/test-classes), so the files are found whatever the working
     * directory is.
     */
    private static Path moduleDir() {
        try {
            Path classes = Paths.get(RequestTagsEncodingTest.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return classes.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void templatesEncodeTheRequestedTags() throws IOException {
        Path webapp = moduleDir().resolve("src/main/webapp");
        List<String> problems = new ArrayList<>();
        List<Path> templates;
        try (Stream<Path> files = Files.walk(webapp)) {
            templates = files.filter(f -> f.toString().endsWith(".vm")).sorted().toList();
        }
        assertTrue(templates.size() > 20, "templates not found under " + webapp);

        for (Path template : templates) {
            List<String> lines = Files.readAllLines(template, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String where = webapp.relativize(template) + ":" + (i + 1);

                Matcher loop = TAG_LOOP.matcher(line);
                if (loop.find()) {
                    String body = line.substring(loop.end());
                    Matcher use = Pattern.compile("\\$!?\\{?" + loop.group(1) + "\\b").matcher(body);
                    while (use.find()) {
                        if (!body.substring(0, use.start()).endsWith("escapeHTML(")) {
                            problems.add(where + ": $" + loop.group(1) + " is printed as is");
                        }
                    }
                }

                Matcher whole = WHOLE_TAGS.matcher(line);
                while (whole.find()) {
                    String before = line.substring(0, whole.start());
                    String after = line.substring(whole.end());
                    boolean encoded = before.endsWith("escapeHTML(");
                    boolean condition = before.matches(".*#(?:else)?if ?\\(\\s*!?\\s*");
                    // a #foreach source, or an argument such as the feed URL
                    // builders take, which encode the tags themselves
                    boolean notPrinted = before.endsWith(" in ")
                            || after.startsWith(",") || after.startsWith(")")
                            || after.startsWith(" &&");
                    if (!encoded && !condition && !notPrinted) {
                        problems.add(where + ": " + whole.group() + " is printed as is");
                    }
                }
            }
        }

        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }
}
