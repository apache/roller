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
package org.apache.roller.weblogger.util.cache;

/**
 * Cache for weblog page content.
 */
public class LazyExpiringCache extends ExpiringCache {

    public Object get(String key, long lastModified) {
        if (enabled) {
            Object entry = null;
            LazyExpiringCacheEntry lazyEntry = (LazyExpiringCacheEntry) this.contentCache.get(key);
            if (lazyEntry != null) {
                entry = lazyEntry.getValue(lastModified);

                if (entry != null) {
                    log.debug("HIT " + key);
                } else {
                    log.debug("HIT-EXPIRED " + key);
                }
            } else {
                log.debug("MISS " + key);
            }
            return entry;
        } else {
            return null;
        }
    }

    public void put(String key, Object value) {
        if (enabled) {
            contentCache.put(key, new LazyExpiringCacheEntry(value));
            log.debug("PUT "+key);
        }
    }
}
