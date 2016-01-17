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

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.ExpiringCacheEntry;

/**
 * Cache for XSRF salt values. This cache is part of XSRF protection wherein 
 * each HTTP POST must be accompanied by a valid salt value, i.e. one generated 
 * by Roller. If you're running distributed, then you must use a distributed 
 * cache, e.g. memcached
 */
public final class SaltCache {
    private static Log log = LogFactory.getLog(SaltCache.class);
    
    public static final String CACHE_ID = "cache.salt";

    private int size = 5000;

    public void setSize(int size) {
        this.size = size;
    }

    private int timeoutSec = 3600;

    public void setTimeoutSec(int timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    private Cache contentCache = null;
    
    private SaltCache() {
        contentCache = CacheManager.constructCache(null, CACHE_ID, size, timeoutSec);
    }

    public Object get(String key) {
        Object entry = null;
        
        ExpiringCacheEntry lazyEntry =
                (ExpiringCacheEntry) this.contentCache.get(key);
        if(lazyEntry != null) {
            entry = lazyEntry.getValue();
            if(entry != null) {
                log.debug("HIT "+key);
            } else {
                log.debug("HIT-EXPIRED "+key);
            }
            
        } else {
            log.debug("MISS "+key);
        }
        
        return entry;
    }

    public void put(String key, Object value) {
		// expire after 60 minutes
        contentCache.put(key, new ExpiringCacheEntry(value, DateUtils.MILLIS_PER_HOUR));
        log.debug("PUT "+key);
    }

    public void remove(String key) {
        contentCache.remove(key);
        log.debug("REMOVE "+key);
    }

    public void clear() {
        contentCache.clear();
        log.debug("CLEAR");
    }
}
