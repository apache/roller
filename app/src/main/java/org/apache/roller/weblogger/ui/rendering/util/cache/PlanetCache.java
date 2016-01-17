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
package org.apache.roller.weblogger.ui.rendering.util.cache;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.rendering.util.PlanetRequest;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.ExpiringCacheEntry;

/**
 * Cache for planet content.
 */
public final class PlanetCache {
    
    private static Log log = LogFactory.getLog(PlanetCache.class);
    
    public static final String CACHE_ID = "cache.planet";
    
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private int size = 10;

    public void setSize(int size) {
        this.size = size;
    }

    private int timeoutSec = 1800;

    public void setTimeoutSec(int timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    private Cache contentCache = null;
    
    // keep a cached version of last expired time
    private ExpiringCacheEntry lastUpdateTime = null;

    private long timeout = 15 * DateUtils.MILLIS_PER_MINUTE;
    
    private PlanetCache() {
        if (enabled) {
            contentCache = CacheManager.constructCache(null, CACHE_ID, size, timeoutSec);
        } else {
            log.warn("Planet cache has been DISABLED");
        }
        
        this.timeout = timeoutSec * DateUtils.MILLIS_PER_SECOND;
    }

    public Object get(String key) {
        if (!enabled) {
            return null;
        }
        
        Object entry = contentCache.get(key);
        
        if(entry == null) {
            log.debug("MISS "+key);
        } else {
            log.debug("HIT "+key);
        }
        
        return entry;
    }
    
    
    public void put(String key, Object value) {
        if (!enabled) {
            return;
        }
        
        contentCache.put(key, value);
        log.debug("PUT "+key);
    }
    
    
    public void remove(String key) {
        if (!enabled) {
            return;
        }
        
        contentCache.remove(key);
        log.debug("REMOVE "+key);
    }
    
    public void clear() {
        if (!enabled) {
            return;
        }
        
        contentCache.clear();
        this.lastUpdateTime = null;
        log.debug("CLEAR");
    }
    
    
    public Date getLastModified() {
        Date lastModified = null;
        
        // first try our cached version
        if(this.lastUpdateTime != null) {
            lastModified = (Date) this.lastUpdateTime.getValue();
        }
        
        // still null, we need to get a fresh value
        if(lastModified == null) {
            lastModified = new Date();
            log.warn("Can't get lastUpdate time, using current time instead");
            this.lastUpdateTime = new ExpiringCacheEntry(lastModified, this.timeout);
        }
        
        return lastModified;
    }
    
    
    /**
     * Generate a cache key from a parsed planet request.
     * This generates a key of the form ...
     *
     * <context>/<type>/<language>[/user]
     *   or
     * <context>/<type>[/flavor]/<language>[/excerpts]
     *
     *
     * examples ...
     *
     * planet/page/en
     * planet/feed/rss/en/excerpts
     *
     */
    public String generateKey(PlanetRequest planetRequest) {
        
        StringBuilder key = new StringBuilder();
        
        key.append(CACHE_ID).append(":");
        key.append(planetRequest.getContext());
        key.append("/");
        key.append(planetRequest.getType());
        
        if(planetRequest.getFlavor() != null) {
            key.append("/").append(planetRequest.getFlavor());
        }
        
        // add language
        key.append("/").append(planetRequest.getLanguage());
        
        if(planetRequest.getFlavor() != null) {
            // add excerpts
            if(planetRequest.isExcerpts()) {
                key.append("/excerpts");
            }
        } else {
            // add login state
            if(planetRequest.getAuthenticUser() != null) {
                key.append("/user=").append(planetRequest.getAuthenticUser());
            }
        }
        
        // add group
        if (planetRequest.getPlanet() != null) {
            key.append("/group=").append(planetRequest.getPlanet());
        }

        return key.toString();
    }
    
}
