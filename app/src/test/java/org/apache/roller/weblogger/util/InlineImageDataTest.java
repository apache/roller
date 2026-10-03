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

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

class InlineImageDataTest {

    // 1x1 transparent PNG
    private static final String PNG = "data:image/png;base64,"
            + "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

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
