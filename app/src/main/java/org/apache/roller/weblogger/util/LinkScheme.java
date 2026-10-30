/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reads the scheme of a link the way a browser would.
 */
public final class LinkScheme {

    private static final Pattern SCHEME = Pattern.compile("[a-zA-Z][a-zA-Z0-9+.-]*");

    private LinkScheme() {
    }

    /**
     * Removes what browsers ignore in a link: tabs and line breaks anywhere,
     * and spaces and control characters at either end.
     */
    public static String compact(String link) {
        return link.replaceAll("[\\t\\n\\r]", "").trim();
    }

    /**
     * Returns the lower-case scheme of the link, an empty string if the link
     * is relative, or null if the part before the path cannot be read. That
     * part is everything before the first '/', '?' or '#'; it is refused if
     * it holds a character reference ('&') or a scheme that is not valid.
     */
    public static String of(String link) {
        if (link == null) {
            return null;
        }
        String compact = compact(link);
        int end = compact.length();
        for (char c : new char[] {'/', '?', '#'}) {
            int i = compact.indexOf(c);
            if (i >= 0 && i < end) {
                end = i;
            }
        }
        String head = compact.substring(0, end);
        if (head.indexOf('&') >= 0) {
            return null;
        }
        int colon = head.indexOf(':');
        if (colon < 0) {
            return "";
        }
        String scheme = head.substring(0, colon);
        if (!SCHEME.matcher(scheme).matches()) {
            return null;
        }
        return scheme.toLowerCase(Locale.ROOT);
    }
}
