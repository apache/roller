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

package org.apache.roller.planet.business.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.AbstractManagerImpl;
import org.apache.roller.planet.business.PlanetURLStrategy;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;

/**
 * Implements Planet, the entry point interface for the Roller-Planet business 
 * tier APIs using the Java Persistence API (JPA).
 */
@com.google.inject.Singleton
public class JPAPlanetImpl extends AbstractManagerImpl implements Planet {   
    
    private static Log log = LogFactory.getLog(JPAPlanetImpl.class);
    
    // a persistence utility class
    private final JPAPersistenceStrategy strategy;
        
    // references to the managers we maintain
    private final PlanetManager planetManager;
    private final PropertiesManager propertiesManager;
    
    // url strategy
    private final PlanetURLStrategy urlStrategy;
    
    // feed fetcher
    private final FeedFetcher feedFetcher;
    
        
    @com.google.inject.Inject  
    protected JPAPlanetImpl(
            JPAPersistenceStrategy strategy, 
            PlanetManager     planetManager, 
            PropertiesManager propertiesManager,
            PlanetURLStrategy       urlStrategy,
            FeedFetcher       feedFetcher) throws RollerException {
        
        this.strategy = strategy;
        this.propertiesManager = propertiesManager;
        this.planetManager = planetManager;
        this.urlStrategy = urlStrategy;
        this.feedFetcher = feedFetcher;
    }
    

    @Override
    public void initialize() throws Exception {
        
        log.info("Initializing Roller Planet business tier");
        
        getPropertiesManager().initialize();
        getWebloggerManager().initialize();
        
        // we always need to do a flush after initialization because it's
        // possible that some changes need to be persisted
        try {
            flush();
        } catch(RollerException ex) {
            throw new InitializationException("Error flushing after initialization", ex);
        }
        
        log.info("Roller Planet business tier successfully initialized");
    }
    
    
    public void flush() throws RollerException {
		this.strategy.flush();
    }

    
    @Override
    public void release() {
        this.strategy.release();
    }

    
    @Override
    public void shutdown() {
        this.release();
    }
    
    
    /**
     * @see org.apache.roller.weblogger.business.Weblogger#getBookmarkManager()
     */
    public PlanetManager getWebloggerManager() {
        return planetManager;
    }

     
    /**
     * @see org.apache.roller.weblogger.business.Weblogger#getBookmarkManager()
     */
    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    
    public PlanetURLStrategy getURLStrategy() {
        return this.urlStrategy;
    }
    
    
    public FeedFetcher getFeedFetcher() {
        return this.feedFetcher;
    }
    
}
