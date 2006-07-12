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

package org.apache.roller.ui.rendering.util;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.cache.Cache;
import org.apache.roller.util.cache.CacheHandler;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.util.cache.ExpiringCacheEntry;


/**
 * Special cache class for planet content.  We do this as it's own class instead
 * of in a servlet like elsewhere because this cache is shared between the
 * planetrss servlet and the planet.do struts action.
 */
public class PlanetCache implements CacheHandler {
    
    private static Log log = LogFactory.getLog(PlanetCache.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    public static final String CACHE_ID = "cache.planet";
    
    private Cache contentCache = null;
    
    private ExpiringCacheEntry lastUpdateTime = null;
    private long timeout = 15 * 60 * 1000;
    
    // for metrics
    private double hits = 0;
    private double misses = 0;
    private Date startTime = new Date();
    
    private static PlanetCache singletonInstance = new PlanetCache();
    
    
    private PlanetCache() {
        
        Map cacheProps = new HashMap();
        Enumeration allProps = RollerConfig.keys();
        String prop = null;
        while(allProps.hasMoreElements()) {
            prop = (String) allProps.nextElement();
            
            // we are only interested in props for this cache
            if(prop.startsWith(CACHE_ID+".")) {
                cacheProps.put(prop.substring(CACHE_ID.length()+1), 
                        RollerConfig.getProperty(prop));
            }
        }
        
        log.info("Planet cache = "+cacheProps);
        
        contentCache = CacheManager.constructCache(this, cacheProps);
        
        // lookup our timeout value
        String timeoutString = RollerConfig.getProperty("cache.planet.timeout");
        try {
            long timeoutSecs = Long.parseLong(timeoutString);
            this.timeout = timeoutSecs * 1000;
        } catch(Exception e) {
            // ignored ... illegal value
        }
    }
    
    
    public static PlanetCache getInstance() {
        return singletonInstance;
    }
    
    
    public Object get(String key) {
        
        Object entry = contentCache.get(key);
        
        if(entry == null) {
            this.misses++;
            log.debug("MISS "+key);
        } else {
            this.hits++;
            log.debug("HIT "+key);
        }
        
        return entry;
    }
    
    
    public void put(String key, Object value) {
        contentCache.put(key, value);
        log.debug("PUT "+key);
    }
    
    
    public Date lastModified() {
        
        Date lastModified = null;
        
        // first try our cached version
        if(this.lastUpdateTime != null) {
            lastModified = (Date) this.lastUpdateTime.getValue();
        }
        
        // still null, we need to get a fresh value
        if(lastModified == null) {
            
            try {
                lastModified = RollerFactory.getRoller().getPlanetManager().getLastUpdated();
            } catch (RollerException ex) {
                log.error("Error getting planet manager", ex);
            }
            
            if (lastModified == null) {
                lastModified = new Date();
                log.warn("Can't get lastUpdate time, using current time instead");
            }
            
            this.lastUpdateTime = new ExpiringCacheEntry(lastModified, this.timeout);
        }
        
        return lastModified;
    }

    
    /**
     * A weblog entry has changed.
     */
    public void invalidate(WeblogEntryData entry) {
        // ignored
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(WebsiteData website) {
        // ignored
    }
    
    
    /**
     * A bookmark has changed.
     */
    public void invalidate(BookmarkData bookmark) {
        // ignored
    }
    
    
    /**
     * A folder has changed.
     */
    public void invalidate(FolderData folder) {
        // ignored
    }
    
    
    /**
     * A comment has changed.
     */
    public void invalidate(CommentData comment) {
        // ignored
    }
    
    
    /**
     * A referer has changed.
     */
    public void invalidate(RefererData referer) {
        // ignored
    }
    
    
    /**
     * A user profile has changed.
     */
    public void invalidate(UserData user) {
        // ignored
    }
    
    
    /**
     * A category has changed.
     */
    public void invalidate(WeblogCategoryData category) {
        // ignored
    }
    
    
    /**
     * A weblog template has changed.
     */
    public void invalidate(WeblogTemplate template) {
        // ignored
    }
    
    
    /**
     * Clear the entire cache.
     */
    public void clear() {
        log.info("Clearing cache");
        this.contentCache.clear();
        this.startTime = new Date();
        this.hits = 0;
        this.misses = 0;
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("cacheType", this.contentCache.getClass().getName());
        stats.put("startTime", this.startTime);
        stats.put("hits", new Double(this.hits));
        stats.put("misses", new Double(this.misses));
        
        // calculate efficiency
        if(misses > 0) {
            double efficiency = hits / (misses + hits);
            stats.put("efficiency", new Double(efficiency * 100));
        }
        
        return stats;
    }
    
}
