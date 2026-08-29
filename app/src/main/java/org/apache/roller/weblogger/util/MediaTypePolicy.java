/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */

package org.apache.roller.weblogger.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * Decides what type an uploaded file is stored as, and how it is served back.
 *
 * <p>A client uploading a file states a type, but the stored type is derived
 * from the file name. The declared value is a hint only, consulted where the
 * name yields nothing, and it cannot introduce a type the browser would
 * execute.
 *
 * <p>Serving applies the second half. Only a short list of formats that
 * browsers render passively are sent inline; everything else is sent as an
 * attachment, and {@code nosniff} accompanies every response so browsers do
 * not substitute their own type guess.
 */
public final class MediaTypePolicy {

    private MediaTypePolicy() {
    }

    public static final String DEFAULT_TYPE = "application/octet-stream";

    /**
     * Formats browsers render without executing anything the file carries.
     * SVG is deliberately absent: it is an XML document that can carry script.
     */
    private static final Set<String> INLINE_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "image/jpeg", "image/pjpeg", "image/png", "image/gif",
                    "image/bmp", "image/x-ms-bmp", "image/webp", "image/tiff",
                    "image/x-icon", "image/vnd.microsoft.icon",
                    "application/pdf")));

    /** Families served inline whatever the subtype. */
    private static final String[] INLINE_PREFIXES = {"audio/", "video/"};

    /**
     * Types a browser may execute, or that can carry something it will. These
     * are never adopted from a client's declaration.
     */
    private static final Set<String> ACTIVE_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "text/html", "application/xhtml+xml", "application/xhtml",
                    "image/svg+xml", "text/xml", "application/xml",
                    "text/javascript", "application/javascript",
                    "application/ecmascript", "text/ecmascript",
                    "text/vbscript", "application/x-shockwave-flash",
                    "text/xsl", "application/xslt+xml")));

    /**
     * @param fileName      the uploaded file's name
     * @param declaredType  the type the client said it was, may be null
     * @return the type to store: derived from the name where that is
     *         conclusive, otherwise the declared type if it is not one a
     *         browser would act on, otherwise the generic binary type
     */
    public static String storedTypeFor(String fileName, String declaredType) {
        String derived = normalize(deriveFromName(fileName));
        if (isConclusive(derived)) {
            return derived;
        }

        String declared = normalize(declaredType);
        if (isConclusive(declared) && !isActive(declared)) {
            return declared;
        }

        return DEFAULT_TYPE;
    }

    /** @return true when browsers render this type without executing it */
    public static boolean isInlineSafe(String contentType) {
        String type = normalize(contentType);
        if (type == null) {
            return false;
        }
        if (INLINE_TYPES.contains(type)) {
            return true;
        }
        for (String prefix : INLINE_PREFIXES) {
            if (type.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /** @return true when a browser may execute this type, or script inside it */
    public static boolean isActive(String contentType) {
        String type = normalize(contentType);
        if (type == null) {
            return false;
        }
        return ACTIVE_TYPES.contains(type) || type.endsWith("+xml");
    }

    /**
     * Sets the type and the headers that govern how the response is treated.
     * Anything outside the inline list is marked as an attachment.
     */
    public static void applyResponseHeaders(HttpServletResponse response,
                                            String contentType, String fileName) {
        response.setHeader("X-Content-Type-Options", "nosniff");

        String type = normalize(contentType);
        if (type == null) {
            type = DEFAULT_TYPE;
        }

        if (isInlineSafe(type)) {
            response.setContentType(type);
            return;
        }

        // Served as bytes to be saved rather than a document to be rendered.
        response.setContentType(DEFAULT_TYPE);
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + headerSafe(fileName) + "\"");
    }

    private static String deriveFromName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        try {
            return Utilities.getContentTypeFromFileName(fileName);
        } catch (Exception undetermined) {
            return null;
        }
    }

    /** @return the bare type in lower case, without parameters such as charset */
    private static String normalize(String contentType) {
        if (contentType == null) {
            return null;
        }
        String type = contentType.trim();
        int semicolon = type.indexOf(';');
        if (semicolon > -1) {
            type = type.substring(0, semicolon).trim();
        }
        return type.isEmpty() ? null : type.toLowerCase(Locale.ENGLISH);
    }

    private static boolean isConclusive(String type) {
        return type != null && !DEFAULT_TYPE.equals(type);
    }

    /**
     * @return the name with the characters that would end the quoted string or
     *         start another header removed, since it is placed in one
     */
    private static String headerSafe(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "download";
        }
        String safe = fileName.replaceAll("[\\r\\n\"\\\\]", "");
        return safe.trim().isEmpty() ? "download" : safe;
    }
}
