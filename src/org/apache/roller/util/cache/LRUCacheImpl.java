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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A simple LRU Cache.
 */
public class LRUCacheImpl implements Cache {
    
    private static Log log = LogFactory.getLog(LRUCacheImpl.class);
    
    private String id = null;
    private Map cache = null;
    
    // for metrics
    protected double hits = 0;
    protected double misses = 0;
    protected double puts = 0;
    protected double removes = 0;
    protected Date startTime = new Date();
    
    
    protected LRUCacheImpl(String id) {
        
        this.id = id;
        this.cache = Collections.synchronizedMap(new LRULinkedHashMap(100));
    }
    
    
    protected LRUCacheImpl(String id, int maxsize) {
        
        this.id = id;
        this.cache = Collections.synchronizedMap(new LRULinkedHashMap(maxsize));
    }
    
    
    public String getId() {
        return this.id;
    }
    
    
    /**
     * Store an entry in the cache.
     */
    public synchronized void put(String key, Object value) {
        
        this.cache.put(key, value);
        puts++;
    }
    
    
    /**
     * Retrieve an entry from the cache.
     */
    public synchronized Object get(String key) {
        
        Object obj = this.cache.get(key);
        
        // for metrics
        if(obj == null) {
            misses++;
        } else {
            hits++;
        }
        
        return obj;
    }
    
    
    public synchronized void remove(String key) {
        
        this.cache.remove(key);
        removes++;
    }
    
    
    public synchronized void clear() {
        
        this.cache.clear();
        
        // clear metrics
        hits = 0;
        misses = 0;
        puts = 0;
        removes = 0;
        startTime = new Date();
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("startTime", this.startTime);
        stats.put("hits", new Double(this.hits));
        stats.put("misses", new Double(this.misses));
        stats.put("puts", new Double(this.puts));
        stats.put("removes", new Double(this.removes));
        
        // calculate efficiency
        if((misses - removes) > 0) {
            double efficiency = hits / (misses + hits);
            stats.put("efficiency", new Double(efficiency * 100));
        }
        
        return stats;
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
