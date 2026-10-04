/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the
 * top level directory of this distribution.
 */
package org.apache.roller.weblogger.pojos.wrapper;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Covers what a template can reach through the pojo wrappers.
 *
 * <p>Weblog templates are authored by weblog administrators, a role Roller
 * treats as untrusted and renders under <code>SecureUberspector</code>. The
 * wrappers exist precisely so that role only sees a safe surface, so the
 * wrapped objects themselves must stay out of the template-visible surface.
 * Java rendering code that does need them goes through {@link Wrappers},
 * which is never put in a template context.
 */
public class WrapperPojoConfinementTest {

    private static final Class<?>[] WRAPPED_TYPES = {
            Weblog.class, WeblogEntry.class, User.class,
    };

    private static VelocityEngine engine;

    /** The configuration the weblog renderer loads. */
    private static final Path VELOCITY_CONFIG =
            Paths.get("src", "main", "webapp", "WEB-INF", "velocity.properties");

    @BeforeAll
    public static void setUpEngine() throws Exception {
        // Load the shipped renderer configuration, so the introspection
        // settings under test are the real ones. Only the settings that need
        // a running webapp (resource loaders, macro libraries, the include
        // handler and the logger) are dropped.
        Properties shipped = new Properties();
        try (InputStream in = Files.newInputStream(VELOCITY_CONFIG)) {
            shipped.load(in);
        }
        assertNotNull(shipped.getProperty("introspector.uberspect.class"),
                VELOCITY_CONFIG + " no longer sets an uberspector");

        Properties props = new Properties();
        for (String key : shipped.stringPropertyNames()) {
            if (!key.startsWith("resource.loader")
                    && !key.startsWith("velocimacro.")
                    && !key.startsWith("event_handler.")
                    && !key.startsWith("runtime.log.logsystem")) {
                props.setProperty(key, shipped.getProperty(key));
            }
        }
        engine = new VelocityEngine();
        engine.init(props);
    }

    private static String render(String template, VelocityContext ctx)
            throws Exception {
        StringWriter out = new StringWriter();
        engine.evaluate(ctx, out, "wrapper-confinement", template);
        return out.toString();
    }

    /**
     * The sandbox reaches public methods only, so none of the wrapper's
     * template-visible methods may hand out the wrapped types — directly or
     * through any other public signature the classes carry.
     */
    @Test
    public void wrapperSurfaceHandsOutNoWrappedObjects() {
        for (Class<?> wrapper : Arrays.asList(
                WeblogWrapper.class, WeblogEntryWrapper.class)) {
            for (Method method : wrapper.getMethods()) {
                for (Class<?> wrapped : WRAPPED_TYPES) {
                    if (wrapped.equals(method.getReturnType())) {
                        fail(wrapper.getSimpleName() + "." + method.getName()
                                + " hands out " + wrapped.getSimpleName()
                                + " through its template-visible surface");
                    }
                }
            }
        }
    }

    /**
     * End to end against the real engine: an unresolved reference is left
     * in the output as-is, so each of these renders its own literal text
     * when the sandbox finds nothing to call.
     */
    @Test
    public void templatesDoNotResolveTheWrappedObjects() throws Exception {
        Weblog weblog = new Weblog();
        weblog.setName("confinement");
        WeblogEntry entry = new WeblogEntry();

        VelocityContext ctx = new VelocityContext();
        ctx.put("weblog", WeblogWrapper.wrap(weblog, null));
        ctx.put("entry", WeblogEntryWrapper.wrap(entry, null));

        // control: the wrappers themselves are resolvable
        assertTrue(render("[$weblog.name]", ctx).contains("confinement"),
                "control failed: the wrapper is not visible to the engine, so "
                        + "the assertions below can show nothing");

        String[] mustStayUnresolved = {
                "$weblog.pojo",
                "$weblog.getPojo()",
                "$weblog.pojo.handle",
                "$entry.pojo",
                "$entry.getPojo()",
        };
        for (String reference : mustStayUnresolved) {
            assertEquals(reference, render(reference, ctx),
                    "the template resolved [" + reference + "] to one of the "
                            + "wrapped objects");
        }
    }

    /**
     * The Java door still opens: the pagers and permission checks keep
     * working on the same object the wrapper holds.
     */
    @Test
    public void javaCodeStillReachesTheWrappedObjects() {
        Weblog weblog = new Weblog();
        WeblogEntry entry = new WeblogEntry();

        assertSame(weblog, Wrappers.unwrap(WeblogWrapper.wrap(weblog, null)));
        assertSame(entry, Wrappers.unwrap(WeblogEntryWrapper.wrap(entry, null)));
    }
}
