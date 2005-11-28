/*
 * LRUCacheFactoryImpl.java
 *
 * Created on October 26, 2005, 3:33 PM
 */

package org.roller.presentation.cache;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Roller Expiring LRU cache factory.
 *
 * @author Allen Gilliland
 */
public class ExpiringLRUCacheFactoryImpl implements CacheFactory {
    
    private static Log mLogger = 
            LogFactory.getLog(ExpiringLRUCacheFactoryImpl.class);
    
    
    // protected so only the CacheManager can instantiate us
    protected ExpiringLRUCacheFactoryImpl() {}
    
    
    /**
     * Construct a new instance of a Roller Expiring LRUCache.
     */
    public Cache constructCache(Map properties) {
        
        int size = 100;
        long timeout = 15 * 60;
        
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
        
        Cache cache = new ExpiringLRUCacheImpl(size, timeout);
        
        mLogger.debug("new cache constructed. size="+size+", timeout="+timeout);
        
        return cache;
    }
    
}