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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers where a weblog template may resolve resources from.
 *
 * <p>Weblog templates are authored by weblog administrators, a role Roller
 * treats as untrusted and renders under <code>SecureUberspector</code>. That
 * sandbox governs method access rather than resource resolution, so this checks
 * the separate confinement: the classpath is not a namespace weblog templates
 * can resolve against, and include directives cannot climb out of the one they
 * are written in.
 */
public class ThemeIncludeConfinementTest {

    /**
     * Every Velocity configuration in the tree, because a second copy that
     * still admits the classpath is a copy that can quietly become live.
     */
    private static final Path[] VELOCITY_PROPERTIES = {
            Paths.get("src", "main", "webapp", "WEB-INF", "velocity.properties"),
            Paths.get("src", "test", "resources", "WEB-INF", "velocity.properties"),
    };

    private String read(Path path) throws Exception {
        assertTrue(Files.isReadable(path),
                "cannot read " + path.toAbsolutePath() + " (run from the app module)");
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    /**
     * The classpath must not be in the loader set used for weblog rendering.
     * With it present, any file packaged in the WAR is resolvable by name.
     */
    @Test
    public void classpathIsNotAResolvableNamespace() throws Exception {
        for (Path path : VELOCITY_PROPERTIES) {
            String props = read(path);
            for (String line : props.split("\n")) {
                String trimmed = line.trim();
                if (trimmed.startsWith("resource.loaders")) {
                    assertFalse(trimmed.matches(".*\\bclass\\b.*"),
                            path + ": the classpath loader must not be in the weblog "
                                    + "loader set: " + trimmed);
                }
            }
            assertFalse(props.contains("ClasspathResourceLoader"),
                    path + ": the classpath loader must not be configured for "
                            + "weblog rendering");
        }
    }

    /** The include handler must actually be registered, under Velocity 2's key. */
    @Test
    public void includeHandlerIsRegistered() throws Exception {
        for (Path path : VELOCITY_PROPERTIES) {
            assertTrue(read(path).contains(
                            "event_handler.include.class=org.apache.roller.weblogger.ui."
                                    + "rendering.velocity.ThemeIncludeEventHandler"),
                    path + ": the include event handler must be registered under "
                            + "Velocity 2's event_handler.include.class key");
        }
    }

    /** The sandbox that governs method access stays in place alongside it. */
    @Test
    public void secureUberspectorIsRetained() throws Exception {
        for (Path path : VELOCITY_PROPERTIES) {
            assertTrue(read(path).contains("SecureUberspector"),
                    path + ": the introspection sandbox must be retained");
        }
    }

    /** Names that reach outside the namespace are refused. */
    @Test
    public void namesThatLeaveTheNamespaceAreRefused() {
        ThemeIncludeEventHandler handler = new ThemeIncludeEventHandler();
        String[] refused = {
                "/WEB-INF/classes/roller-custom.properties",
                "../roller-custom.properties",
                "../../WEB-INF/classes/roller-custom.properties",
                "themes/../../roller-custom.properties",
                "..",
                "file:/etc/passwd",
                "http://example.test/evil.vm",
                "\\WEB-INF\\classes\\roller-custom.properties",
                "",
                "   ",
        };
        for (String name : refused) {
            assertNull(handler.includeEvent(new VelocityContext(), name, "weblog.vm", "include"),
                    "expected [" + name + "] to be refused");
        }
        assertNull(handler.includeEvent(new VelocityContext(), null, "weblog.vm", "include"),
                "a null resource name must be refused");
    }

    /**
     * The shapes Roller itself includes must still pass: a stored template
     * resolved by id, and the feed templates the servlets name directly.
     */
    @Test
    public void legitimateIncludesStillPass() {
        ThemeIncludeEventHandler handler = new ThemeIncludeEventHandler();
        String[] allowed = {
                "9cf62fb5-9e6e-11f1-8b02-0e09da24358c|standard", // stored template id
                "_day.vm",                                        // theme resource
                "feeds/weblog-search-atom.vm",                    // servlet-named feed
                "site-search-atom.vm",
        };
        for (String name : allowed) {
            assertEquals(name,
                    handler.includeEvent(new VelocityContext(), name, "weblog.vm", "parse"),
                    "expected [" + name + "] to be allowed through");
        }
    }

    /**
     * End to end against the real engine.
     *
     * <p>Velocity's ClasspathResourceLoader resolves a plain resource name
     * against the classpath, with no traversal involved, so a loader set that
     * includes it makes any packaged file resolvable by name. This renders the
     * same template with that loader and without it, which anchors the
     * assertion to a demonstrated difference rather than to an include that
     * might not have resolved under either configuration.
     */
    @Test
    public void classpathResourcesAreUnreachableOnceTheLoaderIsRemoved() throws Exception {
        Path dir = Files.createTempDirectory("roller-include-confinement");
        Files.write(dir.resolve("include-by-name.vm"),
                "BEFORE[#include(\"roller-custom.properties\")]AFTER"
                        .getBytes(StandardCharsets.UTF_8));

        String withClasspath = render(dir, true);
        assertTrue(withClasspath.contains("database.jdbc"),
                "control failed: the classpath loader did not resolve the resource, so "
                        + "this test cannot show that the loader set matters:\n" + withClasspath);

        String withoutClasspath = render(dir, false);
        assertFalse(withoutClasspath.contains("database.jdbc"),
                "a weblog template resolved a classpath resource:\n" + withoutClasspath);
    }

    /**
     * Renders include-by-name.vm with and without the classpath in the loader set,
     * mirroring the shipped configuration in each case.
     */
    private String render(Path dir, boolean includeClasspathLoader) {
        Properties props = new Properties();
        props.setProperty("resource.loaders", includeClasspathLoader ? "file, class" : "file");
        props.setProperty("resource.loader.file.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("resource.loader.file.path", dir.toString());
        props.setProperty("resource.loader.class.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("event_handler.include.class",
                ThemeIncludeEventHandler.class.getName());
        VelocityEngine engine = new VelocityEngine();
        engine.init(props);

        StringWriter out = new StringWriter();
        try {
            engine.mergeTemplate("include-by-name.vm", "UTF-8", new VelocityContext(), out);
        } catch (Exception ex) {
            // Velocity raises when nothing can resolve the name, which is the
            // outcome we want in the without-classpath case.
            return "unresolved: " + ex.getClass().getSimpleName();
        }
        return out.toString();
    }
}
