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
package org.tightblog.rendering.cache;

import org.junit.Test;
import org.tightblog.domain.Template;

import java.time.Instant;

import static org.junit.Assert.*;

public class LazyExpiringCacheEntryTest {

    private Instant twentySecondsAgo = Instant.now().minusSeconds(20);
    private Instant twentySecondsLater = twentySecondsAgo.plusSeconds(40);

    @Test
    public void testGetNonexpiredValue() {
        CachedContent testContent = new CachedContent(Template.Role.ATOMFEED);
        LazyExpiringCacheEntry entry = new LazyExpiringCacheEntry(testContent);
        assertEquals(testContent, entry.getValueIfFresh(twentySecondsAgo));

        // test get value of any age
        assertEquals(testContent, entry.getValueIfFresh(null));
    }

    @Test
    public void testExpiredValueReturnsNull() {
        CachedContent testContent = new CachedContent(Template.Role.ATOMFEED);
        LazyExpiringCacheEntry entry = new LazyExpiringCacheEntry(testContent);
        assertNull(entry.getValueIfFresh(twentySecondsLater));
    }
}