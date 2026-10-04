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

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

class InlineImageDataTest {

    // 1x1 transparent PNG
    static final String PNG = "data:image/png;base64,"
            + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

    static String dataUrl(String type, byte... bytes) {
        return "data:image/" + type + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    void acceptsPngJpegAndGifWithMatchingSignatures() {
        InlineImageData.Image png = InlineImageData.parse(PNG);
        assertNotNull(png);
        assertEquals("image/png", png.getContentType());
        assertEquals("png", png.getExtension());

        InlineImageData.Image jpeg = InlineImageData.parse(
                dataUrl("jpeg", (byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0));
        assertNotNull(jpeg);
        assertEquals("jpg", jpeg.getExtension());

        assertNotNull(InlineImageData.parse(dataUrl("gif", "GIF89a!".getBytes())));
        assertNotNull(InlineImageData.parse(dataUrl("gif", "GIF87a!".getBytes())));
    }

    @Test
    void refusesOtherTypesAndMismatchedOrMalformedData() {
        assertNull(InlineImageData.parse("data:image/svg+xml;base64,"
                + Base64.getEncoder().encodeToString("<svg onload=alert(1)/>".getBytes())));
        assertNull(InlineImageData.parse("data:text/html;base64,PHNjcmlwdD4="));
        assertNull(InlineImageData.parse(dataUrl("png", "GIF89a!".getBytes())));
        assertNull(InlineImageData.parse("data:image/png;base64,not base64!"));
        assertNull(InlineImageData.parse("data:image/png," + PNG.substring(22)));
        assertNull(InlineImageData.parse("https://example.org/a.png"));
        assertNull(InlineImageData.parse(null));
    }

    @Test
    void uploadParsingFollowsTheUploadLimitNotTheFieldLimit() {
        byte[] big = new byte[100_000];
        System.arraycopy("GIF89a".getBytes(), 0, big, 0, 6);
        String value = dataUrl("gif", big);

        assertNull(InlineImageData.parse(value));
        assertNotNull(InlineImageData.parseForUpload(value, 200_000));
        assertNull(InlineImageData.parseForUpload(value, 50_000));
        assertTrue(InlineImageData.exceedsUploadLimit(value, 50_000));
    }

    @Test
    void findsDataSourcesWithTheirExactPositions() {
        String html = "<p>a</p><img alt=\"x\" src=\"" + PNG + "\"><img src='https://example.org/b.png'>"
                + "<IMG SRC=" + PNG + " />";
        List<InlineImageData.Source> sources = InlineImageData.findSources(html);

        assertEquals(2, sources.size());
        assertEquals("src=\"" + PNG + "\"",
                html.substring(sources.get(0).getStart(), sources.get(0).getEnd()));
        assertEquals("SRC=" + PNG,
                html.substring(sources.get(1).getStart(), sources.get(1).getEnd()));
        assertEquals(PNG, sources.get(1).getValue());
    }

    @Test
    void ignoresSrcTextOutsideTheSrcAttribute() {
        String inAlt = "<img alt='src=\"" + PNG + "\"' src=\"https://example.org/a.png\">";
        assertTrue(InlineImageData.findSources(inAlt).isEmpty());

        String dataSrc = "<img data-src=\"" + PNG + "\" src=\"https://example.org/a.png\">";
        assertTrue(InlineImageData.findSources(dataSrc).isEmpty());

        String afterAlt = "<img alt='src=x' src=\"" + PNG + "\">";
        List<InlineImageData.Source> sources = InlineImageData.findSources(afterAlt);
        assertEquals(1, sources.size());
        assertEquals(PNG, sources.get(0).getValue());
    }

    @Test
    void findsSourcesInTagsWithAngleBracketsInQuotedValues() {
        String before = "<img alt=\"a > b\" src=\"" + PNG + "\">";
        List<InlineImageData.Source> sources = InlineImageData.findSources(before);
        assertEquals(1, sources.size());
        assertEquals(PNG, sources.get(0).getValue());
        assertEquals("src=\"" + PNG + "\"",
                before.substring(sources.get(0).getStart(), sources.get(0).getEnd()));

        String after = "<p>x</p><img src=\"" + PNG + "\" alt=\"a < b\"><img src='" + PNG + "'>";
        sources = InlineImageData.findSources(after);
        assertEquals(2, sources.size());
        assertEquals(PNG, sources.get(0).getValue());
        assertEquals(PNG, sources.get(1).getValue());
    }

    @Test
    void ignoresElementsThatOnlyStartWithImg() {
        assertTrue(InlineImageData.findSources("<imgx src=\"" + PNG + "\">").isEmpty());
    }

    @Test
    void findingSourcesStaysLinearOnAdversarialInput() {
        String unclosedTags = "<img ".repeat(200_000);
        String unclosedQuotes = "<img" + " a=\"".repeat(200_000) + ">";
        String bracketsInQuotes = "<img alt=\"" + "<img >".repeat(200_000) + "\">";
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            assertTrue(InlineImageData.findSources(unclosedTags).isEmpty());
            assertTrue(InlineImageData.findSources(unclosedQuotes).isEmpty());
            assertTrue(InlineImageData.findSources(bracketsInQuotes).isEmpty());
        });
    }

    @Test
    void fieldLimitDefaultsWhenUnset() {
        assertEquals(InlineImageData.DEFAULT_MAX_FIELD_BYTES, withLimit(null));
    }

    @Test
    void fieldLimitFollowsTheSetting() {
        assertEquals(250000, withLimit("250000"));
    }

    @Test
    void invalidFieldLimitFallsBackToTheDefault() {
        assertEquals(InlineImageData.DEFAULT_MAX_FIELD_BYTES, withLimit("big"));
        assertEquals(InlineImageData.DEFAULT_MAX_FIELD_BYTES, withLimit("0"));
        assertEquals(InlineImageData.DEFAULT_MAX_FIELD_BYTES, withLimit("-1"));
    }

    @Test
    void inlineImagesRespectTheFieldLimit() {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(() -> WebloggerConfig.getProperty(
                    InlineImageData.MAX_FIELD_BYTES_PROPERTY)).thenReturn("20");
            assertNull(InlineImageData.parse(PNG));

            config.when(() -> WebloggerConfig.getProperty(
                    InlineImageData.MAX_FIELD_BYTES_PROPERTY)).thenReturn("1000");
            assertNotNull(InlineImageData.parse(PNG));
        }
    }

    private static int withLimit(String value) {
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class)) {
            config.when(() -> WebloggerConfig.getProperty(
                    InlineImageData.MAX_FIELD_BYTES_PROPERTY)).thenReturn(value);
            return InlineImageData.maxFieldBytes();
        }
    }
}
