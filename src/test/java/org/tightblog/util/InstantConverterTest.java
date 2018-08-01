/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.util;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.*;

public class InstantConverterTest {

    @Test
    public void convertToDatabaseColumn() {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.from(now);

        InstantConverter converter = new InstantConverter();
        Timestamp test = converter.convertToDatabaseColumn(now);
        assertEquals(timestamp, test);
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void convertToEntityAttribute() {
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.from(now);

        InstantConverter converter = new InstantConverter();
        Instant test = converter.convertToEntityAttribute(timestamp);
        assertEquals(now, test);
        assertNull(converter.convertToEntityAttribute(null));
    }
}
