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

package org.apache.roller.planet.ui.admin.struts2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.ui.core.struts2.PlanetActionSupport;


/**
 * Planet List Action.
 *
 * Displays the list of planets in the system.
 *
 * TODO: validation and security.
 */
public class PlanetsList extends PlanetActionSupport {
    
    private static Log log = LogFactory.getLog(PlanetsList.class);
    
    private static final String LIST = "list";
    
    // action properties
    private String planetid = null;
    private Collection planets = Collections.EMPTY_LIST;
    
    
    public String execute() {
        try {
            PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
            List planets = pMgr.getPlanets();
            //Collections.sort(planets);
            setPlanets(planets);
        } catch(Exception e) {
            log.error("PlanetsList.error.general", e);
            return LIST;
        }
        
        return LIST;
    }
    
    
    /**
     * Delete a planet.
     */
    public String deletePlanet() {
        
        if(getPlanetid() != null && getPlanetid().length() > 0) {
            // delete a planet
            log.debug("Deleting Planet ... "+getPlanetid());
            
            try {
                PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
                PlanetData planet = pMgr.getPlanetById(getPlanetid());
                if(planet != null) {
                    pMgr.deletePlanet(planet);
                    PlanetFactory.getPlanet().flush();
                }
                
                // delete succeeded, handle rest of request as usual
                setSuccess("PlanetsList.message.planetDeleteSucceeded", planet.getHandle());
                return execute();
            } catch(Exception e) {
                log.error("Error deleting planet", e);
                setError("PlanetsList.error.planetDeleteFailed", getPlanetid());
                return LIST;
            }
            
        } else {
            setError("PlanetsList.error.planetNull");
            return execute();
        }
        
    }
    
    
    public Collection getPlanets() {
        return this.planets;
    }
    
    private void setPlanets(Collection planets) {
        this.planets = planets;
    }

    public String getPlanetid() {
        return planetid;
    }

    public void setPlanetid(String planetid) {
        this.planetid = planetid;
    }
    
}
