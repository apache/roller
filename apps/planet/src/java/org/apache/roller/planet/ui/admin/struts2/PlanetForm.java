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

import com.opensymphony.xwork2.Preparable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.ui.core.struts2.PlanetActionSupport;


/**
 * Planet Form Action.
 *
 * Handles adding/modifying Planets.
 *
 * TODO: validation and security.
 */
public class PlanetForm extends PlanetActionSupport implements Preparable {
    
    private static Log log = LogFactory.getLog(PlanetForm.class);
    
    // the objects to work on
    private PlanetData planet = null;
    private PlanetGroupData group = null;
    
    // form fields
    private String planetid = null;
    private String groupid = null;
    
    
    /**
     * Load relevant form data if possible.
     */
    public void prepare() throws Exception {
        
        PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // load existing planet
        if(getPlanetid() != null && !"".equals(getPlanetid())) {
            log.debug("Loading Planet ... "+getPlanetid());
            this.planet = pMgr.getPlanetById(getPlanetid());
        } else {
            // new planet
            log.debug("No planet specified, constructing new one");
            this.planet = new PlanetData();
        }
    }
    
    
    public String execute() {
        return INPUT;
    }
    
    
    // TODO: Validation - check that planet handle is unique
    // TODO: Validation - make sure html is not allowed in handle or title
    public String save() {
        
        if(this.planet != null) {
            // save planet
            log.debug("Saving Planet ...");
            
            try {
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                pmgr.savePlanet(this.planet);
                PlanetFactory.getPlanet().flush();
                
                // need to set planetid attribute
                setPlanetid(this.planet.getId());
            } catch (RollerException ex) {
                log.error("Error saving planet ", ex);
                setError("PlanetForm.error.saveFailed");
                return INPUT;
            }
            
            setSuccess("PlanetForm.message.saveSucceeded");
            return INPUT;
        } else {
            setError("PlanetForm.error.planetNull");
            return INPUT;
        }

    }
    
    
    public String deleteGroup() {
        
        if(getGroupid() != null && !"".equals(getGroupid())) {
            // delete a planet group
            log.debug("Deleting Planet Group ... "+getGroupid());
            
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            try {
                PlanetGroupData group = pmgr.getGroupById(getGroupid());
                this.planet = group.getPlanet();
                this.planet.getGroups().remove(group);
                pmgr.savePlanet(this.planet);
                pmgr.deleteGroup(group);
                PlanetFactory.getPlanet().flush();
                
                setSuccess("PlanetForm.message.groupDeleteSucceeded", group.getHandle());
                return INPUT;
            } catch (RollerException ex) {
                log.error("Error deleting planet group", ex);
                setError("PlanetForm.error.groupDeleteFailed", getGroupid());
                return INPUT;
            }
            
        } else {
            setError("PlanetForm.error.groupNull");
            return INPUT;
        }
    }

    public String getPlanetid() {
        return planetid;
    }

    public void setPlanetid(String id) {
        this.planetid = id;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }
    
    public PlanetData getPlanet() {
        return planet;
    }

    public void setPlanet(PlanetData planet) {
        this.planet = planet;
    }
    
}
