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

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * What applies while one template is rendered on this thread: the weblog whose
 * stored templates it may load, and how much output and time it may use.
 *
 * <p>Weblog templates are written by weblog authors, so the engine's hooks
 * (the resource loader, the output writer, #foreach, introspection and
 * reference insertion) check the current session rather than trusting the
 * template. Outside a session those hooks apply no limits.
 *
 * <p>Besides the output and time limits, a render has a budget for what it
 * builds in memory: the characters of the strings it makes, and the items it
 * adds to lists and maps. A template that keeps many strings or a long list
 * never writes them, so the output limit alone doesn't stop it. A session
 * started inside another one shares that one's time and budget.
 */
final class RenderSession {

    private static final Log LOG = LogFactory.getLog(RenderSession.class);

    /** Most characters one render may write, or put in one string. */
    static final String MAX_OUTPUT_CHARS = "rendering.maxOutputChars";

    /** Most seconds one render may take. */
    static final String MAX_RENDER_SECONDS = "rendering.maxRenderSeconds";

    private static final int DEFAULT_MAX_OUTPUT_CHARS = 10 * 1024 * 1024;
    private static final int DEFAULT_MAX_RENDER_SECONDS = 30;

    /** The characters of strings one render may build, as a multiple of the output limit. */
    static final int BUILT_CHARS_PER_OUTPUT_CHAR = 10;

    /** Most items one render may add to lists and maps, or get from split calls. */
    static final long MAX_ITEMS = 1_000_000;

    private static final ThreadLocal<RenderSession> CURRENT = new ThreadLocal<>();

    private final String weblogId;
    private final Budget budget;
    private final RenderSession previous;

    /** What one render may use, shared by the sessions started inside it. */
    private static final class Budget {
        final long maxOutputChars;
        final int maxRenderSeconds;
        final long deadline;
        final long maxBuiltChars;
        final long maxItems;
        long builtChars = 0;
        long items = 0;

        Budget() {
            int chars = intProperty(MAX_OUTPUT_CHARS, DEFAULT_MAX_OUTPUT_CHARS);
            maxOutputChars = chars > 0 ? chars : Long.MAX_VALUE;
            maxBuiltChars = chars > 0 ? (long) chars * BUILT_CHARS_PER_OUTPUT_CHAR
                    : Long.MAX_VALUE;
            maxItems = chars > 0 ? MAX_ITEMS : Long.MAX_VALUE;
            maxRenderSeconds = intProperty(MAX_RENDER_SECONDS, DEFAULT_MAX_RENDER_SECONDS);
            deadline = maxRenderSeconds > 0
                    ? System.nanoTime() + TimeUnit.SECONDS.toNanos(maxRenderSeconds)
                    : 0;
        }
    }

    private RenderSession(String weblogId, RenderSession previous) {
        this.weblogId = weblogId;
        this.previous = previous;
        this.budget = previous != null ? previous.budget : new Budget();
    }

    /** @return the property's value, or the default when it isn't a number */
    private static int intProperty(String name, int defaultValue) {
        String value = WebloggerConfig.getProperty(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            LOG.warn(name + " is not a number, using " + defaultValue + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Starts a session on this thread.
     *
     * @param weblogId the weblog being rendered, or null when there is none,
     *                 in which case no stored weblog template may be loaded
     */
    static RenderSession begin(String weblogId) {
        RenderSession session = new RenderSession(weblogId, CURRENT.get());
        CURRENT.set(session);
        return session;
    }

    /** Ends this session, restoring any session it was started inside. */
    void end() {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    /** @return the session on this thread, or null outside rendering */
    static RenderSession current() {
        return CURRENT.get();
    }

    String getWeblogId() {
        return weblogId;
    }

    long getMaxOutputChars() {
        return budget.maxOutputChars;
    }

    /** Throws once the current render has used up its time. */
    static void checkDeadline() {
        RenderSession session = CURRENT.get();
        if (session != null && session.budget.maxRenderSeconds > 0
                && System.nanoTime() - session.budget.deadline > 0) {
            throw new RenderLimitException("rendering took longer than "
                    + session.budget.maxRenderSeconds + " seconds");
        }
    }

    /** Throws if one string would be longer than one render may write. */
    static void checkLength(long length) {
        RenderSession session = CURRENT.get();
        if (session != null && length > session.budget.maxOutputChars) {
            throw new RenderLimitException("a string would be longer than "
                    + session.budget.maxOutputChars + " characters");
        }
    }

    /** Counts the characters of a string the render built. */
    static void chargeBuilt(long chars) {
        RenderSession session = CURRENT.get();
        if (session != null && chars > 0) {
            Budget b = session.budget;
            b.builtChars = add(b.builtChars, chars);
            if (b.builtChars > b.maxBuiltChars) {
                throw new RenderLimitException("rendering built more than "
                        + b.maxBuiltChars + " characters of strings");
            }
        }
    }

    /** Throws, without counting them, if this many more items would be over the budget. */
    static void checkItems(long count) {
        RenderSession session = CURRENT.get();
        if (session != null && add(session.budget.items, count) > session.budget.maxItems) {
            throw itemsException(session.budget);
        }
    }

    /** Counts items the render added to a list or map, or got from a split. */
    static void chargeItems(long count) {
        RenderSession session = CURRENT.get();
        if (session != null && count > 0) {
            Budget b = session.budget;
            b.items = add(b.items, count);
            if (b.items > b.maxItems) {
                throw itemsException(b);
            }
        }
    }

    private static RenderLimitException itemsException(Budget b) {
        return new RenderLimitException("rendering added more than "
                + b.maxItems + " items to lists and maps");
    }

    private static long add(long a, long b) {
        long sum = a + b;
        return sum < 0 ? Long.MAX_VALUE : sum;
    }
}
