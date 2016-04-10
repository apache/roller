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
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
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
    private Planet bean = new Planet();
    
    // the planet we are working on
    private Planet planet = null;

    private PlanetManager planetManager;

    // full list of planets
    private List<Planet> planets = new ArrayList<>();

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

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
    public void prepare() {
        try {
            for (Planet planet : planetManager.getPlanets()) {
                // The "all" planet is considered a special planet and cannot be
                // managed independently
                if (!planet.getHandle().equals("all")) {
                    planets.add(planet);
                }
            }
        } catch(Exception ex) {
            log.error("Error getting planets - " + getBean().getId(), ex);
        }

        if(getBean().getId() != null) {
            try {
                setPlanet(planetManager.getPlanetById(getBean().getId()));
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
            bean.setId(getPlanet().getId());
            bean.setTitle(getPlanet().getTitle());
            bean.setHandle(getPlanet().getHandle());
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
                    aPlanet.setId(WebloggerCommon.generateUUID());
                } else {
                    log.debug("Updating Existing Planet");
                }

                // copy in submitted data
                aPlanet.setTitle(bean.getTitle().trim());
                aPlanet.setHandle(bean.getHandle().trim());

                // save and flush
                planetManager.savePlanet(aPlanet);
                planets.add(aPlanet);
                WebloggerFactory.flush();

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
                planetManager.deletePlanet(getPlanet());
                planets.remove(getPlanet());
                WebloggerFactory.flush();
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
        return planets;
    }

    public Planet getBean() {
        return bean;
    }

    public void setBean(Planet bean) {
        this.bean = bean;
    }
    
    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }
}