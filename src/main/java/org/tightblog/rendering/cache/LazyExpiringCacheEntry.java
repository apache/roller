/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.cache;

import java.time.Clock;
import java.time.Instant;

/**
 * A cache entry that is meant to expire in a lazy fashion.
 * <p>
 * The way to use this class is to wrap the object you want to cache in an
 * instance of this class and store that in your cache.  Then when you want
 * to retrieve this entry you must input a last-expired time which can be
 * compared against the time this entry was cached to determine if the cached
 * entry is "fresh".  If the object is not fresh then we don't return it.
 * <p>
 * This essentially allows us to track when an object is cached and then before
 * we can retrieve that cached object we must compare it with its last known
 * invalidation time to make sure it hasn't expired.  This is useful because
 * instead of actively purging lots of cached objects from the cache at
 * invalidation time, we can now be lazy and just invalidate them when we
 * actually try to retrieve the cached object.
 */
class LazyExpiringCacheEntry {

    private CachedContent value;
    private Instant timeCached;

    LazyExpiringCacheEntry(CachedContent item) {
        this.value = item;
        this.timeCached = Clock.systemDefaultZone().instant();
    }

    /**
     * Retrieve the value of this cache entry if it is still "fresh".
     * <p>
     * If the value has expired then we return null.
     */
    public CachedContent getValueIfFresh(Instant objectLastChanged) {
        return isFresh(objectLastChanged) ? value : null;
    }

    /**
     * Determine if this cache entry is still usable.
     */
    private boolean isFresh(Instant objectLastChanged) {
        return objectLastChanged == null || timeCached.isAfter(objectLastChanged);
    }

}
