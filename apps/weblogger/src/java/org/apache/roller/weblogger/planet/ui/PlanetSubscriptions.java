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
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;


/**
 * Manage planet group subscriptions, default group is "all".
 */
public class PlanetSubscriptions extends PlanetUIAction {
    
    private static final Log log = LogFactory.getLog(PlanetSubscriptions.class);
    
    // id of the group we are working in
    private String groupHandle = null;
    
    // the planet group we are working in
    private PlanetGroupData group = null;
    
    // a bean for managing submitted data
    private PlanetSubscriptionsBean bean = new PlanetSubscriptionsBean();
    
    // the subscription we are working on
    private PlanetSubscriptionData subscription = null;
    
    
    public PlanetSubscriptions() {
        this.actionName = "planetSubscriptions";
        this.desiredMenu = "admin";
        this.pageTitle = "planetSubscriptions.title";
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
        
        PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // lookup group we are operating on, if none specified then use default
        if (getGroupHandle() == null) {
            setGroupHandle("all");
        }
        
        try {
            setGroup(pmgr.getGroup(getPlanet(), getGroupHandle()));
        } catch (RollerException ex) {
            log.error("Error looking up planet group - "+getGroupHandle(), ex);
        }
                
        // lookup subscription we are working with
        if(getBean().getId() != null) try {
            setSubscription(pmgr.getSubscriptionById(getBean().getId()));
        } catch(Exception ex) {
            log.error("Error looking up planet subscription - "+getBean().getId(), ex);
        }
    }
    
    
    /**
     * Populate page model and forward to subscription page
     */
    public String execute() {
        
        if(getSubscription() != null) {
            getBean().copyFrom(getSubscription());
        }
        
        return LIST;
    }

    
    /** 
     * Save subscription, add to current group 
     */
    public String save() {
        
        myValidate();
        
        if(!hasActionErrors()) try {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            PlanetSubscriptionData sub = getSubscription();
            if(sub == null) {
                // Adding new subscription to group
                // Does subscription to that feed already exist?
                sub = pmgr.getSubscription(getBean().getNewsfeedURL());
                if (sub != null) {
                    log.debug("Adding Existing Subscription");
                    
                    // Subscription already exists
                    addMessage("planetSubscription.foundExisting", sub.getTitle());
                } else {
                    log.debug("Adding New Subscription");
                    
                    // Add new subscription
                    sub = new PlanetSubscriptionData();
                    getBean().copyTo(sub);
                    pmgr.saveSubscription(sub);
                    
                    // now that we know our new subs id, keep track of that
                    getBean().setId(sub.getId());
                }
                
                // add the subscription to our group
                getGroup().getSubscriptions().add(sub);
                sub.getGroups().add(getGroup());
                pmgr.saveGroup(getGroup());
            } else {
                log.debug("Updating Subscription");
                
                // User editing an existing subscription within a group
                getBean().copyTo(sub);
                pmgr.saveSubscription(sub);
            }
            
            // flush changes
            PlanetFactory.getPlanet().flush();
            
            addMessage("planetSubscription.success.saved");
            
        } catch (RollerException ex) {
            log.error("Unexpected error saving subscription", ex);
            addError("planetSubscriptions.error.duringSave", ex.getRootCauseMessage());
        }
        
        return LIST;
    }

    
    /** 
     * Delete subscription, reset form  
     */
    public String delete() {
        
        if(getSubscription() != null) try {
            
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            getGroup().getSubscriptions().remove(getSubscription());
            pmgr.deleteSubscription(getSubscription());
            PlanetFactory.getPlanet().flush();
            
            addMessage("planetSubscription.success.deleted");
            
        } catch (RollerException ex) {
            log.error("Error removing planet subscription", ex);
            addError("planetSubscription.error.deleting");
        }

        return LIST;
    }
    
    
    /** 
     * Validate posted subscription, fill in blanks via Technorati 
     */
    private void myValidate() {
        
        if(StringUtils.isEmpty(getBean().getTitle())) {
            addError("planetSubscription.error.title");
        }
        
        if(StringUtils.isEmpty(getBean().getNewsfeedURL())) {
            addError("planetSubscription.error.feedUrl");
        }
        
        if(StringUtils.isEmpty(getBean().getWebsiteURL())) {
            addError("planetSubscription.error.siteUrl");
        }
    }
    
    
    public List<PlanetSubscriptionData> getSubscriptions() {
        
        List<PlanetSubscriptionData> subs = Collections.EMPTY_LIST;
        if(getGroup() != null) {
            Set<PlanetSubscriptionData> subsSet = getGroup().getSubscriptions();
            
            String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
            
            // iterate over list and build display list
            subs = new ArrayList();
            for( PlanetSubscriptionData sub : subsSet ) {
                // only include external subs for display
                if(!sub.getFeedURL().startsWith(absUrl)) {
                    subs.add(sub);
                }
            }
        }
        
        return subs;
    }
    
    
    public String getGroupHandle() {
        return groupHandle;
    }

    public void setGroupHandle(String groupHandle) {
        this.groupHandle = groupHandle;
    }
    
    public PlanetGroupData getGroup() {
        return group;
    }

    public void setGroup(PlanetGroupData group) {
        this.group = group;
    }
    
    public PlanetSubscriptionsBean getBean() {
        return bean;
    }

    public void setBean(PlanetSubscriptionsBean bean) {
        this.bean = bean;
    }

    public PlanetSubscriptionData getSubscription() {
        return subscription;
    }

    public void setSubscription(PlanetSubscriptionData subscription) {
        this.subscription = subscription;
    }
    
}
