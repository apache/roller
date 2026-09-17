/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;

/** Process-scoped gate for the unauthenticated installation flow. */
public final class BootstrapSecurity {

    public static final String COMPLETION_PROPERTY = "bootstrap.completed";
    private static final Log LOG = LogFactory.getLog(BootstrapSecurity.class);
    private static final long LIFETIME_MS = 60 * 60 * 1000L;
    private static final String SESSION = BootstrapSecurity.class.getName() + ".grant";
    private static byte[] digest;
    private static long expires;
    private static volatile boolean completed;
    private static final ThreadLocal<Boolean> INITIAL = new ThreadLocal<>();
    private BootstrapSecurity() {
    }

    public static synchronized void start() {
        if (completed || digest != null) {
            return;
        }
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        digest = sha256(token);
        expires = System.currentTimeMillis() + LIFETIME_MS;
        LOG.warn(box(
                "ROLLER INITIAL SETUP REQUIRED",
                "",
                "Open Roller in a web browser.",
                "You will be redirected to the secure initial-setup page.",
                "",
                "Enter this one-time setup token (expires in 60 minutes):",
                token));
    }

    public static boolean isCompleted() {
        return completed;
    }

    public static void complete() {
        completed = true;
        digest = null;
    }

    /**
     * Close the gate if this database is already installed, recording the
     * completion marker when it is missing. A database that already has users
     * has no first administrator left to register, so setup is finished and
     * nothing further should be allowed through the bootstrap flow.
     *
     * <p>Callers reach this from two directions: at startup, and at the end of
     * the installer's own bootstrap action. The latter matters for the upgrade
     * path, where there is no first-user registration to close the gate.
     *
     * @return true if bootstrap access is now closed, false if the first
     *         administrator has yet to be created or the marker could not be
     *         written.
     */
    public static boolean completeIfInstalled(Weblogger weblogger) {
        if (completed) {
            return true;
        }
        try {
            PropertiesManager properties = weblogger.getPropertiesManager();
            RuntimeConfigProperty marker = properties.getProperty(COMPLETION_PROPERTY);
            if (marker == null || !"true".equalsIgnoreCase(marker.getValue())) {
                if (weblogger.getUserManager().getUserCount() < 1) {
                    // Brand new install; Register closes the gate when the
                    // first administrator is committed.
                    return false;
                }
                properties.saveProperty(new RuntimeConfigProperty(COMPLETION_PROPERTY, "true"));
                weblogger.flush();
            }
            complete();
            return true;
        } catch (WebloggerException ex) {
            LOG.warn("Could not record bootstrap completion; it will be recorded at the next restart", ex);
            return false;
        }
    }

    public static void beginInitialAdmin() {
        INITIAL.set(Boolean.TRUE);
    }

    public static void endInitialAdmin() {
        INITIAL.remove();
    }

    public static boolean initialAdminScope() {
        return Boolean.TRUE.equals(INITIAL.get());
    }

    public static boolean isValid(HttpServletRequest request) {
        Object grant = request.getSession(false) == null ? null : request.getSession(false).getAttribute(SESSION);
        return !completed && grant instanceof Long && ((Long) grant) >= System.currentTimeMillis();
    }

    public static synchronized boolean redeem(HttpServletRequest request, String token) {
        if (completed || digest == null || token == null || System.currentTimeMillis() > expires) {
            return false;
        }
        byte[] supplied = sha256(token);
        if (!MessageDigest.isEqual(digest, supplied)) {
            return false;
        }
        request.getSession(true).setAttribute(SESSION, System.currentTimeMillis() + LIFETIME_MS);
        digest = null;
        return true;
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String box(String... lines) {
        int width = 0;
        for (String line : lines) {
            width = Math.max(width, line.length());
        }

        String border = "+" + "-".repeat(width + 2) + "+";
        StringBuilder message = new StringBuilder("\n").append(border);
        for (String line : lines) {
            message.append("\n| ").append(line)
                    .append(" ".repeat(width - line.length())).append(" |");
        }
        return message.append("\n").append(border).toString();
    }
}
