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
 * (the resource loader, the output writer, #foreach and reference
 * insertion) check the current session rather than trusting the
 * template. Outside a session those hooks apply no limits. A session started
 * inside another one shares that one's limits and deadline.
 */
final class RenderSession {

    private static final Log LOG = LogFactory.getLog(RenderSession.class);

    /** Most characters one render may write, or put in one string. */
    static final String MAX_OUTPUT_CHARS = "rendering.maxOutputChars";

    /** Most seconds one render may take. */
    static final String MAX_RENDER_SECONDS = "rendering.maxRenderSeconds";

    private static final int DEFAULT_MAX_OUTPUT_CHARS = 10 * 1024 * 1024;
    private static final int DEFAULT_MAX_RENDER_SECONDS = 30;

    private static final ThreadLocal<RenderSession> CURRENT = new ThreadLocal<>();

    private final String weblogId;
    private final Limits limits;
    private final RenderSession previous;

    /** The limits of one render, shared by the sessions started inside it. */
    private static final class Limits {
        final long maxOutputChars;
        final int maxRenderSeconds;
        final long deadline;

        Limits() {
            int chars = intProperty(MAX_OUTPUT_CHARS, DEFAULT_MAX_OUTPUT_CHARS);
            maxOutputChars = chars > 0 ? chars : Long.MAX_VALUE;
            maxRenderSeconds = intProperty(MAX_RENDER_SECONDS, DEFAULT_MAX_RENDER_SECONDS);
            deadline = maxRenderSeconds > 0
                    ? System.nanoTime() + TimeUnit.SECONDS.toNanos(maxRenderSeconds)
                    : 0;
        }
    }

    private RenderSession(String weblogId, RenderSession previous) {
        this.weblogId = weblogId;
        this.previous = previous;
        this.limits = previous != null ? previous.limits : new Limits();
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
        return limits.maxOutputChars;
    }

    /** Throws once the current render has used up its time. */
    static void checkDeadline() {
        RenderSession session = CURRENT.get();
        if (session != null && session.limits.maxRenderSeconds > 0
                && System.nanoTime() - session.limits.deadline > 0) {
            throw new RenderLimitException("rendering took longer than "
                    + session.limits.maxRenderSeconds + " seconds");
        }
    }

    /** Throws if one string would be longer than one render may write. */
    static void checkLength(long length) {
        RenderSession session = CURRENT.get();
        if (session != null && length > session.limits.maxOutputChars) {
            throw new RenderLimitException("a string would be longer than "
                    + session.limits.maxOutputChars + " characters");
        }
    }
}
