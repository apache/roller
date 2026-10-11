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
 * limitations under the License.
 */
package org.apache.roller.weblogger.util;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Normalizes comment author URLs before they are rendered as links.
 */
public final class CommentAuthorUrl {

    private CommentAuthorUrl() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        try {
            URI uri = new URI(normalized);
            String scheme = uri.getScheme();
            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    || uri.isOpaque()
                    || !hasValidAuthority(uri)) {
                return null;
            }
            return normalized;
        } catch (IllegalArgumentException | URISyntaxException ignored) {
            return null;
        }
    }

    /**
     * Normalizes user-entered URLs, retaining the long-standing behavior of
     * supplying an HTTP scheme when one was omitted.
     */
    public static String normalizeInput(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if (!lowerCase.startsWith("http://") && !lowerCase.startsWith("https://")) {
            normalized = "http://" + normalized;
        }
        return normalize(normalized);
    }

    private static boolean hasValidAuthority(URI uri) {
        String authority = uri.getRawAuthority();
        if (authority == null || authority.isBlank() || uri.getRawUserInfo() != null) {
            return false;
        }

        if (authority.startsWith("[")) {
            int closingBracket = authority.indexOf(']');
            if (closingBracket < 2 || uri.getHost() == null) {
                return false;
            }
            return hasValidPort(authority.substring(closingBracket + 1));
        }

        String host = authority;
        int colon = authority.lastIndexOf(':');
        if (colon >= 0) {
            if (authority.indexOf(':') != colon
                    || !hasValidPort(authority.substring(colon))) {
                return false;
            }
            host = authority.substring(0, colon);
        }

        try {
            return !IDN.toASCII(host).isBlank();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private static boolean hasValidPort(String suffix) {
        if (suffix.isEmpty()) {
            return true;
        }
        if (suffix.charAt(0) != ':' || suffix.length() == 1) {
            return false;
        }
        try {
            int port = Integer.parseInt(suffix.substring(1));
            return port >= 0 && port <= 65535;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
