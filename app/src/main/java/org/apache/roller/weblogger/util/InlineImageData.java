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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;

/** Image data URLs accepted in entry content and their locations in HTML. */
public final class InlineImageData {

    private static final Log log = LogFactory.getLog(InlineImageData.class);

    static final String MAX_FIELD_BYTES_PROPERTY = "weblog.inlineImages.maxFieldBytes";

    // MySQL's TEXT column holds 65,535 bytes. Leave room for the rest of an entry.
    static final int DEFAULT_MAX_FIELD_BYTES = 60000;

    private static final Pattern DATA_URL = Pattern.compile(
            "(?i)^data:image/(png|jpeg|gif);base64,([a-z0-9+/]+={0,2})$");

    private InlineImageData() {
    }

    /** Largest inline content or summary field, in UTF-8 bytes. */
    public static int maxFieldBytes() {
        String value = WebloggerConfig.getProperty(MAX_FIELD_BYTES_PROPERTY);
        if (value == null || value.trim().isEmpty()) {
            return DEFAULT_MAX_FIELD_BYTES;
        }
        try {
            int max = Integer.parseInt(value.trim());
            if (max > 0) {
                return max;
            }
        } catch (NumberFormatException invalid) {
            // fall through to the default
        }
        log.warn("Ignoring invalid " + MAX_FIELD_BYTES_PROPERTY + " value '" + value
                + "'; using " + DEFAULT_MAX_FIELD_BYTES);
        return DEFAULT_MAX_FIELD_BYTES;
    }

    public static List<Source> findSources(String html) {
        List<Source> sources = new ArrayList<>();
        if (html == null) {
            return sources;
        }
        int i = 0;
        while ((i = indexOfImageTag(html, i)) >= 0) {
            int[] tag = scanImageTag(html, i);
            if (tag == null) {
                // The tag never closes, so nothing after it is markup.
                break;
            }
            if (tag[1] >= 0) {
                Source source = new Source(tag[1], tag[2], html.substring(tag[3], tag[4]));
                if (source.value.trim().toLowerCase(Locale.ROOT).startsWith("data:")) {
                    sources.add(source);
                }
            }
            i = tag[0];
        }
        return sources;
    }

    /** Index of the next "<img" that starts an img tag, or -1. */
    private static int indexOfImageTag(String html, int from) {
        int length = html.length();
        for (int i = from; i + 4 <= length; i++) {
            if (html.charAt(i) == '<' && html.regionMatches(true, i + 1, "img", 0, 3)
                    && (i + 4 == length || isNameEnd(html.charAt(i + 4)))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Reads the attributes of the img tag at start, in order, so that quoted
     * values may contain any character, including angle brackets, and text
     * inside another attribute's value is never taken for a src attribute.
     * Returns null if the tag does not close. Otherwise returns {tag end,
     * src start, src end, value start, value end} for the first src
     * attribute, with -1 in the src fields when there is none.
     */
    private static int[] scanImageTag(String html, int start) {
        int length = html.length();
        int[] result = {-1, -1, -1, -1, -1};
        int i = start + "<img".length();
        while (i < length) {
            char c = html.charAt(i);
            if (c == '>') {
                result[0] = i + 1;
                return result;
            }
            if (Character.isWhitespace(c) || c == '/' || c == '=') {
                i++;
                continue;
            }
            int nameStart = i;
            while (i < length && !isNameEnd(html.charAt(i))) {
                i++;
            }
            boolean isSrc = "src".equalsIgnoreCase(html.substring(nameStart, i));
            int afterName = i;
            while (i < length && Character.isWhitespace(html.charAt(i))) {
                i++;
            }
            if (i >= length || html.charAt(i) != '=') {
                i = afterName;
                continue;
            }
            i++;
            while (i < length && Character.isWhitespace(html.charAt(i))) {
                i++;
            }
            int valueStart;
            int valueEnd;
            if (i < length && (html.charAt(i) == '"' || html.charAt(i) == '\'')) {
                int close = html.indexOf(html.charAt(i), i + 1);
                if (close < 0) {
                    return null;
                }
                valueStart = i + 1;
                valueEnd = close;
                i = close + 1;
            } else {
                valueStart = i;
                while (i < length && !Character.isWhitespace(html.charAt(i))
                        && html.charAt(i) != '>') {
                    i++;
                }
                valueEnd = i;
            }
            if (isSrc && result[1] < 0) {
                result[1] = nameStart;
                result[2] = i;
                result[3] = valueStart;
                result[4] = valueEnd;
            }
        }
        return null;
    }

    private static boolean isNameEnd(char c) {
        return Character.isWhitespace(c) || c == '=' || c == '>' || c == '/';
    }

    /** Returns null for unsupported, malformed, or oversized image data. */
    public static Image parse(String value) {
        return parse(value, true);
    }

    /** Entry saves may upload larger images under the configured media limit. */
    public static Image parseForUpload(String value, long maxBytes) {
        if (exceedsUploadLimit(value, maxBytes)) {
            return null;
        }
        Image image = parse(value, false);
        return image != null && image.bytes.length <= maxBytes ? image : null;
    }

    public static boolean exceedsUploadLimit(String value, long maxBytes) {
        if (value == null || maxBytes < 0) {
            return true;
        }
        // Base64 expands three bytes to four characters; the prefix is short.
        return value.length() > 64 + ((maxBytes + 2) / 3) * 4;
    }

    private static Image parse(String value, boolean inline) {
        if (value == null || (inline && value.length() > maxFieldBytes())) {
            return null;
        }
        Matcher match = DATA_URL.matcher(value);
        if (!match.matches()) {
            return null;
        }
        String type = match.group(1).toLowerCase(Locale.ROOT);
        try {
            byte[] bytes = Base64.getDecoder().decode(match.group(2));
            if (!hasSignature(type, bytes)) {
                return null;
            }
            return new Image(type, bytes);
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    private static boolean hasSignature(String type, byte[] bytes) {
        if ("png".equals(type)) {
            byte[] signature = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};
            if (bytes.length < signature.length) {
                return false;
            }
            for (int i = 0; i < signature.length; i++) {
                if (bytes[i] != signature[i]) {
                    return false;
                }
            }
            return true;
        }
        if ("jpeg".equals(type)) {
            return bytes.length >= 3 && bytes[0] == (byte) 0xff
                    && bytes[1] == (byte) 0xd8 && bytes[2] == (byte) 0xff;
        }
        return bytes.length >= 6 && bytes[0] == 'G' && bytes[1] == 'I'
                && bytes[2] == 'F' && bytes[3] == '8'
                && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a';
    }

    public static final class Source {
        private final int start;
        private final int end;
        private final String value;

        private Source(int start, int end, String value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public int getStart() { return start; }
        public int getEnd() { return end; }
        public String getValue() { return value; }
    }

    public static final class Image {
        private final String type;
        private final byte[] bytes;

        private Image(String type, byte[] bytes) {
            this.type = type;
            this.bytes = bytes;
        }

        public String getType() { return type; }
        public byte[] getBytes() { return bytes; }
        public String getExtension() { return "jpeg".equals(type) ? "jpg" : type; }
        public String getContentType() { return "image/" + type; }
    }
}
