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

import org.junit.Before;
import org.junit.Test;
import org.tightblog.domain.Template;

import java.time.Instant;

import static org.junit.Assert.*;

public class LazyExpiringCacheTest {

    private LazyExpiringCache cache;

    @Before
    public void initialize() {
        cache = new LazyExpiringCache();
        cache.setMaxEntries(10);
        cache.setTimeoutSec(3600);
        cache.init();
    }

    @Test
    public void testReturnNonExpiredValue() {
        CachedContent testContent = new CachedContent(Template.ComponentType.ATOMFEED);
        Instant twentySecondsAgo = Instant.now().minusSeconds(20);
        cache.put("abc", testContent);
        assertEquals(testContent, cache.get("abc", twentySecondsAgo));
    }

    @Test
    public void testReturnNullIfCacheDisabled() {
        cache.setMaxEntries(0);
        cache.init();
        CachedContent testContent = new CachedContent(Template.ComponentType.ATOMFEED);
        Instant twentySecondsAgo = Instant.now().minusSeconds(20);
        cache.put("abc", testContent);
        assertNull(cache.get("abc", twentySecondsAgo));
    }

    @Test
    public void testReturnNullForExpiredValue() {
        CachedContent testContent = new CachedContent(Template.ComponentType.ATOMFEED);
        cache.put("abc", testContent);
        Instant twentySecondsLater = Instant.now().plusSeconds(20);
        assertNull(cache.get("abc", twentySecondsLater));
    }

    @Test
    public void testReturnNullForNoncachedValue() {
        Instant twentySecondsAgo = Instant.now().minusSeconds(20);
        assertNull(cache.get("abc", twentySecondsAgo));
    }
}