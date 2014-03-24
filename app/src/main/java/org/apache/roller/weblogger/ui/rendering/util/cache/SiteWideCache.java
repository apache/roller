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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheHandler;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.ExpiringCacheEntry;


/**
 * Cache for site-wide weblog content.
 */
public class SiteWideCache implements CacheHandler {
    
    private static Log log = LogFactory.getLog(SiteWideCache.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    public static final String CACHE_ID = "cache.sitewide";
    
    // keep cached content
    private boolean cacheEnabled = true;
    private Cache contentCache = null;
    
    // keep a cached version of last expired time
    private ExpiringCacheEntry lastUpdateTime = null;
    private long timeout = 15 * 60 * RollerConstants.SEC_IN_MS;
    
    // reference to our singleton instance
    private static SiteWideCache singletonInstance = new SiteWideCache();
    
    
    private SiteWideCache() {
        
        cacheEnabled = WebloggerConfig.getBooleanProperty(CACHE_ID+".enabled");
        
        Map<String, String> cacheProps = new HashMap<String, String>();
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
            contentCache = CacheManager.constructCache(this, cacheProps);
        } else {
            log.warn("Caching has been DISABLED");
        }
    }
    
    
    public static SiteWideCache getInstance() {
        return singletonInstance;
    }
    
    
    public Object get(String key) {
        
        if (!cacheEnabled) {
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
        
        if (!cacheEnabled) {
            return;
        }
        
        contentCache.put(key, value);
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
            this.lastUpdateTime = new ExpiringCacheEntry(lastModified, this.timeout);
        }
        
        return lastModified;
    }
    
    
    /**
     * Generate a cache key from a parsed weblog page request.
     * This generates a key of the form ...
     *
     * <handle>/<ctx>[/anchor][/language][/user]
     *   or
     * <handle>/<ctx>[/weblogPage][/date][/category][/tags][/language][/user]
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
        
        StringBuilder key = new StringBuilder();
        
        key.append(CACHE_ID).append(":");
        key.append("page/");
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
      
        key.append("/deviceType=").append(pageRequest.getDeviceType().toString());

        // we allow for arbitrary query params for custom pages
        if(pageRequest.getCustomParams().size() > 0) {
            String queryString = paramsToString(pageRequest.getCustomParams());
            
            key.append("/qp=").append(queryString);
        }

        return key.toString();
    }
    
    
    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     *
     * <handle>/<type>/<format>/[/category][/language][/excerpts]
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
        key.append("feed/");
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
        
        if(feedRequest.getLocale() != null) {
            key.append("/").append(feedRequest.getLocale());
        }
        
        if(feedRequest.isExcerpts()) {
            key.append("/excerpts");
        }
        
        if(feedRequest.getTags() != null && feedRequest.getTags().size() > 0) {
          String[] tags = new String[feedRequest.getTags().size()];
          new TreeSet(feedRequest.getTags()).toArray(tags);
          key.append("/tags/").append(Utilities.stringArrayToString(tags,"+"));
        }       
        
        return key.toString();
    }
    
    
    /**
     * A weblog entry has changed.
     */
    public void invalidate(WeblogEntry entry) {
        
        if (!cacheEnabled) {
            return;
        }
        
        this.contentCache.clear();
        this.lastUpdateTime = null;
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(Weblog website) {
        
        if (!cacheEnabled) {
            return;
        }
        
        this.contentCache.clear();
        this.lastUpdateTime = null;
    }
    
    
    /**
     * A bookmark has changed.
     */
    public void invalidate(WeblogBookmark bookmark) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(bookmark.getWebsite().getHandle())) {
            invalidate(bookmark.getWebsite());
        }
    }
    
    
    /**
     * A folder has changed.
     */
    public void invalidate(WeblogBookmarkFolder folder) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(folder.getWeblog().getHandle())) {
            invalidate(folder.getWeblog());
        }
    }
    
    
    /**
     * A comment has changed.
     */
    public void invalidate(WeblogEntryComment comment) {
        if(WebloggerRuntimeConfig.isSiteWideWeblog(comment.getWeblogEntry().getWebsite().getHandle())) {
            invalidate(comment.getWeblogEntry().getWebsite());
        }
    }
    
    
    /**
     * A referer has changed.
     */
    public void invalidate(WeblogReferrer referer) {
        // ignored
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
        if(WebloggerRuntimeConfig.isSiteWideWeblog(template.getWebsite().getHandle())) {
            invalidate(template.getWebsite());
        }
    }
    
    
    private String paramsToString(Map<String, String[]> map) {
        
        if (map == null) {
            return null;
        }
        
        StringBuilder string = new StringBuilder();
        
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            if(entry.getValue() != null) {
                string.append(",").append(entry.getKey()).append("=").append(entry.getValue()[0]);
            }
        }
        
        return Utilities.toBase64(string.toString().substring(1).getBytes());
    }
    
}
