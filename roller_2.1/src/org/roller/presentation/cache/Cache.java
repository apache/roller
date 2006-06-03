/*
 * RollerCache.java
 *
 * Created on September 18, 2005, 10:59 AM
 */

package org.roller.presentation.cache;

import java.util.Map;
import java.util.Set;


/**
 * Base interface representing a presentation cache in Roller.
 *
 * @author Allen Gilliland
 */
public interface Cache {
    
    /**
     * put an item in the cache.
     */
    public void put(String key, Object value);
    
    
    /**
     * get an item from the cache.
     */
    public Object get(String key);
    
    
    /**
     * remove an item from the cache.
     */
    public void remove(String key);
    
    
    /**
     * remove a set of items from the cache.
     */
    public void remove(Set keys);
    
    
    /**
     * clear the entire cache.
     */
    public void clear();
    
    
    /**
     * get a list of keys used in the cache.
     */
    public Set keySet();
    
    
    /**
     * get cache stats.
     */
    public Map stats();
    
}
