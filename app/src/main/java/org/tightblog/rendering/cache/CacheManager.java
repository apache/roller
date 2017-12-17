/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.tightblog.pojos.WeblogBookmark;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * A governing class for Roller cache objects.
 * <p>
 * The purpose of the CacheManager is to provide a level of abstraction between
 * classes that use a cache and the implementations of a cache.  This allows
 * us to create easily pluggable cache implementations.
 * <p>
 * The other purpose is to provide a single interface for interacting with all
 * Roller caches at the same time.  This is beneficial because as data
 * changes in the system we often need to notify all caches that some part of
 * their cached data needs to be invalidated, and the CacheManager makes that
 * process easier.
 */
public class CacheManager {

    private static Logger log = LoggerFactory.getLogger(CacheManager.class);

    // a set of all registered cache handlers
    private Set<BlogEventListener> cacheHandlers = new HashSet<>();

    public void setCacheHandlers(@Qualifier("cache.customHandlers") Set<BlogEventListener> customHandlers) {
        cacheHandlers.addAll(customHandlers);
    }

    // a map of all registered caches
    private Map<String, Cache<String, Object>> caches = new HashMap<>();

    public CacheManager() {
    }

    Cache<String, Object> constructCache(String id, int size, long timeoutInMS) {
        Cache<String, Object> cache = Caffeine.newBuilder()
                .expireAfterWrite(timeoutInMS, TimeUnit.MILLISECONDS)
                .maximumSize(size)
                .recordStats()
                .build();
        caches.put(id, cache);
        return cache;
    }

    /**
     * Register a BlogEventListener to listen for object invalidations.
     * <p>
     * This is here so that it's possible to add classes which would respond
     * to object invalidations without necessarily having to create a cache.
     * <p>
     * An example would be a handler designed to notify other machines in a
     * cluster when an object has been invalidated, or possibly the search
     * index management classes are interested in knowing when objects are
     * invalidated.
     */
    void registerHandler(BlogEventListener handler) {

        log.debug("Registering handler {}", handler);

        if (handler != null) {
            cacheHandlers.add(handler);
        }
    }

    public void invalidate(WeblogEntry entry) {

        log.debug("invalidating entry = {}", entry.getAnchor());
        for (BlogEventListener handler : cacheHandlers) {
            handler.entryChanged(entry);
        }
    }

    public void invalidate(Weblog weblog) {

        log.debug("invalidating weblog {}", weblog.getHandle());
        for (BlogEventListener handler : cacheHandlers) {
            handler.weblogChanged(weblog);
        }
    }

    public void invalidate(WeblogBookmark bookmark) {

        log.debug("invalidating bookmark {}", bookmark.getId());
        for (BlogEventListener handler : cacheHandlers) {
            handler.bookmarkChanged(bookmark);
        }
    }

    public void invalidate(WeblogEntryComment comment) {

        log.debug("invalidating comment {}", comment.getId());
        for (BlogEventListener handler : cacheHandlers) {
            handler.commentChanged(comment);
        }
    }

    public void invalidate(User user) {

        log.debug("invalidating user {}", user.getUserName());
        for (BlogEventListener handler : cacheHandlers) {
            handler.userChanged(user);
        }
    }

    public void invalidate(WeblogCategory category) {

        log.debug("invalidating category {}", category.getId());
        for (BlogEventListener handler : cacheHandlers) {
            handler.categoryChanged(category);
        }
    }

    public void invalidate(WeblogTemplate template) {
        log.debug("invalidating template {}", template.getId());
        for (BlogEventListener handler : cacheHandlers) {
            handler.templateChanged(template);
        }
    }

    /**
     * Flush the entire cache system.
     */
    public void clear() {
        for (Cache cache : caches.values()) {
            cache.invalidateAll();
        }
    }

    /**
     * Flush a single cache.
     */
    public void clear(String cacheId) {
        Cache cache = caches.get(cacheId);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * Retrieve stats from all registered caches.
     */
    public Map<String, CacheStats> getStats() {
        Map<String, CacheStats> allStats = new HashMap<>();
        for (Map.Entry<String, Cache<String, Object>> cache : caches.entrySet()) {
            allStats.put(cache.getKey(), cache.getValue().stats());
        }
        return allStats;
    }

    public CacheStats getStats(String cacheName) {
        Cache cache = caches.get(cacheName);
        return cache.stats();
    }

}
