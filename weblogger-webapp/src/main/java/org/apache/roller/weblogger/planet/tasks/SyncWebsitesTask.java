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

package org.apache.roller.weblogger.planet.tasks;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.GuiceWebloggerProvider;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WebloggerProvider;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * This tasks is responsible for ensuring that the planet group 'all' contains
 * a subscription for every weblogs in the Roller system. It also takes care 
 * of deleting subsctiptions for weblogs that no longer exist.
 */
public class SyncWebsitesTask extends RollerTaskWithLeasing {
    private static Log log = LogFactory.getLog(SyncWebsitesTask.class);

    public static String NAME = "SyncWebsitesTask";

    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = "unspecifiedClientId";
    
    // a String description of when to start this task
    private String startTimeDesc = "startOfDay";
    
    // interval at which the task is run, default is 1 day
    private int interval = 1440;
    
    // lease time given to ping task lock, default is 30 minutes
    private int leaseTime = 30;
    
    public String getClientId() {
        return clientId;
    }
    
    public Date getStartTime(Date currentTime) {
        return getAdjustedTime(currentTime, startTimeDesc);
    }
    
    public String getStartTimeDesc() {
        return startTimeDesc;
    }
    
    public int getInterval() {
        return this.interval;
    }
    
    public int getLeaseTime() {
        return this.leaseTime;
    }
    
    
    public void init() throws WebloggerException {
        this.init(RefreshRollerPlanetTask.NAME);
    }

    public void init(String name) throws WebloggerException {
        super.init(name);

        // get relevant props
        Properties props = this.getTaskProperties();
        
        // extract clientId
        String client = props.getProperty("clientId");
        if(client != null) {
            this.clientId = client;
        }
        
        // extract start time
        String startTimeStr = props.getProperty("startTime");
        if(startTimeStr != null) {
            this.startTimeDesc = startTimeStr;
        }
        
        // extract interval
        String intervalStr = props.getProperty("interval");
        if(intervalStr != null) {
            try {
                this.interval = Integer.parseInt(intervalStr);
            } catch (NumberFormatException ex) {
                log.warn("Invalid interval: "+intervalStr);
            }
        }
        
        // extract lease time
        String leaseTimeStr = props.getProperty("leaseTime");
        if(leaseTimeStr != null) {
            try {
                this.leaseTime = Integer.parseInt(leaseTimeStr);
            } catch (NumberFormatException ex) {
                log.warn("Invalid leaseTime: "+leaseTimeStr);
            }
        }
    }
    
    
    /**
     * Ensure there's a subscription in the "all" group for every Roller weblog.
     */
    public void runTask() {
        
        log.info("Syncing local weblogs with planet subscriptions list");
        
        try {
            PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
            
            // first, make sure there is an "all" pmgr group
            Planet planetObject = pmgr.getWebloggerById("zzz_default_planet_zzz");
            PlanetGroup group = pmgr.getGroup(planetObject, "all");
            if (group == null) {
                group = new PlanetGroup();
                group.setPlanet(planetObject);
                group.setHandle("all");
                group.setTitle("all");
                pmgr.saveGroup(group);
                WebloggerFactory.getWeblogger().flush();
            }
            
            // walk through all enable weblogs and add/update subs as needed
            List liveUserFeeds = new ArrayList();
            List<Weblog> websites = WebloggerFactory.getWeblogger()
                    .getWeblogManager().getWeblogs(Boolean.TRUE, Boolean.TRUE, null, null, 0, -1);
            for ( Weblog weblog : websites ) {
                
                log.debug("processing weblog - "+weblog.getHandle());
                String feedUrl = "weblogger:"+weblog.getHandle();
                
                // add feed url to the "live" list
                liveUserFeeds.add(feedUrl);
                
                // if sub already exists then update it, otherwise add it
                Subscription sub = pmgr.getSubscription(feedUrl);
                if (sub == null) {
                    log.debug("ADDING feed: "+feedUrl);
                    
                    sub = new Subscription();
                    sub.setTitle(weblog.getName());
                    sub.setFeedURL(feedUrl);
                    sub.setSiteURL(
                        WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(weblog, null, true));
                    sub.setAuthor(weblog.getName());
                    sub.setLastUpdated(new Date(0));
                    pmgr.saveSubscription(sub);
                    
                    sub.getGroups().add(group);
                    group.getSubscriptions().add(sub);

                    pmgr.saveGroup(group);

                } else {
                    log.debug("UPDATING feed: "+feedUrl);
                    
                    sub.setTitle(weblog.getName());
                    sub.setAuthor(weblog.getName());
                    
                    pmgr.saveSubscription(sub);
                }
                
                // save as we go
                WebloggerFactory.getWeblogger().flush();
            }
            
            // new subs added, existing subs updated, now delete old subs
            Set<Subscription> deleteSubs = new HashSet();
            Set<Subscription> subs = group.getSubscriptions();
            for( Subscription sub : subs ) {
                
                // only delete subs from the group if ...
                // 1. they are local
                // 2. they are no longer listed as a weblog 
                if (sub.getFeedURL().startsWith("weblogger:") && 
                        !liveUserFeeds.contains(sub.getFeedURL())) {
                    deleteSubs.add(sub);
                }
            }
            
            // now go back through deleteSubs and do actual delete
            // this is required because deleting a sub in the loop above
            // causes a ConcurrentModificationException because we can't
            // modify a collection while we iterate over it
            for( Subscription deleteSub : deleteSubs ) {
                
                log.debug("DELETING feed: "+deleteSub.getFeedURL());
                pmgr.deleteSubscription(deleteSub);
                group.getSubscriptions().remove(deleteSub);
            }
            
            // all done, lets save
            pmgr.saveGroup(group);
            WebloggerFactory.getWeblogger().flush();
            
        } catch (RollerException e) {
            log.error("ERROR refreshing entries", e);
        } finally {
            // don't forget to release
            WebloggerFactory.getWeblogger().release();
        }
    }
    
    
    /** 
     * Task may be run from the command line 
     */
    public static void main(String[] args) throws Exception {
        
        // before we can do anything we need to bootstrap the planet backend
        WebloggerStartup.prepare();
        
        // we need to use our own planet provider for integration
        String guiceModule = WebloggerConfig.getProperty("planet.aggregator.guice.module");
        WebloggerProvider provider = new GuiceWebloggerProvider(guiceModule);
        WebloggerFactory.bootstrap(provider);
        
        SyncWebsitesTask task = new SyncWebsitesTask();
        task.init(); // use default name
        task.run();
    }
    
}
