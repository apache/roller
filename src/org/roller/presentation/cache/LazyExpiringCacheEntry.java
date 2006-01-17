/*
 * LazyExpiringCacheEntry.java
 *
 * Created on January 17, 2006, 10:14 AM
 */

package org.roller.presentation.cache;

import java.util.Date;

/**
 * A cache entry that is meant to expire in a lazy fashion.
 *
 * The way to use this class is to wrap the object you want to cache in an
 * instance of this class and store that in your cache.  Then when you want
 * to retrieve this entry you must input a last-expired time which can be
 * compared against the time this entry was cached to determine if the cached
 * entry is "fresh".  If the object is not fresh then we don't return it.
 *
 * This essentially allows us to track when an object is cached and then before
 * we can retrieve that cached object we must compare it with it's last known
 * invalidation time to make sure it hasn't expired.  This is useful because
 * instead of actively purging lots of cached objects from the cache at 
 * invalidation time, we can now be lazy and just invalidate them when we
 * actually try to retrieve the cached object.
 *
 * This is useful for Roller because we will no longer have to iterate through
 * the list of cached objects and inspect the keys to figure out what items to
 * invalidate.  Instead we can just sit back and let the items be invalidated as
 * we try to use them.
 *
 * @author Allen Gilliland
 */
public class LazyExpiringCacheEntry {
    
    private Object value = null;
    private long timeCached = -1;
    
    
    public LazyExpiringCacheEntry(Object item) {
        this.value = item;
        this.timeCached = System.currentTimeMillis();
    }
    
    
    /**
     * Retrieve the value of this cache entry if it is still "fresh".
     *
     * If the value has expired then we return null.
     */
    public Object getValue(long lastInvalidated) {
        if(this.isInvalid(lastInvalidated)) {
            return null;
        } else {
            return this.value;
        }
    }
    
    
    /**
     * Determine if this cache entry has expired.
     */
    public boolean isInvalid(long lastInvalidated) {
        
        return (this.timeCached < lastInvalidated);
    }

    
    public long getTimeCached() {
        return timeCached;
    }
    
}
