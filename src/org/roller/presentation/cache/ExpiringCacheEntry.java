/*
 * ExpiringCacheEntry.java
 *
 * Created on November 4, 2005, 12:10 PM
 */

package org.roller.presentation.cache;

/**
 * A cache entry that expires.
 *
 * We use this class to wrap objects being cached and associate a timestamp
 * and timeout period with them so we can know when they expire.
 *
 * @author Allen Gilliland
 */
public class ExpiringCacheEntry {
    
    private Object value;
    private long timeCached = -1;
    private long timeout = 0;
    
    
    public ExpiringCacheEntry(Object value, long timeout) {
        this.value = value;
        
        // make sure that we don't support negative values
        if(timeout > 0) {
            this.timeout = timeout;
        }
        
        this.timeCached = System.currentTimeMillis();
    }
    
    
    public long getTimeCached() {
        return this.timeCached;
    }
    
    
    public long getTimeout() {
        return this.timeout;
    }
    
    
    /**
     * Retrieve the value of this cache entry.
     *
     * If the value has expired then we return null.
     */
    public Object getValue() {
        if(this.hasExpired()) {
            return null;
        } else {
            return this.value;
        }
    }
    
    
    /**
     * Determine if this cache entry has expired.
     */
    public boolean hasExpired() {
        
        long now = System.currentTimeMillis();
        
        return ((this.timeCached + this.timeout) < now);
    }
    
}
