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
 * Pages load webjar files by versioned paths such as
 * <code>/webjars/bootstrap-icons/1.13.2/font/bootstrap-icons.css</code>. When a
 * webjar's version changes in the pom and a path is left behind, the server
 * answers 404 and the page loses its styles or scripts. Every such path must
 * name a file that a webjar on the classpath actually contains.
 */
public class WebjarPathsTest {

    private static final Path WEBAPP = Paths.get(
            System.getProperty("project.basedir", System.getProperty("user.dir")),
            "src", "main", "webapp");

    private static final Pattern WEBJAR_PATH = Pattern.compile("/webjars/([^'\"?#\\s]+)");

    @Test
    public void everyWebjarPathPointsAtAFileInAWebjar() throws IOException {
        List<String> missing = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> files = Files.walk(WEBAPP)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".jsp")
                    || p.toString().endsWith(".vm"))::iterator) {
                String text = Files.readString(file, StandardCharsets.UTF_8);
                Matcher m = WEBJAR_PATH.matcher(text);
                while (m.find()) {
                    checked++;
                    String resource = "META-INF/resources/webjars/" + m.group(1);
                    if (getClass().getClassLoader().getResource(resource) == null) {
                        missing.add(WEBAPP.relativize(file) + ": /webjars/" + m.group(1));
                    }
                }
            }
        }
        assertTrue(checked > 0, "no webjar paths found under " + WEBAPP);
        assertTrue(missing.isEmpty(), "webjar files that no webjar contains:\n" + String.join("\n", missing));
    }
}
