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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnclosureMetadataTest {

    @Test
    void acceptsHttpMetadataWithoutOpeningTheResource() {
        EnclosureMetadata metadata = EnclosureMetadata.of(
                "http://127.0.0.1:9/audio.ogg", "audio/ogg", "1234");

        assertEquals("http://127.0.0.1:9/audio.ogg", metadata.getUrl());
        assertEquals("audio/ogg", metadata.getContentType());
        assertEquals("1234", metadata.getLength());
    }

    @Test
    void acceptsHttpsAndTrimsMetadata() {
        EnclosureMetadata metadata = EnclosureMetadata.of(
                " https://media.example.org/show.mp3 ",
                " audio/mpeg ", " 3141592654 ");

        assertEquals("https://media.example.org/show.mp3", metadata.getUrl());
        assertEquals("audio/mpeg", metadata.getContentType());
        assertEquals("3141592654", metadata.getLength());
    }

    @Test
    void acceptsLocalAndInternationalizedAuthoritiesWithoutNetworkAccess() {
        assertEquals("http://roller.internal/audio.ogg",
                EnclosureMetadata.of("http://roller.internal/audio.ogg",
                        "audio/ogg", "12").getUrl());
        assertEquals("https://例え.テスト/audio.ogg",
                EnclosureMetadata.of("https://例え.テスト/audio.ogg",
                        "audio/ogg", "12").getUrl());
    }

    @Test
    void stripsLegacyParametersAndNormalizesTheMediaType() {
        EnclosureMetadata metadata = EnclosureMetadata.of(
                "https://media.example.org/show.mp3",
                " Audio/MPEG; charset=utf-8 ", "12");

        assertEquals("audio/mpeg", metadata.getContentType());
    }

    @Test
    void acceptsAllMediaTypeTokenCharacters() {
        assertEquals("audio/x!#$%&'*+-.^_`|~",
                EnclosureMetadata.of("https://example.org/audio",
                        "audio/x!#$%&'*+-.^_`|~", "12").getContentType());
    }

    @Test
    void rejectsUnsupportedOrIncompleteMetadata() {
        assertField(EnclosureMetadata.Field.URL,
                "file:///tmp/audio.ogg", "audio/ogg", "12");
        assertField(EnclosureMetadata.Field.TYPE,
                "https://example.org/audio", "not-a-type", "12");
        assertField(EnclosureMetadata.Field.LENGTH,
                "https://example.org/audio", "audio/ogg", "-1");
        assertField(EnclosureMetadata.Field.LENGTH,
                "https://example.org/audio", "audio/ogg", "unknown");
    }

    @Test
    void templatesEscapeFeedAttributesAndAvoidInlineHandlers() throws Exception {
        String feeds = source("src/main/webapp/WEB-INF/velocity/feeds.vm");
        assertEquals(2, count(feeds, "type=\"$utils.escapeXML($mc_type)\""));
        assertFalse(feeds.contains("type=\"$mc_type\""));

        String upload = source(
                "src/main/webapp/WEB-INF/jsps/editor/MediaFileAddSuccess.jsp");
        assertTrue(upload.contains("data-enclosure-url="));
        assertTrue(upload.contains("data-enclosure-type="));
        assertTrue(upload.contains("selected.attr(\"data-enclosure-type\")"));
        assertFalse(upload.contains("onchange=\"setEnclosure("));

        String policy = source(
                "src/main/java/org/apache/roller/weblogger/util/EnclosureMetadata.java");
        assertFalse(policy.contains("openConnection"));
        assertFalse(policy.contains("UrlValidator"));
    }

    private void assertField(EnclosureMetadata.Field field,
            String url, String type, String length) {
        EnclosureMetadata.ValidationException exception = assertThrows(
                EnclosureMetadata.ValidationException.class,
                () -> EnclosureMetadata.of(url, type, length));
        assertEquals(field, exception.getField());
    }

    private int count(String value, String needle) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(needle, offset)) >= 0) {
            count++;
            offset += needle.length();
        }
        return count;
    }

    private String source(String relativePath) throws Exception {
        Path path = Path.of(relativePath);
        if (!Files.exists(path)) {
            path = Path.of("app").resolve(relativePath);
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
