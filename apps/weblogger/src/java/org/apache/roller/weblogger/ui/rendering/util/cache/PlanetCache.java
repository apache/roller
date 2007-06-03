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

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.weblogger.ui.rendering.util.PlanetRequest;
import org.apache.roller.weblogger.util.cache.Cache;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.cache.ExpiringCacheEntry;


/**
 * Cache for planet content.
 */
public class PlanetCache {
    
    private static Log log = LogFactory.getLog(PlanetCache.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    public static final String CACHE_ID = "cache.planet";
    
    // keep cached content
    private boolean cacheEnabled = true;
    private Cache contentCache = null;
    
    // keep a cached version of last expired time
    private ExpiringCacheEntry lastUpdateTime = null;
    private long timeout = 15 * 60 * 1000;
    
    // reference to our singleton instance
    private static PlanetCache singletonInstance = new PlanetCache();
    
    
    private PlanetCache() {
        
        cacheEnabled = RollerConfig.getBooleanProperty(CACHE_ID+".enabled");
        
        Map cacheProps = new HashMap();
        cacheProps.put("id", CACHE_ID);
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
        
        if(cacheEnabled) {
            contentCache = CacheManager.constructCache(null, cacheProps);
        } else {
            log.warn("Caching has been DISABLED");
        }
        
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
        
        if(!cacheEnabled)
            return null;
        
        Object entry = contentCache.get(key);
        
        if(entry == null) {
            log.debug("MISS "+key);
        } else {
            log.debug("HIT "+key);
        }
        
        return entry;
    }
    
    
    public void put(String key, Object value) {
        
        if(!cacheEnabled)
            return;
        
        contentCache.put(key, value);
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
            
            // TODO: ROLLER40 last updated for planet
            lastModified = null; // PlanetFactory.getPlanet().getPlanetManager().getLastUpdated();
            
            if (lastModified == null) {
                lastModified = new Date();
                log.warn("Can't get lastUpdate time, using current time instead");
            }
            
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
        
        StringBuffer key = new StringBuffer();
        
        key.append(this.CACHE_ID).append(":");
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
        
        return key.toString();
    }
    
}
