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
import java.util.regex.Pattern;

/**
 * Validated metadata for an RSS or Atom enclosure.
 */
public final class EnclosureMetadata {

    private static final Pattern MEDIA_TYPE = Pattern.compile(
            "[!#$%&'*+.^_`|~0-9A-Za-z-]+/[!#$%&'*+.^_`|~0-9A-Za-z-]+");

    private final String url;
    private final String contentType;
    private final String length;

    private EnclosureMetadata(String url, String contentType, String length) {
        this.url = url;
        this.contentType = contentType;
        this.length = length;
    }

    public static EnclosureMetadata of(String url, String contentType, String length) {
        String normalizedUrl = normalize(url);
        String normalizedType = normalizeContentType(contentType);
        String normalizedLength = normalize(length);

        if (!isHttpUri(normalizedUrl)) {
            throw new ValidationException(Field.URL,
                    "Enclosure URL must be an absolute HTTP or HTTPS URL");
        }
        if (!MEDIA_TYPE.matcher(normalizedType).matches()) {
            throw new ValidationException(Field.TYPE,
                    "Enclosure type must be a valid media type");
        }

        final long byteLength;
        try {
            byteLength = Long.parseLong(normalizedLength);
        } catch (NumberFormatException e) {
            throw new ValidationException(Field.LENGTH,
                    "Enclosure length must be a non-negative integer", e);
        }
        if (byteLength < 0) {
            throw new ValidationException(Field.LENGTH,
                    "Enclosure length must be a non-negative integer");
        }

        return new EnclosureMetadata(
                normalizedUrl, normalizedType, Long.toString(byteLength));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeContentType(String value) {
        String type = normalize(value);
        int parameter = type.indexOf(';');
        if (parameter >= 0) {
            type = type.substring(0, parameter).trim();
        }
        return type.toLowerCase(Locale.ENGLISH);
    }

    private static boolean isHttpUri(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (!("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            if (uri.getRawAuthority() == null || uri.getRawAuthority().isEmpty()) {
                return false;
            }
            if (uri.getHost() != null && !uri.getHost().isEmpty()) {
                return uri.getPort() <= 65535;
            }

            // URI.getHost() is null for a Unicode authority. Validate its host
            // locally after converting it to ASCII; this performs no DNS or I/O.
            String authority = uri.getRawAuthority();
            int userInfo = authority.lastIndexOf('@');
            String hostAndPort = userInfo >= 0
                    ? authority.substring(userInfo + 1) : authority;
            int colon = hostAndPort.lastIndexOf(':');
            String host = colon >= 0 ? hostAndPort.substring(0, colon) : hostAndPort;
            if (colon >= 0) {
                int port = Integer.parseInt(hostAndPort.substring(colon + 1));
                if (port > 65535) {
                    return false;
                }
            }
            return !IDN.toASCII(host).isEmpty();
        } catch (IllegalArgumentException | URISyntaxException invalid) {
            return false;
        }
    }

    public enum Field {
        URL, TYPE, LENGTH
    }

    public static final class ValidationException extends IllegalArgumentException {
        private final Field field;

        private ValidationException(Field field, String message) {
            super(message);
            this.field = field;
        }

        private ValidationException(Field field, String message, Throwable cause) {
            super(message, cause);
            this.field = field;
        }

        public Field getField() {
            return field;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public String getLength() {
        return length;
    }
}
