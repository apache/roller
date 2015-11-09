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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FeedProcessor;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Manage planet subscriptions
 */
public class PlanetSubscriptions extends UIAction {
    
    private static final Log LOGGER = LogFactory.getLog(PlanetSubscriptions.class);

    private PlanetManager planetManager;

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    private FeedProcessor feedProcessor;

    public void setFeedProcessor(FeedProcessor feedProcessor) {
        this.feedProcessor = feedProcessor;
    }

    // planet handle we are working in
    private String planetHandle = null;
    
    // the planet we are working in
    private Planet planet = null;
    
    // the subscription to deal with
    private String subUrl = null;
    
    public PlanetSubscriptions() {
        this.actionName = "planetSubscriptions";
        this.desiredMenu = "admin";
        this.pageTitle = "planetSubscriptions.title";
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    @Override
    public void myPrepare() {
        try {
            setPlanet(planetManager.getPlanet(getPlanetHandle()));
        } catch (WebloggerException ex) {
            LOGGER.error("Error looking up planet group - " + getPlanetHandle(), ex);
        }
    }
    
    
    /**
     * Populate page model and forward to subscription page
     */
    public String execute() {
        return LIST;
    }

    
    /** 
     * Save subscription, add to current group 
     */
    public String save() {
        
        myValidate();
        
        if(!hasActionErrors()) {
            try {
                // check if this subscription already exists before adding it
                Subscription sub = planetManager.getSubscription(planet, getSubUrl());

                if (sub == null) {
                    LOGGER.debug("Adding New Subscription - " + getSubUrl());

                    // sub doesn't exist yet, so we need to fetch it
                    sub = feedProcessor.fetchSubscription(getSubUrl());
                    sub.setPlanet(planet);

                    // save new sub
                    planetManager.saveSubscription(sub);

                    // add the sub to the group
                    planet.getSubscriptions().add(sub);
                    planetManager.savePlanet(planet);

                    // flush changes
                    WebloggerFactory.flush();

                    // clear field after success
                    setSubUrl(null);

                    addMessage("planetSubscription.success.saved");
                } else {
                    addError("planetSubscriptions.subscriptionAlreadyExists");
                    return LIST;
                }
            } catch (WebloggerException ex) {
                LOGGER.error("Unexpected error saving subscription", ex);
                addError("planetSubscriptions.error.duringSave", ex.getErrorMessageChain());
            }
        }
        
        return LIST;
    }

    
    /** 
     * Delete subscription, reset form  
     */
    public String delete() {
        
        if(getSubUrl() != null) {
            try {

                // remove subscription
                Subscription sub = planetManager.getSubscription(getPlanet(), getSubUrl());
                getPlanet().getSubscriptions().remove(sub);
                planetManager.deleteSubscription(sub);
                planetManager.savePlanet(getPlanet());
                WebloggerFactory.flush();

                // clear field after success
                setSubUrl(null);

                addMessage("planetSubscription.success.deleted");

            } catch (WebloggerException ex) {
                LOGGER.error("Error removing planet subscription", ex);
                addError("planetSubscription.error.deleting");
            }
        }

        return LIST;
    }
    
    
    /** 
     * Validate posted subscription
     */
    private void myValidate() {
        
        if(StringUtils.isEmpty(getSubUrl())) {
            addError("planetSubscription.error.feedUrl");
        }
    }
    
    
    public List<Subscription> getSubscriptions() {
        
        List<Subscription> subs = Collections.emptyList();
        if(getPlanet() != null) {
            Set<Subscription> subsSet = planet.getSubscriptions();
            
            // iterate over list and build display list
            subs = new ArrayList<>();
            for (Subscription sub : subsSet) {
                // only include external subs for display
                if(!sub.getFeedURL().startsWith("weblogger:")) {
                    subs.add(sub);
                }
            }
        }
        
        return subs;
    }
    
    
    public String getPlanetHandle() {
        return planetHandle;
    }

    public void setPlanetHandle(String planetHandle) {
        this.planetHandle = planetHandle;
    }
    
    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public String getSubUrl() {
        return subUrl;
    }

    public void setSubUrl(String subUrl) {
        this.subUrl = subUrl;
    }    
}
