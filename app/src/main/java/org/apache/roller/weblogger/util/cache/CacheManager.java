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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * A governing class for Roller cache objects.
 *
 * The purpose of the CacheManager is to provide a level of abstraction between
 * classes that use a cache and the implementations of a cache.  This allows
 * us to create easily pluggable cache implementations.
 * 
 * The other purpose is to provide a single interface for interacting with all
 * Roller caches at the same time.  This is beneficial because as data
 * changes in the system we often need to notify all caches that some part of
 * their cached data needs to be invalidated, and the CacheManager makes that
 * process easier.
 */
public final class CacheManager {
    
    private static Log log = LogFactory.getLog(CacheManager.class);
    
    // a set of all registered cache handlers
    private static Set<CacheHandler> cacheHandlers = new HashSet<>();
    
    // a map of all registered caches
    private static Map<String, Cache> caches = new HashMap<>();
    
    static {
        // add custom handlers
        String customHandlers = WebloggerConfig.getProperty("cache.customHandlers");
        if(customHandlers != null && customHandlers.trim().length() > 0) {
            
            String[] cHandlers = customHandlers.split(",");
            for (String cHandler : cHandlers) {
                // use reflection to instantiate the handler class
                try {
                    Class handlerClass = Class.forName(cHandler);
                    CacheHandler customHandler = 
                            (CacheHandler) handlerClass.newInstance();
                    
                    cacheHandlers.add(customHandler);
                } catch(ClassCastException cce) {
                    log.error("It appears that your handler does not implement "+
                            "the CacheHandler interface",cce);
                } catch(Exception e) {
                    log.error("Unable to instantiate cache handler ["+cHandler+"]", e);
                }
            }
        }
    }

    // a non-instantiable class
    private CacheManager() {}

    public static Cache constructCache(CacheHandler handler, String id, int size, int timeoutSec) {
        Cache cache = new ExpiringLRUCacheImpl(id, size, timeoutSec);
        caches.put(cache.getId(), cache);

        // register the handler for this new cache
        if(handler != null) {
            cacheHandlers.add(handler);
        }

        return cache;
    }

    /**
     * Register a CacheHandler to listen for object invalidations.
     *
     * This is here so that it's possible to add classes which would respond
     * to object invalidations without necessarily having to create a cache.
     *
     * An example would be a handler designed to notify other machines in a 
     * cluster when an object has been invalidated, or possibly the search
     * index management classes are interested in knowing when objects are
     * invalidated.
     */
    public static void registerHandler(CacheHandler handler) {

        log.debug("Registering handler "+handler);

        if(handler != null) {
            cacheHandlers.add(handler);
        }
    }
    
    
    public static void invalidate(WeblogEntry entry) {
        
        log.debug("invalidating entry = "+entry.getAnchor());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(entry);
        }
    }
    
    
    public static void invalidate(Weblog website) {
        
        log.debug("invalidating website = "+website.getHandle());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(website);
        }
    }
    
    
    public static void invalidate(WeblogBookmark bookmark) {
        
        log.debug("invalidating bookmark = "+bookmark.getId());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(bookmark);
        }
    }
    
    
    public static void invalidate(WeblogEntryComment comment) {
        
        log.debug("invalidating comment = "+comment.getId());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(comment);
        }
    }
    
    
    public static void invalidate(User user) {
        
        log.debug("invalidating user = "+user.getUserName());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(user);
        }
    }
    
    
    public static void invalidate(WeblogCategory category) {
        
        log.debug("invalidating category = " + category.getId());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(category);
        }
    }
    
    
    public static void invalidate(WeblogTemplate template) {
        log.debug("invalidating template = " + template.getId());
        for (CacheHandler handler : cacheHandlers) {
            handler.invalidate(template);
        }
    }

    
    /**
     * Flush the entire cache system.
     */
    public static void clear() {
        for (Cache cache : caches.values()) {
            cache.clear();
        }
    }
    
    
    /**
     * Flush a single cache.
     */
    public static void clear(String cacheId) {
        Cache cache = caches.get(cacheId);
        if(cache != null) {
            cache.clear();
        }
    }
    
    
    /**
     * Compile stats from all registered caches.
     *
     * This is basically a hacky version of instrumentation which is being
     * thrown in because we don't have a full instrumentation strategy yet.
     * This is here with the full expectation that it will be replaced by
     * something a bit more elaborate, like JMX.
     */
    public static Map<String, Map<String, Object>> getStats() {
        Map<String, Map<String, Object>> allStats = new HashMap<>();
        for (Cache cache : caches.values()) {
            allStats.put(cache.getId(), cache.getStats());
        }
        return allStats;
    }

}
