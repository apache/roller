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
import org.apache.roller.planet.business.FeedFetcher;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.hibernate.HibernatePersistenceStrategy;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.business.URLStrategy;


/**
 * A Hibernate specific implementation of the Roller Planet business layer.
 */
public class HibernatePlanetImpl implements Planet {   
    
    private static Log log = LogFactory.getLog(HibernatePlanetImpl.class);
    
    // our singleton instance
    protected static HibernatePlanetImpl me = null;
    
    // a persistence utility class
    protected HibernatePersistenceStrategy strategy = null;
    
    // references to the managers we maintain
    protected PlanetManager planetManager = null;
    protected PropertiesManager propertiesManager = null;
    
    // url strategy
    protected URLStrategy urlStrategy = null;
    
    // feed fetcher
    protected FeedFetcher feedFetcher = null;
    
        
    /**
     * Create HibernatePlanetImpl using Hibernate XML config file or config
     * file plus JDBC overrides from planet-custom.properties.
     */
    public HibernatePlanetImpl() throws PlanetException {
        
        strategy = getStrategy();
        
        try {
            String feedFetchClass = PlanetConfig.getProperty("feedfetcher.classname");
            if(feedFetchClass == null || feedFetchClass.trim().length() < 1) {
                throw new PlanetException("No FeedFetcher configured!!!");
            }
            
            Class fetchClass = Class.forName(feedFetchClass);
            FeedFetcher feedFetcher = (FeedFetcher) fetchClass.newInstance();
            
            // plug it in
            setFeedFetcher(feedFetcher);
            
        } catch (Exception e) {
            throw new PlanetException("Error initializing feed fetcher", e);
        }
    }
    
    protected HibernatePersistenceStrategy getStrategy() throws PlanetException {
        try {
            String dialect =  
                PlanetConfig.getProperty("hibernate.dialect");
            String connectionProvider = 
                PlanetConfig.getProperty("hibernate.connectionProvider");
            return new HibernatePersistenceStrategy(
                "/hibernate.cfg.xml", dialect, connectionProvider);

        } catch(Throwable t) {
            // if this happens then we are screwed
            log.fatal("Error initializing Hibernate", t);
            throw new PlanetException(t);
        }        
    }
    
    
    /**
     * Instantiates and returns an instance of HibernatePlanetImpl.
     */
    public static Planet instantiate() throws PlanetException {
        if (me == null) {
            log.debug("Instantiating HibernatePlanetImpl");
            me = new HibernatePlanetImpl();
        }
        
        return me;
    }
    
    
    public PlanetManager getPlanetManager() {
        if ( planetManager == null ) {
            planetManager = new HibernatePlanetManagerImpl(strategy);  
        }
        return planetManager;
    }
    
    
    public PropertiesManager getPropertiesManager() {
        if ( propertiesManager == null ) {
            propertiesManager = new HibernatePropertiesManagerImpl(strategy);  
        }
        return propertiesManager;
    }
    
    
    public URLStrategy getURLStrategy() {
        return this.urlStrategy;
    }
    
    public void setURLStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
        log.info("Using URLStrategy: " + urlStrategy.getClass().getName());
    }
    
    
    public FeedFetcher getFeedFetcher() {
        return this.feedFetcher;
    }
    
    public void setFeedFetcher(FeedFetcher feedFetcher) {
        this.feedFetcher = feedFetcher;
        log.info("Using FeedFetcher: " + feedFetcher.getClass().getName());
    }
    
    
    public void flush() throws PlanetException {
        this.strategy.flush();
    }
    
    
    public void release() {
        // allow managers to do any session cleanup
        if(this.propertiesManager != null) {
            this.propertiesManager.release();
        }
        
        if(this.planetManager != null) {
            this.planetManager.release();
        }
        
        // close down the session
        this.strategy.release();
    }
    
    
    public void shutdown() {
        // allow managers to do any shutdown needed
        if(this.propertiesManager != null) {
            this.propertiesManager.shutdown();
        }
        
        if(this.planetManager != null) {
            this.planetManager.shutdown();
        }
        
        // trigger the final release()
        this.release();
    }
    
}
