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
package org.apache.roller.planet.model;


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
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(PlanetFactory.class);
    
    /**
     * Let's just be doubling certain this class cannot be instantiated.
     * @see java.lang.Object#Object()
     */
    private PlanetFactory() {
        // hello planetary citizens
    }
    
    /**
     * Static accessor for the instance of Planet
     * held by this class.
     *
     * @return Planet
     */
    public static Planet getPlanet() {
        // check to see if we need to instantiate
        if(planetInstance == null) {
            // lookup value for the planet classname to use
            String planet_classname =
                    PlanetConfig.getProperty("persistence.planet.classname");
            
            // now call setPlanet ourselves
            PlanetFactory.setPlanet(planet_classname);
        }
        
        return planetInstance;
    }
    
    
    /**
     * Construct the actual Planet implemenation for this instance.
     *
     * Use reflection to call the implementation's instantiate() method.
     * Should this fail (either no implementation is specified or
     * instantiate() throws an Exception) then the DEFAULT_IMPL will be tried.
     * If this should fail then we are in trouble :/
     *
     * @param planet_classname The name of the Planet implementation class
     * to instantiate.
     */
    public static void setPlanet(String planet_classname) {
        
        if (StringUtils.isEmpty( planet_classname ))
            planet_classname = DEFAULT_IMPL;
        
        try {
            Class planetClass = Class.forName(planet_classname);
            java.lang.reflect.Method instanceMethod =
                    planetClass.getMethod("instantiate", (Class[])null);
            
            // do the invocation
            planetInstance = (Planet)
                instanceMethod.invoke(planetClass, (Object[])null);
            
            mLogger.info("Using Planet Impl: " + planet_classname);
        } catch (Exception e) {
            // uh oh
            mLogger.error("Error instantiating " + planet_classname, e);
            try {
                // if we didn't already try DEFAULT_IMPL then try it now
                if( ! DEFAULT_IMPL.equals(planet_classname)) {
                    mLogger.info("** Trying DEFAULT_IMPL "+DEFAULT_IMPL+" **");
                    
                    Class planetClass = Class.forName(DEFAULT_IMPL);
                    java.lang.reflect.Method instanceMethod =
                            planetClass.getMethod("instantiate", (Class[])null);
                    
                    // do the invocation
                    planetInstance = (Planet)
                        instanceMethod.invoke(planetClass, (Object[])null);
                } else {
                    // we just do this so that the logger gets the message
                    throw new Exception("Doh! Couldn't instantiate the planet class");
                }
                
            } catch (Exception re) {
                mLogger.fatal("Failed to instantiate fallback planet impl", re);
            }
        }
        
    }
    
    
    /**
     * Set Planet to be returned by factory.
     */
    public static void setPlanet(Planet planet) {
        if (planet != null) planetInstance = planet;
    }
}
