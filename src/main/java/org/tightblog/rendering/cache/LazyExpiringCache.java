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

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class LazyExpiringCache {

    public LazyExpiringCache(String cacheHandlerId, int maxEntries, long timeoutInHours) {
        this.cacheHandlerId = cacheHandlerId;
        this.maxEntries = maxEntries;
        this.timeoutInHours = timeoutInHours;
    }

    private static Logger log = LoggerFactory.getLogger(LazyExpiringCache.class);

    private String cacheHandlerId;

    private AtomicLong incomingRequests = new AtomicLong();

    private AtomicLong requestsHandledBy304 = new AtomicLong();

    public long getIncomingRequests() {
        return incomingRequests.get();
    }

    public void incrementIncomingRequests() {
        this.incomingRequests.incrementAndGet();
    }

    public long getRequestsHandledBy304() {
        return requestsHandledBy304.get();
    }

    public void incrementRequestsHandledBy304() {
        this.requestsHandledBy304.incrementAndGet();
    }

    public String getCacheHandlerId() {
        return cacheHandlerId;
    }

    private int maxEntries;

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public void invalidateAll() {
        contentCache.invalidateAll();
    }

    public long getCacheHitCount() {
        return contentCache == null ? 0 : contentCache.stats().hitCount();
    }

    public double getCacheHitRate() {
        return contentCache == null ? 0 : contentCache.stats().hitRate();
    }

    public long getCacheMissCount() {
        return contentCache == null ? 0 : contentCache.stats().missCount();
    }

    public long getCacheRequestCount() {
        return contentCache == null ? 0 : contentCache.stats().requestCount();
    }

    public long getEstimatedSize() {
        return contentCache == null ? 0 : contentCache.estimatedSize();
    }

    private long timeoutInHours;

    private Cache<String, Object> contentCache;

    @PostConstruct
    void init() {
        if (maxEntries > 0) {
            contentCache = Caffeine.newBuilder()
                    .expireAfterWrite(timeoutInHours, TimeUnit.HOURS)
                    .maximumSize(maxEntries)
                    .recordStats()
                    .build();
        } else {
            contentCache = null;
            log.warn("Cache {} has been DISABLED", cacheHandlerId);
        }
    }

    public CachedContent get(String key, Instant objectLastChanged) {
        if (maxEntries > 0) {
            CachedContent content = null;
            LazyExpiringCacheEntry entry = (LazyExpiringCacheEntry) this.contentCache.getIfPresent(key);
            if (entry != null) {
                content = entry.getValueIfFresh(objectLastChanged);
            }
            return content;
        } else {
            return null;
        }
    }

    public void put(String key, CachedContent value) {
        if (maxEntries > 0) {
            contentCache.put(key, new LazyExpiringCacheEntry(value));
            log.debug("PUT {}", key);
        }
    }
}
