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
/*
 * HibernateRollerPlanetManagerImpl.java
 *
 * Created on April 17, 2006, 1:53 PM
 */

package org.apache.roller.business.hibernate;

import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.commons.lang.StringUtils;



/**
 * An extended version of the base PlanetManager implementation.
 * 
 * This is meant for use by Roller installations that are running the planet
 * aggregator in the same application instance and want to fetch feeds from
 * their local Roller blogs in a more efficient manner.
 */
public class HibernateRollerPlanetManagerImpl extends HibernatePlanetManagerImpl {
    
    private static Log log = LogFactory.getLog(HibernateRollerPlanetManagerImpl.class);
    
    
    public HibernateRollerPlanetManagerImpl(HibernatePersistenceStrategy strat) {
        
        super(strat);
        
        log.info("Instantiating Hibernate Roller Planet Manager");
    }
    
    
    protected Set getNewEntries(PlanetSubscriptionData sub,
                                FeedFetcher feedFetcher,
                                FeedFetcherCache feedInfoCache)
            throws RollerException {
        
        String localURL = RollerRuntimeConfig.getProperty("site.absoluteurl");
        
        // if this is not a local url then let parent deal with it
        if (StringUtils.isEmpty(localURL) || !sub.getFeedURL().startsWith(localURL)) {
            
            log.debug("Feed is remote, letting parent handle it "+sub.getFeedURL());
            
            return super.getNewEntries(sub, feedFetcher, feedInfoCache);
        }
        
        // url must be local, lets deal with it
        Set newEntries = new TreeSet();
        try {
            // for local feeds, sub.author = website.handle
            if (sub.getAuthor()!=null && sub.getFeedURL().endsWith(sub.getAuthor())) {
                
                log.debug("Getting LOCAL feed "+sub.getFeedURL());
                
                // get corresponding website object
                UserManager usermgr = RollerFactory.getRoller().getUserManager();
                WebsiteData website = usermgr.getWebsiteByHandle(sub.getAuthor());
                if (website == null) return newEntries;
                
                // figure website last update time
                WeblogManager blogmgr = RollerFactory.getRoller().getWeblogManager();
                
                Date siteUpdated = blogmgr.getWeblogLastPublishTime(website);
                if (siteUpdated == null) { // Site never updated, skip it
                    log.warn("Last-publish time null, skipping local feed ["
                            + website.getHandle() + "]");
                    return newEntries;
                }
                
                // if website last update time > subsciption last update time
                List entries = new ArrayList();
                if (sub.getLastUpdated()==null || siteUpdated.after(sub.getLastUpdated())) {
                    int entryCount = RollerRuntimeConfig.getIntProperty(
                            "site.newsfeeds.defaultEntries");
                    entries = blogmgr.getWeblogEntries(
                            website,
                            null,                             
                            null,                        // startDate
                            new Date(),                  // endDate
                            null,                        // catName
                            WeblogEntryData.PUBLISHED,   // status
                            null,                        // sortby (null means pubTime)
                            0,                           // offset
                            entryCount);                // length
                    
                    sub.setLastUpdated(siteUpdated);
                    saveSubscription(sub);
                    
                } else {
                    if (log.isDebugEnabled()) {
                        String msg = MessageFormat.format(
                                "   Skipping ({0} / {1})", new Object[] {
                            siteUpdated, sub.getLastUpdated()});
                        log.debug(msg);
                    }
                }
                
                // Populate subscription object with new entries
                PluginManager ppmgr = RollerFactory.getRoller().getPagePluginManager();
                Map pagePlugins = ppmgr.getWeblogEntryPlugins(website, new HashMap());
                Iterator entryIter = entries.iterator();
                while (entryIter.hasNext()) {
                    try {
                        WeblogEntryData rollerEntry =
                                (WeblogEntryData)entryIter.next();
                        PlanetEntryData entry =
                                new PlanetEntryData(rollerEntry, sub, pagePlugins);
                        saveEntry(entry);
                        newEntries.add(entry);
                    } catch (Exception e) {
                        log.error("ERROR processing subscription entry", e);
                    }
                }
                return newEntries;
            }
        } catch (Exception e) {
            log.warn("Problem reading local feed", e);
        }
        
        log.debug("Failed to fetch locally, trying remote "+sub.getFeedURL());
        
        // if there was an error then try normal planet method
        return super.getNewEntries(sub, feedFetcher, feedInfoCache);
    }
    
}
