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

package org.apache.roller.weblogger.planet.ui;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.weblogger.business.WebloggerFactory;

/**
 * Manage planet groups.
 */
public class PlanetGroups extends PlanetUIAction {
    
    private static Log log = LogFactory.getLog(PlanetGroups.class);
    
    // a bean to manage submitted data
    private PlanetGroupsBean bean = new PlanetGroupsBean();
    
    // the planet group we are working on
    private Planet group = null;
    
    
    public PlanetGroups() {
        this.actionName = "planetGroups";
        this.desiredMenu = "admin";
        this.pageTitle = "planetGroups.pagetitle";
    }
    
    @Override
    public void myPrepare() {
        
        if(getBean().getId() != null) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                setGroup(pmgr.getPlanetById(getBean().getId()));
            } catch(Exception ex) {
                log.error("Error looking up planet group - " + getBean().getId(), ex);
            }
        }
    }

    
    /** 
     * Show planet groups page.
     */
    public String execute() {
        
        // if we are loading an existing group then populate the bean
        if(getGroup() != null) {
            getBean().copyFrom(getGroup());
        }
        
        return LIST;
    }
    
    
    /** 
     * Save group.
     */
    public String save() {
        
        myValidate();
        
        if (!hasActionErrors()) {
            try {
                Planet planetGroup = getGroup();
                if(planetGroup == null) {
                    log.debug("Adding New Group");
                    planetGroup = new Planet();
                } else {
                    log.debug("Updating Existing Group");
                }

                // copy in submitted data
                getBean().copyTo(planetGroup);

                // save and flush
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                pmgr.savePlanet(planetGroup);
                WebloggerFactory.getWeblogger().flush();

                addMessage("planetGroups.success.saved");
            } catch(Exception ex) {
                log.error("Error saving planet group - " + getBean().getId(), ex);
                addError("planetGroups.error.saved");
            }
        }
        
        return LIST;
    }

    
    /** 
     * Delete group, reset form  
     */
    public String delete() {
        
        if(getGroup() != null) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                pmgr.deletePlanet(getGroup());
                WebloggerFactory.getWeblogger().flush();
                addMessage("planetGroups.success.deleted");
            } catch(Exception ex) {
                log.error("Error deleting planet group - "+getBean().getId());
                addError("Error deleting planet group");
            }
        }
        
        return LIST;
    }
    
    
    /** 
     * Validate posted group 
     */
    private void myValidate() {
        
        if(StringUtils.isEmpty(getBean().getTitle())) {
            addError("planetGroups.error.title");
        }
        
        if(StringUtils.isEmpty(getBean().getHandle())) {
            addError("planetGroups.error.handle");
        }
        
        if(getBean().getHandle() != null && "all".equals(getBean().getHandle())) {
            addError("planetGroups.error.nameReserved");
        }
        
        // make sure duplicate group handles are prevented
    }
    
    
    public List<Planet> getGroups() {
        List<Planet> displayGroups = new ArrayList<Planet>();

        PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();

        try {
            for (Planet planetGroup : pmgr.getPlanets()) {
                // The "all" group is considered a special group and cannot be
                // managed independently
                if (!planetGroup.getHandle().equals("all")) {
                    displayGroups.add(planetGroup);
                }
            }
        } catch(Exception ex) {
            log.error("Error getting planet groups - " + getBean().getId());
            addError("Error getting planet groups");
        }
        return displayGroups;
    }
    
    
    public PlanetGroupsBean getBean() {
        return bean;
    }

    public void setBean(PlanetGroupsBean bean) {
        this.bean = bean;
    }
    
    public Planet getGroup() {
        return group;
    }

    public void setGroup(Planet group) {
        this.group = group;
    }
}
