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
import org.apache.roller.planet.config.PlanetConfig; 
import org.apache.commons.lang.StringUtils;


/**
 * Instantiates planet implementation.
 */
public abstract class PlanetFactory {
    
    private static final String DEFAULT_IMPL =
        "org.apache.roller.planet.business.hibernate.HibernatePlanetImpl";
    
    private static Planet planetInstance = null;
    
    private static Log log = LogFactory.getLog(PlanetFactory.class);
    
    
    /**
     * We instantiate the Planet implementation statically at class loading time
     * to make absolutely sure that there is no way for our singleton to get
     * instantiated twice.
     */
    static {
        // lookup value for the roller classname to use
        String planet_classname =
                PlanetConfig.getProperty("persistence.planet.classname");
        if(planet_classname == null || planet_classname.trim().length() < 1)
            planet_classname = DEFAULT_IMPL;
        
        try {
            Class planetClass = Class.forName(planet_classname);
            java.lang.reflect.Method instanceMethod =
                    planetClass.getMethod("instantiate", (Class[])null);
            
            // do the invocation
            planetInstance = (Planet) instanceMethod.invoke(planetClass, (Object[])null);
            
            log.info("Using Planet Impl: " + planet_classname);
            
        } catch (Throwable e) {
            
            // uh oh
            log.error("Error instantiating " + planet_classname, e);
            
            try {
                // if we didn't already try DEFAULT_IMPL then try it now
                if( ! DEFAULT_IMPL.equals(planet_classname)) {
                    
                    log.info("** Trying DEFAULT_IMPL "+DEFAULT_IMPL+" **");
                    
                    Class rollerClass = Class.forName(DEFAULT_IMPL);
                    java.lang.reflect.Method instanceMethod =
                            rollerClass.getMethod("instantiate", (Class[])null);
                    
                    // do the invocation
                    planetInstance = (Planet) instanceMethod.invoke(rollerClass, (Object[])null);
                } else {
                    // we just do this so that the logger gets the message
                    throw new Exception("Doh! Couldn't instantiate a planet class");
                }
                
            } catch (Exception re) {
                log.fatal("Failed to instantiate fallback planet impl", re);
            }
        }
    }
    
    
    /**
     * Let's just be doubling certain this class cannot be instantiated.
     * @see java.lang.Object#Object()
     */
    private PlanetFactory() {
        // hello planetary citizens
    }
    
    
    public static Planet getPlanet() {
        return planetInstance;
    }
    
}
