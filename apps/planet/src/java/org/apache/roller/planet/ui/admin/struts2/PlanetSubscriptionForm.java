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
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.planet.ui.core.struts2.PlanetActionSupport;


/**
 * Planet Sub Form Action.
 * 
 * Handles adding/modifying subscriptions for a group.
 *
 * TODO: validation and security.
 */
public class PlanetSubscriptionForm extends PlanetActionSupport implements Preparable {
    
    private static Log log = LogFactory.getLog(PlanetSubscriptionForm.class);
    
    // the PlanetSubscriptionData to work on
    private PlanetSubscriptionData subscription = null;
    
    // form fields
    private String groupid = null;
    private String subid = null;
    
    
    /**
     * Load relevant PlanetSubscriptionData if possible.
     */
    public void prepare() throws Exception {
        PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
        if(getSubid() != null && !"".equals(getSubid())) {
            // load a planet subscription
            log.debug("Loading Planet Subscription ...");
            
            subscription = pMgr.getSubscriptionById(getSubid());
        } else {
            subscription = new PlanetSubscriptionData();
        }
    }
    
    public String execute() {
        return INPUT;
    }
    
    
    // TODO: Validation - make sure that html is not allowed in title
    public String save() {
        // save a subscription
        log.debug("Saving Planet Subscription ...");
        
        PlanetManager pMgr = PlanetFactory.getPlanet().getPlanetManager();
        try {
            if(this.subscription.getId() == null) {
                // adding a new sub to a group, so make sure we have a group
                PlanetGroupData group = pMgr.getGroupById(getGroupid());
                if(group == null) {
                    setError("PlanetSubscriptionForm.error.groupNull");
                    return INPUT;
                }
                
                // check if this subscription already exists before adding it
                PlanetSubscriptionData sub = pMgr.getSubscription(this.subscription.getFeedURL());
                if(sub != null) {
                    this.subscription = sub;
                } else {
                    pMgr.saveSubscription(this.subscription);
                    
                    // set subid now that we have one
                    setSubid(this.subscription.getId());
                }
                
                // add the sub to the group
                group.getSubscriptions().add(this.subscription);
                this.subscription.getGroups().add(group);
                pMgr.saveGroup(group);
                
            } else {
                // updating and existing subscription, so just save it
                pMgr.saveSubscription(this.subscription);
            }
            
            // flush changes
            PlanetFactory.getPlanet().flush();
        } catch (RollerException ex) {
            log.error("Error saving subscription", ex);
            setError("PlanetSubscriptionForm.error.saveFailed");
            return INPUT;
        }
        
        setSuccess("PlanetSubscriptionForm.message.saveSucceeded");
        return INPUT;
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

    public PlanetSubscriptionData getSubscription() {
        return subscription;
    }

    public void setSubscription(PlanetSubscriptionData subscription) {
        this.subscription = subscription;
    }
    
}
