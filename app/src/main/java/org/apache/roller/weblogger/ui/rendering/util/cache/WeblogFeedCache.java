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
 */

package org.apache.roller.weblogger.ui.rendering.util.cache;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.LazyExpiringCacheEntry;


/**
 * Cache for weblog feed content.
 */
public final class WeblogFeedCache {
    
    private static Log log = LogFactory.getLog(WeblogFeedCache.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    public static final String CACHE_ID = "cache.weblogfeed";
    
    // keep cached content
    private boolean cacheEnabled = true;
    private Cache contentCache = null;
    
    // reference to our singleton instance
    private static WeblogFeedCache singletonInstance = new WeblogFeedCache();
    
    
    private WeblogFeedCache() {
        
        cacheEnabled = WebloggerConfig.getBooleanProperty(CACHE_ID+".enabled");
        
        Map cacheProps = new HashMap();
        cacheProps.put("id", CACHE_ID);
        Enumeration allProps = WebloggerConfig.keys();
        String prop = null;
        while(allProps.hasMoreElements()) {
            prop = (String) allProps.nextElement();
            
            // we are only interested in props for this cache
            if(prop.startsWith(CACHE_ID+".")) {
                cacheProps.put(prop.substring(CACHE_ID.length()+1), 
                        WebloggerConfig.getProperty(prop));
            }
        }
        
        log.info(cacheProps);
        
        if(cacheEnabled) {
            contentCache = CacheManager.constructCache(null, cacheProps);
        } else {
            log.warn("Caching has been DISABLED");
        }
    }
    
    
    public static WeblogFeedCache getInstance() {
        return singletonInstance;
    }
    
    
    public Object get(String key, long lastModified) {
        
        if (!cacheEnabled) {
            return null;
        }
        
        Object entry = null;
        
        LazyExpiringCacheEntry lazyEntry =
                (LazyExpiringCacheEntry) this.contentCache.get(key);
        if(lazyEntry != null) {
            entry = lazyEntry.getValue(lastModified);
            
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
        
        if (!cacheEnabled) {
            return;
        }
        
        contentCache.put(key, new LazyExpiringCacheEntry(value));
        log.debug("PUT "+key);
    }
    
    
    public void remove(String key) {
        
        if (!cacheEnabled) {
            return;
        }
        
        contentCache.remove(key);
        log.debug("REMOVE "+key);
    }
    
    
    public void clear() {
        
        if (!cacheEnabled) {
            return;
        }
        
        contentCache.clear();
        log.debug("CLEAR");
    }
    
    
    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     *
     * <handle>/<type>/<format>/<term>[/category][/language][/excerpts]
     *
     * examples ...
     *
     * foo/entries/rss/en
     * foo/comments/rss/MyCategory/en
     * foo/entries/atom/en/excerpts
     *
     */
    public String generateKey(WeblogFeedRequest feedRequest) {
        
        StringBuilder key = new StringBuilder();
        
        key.append(this.CACHE_ID).append(":");
        key.append(feedRequest.getWeblogHandle());
        
        key.append("/").append(feedRequest.getType());
        key.append("/").append(feedRequest.getFormat());
        
        if (feedRequest.getTerm() != null) {
            key.append("/search/").append(feedRequest.getTerm());
        }
        
        if(feedRequest.getWeblogCategoryName() != null) {
            String cat = feedRequest.getWeblogCategoryName();
            try {
                cat = URLEncoder.encode(cat, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // should never happen, utf-8 is always supported
            }
            
            key.append("/").append(cat);
        }
        
        if(feedRequest.getTags() != null && feedRequest.getTags().size() > 0) {
          Set ordered = new TreeSet(feedRequest.getTags());
          String[] tags = (String[]) ordered.toArray(new String[ordered.size()]);  
          key.append("/tags/").append(Utilities.stringArrayToString(tags,"+"));
        }        
        
        if(feedRequest.getLocale() != null) {
            key.append("/").append(feedRequest.getLocale());
        }
        
        if(feedRequest.isExcerpts()) {
            key.append("/excerpts");
        }
        
        return key.toString();
    }
    
}
