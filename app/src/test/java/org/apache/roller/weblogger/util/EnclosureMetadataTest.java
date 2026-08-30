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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void rejectsUnsupportedOrIncompleteMetadata() {
        assertThrows(IllegalArgumentException.class,
                () -> EnclosureMetadata.of("file:///tmp/audio.ogg", "audio/ogg", "12"));
        assertThrows(IllegalArgumentException.class,
                () -> EnclosureMetadata.of("https://example.org/audio", "not-a-type", "12"));
        assertThrows(IllegalArgumentException.class,
                () -> EnclosureMetadata.of("https://example.org/audio", "audio/ogg", "-1"));
        assertThrows(IllegalArgumentException.class,
                () -> EnclosureMetadata.of("https://example.org/audio", "audio/ogg", "unknown"));
    }
}
