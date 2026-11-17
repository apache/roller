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

import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * What applies while one template is rendered on this thread: the weblog whose
 * stored templates it may load, and how much output and time it may use.
 *
 * <p>Weblog templates are written by weblog authors, so the engine's hooks
 * (the resource loader, the output writer, #foreach, introspection and
 * reference insertion) check the current session rather than trusting the
 * template. Outside a session those hooks apply no limits.
 */
final class RenderSession {

    /** Most characters one render may write, or put in one string. */
    static final String MAX_OUTPUT_CHARS = "rendering.maxOutputChars";

    /** Most seconds one render may take. */
    static final String MAX_RENDER_SECONDS = "rendering.maxRenderSeconds";

    private static final int DEFAULT_MAX_OUTPUT_CHARS = 10 * 1024 * 1024;
    private static final int DEFAULT_MAX_RENDER_SECONDS = 30;

    private static final ThreadLocal<RenderSession> CURRENT = new ThreadLocal<>();

    private final String weblogId;
    private final long maxOutputChars;
    private final int maxRenderSeconds;
    private final long deadline;
    private final RenderSession previous;

    private RenderSession(String weblogId, RenderSession previous) {
        this.weblogId = weblogId;
        this.previous = previous;
        int chars = WebloggerConfig.getIntProperty(MAX_OUTPUT_CHARS,
                DEFAULT_MAX_OUTPUT_CHARS);
        this.maxOutputChars = chars > 0 ? chars : Long.MAX_VALUE;
        this.maxRenderSeconds = WebloggerConfig.getIntProperty(MAX_RENDER_SECONDS,
                DEFAULT_MAX_RENDER_SECONDS);
        this.deadline = maxRenderSeconds > 0
                ? System.nanoTime() + TimeUnit.SECONDS.toNanos(maxRenderSeconds)
                : 0;
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
        return maxOutputChars;
    }

    /** Throws once the current render has used up its time. */
    static void checkDeadline() {
        RenderSession session = CURRENT.get();
        if (session != null && session.maxRenderSeconds > 0
                && System.nanoTime() - session.deadline > 0) {
            throw new RenderLimitException("rendering took longer than "
                    + session.maxRenderSeconds + " seconds");
        }
    }

    /** Throws if a value is longer than one render may write. */
    static void checkLength(long length) {
        RenderSession session = CURRENT.get();
        if (session != null && length > session.maxOutputChars) {
            throw new RenderLimitException("rendering produced more than "
                    + session.maxOutputChars + " characters");
        }
    }
}
