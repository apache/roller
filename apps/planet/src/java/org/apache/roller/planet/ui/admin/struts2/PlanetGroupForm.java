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
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.ui.core.struts2.PlanetActionSupport;


/**
 * Planet Group Form Action.
 *
 * Handles adding/modifying groups for a planet.
 *
 * TODO: validation and security.
 */
public class PlanetGroupForm extends PlanetActionSupport implements Preparable {
    
    private static Log log = LogFactory.getLog(PlanetGroupForm.class);
    
    // the PlanetGroup to work on
    private PlanetGroup group = null;
    
    // form fields
    private String planetid = null;
    private String groupid = null;
    private String subid = null;
    
    
    /**
     * Load relevant Planet if possible.
     */
    public void prepare() throws Exception {
        PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
        if(getGroupid() != null && !"".equals(getGroupid())) {
            // load a planet group
            log.debug("Loading Planet Group ...");
            
            group = pMgr.getGroupById(getGroupid());
        } else {
            // new group, must have a planet to add it to
            Planet planet = pMgr.getPlanetById(getPlanetid());
            if(planet != null) {
                group = new PlanetGroup();
                group.setPlanet(planet);
            } else {
                throw new PlanetException("could not determine planet "+getPlanetid());
            }
        }
    }
    
    public String execute() {
        return INPUT;
    }
    
    
    // TODO: Validation - check that group handle is unique within planet
    // TODO: Validation - make sure that html is not allowed in handle or title
    // TODO: Validation - make sure maxXXXEntries have a proper value range
    public String save() {
        // save a group group
        log.debug("Saving Planet Group ...");
        
        try {
            PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
            pMgr.saveGroup(this.group);
            PlanetFactory.getPlanet().flush();
            
            // call setGroupid() just in case this was a new group with no id yet
            setGroupid(this.group.getId());
        } catch (PlanetException ex) {
            log.error("Error saving planet group", ex);
            setError("PlanetGroupForm.error.saveFailed");
            return INPUT;
        }
        
        setSuccess("PlanetGroupForm.message.saveSucceeded");
        return INPUT;
    }

    public String deleteSub() {
        // delete a planet subscription
        log.debug("Deleting Planet Subscription ...");
        
        PlanetManager pmgr= PlanetFactory.getPlanet().getPlanetManager();
        try {
            if(getSubid() != null && !"".equals(getSubid())) {
                Subscription sub = pmgr.getSubscriptionById(getSubid());
                if(sub == null) {
                    setError("PlanetGroupForm.error.nullSubscription");
                    return INPUT;
                } else {
                    PlanetGroup group = getGroup();
                    group.getSubscriptions().remove(sub);
                    sub.getGroups().remove(group);
                    pmgr.saveGroup(group);
                    PlanetFactory.getPlanet().flush();
                }
                
                setSuccess("PlanetGroupForm.message.subscriptionDeleteSucceeded", sub.getTitle());
            }
            
            setError("PlanetGroupForm.error.subscriptionNull");
            return INPUT;
            
        } catch (PlanetException ex) {
            log.error("Unable to lookup planet group", ex);
            setError("PlanetGroupForm.error.subscriptionDeleteFailed", getSubid());
            return INPUT;
        }
    }

    public String getPlanetid() {
        return planetid;
    }

    public void setPlanetid(String planetid) {
        this.planetid = planetid;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getSubid() {
        return subid;
    }

    public void setSubid(String subid) {
        this.subid = subid;
    }
    
    public PlanetGroup getGroup() {
        return group;
    }

    public void setGroup(PlanetGroup group) {
        this.group = group;
    }
    
}
