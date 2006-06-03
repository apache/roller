/*
 * CacheFactory.java
 *
 * Created on October 26, 2005, 3:25 PM
 */

package org.roller.presentation.cache;

import java.util.Map;


/**
 * An interface representing a cache factory.  Implementors of this interface
 * are responsible for providing a method to construct cache implementations.
 *
 * In Roller you switch between various caching options by choosing a different
 * cache factory before starting up the application.
 *
 * @author Allen Gilliland
 */
public interface CacheFactory {
    
    public Cache constructCache(Map properties);
    
}
