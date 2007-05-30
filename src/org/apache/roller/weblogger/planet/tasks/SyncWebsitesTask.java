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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * This tasks is responsible for ensuring that the planet group 'all' contains
 * a subscription for every weblogs in the Roller system. It also takes care 
 * of deleting subsctiptions for weblogs that no longer exist.
 */
public class SyncWebsitesTask extends RollerTaskWithLeasing {
    
    private static Log log = LogFactory.getLog(SyncWebsitesTask.class);
    
    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = "unspecifiedClientId";
    
    // a String description of when to start this task
    private String startTimeDesc = "startOfDay";
    
    // interval at which the task is run, default is 1 day
    private int interval = 1440;
    
    // lease time given to ping task lock, default is 30 minutes
    private int leaseTime = 30;
    
    
    public String getName() {
        return "SyncWebsitesTask";
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public Date getStartTime(Date currentTime) {
        return getAdjustedTime(currentTime, startTimeDesc);
    }
    
    public int getInterval() {
        return this.interval;
    }
    
    public int getLeaseTime() {
        return this.leaseTime;
    }
    
    
    public void init() throws RollerException {
        
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
        
        // make sure we have an absolute url value
        String absUrl = RollerRuntimeConfig.getProperty("site.absoluteurl");
        if(absUrl == null || absUrl.trim().length() == 0) {
            log.error("ERROR: cannot sync websites with Planet Roller - "
                    +"absolute URL not specified in Roller Config");
            return;
        }
        
        try {
            PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
            UserManager userManager = RollerFactory.getRoller().getUserManager();
            
            // first, make sure there is an "all" planet group
            PlanetData planetObject = planet.getPlanet("zzz_default_planet_zzz");
            PlanetGroupData group = planet.getGroup(planetObject, "all");
            if(group == null) {
                group = new PlanetGroupData();
                group.setPlanet(planetObject);
                group.setHandle("all");
                group.setTitle("all");
                planet.saveGroup(group);
            }
            
            // walk through all enable weblogs and add/update subs as needed
            List liveUserFeeds = new ArrayList();
            Iterator websites =
                    userManager.getWebsites(null, Boolean.TRUE, Boolean.TRUE, null, null, 0, -1).iterator();
            while(websites.hasNext()) {
                Weblog weblog = (Weblog) websites.next();
                
                String siteUrl = URLUtilities.getWeblogURL(weblog, null, true);
                String feedUrl = URLUtilities.getWeblogFeedURL(weblog, null, "entries", "rss", null, null, null, false, true);
                
                // add feed url to the "live" list
                liveUserFeeds.add(feedUrl);
                
                // if sub already exists then update it, otherwise add it
                PlanetSubscriptionData sub = planet.getSubscription(feedUrl);
                if (sub == null) {
                    log.info("ADDING feed: "+feedUrl);
                    
                    sub = new PlanetSubscriptionData();
                    sub.setTitle(weblog.getName());
                    sub.setFeedURL(feedUrl);
                    sub.setSiteURL(siteUrl);
                    sub.setAuthor(weblog.getHandle());
                    
                    planet.saveSubscription(sub);
                    group.getSubscriptions().add(sub);
                } else {
                    sub.setTitle(weblog.getName());
                    sub.setAuthor(weblog.getHandle());
                    
                    planet.saveSubscription(sub);
                }
            }
            
            // new subs added, existing subs updated, now delete old subs
            List deleteSubs = new ArrayList();
            Iterator subs = group.getSubscriptions().iterator();
            while(subs.hasNext()) {
                PlanetSubscriptionData sub =
                        (PlanetSubscriptionData) subs.next();
                
                // only delete subs from the group if ...
                // 1. they are local, meaning they use our absolute url
                // 2. they are no longer listed as a weblog 
                if (sub.getFeedURL().startsWith(absUrl) && 
                        !liveUserFeeds.contains(sub.getFeedURL())) {
                    deleteSubs.add(sub);
                }
            }
            
            // now go back through deleteSubs and do actual delete
            // this is required because deleting a sub in the loop above
            // causes a ConcurrentModificationException because we can't
            // modify a collection while we iterate over it
            Iterator deletes = deleteSubs.iterator();
            while(deletes.hasNext()) {
                PlanetSubscriptionData sub =
                        (PlanetSubscriptionData) deletes.next();
                
                log.info("DELETING feed: "+sub.getFeedURL());
                planet.deleteSubscription(sub);
                group.getSubscriptions().remove(sub);
            }
            
            // all done, lets save
            planet.saveGroup(group);
            PlanetFactory.getPlanet().flush();
            
        } catch (RollerException e) {
            log.error("ERROR refreshing entries", e);
        } finally {
            // don't forget to release
            RollerFactory.getRoller().release();
            PlanetFactory.getPlanet().release();
        }
    }
    
    
    /** 
     * Task may be run from the command line 
     */
    public static void main(String[] args) {
        try {
            SyncWebsitesTask task = new SyncWebsitesTask();
            task.init();
            task.run();
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }
    
}
