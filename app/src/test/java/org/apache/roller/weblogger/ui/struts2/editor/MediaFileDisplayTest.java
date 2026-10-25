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
package org.apache.roller.weblogger.ui.struts2.editor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaFileDisplayTest {

    @Test
    void formatsSizesInReadableUnits() {
        assertEquals("0 bytes", MediaFileDisplay.formatSize(0));
        assertEquals("1 byte", MediaFileDisplay.formatSize(1));
        assertEquals("1023 bytes", MediaFileDisplay.formatSize(1023));
        assertEquals("1 KB", MediaFileDisplay.formatSize(1024));
        assertEquals("1.5 KB", MediaFileDisplay.formatSize(1536));
        assertEquals("10 KB", MediaFileDisplay.formatSize(10 * 1024));
        assertEquals("200 KB", MediaFileDisplay.formatSize(200 * 1024 + 300));
        assertEquals("2.3 MB", MediaFileDisplay.formatSize((long) (2.3 * 1024 * 1024)));
        assertEquals("5 GB", MediaFileDisplay.formatSize(5L * 1024 * 1024 * 1024));
    }

    @Test
    void formatsContentTypesAsShortLabels() {
        assertEquals("PNG", MediaFileDisplay.formatType("image/png"));
        assertEquals("JPG", MediaFileDisplay.formatType("image/jpeg"));
        assertEquals("SVG", MediaFileDisplay.formatType("image/svg+xml"));
        assertEquals("PDF", MediaFileDisplay.formatType("application/pdf"));
        assertEquals("MP3", MediaFileDisplay.formatType("audio/mpeg"));
        assertEquals("MPEG", MediaFileDisplay.formatType("video/mpeg"));
        assertEquals("ZIP-COMPRESSED", MediaFileDisplay.formatType("application/x-zip-compressed"));
        assertEquals("HTML", MediaFileDisplay.formatType("text/html; charset=UTF-8"));
        assertEquals("", MediaFileDisplay.formatType(null));
        assertEquals("WMV", MediaFileDisplay.formatType("application/octet-stream", "talk.wmv"));
        assertEquals("OCTET-STREAM", MediaFileDisplay.formatType("application/octet-stream", "README"));
        assertEquals("PNG", MediaFileDisplay.formatType("image/png", "logo.gif"));
    }

    @Test
    void picksAnIconForEachKindOfFile() {
        assertEquals("bi-file-earmark-image", MediaFileDisplay.typeIcon("image/png"));
        assertEquals("bi-file-earmark-music", MediaFileDisplay.typeIcon("audio/mpeg"));
        assertEquals("bi-file-earmark-play", MediaFileDisplay.typeIcon("video/mp4"));
        assertEquals("bi-file-earmark-pdf", MediaFileDisplay.typeIcon("application/pdf"));
        assertEquals("bi-file-earmark-zip", MediaFileDisplay.typeIcon("application/zip"));
        assertEquals("bi-file-earmark-text", MediaFileDisplay.typeIcon("text/plain"));
        assertEquals("bi-file-earmark", MediaFileDisplay.typeIcon("application/octet-stream"));
        assertEquals("bi-file-earmark", MediaFileDisplay.typeIcon(null));
    }
}
