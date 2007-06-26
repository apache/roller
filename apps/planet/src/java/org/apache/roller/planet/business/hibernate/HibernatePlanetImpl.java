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

package org.apache.roller.planet.business.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.AbstractManagerImpl;
import org.apache.roller.planet.business.FeedFetcher;
import org.apache.roller.planet.business.InitializationException;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.business.URLStrategy;


/**
 * A Hibernate specific implementation of the Roller Planet business layer.
 */
@com.google.inject.Singleton
public class HibernatePlanetImpl extends AbstractManagerImpl implements Planet {   
    
    private static Log log = LogFactory.getLog(HibernatePlanetImpl.class);
    
    // a persistence utility class
    private final HibernatePersistenceStrategy strategy;
    
    // references to the managers we maintain
    private final PlanetManager planetManager;
    private final PropertiesManager propertiesManager;
    
    // url strategy
    private final URLStrategy urlStrategy;
    
    // feed fetcher
    private final FeedFetcher feedFetcher;
    
        
    /**
     * Create HibernatePlanetImpl using Hibernate XML config file or config
     * file plus JDBC overrides from planet-custom.properties.
     */
    @com.google.inject.Inject 
    protected HibernatePlanetImpl(
            HibernatePersistenceStrategy strategy, 
            PlanetManager     planetManager, 
            PropertiesManager propertiesManager,
            URLStrategy       urlStrategy,
            FeedFetcher       feedFetcher) throws PlanetException {
        
        this.strategy = strategy;
        this.propertiesManager = propertiesManager;
        this.planetManager = planetManager;
        this.urlStrategy = urlStrategy;
        this.feedFetcher = feedFetcher;
    }
    
    
    public PlanetManager getPlanetManager() {
        return planetManager;
    }
    
    
    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }
    
    
    public URLStrategy getURLStrategy() {
        return this.urlStrategy;
    }
    
    
    public FeedFetcher getFeedFetcher() {
        return this.feedFetcher;
    }
    
    
    @Override
    public void initialize() throws InitializationException {
        getPropertiesManager().initialize();
        getPlanetManager().initialize();
        
        // we always need to do a flush after initialization because it's
        // possible that some changes need to be persisted
        try {
            flush();
        } catch(PlanetException ex) {
            throw new InitializationException("Error flushing after initialization", ex);
        }
    }
    
    
    public void flush() throws PlanetException {
        this.strategy.flush();
    }
    
    
    @Override
    public void release() {
        // allow managers to do any session cleanup
        getPropertiesManager().release();
        getPlanetManager().release();
        
        // close down the session
        this.strategy.release();
    }
    
    
    @Override
    public void shutdown() {
        // allow managers to do any shutdown needed
        getPropertiesManager().shutdown();
        getPlanetManager().shutdown();
        
        // trigger the final release()
        this.release();
    }
    
}
