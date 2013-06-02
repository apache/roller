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
    public PlanetManager getWebloggerManager();
    
    
    /**
     * Get PropertiesManager.
     */
    public PropertiesManager getPropertiesManager();
    
    
    /**
     * Get the configured URLStrategy.
     */
    public PlanetURLStrategy getURLStrategy();
    
    
    /**
     * Get the configured FeedFetcher.
     */
    public FeedFetcher getFeedFetcher();
    
        
    /**
     * Flush object states.
     */
    public void flush() throws RollerException;
    
    
    /**
     * Initialize any resources necessary for this instance of Roller.
     */
    public void initialize() throws Exception;
    

    /**
     * Release any resources associated with a session.
     */
    public void release();
    
    
    /**
     * Shutdown the application.
     */
    public void shutdown();
    
}

