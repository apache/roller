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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletContext;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.ui.core.RollerContext;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Lets tests drive the real {@link RollerVelocity} engine, configured from the
 * shipped WEB-INF/velocity.properties, without a servlet container. Files under
 * src/main/webapp are served as they are; a test can add its own templates,
 * which are served from /WEB-INF/velocity/ by name.
 */
final class RollerVelocityTestSupport {

    private static final Path WEBAPP = Paths.get(
            System.getProperty("project.basedir", System.getProperty("user.dir")),
            "src", "main", "webapp");

    private static final String TEMPLATE_DIR = "/WEB-INF/velocity/";

    private static final Map<String, String> TEMPLATES = new ConcurrentHashMap<>();

    private static boolean installed = false;

    private RollerVelocityTestSupport() {
    }

    /** Points RollerContext at a servlet context that serves the webapp. */
    static synchronized void install() throws Exception {
        if (installed) {
            return;
        }
        ServletContext context = mock(ServletContext.class);
        when(context.getResourceAsStream(anyString()))
                .thenAnswer(inv -> open(inv.getArgument(0)));
        Field field = RollerContext.class.getDeclaredField("servletContext");
        field.setAccessible(true);
        field.set(null, context);
        installed = true;
    }

    /**
     * Sets a Roller property, or removes it when value is null, until the
     * returned handle is closed, which puts the old value back.
     */
    static AutoCloseable property(String name, String value) throws Exception {
        Field field = WebloggerConfig.class.getDeclaredField("config");
        field.setAccessible(true);
        Properties config = (Properties) field.get(null);
        String old = config.getProperty(name);
        set(config, name, value);
        return () -> set(config, name, old);
    }

    private static void set(Properties config, String name, String value) {
        if (value == null) {
            config.remove(name);
        } else {
            config.setProperty(name, value);
        }
    }

    /** Adds a template the engine can load by name. Names must be unique. */
    static void addTemplate(String name, String source) {
        TEMPLATES.put(name, source);
    }

    private static InputStream open(String path) throws IOException {
        if (path.startsWith(TEMPLATE_DIR)) {
            String source = TEMPLATES.get(path.substring(TEMPLATE_DIR.length()));
            if (source != null) {
                return new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            }
        }
        Path file = WEBAPP.resolve(path.substring(1)).normalize();
        if (file.startsWith(WEBAPP) && Files.isRegularFile(file)) {
            return Files.newInputStream(file);
        }
        return null;
    }

    /** A page template known to the renderer only by its id. */
    static Template page(final String id) {
        return new Template() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public String getName() {
                return id;
            }

            @Override
            public String getDescription() {
                return id;
            }

            @Override
            public Date getLastModified() {
                return new Date();
            }

            @Override
            public String getOutputContentType() {
                return null;
            }

            @Override
            public TemplateRendition getTemplateRendition(RenditionType type) {
                return null;
            }
        };
    }

    /**
     * Collects output up to a ceiling well above Roller's own limit, so a
     * render the limit fails to stop still ends, and the test can say so.
     */
    static final class CeilingWriter extends Writer {

        static final long CEILING = 64L * 1024 * 1024;

        private long count = 0;
        private final StringBuilder head = new StringBuilder();

        @Override
        public void write(char[] cbuf, int off, int len) {
            count += len;
            if (head.length() < 4096) {
                head.append(cbuf, off, Math.min(len, 4096 - head.length()));
            }
            if (count > CEILING) {
                throw new IllegalStateException("test ceiling reached");
            }
        }

        long getCount() {
            return count;
        }

        String getHead() {
            return head.toString();
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }
}
