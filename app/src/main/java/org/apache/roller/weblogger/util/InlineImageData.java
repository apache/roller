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

/** Image data URLs accepted in entry content and their locations in HTML. */
public final class InlineImageData {

    // MySQL's TEXT column holds 65,535 bytes. Leave room for the rest of an entry.
    public static final int MAX_FIELD_BYTES = 60000;

    private static final Pattern IMAGE_TAG = Pattern.compile("(?is)<img\\b[^>]*>");
    private static final Pattern SOURCE_ATTRIBUTE = Pattern.compile(
            "(?is)(?<![\\w:-])src\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s>]+))");
    private static final Pattern DATA_URL = Pattern.compile(
            "(?i)^data:image/(png|jpeg|gif);base64,([a-z0-9+/]+={0,2})$");

    private InlineImageData() {
    }

    public static List<Source> findSources(String html) {
        List<Source> sources = new ArrayList<>();
        if (html == null) {
            return sources;
        }
        Matcher image = IMAGE_TAG.matcher(html);
        while (image.find()) {
            Matcher source = SOURCE_ATTRIBUTE.matcher(image.group());
            if (!source.find()) {
                continue;
            }
            int group = source.start(1) >= 0 ? 1 : source.start(2) >= 0 ? 2 : 3;
            String value = source.group(group);
            if (value.trim().toLowerCase(Locale.ROOT).startsWith("data:")) {
                sources.add(new Source(image.start() + source.start(),
                        image.start() + source.end(), value));
            }
        }
        return sources;
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
        if (value == null || (inline && value.length() > MAX_FIELD_BYTES)) {
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
