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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * Short, human-readable labels for media files, shared by the media library
 * and the editor's media chooser.
 */
public final class MediaFileDisplay {

    private static final String[] UNITS = {"KB", "MB", "GB", "TB"};

    private MediaFileDisplay() {
    }

    /**
     * Size for display: "512 bytes", "1.5 KB", "23 MB". One decimal below 10
     * of a unit, none above.
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes == 1 ? "1 byte" : bytes + " bytes";
        }
        double value = bytes;
        int unit = -1;
        do {
            value /= 1024;
            unit++;
        } while (value >= 1024 && unit < UNITS.length - 1);
        String number = value < 10
                ? String.format(Locale.ROOT, "%.1f", value).replace(".0", "")
                : String.format(Locale.ROOT, "%.0f", value);
        return number + " " + UNITS[unit];
    }

    /**
     * File type for display, from a content type: "image/png" is "PNG",
     * "image/svg+xml" is "SVG", "application/x-zip-compressed" is "ZIP-COMPRESSED".
     */
    public static String formatType(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return "";
        }
        String type = contentType.trim().toLowerCase(Locale.ROOT);
        int semicolon = type.indexOf(';');
        if (semicolon != -1) {
            type = type.substring(0, semicolon).trim();
        }
        String subtype = type.substring(type.indexOf('/') + 1);
        int plus = subtype.indexOf('+');
        if (plus > 0) {
            subtype = subtype.substring(0, plus);
        }
        subtype = StringUtils.removeStart(subtype, "x-");
        subtype = StringUtils.removeStart(subtype, "vnd.");
        if ("jpeg".equals(subtype) || "pjpeg".equals(subtype)) {
            subtype = "jpg";
        } else if ("mpeg".equals(subtype) && type.startsWith("audio/")) {
            subtype = "mp3";
        }
        return subtype.toUpperCase(Locale.ROOT);
    }

    /**
     * File type for display. When the content type says nothing useful
     * (missing, or application/octet-stream), the file name extension is used.
     */
    public static String formatType(String contentType, String fileName) {
        String label = formatType(contentType);
        if ((label.isEmpty() || "OCTET-STREAM".equals(label)) && fileName != null) {
            int dot = fileName.lastIndexOf('.');
            if (dot > 0 && dot < fileName.length() - 1) {
                return fileName.substring(dot + 1).toUpperCase(Locale.ROOT);
            }
        }
        return label;
    }

    /** Bootstrap Icons class for a file that has no thumbnail. */
    public static String typeIcon(String contentType) {
        String type = StringUtils.defaultString(contentType).toLowerCase(Locale.ROOT);
        if (type.startsWith("image/")) {
            return "bi-file-earmark-image";
        } else if (type.startsWith("audio/")) {
            return "bi-file-earmark-music";
        } else if (type.startsWith("video/")) {
            return "bi-file-earmark-play";
        } else if (type.equals("application/pdf")) {
            return "bi-file-earmark-pdf";
        } else if (type.contains("zip") || type.contains("compressed")
                || type.contains("tar") || type.contains("gzip")) {
            return "bi-file-earmark-zip";
        } else if (type.startsWith("text/")) {
            return "bi-file-earmark-text";
        }
        return "bi-file-earmark";
    }
}
