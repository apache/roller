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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An LRU cache where entries expire after a given timeout period.
 */
public class ExpiringLRUCacheImpl extends LRUCacheImpl {
    
    private static Log log = LogFactory.getLog(ExpiringLRUCacheImpl.class);
    
    private long timeout = 0;
    
    
    protected ExpiringLRUCacheImpl(String id) {
        
        super(id);
        this.timeout = 60 * 60 * 1000;
    }
    
    
    protected ExpiringLRUCacheImpl(String id, int maxsize, long timeout) {
        
        super(id, maxsize);
        
        // timeout is specified in seconds; only positive values allowed
        if(timeout > 0) {
            this.timeout = timeout * 1000;
        }
    }
    
    
    /**
     * Store an entry in the cache.
     *
     * We wrap the cached object in our ExpiringCacheEntry object so that we
     * can track when the entry has expired.
     */
    public synchronized void put(String key, Object value) {
        
        ExpiringCacheEntry entry = new ExpiringCacheEntry(value, this.timeout);
        super.put(key, entry);
    }
    
    
    /**
     * Retrieve an entry from the cache.
     *
     * This LRU cache supports timeouts, so if the cached object has expired
     * then we return null, just as if the entry wasn't found.
     */
    public Object get(String key) {
        
        Object value = null;
        ExpiringCacheEntry entry = null;
        
        synchronized(this) {
            entry = (ExpiringCacheEntry) super.get(key);
        }
        
        if (entry != null) {
            
            value = entry.getValue();
            
            // if the value is null then that means this entry expired
            if (value == null) {
                log.debug("EXPIRED ["+key+"]");
                hits--;
                super.remove(key);
            }
        }
        
        return value;
    }
    
}
