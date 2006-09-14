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

package org.apache.roller.util.cache;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Roller LRU Cache factory.
 */
public class LRUCacheFactoryImpl implements CacheFactory {
    
    private static Log log = LogFactory.getLog(LRUCacheFactoryImpl.class);
    
    
    // protected so that only the CacheManager can instantiate us
    protected LRUCacheFactoryImpl() {}
    
    
    /**
     * Construct a new instance of a Roller LRUCache.
     */
    public Cache constructCache(Map properties) {
        
        int size = 100;
        String id = "unknown";
        
        try {
            size = Integer.parseInt((String) properties.get("size"));
        } catch(Exception e) {
            // ignored
        }
        
        String cacheId = (String) properties.get("id");
        if(cacheId != null) {
            id = cacheId;
        }
        
        Cache cache = new LRUCacheImpl(id, size);
        
        log.debug("new cache constructed. size="+size);
        
        return cache;
    }
    
}
