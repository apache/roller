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

package org.apache.roller.weblogger.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Templates, stylesheets and theme descriptors name files by path. When a file
 * is removed or renamed and a reference is left behind, the page shows a
 * broken image or loses a script, and a theme that lists a missing resource
 * cannot be copied into a weblog. Every such path must name a file that
 * exists.
 */
public class ThemeAssetReferencesTest {

    private static final Path WEBAPP = moduleDir().resolve("src/main/webapp");

    private static final Path THEMES = WEBAPP.resolve("themes");

    /**
     * The app module's directory, found from where this class was compiled to
     * (target/test-classes), so the files are found whatever the working
     * directory is, as when the test runs from an IDE.
     */
    private static Path moduleDir() {
        try {
            Path classes = Paths.get(ThemeAssetReferencesTest.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            return classes.getParent().getParent();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** {@code <resource path="..."/>} and {@code <contentsFile>...</contentsFile>} */
    private static final Pattern THEME_XML_FILE = Pattern.compile(
            "<resource\\s+path=\"([^\"]+)\"|<contentsFile>([^<]+)</contentsFile>");

    /** {@code $url.resource('...')} in a theme template */
    private static final Pattern URL_RESOURCE = Pattern.compile(
            "\\$url\\.resource\\(\\s*['\"]([^'\"]+)['\"]\\s*\\)");

    /** a relative {@code url(...)} in a theme's own stylesheet */
    private static final Pattern CSS_URL = Pattern.compile(
            "url\\(\\s*['\"]?(?!data:|https?:|/|\\$|#)([^'\")?#]+)");

    /** a site path to an image or script in a JSP, tag or template */
    private static final Pattern SITE_PATH = Pattern.compile(
            "(?<!\\.)(/(?:roller-ui/|theme/)?(?:images|scripts)/[A-Za-z0-9_./-]+\\.(?:png|gif|jpe?g|svg|js))");

    @Test
    public void everyThemeFileExistsAndIsListed() throws IOException {
        List<String> missing = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> themes = Files.list(THEMES)) {
            for (Path theme : (Iterable<Path>) themes.filter(Files::isDirectory)::iterator) {
                Path descriptor = theme.resolve("theme.xml");
                if (!Files.exists(descriptor)) {
                    continue;
                }
                Set<String> listed = new HashSet<>();
                Matcher m = THEME_XML_FILE.matcher(Files.readString(descriptor, StandardCharsets.UTF_8));
                while (m.find()) {
                    checked++;
                    String file = (m.group(1) != null ? m.group(1) : m.group(2)).trim();
                    listed.add(file);
                    if (!Files.isRegularFile(theme.resolve(file))) {
                        missing.add(WEBAPP.relativize(descriptor) + " lists missing " + file);
                    }
                }

                try (Stream<Path> files = Files.walk(theme)) {
                    for (Path file : (Iterable<Path>) files.filter(Files::isRegularFile)::iterator) {
                        String name = file.getFileName().toString();
                        if (!name.endsWith(".vm") && !name.endsWith(".css")) {
                            continue;
                        }
                        String text = Files.readString(file, StandardCharsets.UTF_8);
                        Matcher r = URL_RESOURCE.matcher(text);
                        while (r.find()) {
                            checked++;
                            String resource = r.group(1);
                            if (!Files.isRegularFile(theme.resolve(resource))) {
                                missing.add(WEBAPP.relativize(file) + " loads missing " + resource);
                            } else if (!listed.contains(resource)) {
                                missing.add(WEBAPP.relativize(file) + " loads " + resource
                                        + ", which theme.xml does not list");
                            }
                        }
                        if (name.endsWith(".css") && !name.equals("bootstrap.css")) {
                            // bootstrap.css is Bootstrap's own stylesheet; its
                            // Glyphicons font is not shipped and no template uses it
                            Matcher u = CSS_URL.matcher(text);
                            while (u.find()) {
                                checked++;
                                Path target = file.getParent().resolve(u.group(1).trim()).normalize();
                                if (!Files.isRegularFile(target)) {
                                    missing.add(WEBAPP.relativize(file) + " refers to missing " + u.group(1));
                                }
                            }
                        }
                    }
                }
            }
        }
        assertTrue(checked > 0, "no theme files checked under " + THEMES);
        assertTrue(missing.isEmpty(), "theme files that do not exist:\n" + String.join("\n", missing));
    }

    @Test
    public void everySiteImageAndScriptPathExists() throws IOException {
        List<String> missing = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> files = Files.walk(WEBAPP)) {
            for (Path file : (Iterable<Path>) files.filter(p -> {
                String name = p.toString();
                return name.endsWith(".jsp") || name.endsWith(".jspf")
                        || name.endsWith(".tag") || name.endsWith(".vm")
                        || name.endsWith(".css");
            })::iterator) {
                Matcher m = SITE_PATH.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (m.find()) {
                    checked++;
                    if (!Files.isRegularFile(WEBAPP.resolve(m.group(1).substring(1)))) {
                        missing.add(WEBAPP.relativize(file) + ": " + m.group(1));
                    }
                }
            }
        }
        assertTrue(checked > 0, "no image or script paths found under " + WEBAPP);
        assertTrue(missing.isEmpty(), "files that do not exist:\n" + String.join("\n", missing));
    }
}
