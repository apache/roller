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
 */

package org.apache.roller.weblogger.planet.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroup;


/**
 * Manage planet groups.
 */
public class PlanetGroups extends PlanetUIAction {
    
    private static Log log = LogFactory.getLog(PlanetGroups.class);
    
    // a bean to manage submitted data
    private PlanetGroupsBean bean = new PlanetGroupsBean();
    
    // the planet group we are working on
    private PlanetGroup group = null;
    
    
    public PlanetGroups() {
        this.actionName = "planetGroups";
        this.desiredMenu = "admin";
        this.pageTitle = "planetGroups.pagetitle";
    }
    
    
    @Override
    public String requiredUserRole() {
        return "admin";
    }
    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    @Override
    public void myPrepare() {
        
        if(getPlanet() != null && getBean().getId() != null) try {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            setGroup(pmgr.getGroupById(getBean().getId()));
        } catch(Exception ex) {
            log.error("Error looking up planet group - "+getBean().getId(), ex);
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
        
        if (!hasActionErrors()) try {
            
            PlanetGroup group = getGroup();
            if(group == null) {
                log.debug("Adding New Group");
                group = new PlanetGroup();
                group.setPlanet(getPlanet());
            } else {
                log.debug("Updating Existing Group");
            }
            
            // copy in submitted data
            getBean().copyTo(group);
            
            // save and flush
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            pmgr.saveGroup(group);
            PlanetFactory.getPlanet().flush();
            
            addMessage("planetGroups.success.saved");
            
        } catch(Exception ex) {
            log.error("Error saving planet group - "+getBean().getId(), ex);
            // TODO: i18n
            addError("Error saving planet group");
        }
        
        return LIST;
    }

    
    /** 
     * Delete group, reset form  
     */
    public String delete() {
        
        if(getGroup() != null) {
            try {
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                pmgr.deleteGroup(getGroup());
                PlanetFactory.getPlanet().flush();
                
                addMessage("planetSubscription.success.deleted");
            } catch(Exception ex) {
                log.error("Error deleting planet group - "+getBean().getId());
                // TODO: i18n
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
    
    
    public List<PlanetGroup> getGroups() {
        
        List<PlanetGroup> displayGroups = new ArrayList();
        
        Iterator allgroups = getPlanet().getGroups().iterator();
        while (allgroups.hasNext()) {
            PlanetGroup agroup = (PlanetGroup) allgroups.next();
            
            // The "all" group is considered a special group and cannot be
            // managed independently
            if (!agroup.getHandle().equals("all")) {
                displayGroups.add(agroup);
            }
        }
        
        return displayGroups;
    }
    
    
    public PlanetGroupsBean getBean() {
        return bean;
    }

    public void setBean(PlanetGroupsBean bean) {
        this.bean = bean;
    }
    
    public PlanetGroup getGroup() {
        return group;
    }

    public void setGroup(PlanetGroup group) {
        this.group = group;
    }
    
}
