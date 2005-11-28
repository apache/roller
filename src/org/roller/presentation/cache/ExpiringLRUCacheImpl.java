/*
 * ExpiringLRUCacheImpl.java
 *
 * Created on November 6, 2005, 10:33 AM
 */

package org.roller.presentation.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An LRU cache where entries expire after a given timeout period.
 *
 * @author Allen Gilliland
 */
public class ExpiringLRUCacheImpl extends LRUCacheImpl {
    
    private static Log mLogger = LogFactory.getLog(ExpiringLRUCacheImpl.class);
    
    private long timeout = 0;
    
    
    public ExpiringLRUCacheImpl() {
        
        super();
        this.timeout = 60 * 60 * 1000;
    }
    
    
    public ExpiringLRUCacheImpl(int maxsize, long timeout) {
        
        super(maxsize);
        
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
                mLogger.debug("entry expired ["+key+"]");
                super.remove(key);
            }
        }
        
        return value;
    }
    
}
