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

package org.apache.roller.weblogger.planet.business.jpa;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.business.URLStrategy;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.planet.business.jpa.JPAPlanetImpl;
import org.apache.roller.planet.business.jpa.JPAPlanetManagerImpl;
import org.apache.roller.planet.business.jpa.JPAPropertiesManagerImpl;
import org.apache.roller.weblogger.planet.business.WebloggerRomeFeedFetcher;
import org.apache.roller.weblogger.planet.ui.PlanetURLStrategy;


/**
 * Guice module for configuring Roller's built-in Planet, JPA version.
 */
public class RollerPlanetModule implements Module {

    public void configure(Binder binder) {
        
        binder.bind(Planet.class).to(JPAPlanetImpl.class);

        // Use special Planet persistence strategy that works against RollerConfig
        binder.bind(JPAPersistenceStrategy.class).to(JPARollerPlanetPersistenceStrategy.class); 
        
        binder.bind(PlanetManager.class).to(     JPAPlanetManagerImpl.class);   
        binder.bind(PropertiesManager.class).to( JPAPropertiesManagerImpl.class);    
        binder.bind(URLStrategy.class).to(       PlanetURLStrategy.class);
        binder.bind(FeedFetcher.class).to(       WebloggerRomeFeedFetcher.class);
    }
    
}
