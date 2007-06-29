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

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.ui.rendering.util.PlanetGroupRequest;
import org.apache.roller.planet.ui.rendering.util.PlanetRequest;


/**
 * Model which provides information common to a planet group request
 */
public class PlanetGroupModel implements Model {
    
    private static Log log = LogFactory.getLog(PlanetGroupModel.class);
    
    private PlanetGroupRequest planetGroupRequest = null;
    private Planet planet = null;
    private PlanetGroup group = null;
    
    
    /** 
     * Creates an un-initialized new instance, Roller calls init() to complete
     * construction. 
     */
    public PlanetGroupModel() {}
    
    
    /** 
     * Template context name to be used for model.
     */
    public String getModelName() {
        return "model";
    }
    
    
    /** 
     * Init page model based on request. 
     */
    public void init(Map initData) throws PlanetException {
        
        // we expect the init data to contain a planetRequest object
        PlanetRequest planetRequest = (PlanetRequest) initData.get("planetRequest");
        if(planetRequest == null) {
            throw new PlanetException("expected planetRequest from init data");
        }
        
        // only works on planet group requests, so cast planetRequest
        // into a PlanetGroupRequest and if it fails then throw exception
        if(planetRequest instanceof PlanetGroupRequest) {
            this.planetGroupRequest = (PlanetGroupRequest) planetRequest;
        } else {
            throw new PlanetException("planetRequest is not a PlanetGroupRequest."+
                    "  PlanetGroupModel only supports planet group requests.");
        }
        
        // extract planet object
        planet = planetGroupRequest.getPlanet();
        
        // extract group object
        group = planetGroupRequest.getGroup();
    }
    
    
    /**
     * Get planet being displayed.
     */
    public Planet getPlanet() {
        return planet;
    }
    
    
    /**
     * Get group being displayed.
     */
    public PlanetGroup getGroup() {
        return group;
    }
    
}
