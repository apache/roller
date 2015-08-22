/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Manage planets.
 */
public class Planets extends UIAction {
    
    private static Log log = LogFactory.getLog(Planets.class);
    
    // a bean to manage submitted data
    private PlanetBean bean = new PlanetBean();
    
    // the planet we are working on
    private Planet planet = null;
    
    
    public Planets() {
        this.actionName = "planets";
        this.desiredMenu = "admin";
        this.pageTitle = "planets.pagetitle";
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    @Override
    public void myPrepare() {
        
        if(getBean().getId() != null) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                setPlanet(pmgr.getPlanetById(getBean().getId()));
            } catch(Exception ex) {
                log.error("Error looking up planet - " + getBean().getId(), ex);
            }
        }
    }

    
    /** 
     * Show planets page.
     */
    public String execute() {
        
        // if we are loading an existing planet then populate the bean
        if(getPlanet() != null) {
            getBean().copyFrom(getPlanet());
        }
        
        return LIST;
    }
    
    
    public String save() {
        
        myValidate();
        
        if (!hasActionErrors()) {
            try {
                Planet aPlanet = getPlanet();
                if(aPlanet == null) {
                    log.debug("Adding New Planet");
                    aPlanet = new Planet();
                } else {
                    log.debug("Updating Existing Planet");
                }

                // copy in submitted data
                getBean().copyTo(aPlanet);

                // save and flush
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                pmgr.savePlanet(aPlanet);
                WebloggerFactory.getWeblogger().flush();

                addMessage("planets.success.saved");
            } catch(Exception ex) {
                log.error("Error saving planet - " + getBean().getId(), ex);
                addError("planets.error.saved");
            }
        }
        
        return LIST;
    }

    
    /** 
     * Delete planet, reset form
     */
    public String delete() {
        
        if(getPlanet() != null) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                pmgr.deletePlanet(getPlanet());
                WebloggerFactory.getWeblogger().flush();
                addMessage("planets.success.deleted");
            } catch(Exception ex) {
                log.error("Error deleting planet - "+getBean().getId());
                addError("Error deleting planet");
            }
        }
        
        return LIST;
    }
    
    
    /** 
     * Validate posted planet
     */
    private void myValidate() {
        
        if(StringUtils.isEmpty(getBean().getTitle())) {
            addError("planets.error.title");
        }
        
        if(StringUtils.isEmpty(getBean().getHandle())) {
            addError("planets.error.handle");
        }
        
        if(getBean().getHandle() != null && "all".equals(getBean().getHandle())) {
            addError("planets.error.nameReserved");
        }
        
        // make sure duplicate planet handles are prevented
    }
    
    
    public List<Planet> getPlanets() {
        List<Planet> displayPlanets = new ArrayList<Planet>();

        PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();

        try {
            for (Planet planet : pmgr.getPlanets()) {
                // The "all" planet is considered a special planet and cannot be
                // managed independently
                if (!planet.getHandle().equals("all")) {
                    displayPlanets.add(planet);
                }
            }
        } catch(Exception ex) {
            log.error("Error getting planets - " + getBean().getId());
            addError("Error getting planets");
        }
        return displayPlanets;
    }
    
    
    public PlanetBean getBean() {
        return bean;
    }

    public void setBean(PlanetBean bean) {
        this.bean = bean;
    }
    
    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }
}
