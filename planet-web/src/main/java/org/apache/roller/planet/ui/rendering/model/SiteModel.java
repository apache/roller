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

package org.apache.roller.planet.ui.rendering.model; 

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.config.PlanetRuntimeConfig;


/**
 * Model which provides information on an application wide scope.
 */
public class SiteModel implements Model {
    
    private static Log log = LogFactory.getLog(SiteModel.class);
    
    
    /** 
     * Creates an un-initialized new instance.
     */
    public SiteModel() {}
    
    
    /** 
     * Template context name to be used for model.
     */
    public String getModelName() {
        return "site";
    }
    
    
    /** 
     * Init page model based on request. 
     */
    public void init(Map initData) throws PlanetException {
        // no-op
    }
    
    
    public String getTitle() {
        return PlanetRuntimeConfig.getProperty("site.name");
    }
    
    
    public String getDescription() {
        return PlanetRuntimeConfig.getProperty("site.description");
    }
    
    
    /**
     * Get the list of all planets.
     */
    public List getPlanets() {
        PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
        try {
            return pMgr.getPlanets();
        } catch(Exception e) {
            log.error("Error getting planets list", e);
        }
        
        return null;
    }
    
}
