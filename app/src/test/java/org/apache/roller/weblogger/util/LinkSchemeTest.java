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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LinkSchemeTest {

    @Test
    void readsSchemes() {
        assertEquals("https", LinkScheme.of("HTTPS://example.com/"));
        assertEquals("mailto", LinkScheme.of("mailto:reader@example.com"));
        assertEquals("file", LinkScheme.of(" fi\tle:///etc/x"));
    }

    @Test
    void relativeLinksHaveNoScheme() {
        assertEquals("", LinkScheme.of("/a/b"));
        assertEquals("", LinkScheme.of("page?a=b:c"));
        assertEquals("", LinkScheme.of("#top"));
        assertEquals("", LinkScheme.of("/a?b=1&c=2"));
    }

    @Test
    void refusesWhatCannotBeRead() {
        assertNull(LinkScheme.of(null));
        assertNull(LinkScheme.of("x&#58;y"));
        assertNull(LinkScheme.of("x&colon;y"));
        assertNull(LinkScheme.of("&#120;:y"));
        assertNull(LinkScheme.of(":y"));
        assertNull(LinkScheme.of("1x:y"));
    }
}
