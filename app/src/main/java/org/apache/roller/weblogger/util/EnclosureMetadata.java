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

import java.util.regex.Pattern;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Validated metadata for an RSS or Atom enclosure.
 */
public final class EnclosureMetadata {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator(
            new String[] {"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);

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
        String normalizedType = normalize(contentType);
        String normalizedLength = normalize(length);

        if (!URL_VALIDATOR.isValid(normalizedUrl)) {
            throw new IllegalArgumentException("Enclosure URL must be an absolute HTTP or HTTPS URL");
        }
        if (!MEDIA_TYPE.matcher(normalizedType).matches()) {
            throw new IllegalArgumentException("Enclosure type must be a valid media type");
        }

        final long byteLength;
        try {
            byteLength = Long.parseLong(normalizedLength);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enclosure length must be a non-negative integer", e);
        }
        if (byteLength < 0) {
            throw new IllegalArgumentException("Enclosure length must be a non-negative integer");
        }

        return new EnclosureMetadata(
                normalizedUrl, normalizedType, Long.toString(byteLength));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
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
