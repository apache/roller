/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.planet.business;

import org.apache.roller.RollerException;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.weblogger.business.PropertiesManager;


/**
 * The main entry point interface of the Roller business tier.
 */
public interface Planet {
    
    /**
     * Get PlanetManager associated with this Roller instance.
     */
     PlanetManager getWebloggerManager();
    
    
    /**
     * Get PropertiesManager.
     */
    PropertiesManager getPropertiesManager();
    
    
    /**
     * Get the configured URLStrategy.
     */
    PlanetURLStrategy getURLStrategy();
    
    
    /**
     * Get the configured FeedFetcher.
     */
    FeedFetcher getFeedFetcher();
    
        
    /**
     * Flush object states.
     */
    void flush() throws RollerException;
    
    
    /**
     * Initialize any resources necessary for this instance of Roller.
     */
    void initialize() throws Exception;
    

    /**
     * Release any resources associated with a session.
     */
    void release();
    
    
    /**
     * Shutdown the application.
     */
    void shutdown();
    
}

