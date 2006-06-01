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
package org.apache.roller.ui.admin.struts.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ScheduledTask;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Technorati;

/**
 * Ensure that every user is represented by a subscription in Planet Roller
 * database. Also "ranks" each subscription by populating Technorati inbound
 * blogs and links counts.
 * @author Dave Johnson
 */
public class SyncWebsitesTask extends TimerTask implements ScheduledTask {
    private static Log logger =
            LogFactory.getFactory().getInstance(SyncWebsitesTask.class);
    private Roller roller = null;
    
    /** Task may be run from the command line */
    public static void main(String[] args) {
        try {
            RollerFactory.setRoller(
                    "org.apache.roller.business.hibernate.HibernateRollerImpl");
            SyncWebsitesTask task = new SyncWebsitesTask();
            task.init(RollerFactory.getRoller(), "dummy");
            task.run();
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }
    public void init(Roller roller, String realPath) throws RollerException {
        this.roller = roller;
    }
    public void run() {
        syncWebsites();
        rankSubscriptions();
    }
    /**
     * Ensure there's a subscription in the "all" group for every Roller user.
     */
    private void syncWebsites() {
        try {
            List liveUserFeeds = new ArrayList();
            String baseURL = RollerRuntimeConfig.getProperty("site.absoluteurl");
            if (baseURL == null || baseURL.trim().length()==0) {
                logger.error("ERROR: cannot sync websites with Planet Roller - "
                        +"absolute URL not specified in Roller Config");
            } else {
                PlanetManager planet = roller.getPlanetManager();
                UserManager userManager = roller.getUserManager();
                PlanetGroupData group = planet.getGroup("all");
                if (group == null) {
                    group = new PlanetGroupData();
                    group.setHandle("all");
                    group.setTitle("all");
                    planet.saveGroup(group);
                    roller.flush();
                }
                try {
                    String baseFeedURL = baseURL + "/rss/";
                    String baseSiteURL = baseURL + "/page/";
                    // get list of all enabled and active weblogs
                    Iterator websites =
                        roller.getUserManager().getWebsites(null, Boolean.TRUE, Boolean.TRUE, null, null, 0, -1).iterator();
                    while (websites.hasNext()) {
                        WebsiteData website = (WebsiteData)websites.next();
                        
                        StringBuffer sitesb = new StringBuffer();
                        sitesb.append(baseSiteURL);
                        sitesb.append(website.getHandle());
                        String siteUrl = sitesb.toString();
                        
                        StringBuffer feedsb = new StringBuffer();
                        feedsb.append(baseFeedURL);
                        feedsb.append(website.getHandle());
                        String feedUrl = feedsb.toString();
                        
                        liveUserFeeds.add(feedUrl);
                        
                        PlanetSubscriptionData sub =
                                planet.getSubscription(feedUrl);
                        if (sub == null) {
                            logger.info("ADDING feed: "+feedUrl);
                            sub = new PlanetSubscriptionData();
                            sub.setTitle(website.getName());
                            sub.setFeedUrl(feedUrl);
                            sub.setSiteUrl(siteUrl);
                            sub.setAuthor(website.getHandle());
                            planet.saveSubscription(sub);
                            group.addSubscription(sub);
                        } else {
                            sub.setTitle(website.getName());
                            sub.setAuthor(website.getHandle());
                            planet.saveSubscription(sub);
                        }
                    }
                    planet.saveGroup(group);
                    roller.flush();
                    roller.release();
                    
                    // TODO: new planet manager method deleteSubs(list)
                    group = group = planet.getGroup("all");
                    Iterator subs = group.getSubscriptions().iterator();
                    while (subs.hasNext()) {
                        PlanetSubscriptionData sub =
                                (PlanetSubscriptionData)subs.next();
                        if (!liveUserFeeds.contains(sub.getFeedUrl())) {
                            logger.info("DELETING feed: "+sub.getFeedUrl());
                            planet.deleteSubscription(sub);
                        }
                    }
                    roller.flush();
                } finally {
                    roller.release();
                }
            }
        } catch (RollerException e) {
            logger.error("ERROR refreshing entries", e);
        }
    }
    
    /**
     * Loop through all subscriptions get get Technorati rankings for each
     */
    private void rankSubscriptions() {
        int count = 0;
        int errorCount = 0;
        try {
            PlanetManager planet = roller.getPlanetManager();
            PlanetConfigData config = planet.getConfiguration();
            Technorati technorati = null;
            try {
                if (config.getProxyHost()!=null && config.getProxyPort() != -1) {
                    technorati = new Technorati(
                            config.getProxyHost(), config.getProxyPort());
                } else {
                    technorati = new Technorati();
                }
            } catch (IOException e) {
                logger.error("Aborting collection of Technorati rankings.\n"
                +"technorati.license not found at root of classpath.\n"
                +"Get license at http://technorati.com/developers/apikey.html\n"
                +"Put the license string in a file called technorati.license.\n"
                +"And place that file at the root of Roller's classpath.\n"
                +"For example, in the /WEB-INF/classes directory.");
                return;
            }
            UserManager userManager = roller.getUserManager();
            try {
                int limit = RollerConfig.getIntProperty(
                    "planet.aggregator.technorati.limit", 500);
                int userCount = planet.getSubscriptionCount();
                int mod = (userCount / limit) + 1;
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                int day = cal.get(Calendar.DAY_OF_YEAR);
                
                int start = (day % mod) * limit;
                int end = start + limit;
                end = end > userCount ? userCount : end;
                logger.info("Updating subscriptions ["+start+":"+end+"]");
                
                Iterator subs = planet.getAllSubscriptions();
                while (subs.hasNext()) {
                    PlanetSubscriptionData sub =
                            (PlanetSubscriptionData)subs.next();
                    if (count >= start && count < end) {
                        try {
                            Technorati.Result result =
                                    technorati.getBloginfo(sub.getSiteUrl());
                            if (result != null && result.getWeblog() != null) {
                                sub.setInboundblogs(
                                        result.getWeblog().getInboundblogs());
                                sub.setInboundlinks(
                                        result.getWeblog().getInboundlinks());
                                logger.debug("Adding rank for "
                                        +sub.getFeedUrl()+" ["+count+"|"
                                        +sub.getInboundblogs()+"|"
                                        +sub.getInboundlinks()+"]");
                            } else {
                                logger.debug(
                                        "No ranking available for "
                                        +sub.getFeedUrl()+" ["+count+"]");
                                sub.setInboundlinks(0);
                                sub.setInboundblogs(0);
                            }
                            planet.saveSubscription(sub);
                        } catch (Exception e) {
                            logger.warn("WARN ranking subscription ["
                                    + count + "]: " + e.getMessage());
                            if (errorCount++ > 5) {
                                logger.warn(
                                        "   Stopping ranking, too many errors");
                                break;
                            }
                        }
                    }
                    count++;
                }
                roller.flush();
            } finally {
                roller.release();
            }
        } catch (Exception e) {
            logger.error("ERROR ranking subscriptions", e);
        }
    }
}

