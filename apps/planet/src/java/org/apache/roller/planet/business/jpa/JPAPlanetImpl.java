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

import java.util.Enumeration;
import java.util.Properties;
import org.apache.roller.planet.business.DatabaseProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.URLStrategy;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.planet.business.FeedFetcher;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.config.PlanetConfig;


/**
 * Implements Planet, the entry point interface for the Roller-Planet business 
 * tier APIs using the Java Persistence API (JPA).
 */
public class JPAPlanetImpl implements Planet {   
    
    private static Log log = LogFactory.getLog(JPAPlanetImpl.class);
    
    // a persistence utility class
    protected JPAPersistenceStrategy strategy = null;
    
    // our singleton instance
    protected static JPAPlanetImpl me = null;
        
    // references to the managers we maintain
    private PlanetManager planetManager = null;
    private PropertiesManager propertiesManager = null;
    
    // url strategy
    protected URLStrategy urlStrategy = null;
    
    // feed fetcher
    protected FeedFetcher feedFetcher = null;
    
        
    protected JPAPlanetImpl() throws PlanetException {
        
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
    
    protected JPAPersistenceStrategy getStrategy() throws PlanetException {
        
        // Add OpenJPA, Toplink and Hibernate properties to Roller config.
        Properties props = new Properties();
        Enumeration keys = PlanetConfig.keys();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("openjpa.") || key.startsWith("toplink.")) {
                String value = PlanetConfig.getProperty(key);
                log.info(key + ": " + value);
                props.setProperty(key, value);
            }
        }
        
        DatabaseProvider dbProvider = DatabaseProvider.getDatabaseProvider();
        if (dbProvider.getType() == DatabaseProvider.ConfigurationType.JNDI_NAME) {
            return new JPAPersistenceStrategy(
                "PlanetPU", "java:comp/env/" + dbProvider.getJndiName(), props); 
        } else {
            return new JPAPersistenceStrategy(
                "PlanetPU",  
                dbProvider.getJdbcDriverClass(),
                dbProvider.getJdbcConnectionURL(),
                dbProvider.getJdbcUsername(),
                dbProvider.getJdbcPassword(), 
                props);
        }
    }
    
    /**
     * Instantiates and returns an instance of JPAPlanetImpl.
     */
    public static Planet instantiate() throws PlanetException {
        if (me == null) {
            log.debug("Instantiating JPAPlanetImpl");
            me = new JPAPlanetImpl();
        }
        
        return me;
    }    

    public URLStrategy getURLStrategy() {
        return this.urlStrategy;
    }
    
    public void setURLStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
        log.info("Using URLStrategy: " + urlStrategy.getClass().getName());
    }
    
        public void flush() throws PlanetException {
        this.strategy.flush();
    }

    
    public void release() {

        // release our own stuff first
        //if (planetManager != null) planetManager.release();

        // tell Datamapper to close down
        this.strategy.release();
    }

    
    public void shutdown() {

        // do our own shutdown first
        this.release();
    }
    
    /**
     * @see org.apache.roller.business.Roller#getBookmarkManager()
     */
    public PlanetManager getPlanetManager() {
        if ( planetManager == null ) {
            planetManager = createPlanetManager(strategy);
        }
        return planetManager;
    }

    protected PlanetManager createPlanetManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPlanetManagerImpl(strategy);
    }    
    
    /**
     * @see org.apache.roller.business.Roller#getBookmarkManager()
     */
    public PropertiesManager getPropertiesManager() {
        if ( propertiesManager == null ) {
            propertiesManager = createPropertiesManager(strategy);
        }
        return propertiesManager;
    }

    protected PropertiesManager createPropertiesManager(
            JPAPersistenceStrategy strategy) {
        return new JPAPropertiesManagerImpl(strategy);
    } 
    
    public FeedFetcher getFeedFetcher() {
        return this.feedFetcher;
    }
    
    public void setFeedFetcher(FeedFetcher feedFetcher) {
        this.feedFetcher = feedFetcher;
        log.info("Using FeedFetcher: " + feedFetcher.getClass().getName());
    }
    

}
