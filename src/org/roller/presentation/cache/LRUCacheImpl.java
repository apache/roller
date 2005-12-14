/*
 * LRUCacheImpl.java
 *
 * Created on November 6, 2005, 10:33 AM
 */
package org.roller.presentation.cache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A simple LRU Cache.
 *
 * @author Allen Gilliland
 */
public class LRUCacheImpl implements Cache {
    
    private static Log mLogger = LogFactory.getLog(LRUCacheImpl.class);
    
    private Map cache = null;
    
    
    protected LRUCacheImpl() {
        
        this.cache = new LRULinkedHashMap(100);
    }
    
    
    protected LRUCacheImpl(int maxsize) {
        
        this.cache = new LRULinkedHashMap(maxsize);
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
