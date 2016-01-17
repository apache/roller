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
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheHandler;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.ExpiringCacheEntry;

/**
 * Cache for site-wide weblog content.
 */
public final class SiteWideCache implements CacheHandler {
    
    private static Log log = LogFactory.getLog(SiteWideCache.class);
    
    public static final String CACHE_ID = "cache.sitewide";
    
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private int size = 50;

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

    private SiteWideCache() {
        if (enabled) {
            contentCache = CacheManager.constructCache(this, CACHE_ID, size, timeoutSec);
        } else {
            log.warn("Site-wide cache has been DISABLED");
        }
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
            this.lastUpdateTime = new ExpiringCacheEntry(lastModified, 15 * DateUtils.MILLIS_PER_MINUTE);
        }
        
        return lastModified;
    }

    /**
     * A weblog entry has changed.
     */
    public void invalidate(WeblogEntry entry) {
        
        if (!enabled) {
            return;
        }
        
        this.contentCache.clear();
        this.lastUpdateTime = null;
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(Weblog website) {
        
        if (!enabled) {
            return;
        }
        
        this.contentCache.clear();
        this.lastUpdateTime = null;
    }
    
    
    /**
     * A bookmark has changed.
     */
    public void invalidate(WeblogBookmark bookmark) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(bookmark.getWeblog().getHandle())) {
            invalidate(bookmark.getWeblog());
        }
    }

    /**
     * A comment has changed.
     */
    public void invalidate(WeblogEntryComment comment) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(comment.getWeblogEntry().getWeblog().getHandle())) {
            invalidate(comment.getWeblogEntry().getWeblog());
        }
    }
    
    
    /**
     * A user profile has changed.
     */
    public void invalidate(User user) {
        // ignored
    }
    
    
    /**
     * A category has changed.
     */
    public void invalidate(WeblogCategory category) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(category.getWeblog().getHandle())) {
            invalidate(category.getWeblog());
        }
    }
    
    
    /**
     * A weblog template has changed.
     */
    public void invalidate(WeblogTemplate template) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(template.getWeblog().getHandle())) {
            invalidate(template.getWeblog());
        }
    }
    
}
