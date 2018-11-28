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

package org.apache.roller.weblogger.util.cache;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Roller Expiring LRU cache factory.
 */
public class ExpiringLRUCacheFactoryImpl implements CacheFactory {
    
    private static Log log = LogFactory.getLog(ExpiringLRUCacheFactoryImpl.class);
    
    
    // protected so only the CacheManager can instantiate us
    protected ExpiringLRUCacheFactoryImpl() {}
    
    
    /**
     * Construct a new instance of a Roller Expiring LRUCache.
     */
    public Cache constructCache(Map properties) {
        
        int size = 100;
        long timeout = 15 * 60;
        String id = "unknown";
        
        try {
            size = Integer.parseInt((String) properties.get("size"));
        } catch(Exception e) {
            // ignored
        }
        
        try {
            timeout = Long.parseLong((String) properties.get("timeout"));
        } catch(Exception e) {
            // ignored
        }
        
        String cacheId = (String) properties.get("id");
        if(cacheId != null) {
            id = cacheId;
        }
        
        Cache cache = new ExpiringLRUCacheImpl(id, size, timeout);
        
        log.debug("new cache constructed. size="+size+", timeout="+timeout);
        
        return cache;
    }
    
}
