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
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.pojos.GlobalPermission;


/**
 * Manage planet group subscriptions, default group is "all".
 */
public class PlanetSubscriptions extends PlanetUIAction {
    
    private static final Log log = LogFactory.getLog(PlanetSubscriptions.class);
    
    // id of the group we are working in
    private String groupHandle = null;
    
    // the planet group we are working in
    private PlanetGroup group = null;
    
    // the subscription to deal with
    private String subUrl = null;
    
    
    public PlanetSubscriptions() {
        this.actionName = "planetSubscriptions";
        this.desiredMenu = "admin";
        this.pageTitle = "planetSubscriptions.title";
    }
    
    
    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
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
        
        if(!hasActionErrors()) try {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            
            // check if this subscription already exists before adding it
            Subscription sub = pmgr.getSubscription(getSubUrl());
            if(sub == null) {
                log.debug("Adding New Subscription - "+getSubUrl());
                
                // sub doesn't exist yet, so we need to fetch it
                FeedFetcher fetcher = PlanetFactory.getPlanet().getFeedFetcher();
                sub = fetcher.fetchSubscription(getSubUrl());
                
                // save new sub
                pmgr.saveSubscription(sub);
            } else {
                log.debug("Adding Existing Subscription - "+getSubUrl());
                
                // Subscription already exists
                addMessage("planetSubscription.foundExisting", sub.getTitle());
            }
            
            // add the sub to the group
            group.getSubscriptions().add(sub);
            sub.getGroups().add(group);
            pmgr.saveGroup(group);
            
            // flush changes
            PlanetFactory.getPlanet().flush();
            
            // clear field after success
            setSubUrl(null);
            
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
        
        if(getSubUrl() != null) try {
            
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            
            // remove subscription
            Subscription sub = pmgr.getSubscription(getSubUrl());
            getGroup().getSubscriptions().remove(sub);
            sub.getGroups().remove(getGroup());
            pmgr.saveGroup(getGroup());
            PlanetFactory.getPlanet().flush();
            
            // clear field after success
            setSubUrl(null);
            
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
        
        if(StringUtils.isEmpty(getSubUrl())) {
            addError("planetSubscription.error.feedUrl");
        }
    }
    
    
    public List<Subscription> getSubscriptions() {
        
        List<Subscription> subs = Collections.EMPTY_LIST;
        if(getGroup() != null) {
            Set<Subscription> subsSet = getGroup().getSubscriptions();
            
            // iterate over list and build display list
            subs = new ArrayList();
            for( Subscription sub : subsSet ) {
                // only include external subs for display
                if(!sub.getFeedURL().startsWith("weblogger:")) {
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
    
    public PlanetGroup getGroup() {
        return group;
    }

    public void setGroup(PlanetGroup group) {
        this.group = group;
    }

    public String getSubUrl() {
        return subUrl;
    }

    public void setSubUrl(String subUrl) {
        this.subUrl = subUrl;
    }    
}
