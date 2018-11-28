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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.LazyExpiringCacheEntry;


/**
 * Cache for weblog page content.
 */
public class WeblogPageCache {
    
    private static Log log = LogFactory.getLog(WeblogPageCache.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    public static final String CACHE_ID = "cache.weblogpage";
    
    // keep cached content
    private boolean cacheEnabled = true;
    private Cache contentCache = null;
    
    // reference to our singleton instance
    private static WeblogPageCache singletonInstance = new WeblogPageCache();
    
    
    private WeblogPageCache() {
        
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
    
    
    public static WeblogPageCache getInstance() {
        return singletonInstance;
    }
    
    
    public Object get(String key, long lastModified) {
        
        if(!cacheEnabled)
            return null;
        
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
        
        if(!cacheEnabled)
            return;
        
        contentCache.put(key, new LazyExpiringCacheEntry(value));
        log.debug("PUT "+key);
    }
    
    
    public void remove(String key) {
        
        if(!cacheEnabled)
            return;
        
        contentCache.remove(key);
        log.debug("REMOVE "+key);
    }
    
    
    public void clear() {
        
        if(!cacheEnabled)
            return;
        
        contentCache.clear();
        log.debug("CLEAR");
    }
    
    
    /**
     * Generate a cache key from a parsed weblog page request.
     * This generates a key of the form ...
     *
     * <handle>/<ctx>[/anchor][/language][/user]
     *   or
     * <handle>/<ctx>[/weblogPage][/date][/category][/language][/user]
     *
     *
     * examples ...
     *
     * foo/en
     * foo/entry_anchor
     * foo/20051110/en
     * foo/MyCategory/en/user=myname
     *
     */
    public String generateKey(WeblogPageRequest pageRequest) {
        
        StringBuffer key = new StringBuffer();
        
        key.append(this.CACHE_ID).append(":");
        key.append(pageRequest.getWeblogHandle());
        
        if(pageRequest.getWeblogAnchor() != null) {
            
            String anchor = null;
            try {
                // may contain spaces or other bad chars
                anchor = URLEncoder.encode(pageRequest.getWeblogAnchor(), "UTF-8");
            } catch(UnsupportedEncodingException ex) {
                // ignored
            }
            
            key.append("/entry/").append(anchor);
        } else {
            
            if(pageRequest.getWeblogPageName() != null) {
                key.append("/page/").append(pageRequest.getWeblogPageName());
            }
            
            if(pageRequest.getWeblogDate() != null) {
                key.append("/").append(pageRequest.getWeblogDate());
            }
            
            if(pageRequest.getWeblogCategoryName() != null) {
                String cat = null;
                try {
                    // may contain spaces or other bad chars
                    cat = URLEncoder.encode(pageRequest.getWeblogCategoryName(), "UTF-8");
                } catch(UnsupportedEncodingException ex) {
                    // ignored
                }
                
                key.append("/").append(cat);
            }
            
            if("tags".equals(pageRequest.getContext())) {
                key.append("/tags/");
                if(pageRequest.getTags() != null && pageRequest.getTags().size() > 0) {
                    Set ordered = new TreeSet(pageRequest.getTags());
                    String[] tags = (String[]) ordered.toArray(new String[ordered.size()]);
                    key.append(Utilities.stringArrayToString(tags,"+"));
                }
            }
        }
        
        if(pageRequest.getLocale() != null) {
            key.append("/").append(pageRequest.getLocale());
        }
        
        // add page number when applicable
        if(pageRequest.getWeblogAnchor() == null) {
            key.append("/page=").append(pageRequest.getPageNum());
        }
        
        // add login state
        if(pageRequest.getAuthenticUser() != null) {
            key.append("/user=").append(pageRequest.getAuthenticUser());
        }
        
        // we allow for arbitrary query params for custom pages
        if(pageRequest.getWeblogPageName() != null &&
                pageRequest.getCustomParams().size() > 0) {
            String queryString = paramsToString(pageRequest.getCustomParams());
            
            key.append("/qp=").append(queryString);
        }
        
        return key.toString();
    }
    
    
    private String paramsToString(Map map) {
        
        if(map == null) {
            return null;
        }
        
        StringBuffer string = new StringBuffer();
        
        String key = null;
        String[] value = null;
        Iterator keys = map.keySet().iterator();
        while(keys.hasNext()) {
            key = (String) keys.next();
            value = (String[]) map.get(key);
            
            if(value != null) {
                string.append(",").append(key).append("=").append(value[0]);
            }
        }
        
        return Utilities.toBase64(string.toString().substring(1).getBytes());
    }
    
}
