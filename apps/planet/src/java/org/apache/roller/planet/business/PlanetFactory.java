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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.startup.PlanetStartup;
import org.apache.roller.planet.config.PlanetConfig; 


/**
 * Provides access to the Planet instance.
 */
public abstract class PlanetFactory {
    
    private static Log log = LogFactory.getLog(PlanetFactory.class);
    
    // our configured planet provider
    private static PlanetProvider planetProvider = null;
    
    
    // non-instantiable
    private PlanetFactory() {
        // hello planetary citizens
    }
    
    
    
    /**
     * Static accessor for the instance of Roller
     */
    public static Planet getPlanet() {
        if (planetProvider == null) {
            throw new IllegalStateException("Roller Planet has not been bootstrapped yet");
        }
        
        return planetProvider.getPlanet();
    }
    
    
    /**
     * True if bootstrap process was completed, False otherwise.
     */
    public static boolean isBootstrapped() {
        return (planetProvider != null);
    }
    
    
    /**
     * Bootstrap the Roller Planet business tier, uses default PlanetProvider.
     *
     * Bootstrapping the application effectively instantiates all the necessary
     * pieces of the business tier and wires them together so that the app is 
     * ready to run.
     *
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws BootstrapException If an error happens during the bootstrap process.
     */
    public static final void bootstrap() throws BootstrapException {
        
        // if the app hasn't been properly started so far then bail
        if (!PlanetStartup.isPrepared()) {
            throw new IllegalStateException("Cannot bootstrap until application has been properly prepared");
        }
        
        // default provider is Guice, so lookup our module from PlanetConfig
        String guiceModule = PlanetConfig.getProperty("guice.backend.module");
        
        // instantiate guice provider using the configured module
        PlanetProvider guiceProvider = new GuicePlanetProvider(guiceModule);
        
        // finish things off by calling bootstrap() with our guice provider
        bootstrap(guiceProvider);
    }
    
    
    /**
     * Bootstrap the Roller Planet business tier, uses specified PlanetProvider.
     *
     * Bootstrapping the application effectively instantiates all the necessary
     * pieces of the business tier and wires them together so that the app is 
     * ready to run.
     *
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws BootstrapException If an error happens during the bootstrap process.
     */
    public static final void bootstrap(PlanetProvider provider) 
            throws BootstrapException {
        
        // if the app hasn't been properly started so far then bail
        if (!PlanetStartup.isPrepared()) {
            throw new IllegalStateException("Cannot bootstrap until application has been properly prepared");
        }
        
        if (provider == null) {
            throw new NullPointerException("PlanetProvider is null");
        }
        
        log.info("Bootstrapping Roller Planet business tier");
        
        log.info("Planet Provider = "+provider.getClass().getName());
        
        // save reference to provider
        planetProvider = provider;
        
        // bootstrap planet provider
        planetProvider.bootstrap();
        
        // make sure we are all set
        if(planetProvider.getPlanet() == null) {
            throw new BootstrapException("Bootstrapping failed, Planet instance is null");
        }
        
        log.info("Roller Planet business tier successfully bootstrapped");
    }
    
}
