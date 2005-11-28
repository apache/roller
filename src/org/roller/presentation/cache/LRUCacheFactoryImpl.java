/*
 * LRUCacheFactoryImpl.java
 *
 * Created on November 6, 2005, 10:48 AM
 */

package org.roller.presentation.cache;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Roller LRU Cache factory.
 *
 * @author Allen Gilliland
 */
public class LRUCacheFactoryImpl implements CacheFactory {
    
    private static Log mLogger = LogFactory.getLog(LRUCacheFactoryImpl.class);
    
    
    // protected so that only the CacheManager can instantiate us
    protected LRUCacheFactoryImpl() {}
    
    
    /**
     * Construct a new instance of a Roller LRUCache.
     */
    public Cache constructCache(Map properties) {
        
        int size = 100;
        
        try {
            size = Integer.parseInt((String) properties.get("size"));
        } catch(Exception e) {
            // ignored
        }
        
        Cache cache = new LRUCacheImpl(size);
        
        mLogger.debug("new cache constructed. size="+size);
        
        return cache;
    }
    
}
