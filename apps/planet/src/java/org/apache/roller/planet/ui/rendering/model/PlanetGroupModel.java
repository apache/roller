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
import org.apache.roller.RollerException;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.ui.rendering.util.PlanetGroupRequest;
import org.apache.roller.planet.ui.rendering.util.PlanetRequest;


/**
 * Model which provides information needed to render a weblog page.
 */
public class PlanetGroupModel implements Model {
    
    private static Log log = LogFactory.getLog(PlanetGroupModel.class);
    
    private PlanetGroupRequest planetGroupRequest = null;
    private Map requestParameters = null;
    private PlanetData planet = null;
    private PlanetGroupData group = null;
    
    
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
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a weblogRequest object
        PlanetRequest planetRequest = (PlanetRequest) initData.get("planetRequest");
        if(planetRequest == null) {
            throw new RollerException("expected planetRequest from init data");
        }
        
        // PageModel only works on page requests, so cast planetRequest
        // into a PlanetRequest and if it fails then throw exception
        if(planetRequest instanceof PlanetGroupRequest) {
            this.planetGroupRequest = (PlanetGroupRequest) planetRequest;
        } else {
            throw new RollerException("weblogRequest is not a WeblogPageRequest."+
                    "  PageModel only supports page requests.");
        }
        
        // custom request parameters
        this.requestParameters = (Map)initData.get("requestParameters");
        
        // extract planet object
        planet = planetGroupRequest.getPlanet();
        
        // extract group object
        group = planetGroupRequest.getGroup();
    }
    
    
    /**
     * Get planet being displayed.
     */
    public PlanetData getPlanet() {
        return planet;
    }
    
    
    /**
     * Get group being displayed.
     */
    public PlanetGroupData getGroup() {
        return group;
    }
    
    
    /**
     * Get request parameter by name.
     */
    public String getRequestParameter(String paramName) {
        String[] values = (String[])requestParameters.get(paramName);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }
    
}
