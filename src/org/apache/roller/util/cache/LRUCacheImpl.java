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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A simple LRU Cache.
 */
public class LRUCacheImpl implements Cache {
    
    private static Log mLogger = LogFactory.getLog(LRUCacheImpl.class);
    
    private Map cache = null;
    
    
    protected LRUCacheImpl() {
        
        this.cache = Collections.synchronizedMap(new LRULinkedHashMap(100));
    }
    
    
    protected LRUCacheImpl(int maxsize) {
        
        this.cache = Collections.synchronizedMap(new LRULinkedHashMap(maxsize));
    }
    
    
    /**
     * Store an entry in the cache.
     */
    public synchronized void put(String key, Object value) {
        
        this.cache.put(key, value);
    }
    
    
    /**
     * Retrieve an entry from the cache.
     */
    public synchronized Object get(String key) {
        
        return this.cache.get(key);
    }
    
    
    public synchronized void remove(String key) {
        
        this.cache.remove(key);
    }
    
    
    public synchronized void remove(Set keys) {
        
        Iterator it = keys.iterator();
        while(it.hasNext())
            this.cache.remove((String) it.next());
    }
    
    
    public synchronized void clear() {
        
        this.cache.clear();
    }
    
    
    public synchronized Set keySet() {
        return this.cache.keySet();
    }
    
    
    public Map stats() {
        
        return new HashMap();
    }
    
    
    // David Flanaghan: http://www.davidflanagan.com/blog/000014.html
    private static class LRULinkedHashMap extends LinkedHashMap {
        protected int maxsize;
        
        public LRULinkedHashMap(int maxsize) {
            super(maxsize * 4 / 3 + 1, 0.75f, true);
            this.maxsize = maxsize;
        }
        
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > this.maxsize;
        }
    }
    
}
