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

package org.apache.roller.weblogger.planet.business;

import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.business.RomeFeedFetcher;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Extends Roller Planet's feed fetcher to fetch local feeds directly from Roller.
 */
public class RollerRomeFeedFetcher extends RomeFeedFetcher {
    
    private static Log log = LogFactory.getLog(RollerRomeFeedFetcher.class); 
    
    
    /** Creates a new instance of RollerRomeFeedFetcher */
    public RollerRomeFeedFetcher() {
    }
    
    // get new Entries for a specific Subscription
    protected Set getNewEntries(PlanetSubscriptionData sub,
                                com.sun.syndication.fetcher.FeedFetcher feedFetcher,
                                FeedFetcherCache feedInfoCache)
            throws PlanetException {
        
        String localURL = RollerRuntimeConfig.getProperty("site.absoluteurl");
        
        // if this is not a local url then let parent deal with it
        if (StringUtils.isEmpty(localURL) || !sub.getFeedURL().startsWith(localURL)) {            
            log.debug("Feed is remote, letting parent handle it "+sub.getFeedURL());            
            return super.getNewEntries(sub, feedFetcher, feedInfoCache);
        }
        
        try {
            // for local feeds, sub.author = website.handle
            // feed is from our domain and we have a handle, lets deal with it
            if(sub.getAuthor() != null) {
                
                log.debug("Getting LOCAL feed "+sub.getFeedURL());
                
                Set newEntries = new TreeSet();
                
                // get corresponding website object
                UserManager usermgr = WebloggerFactory.getWeblogger().getUserManager();
                Weblog website = usermgr.getWebsiteByHandle(sub.getAuthor());
                if (website == null) 
                    return newEntries;
                
                // figure website last update time
                WeblogManager blogmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                PlanetManager planetManager = PlanetFactory.getPlanet().getPlanetManager();
                
                Date siteUpdated = website.getLastModified();
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
                            null,                        // endDate
                            null,                        // catName
                            null,WeblogEntry.PUBLISHED,   // status
                            null,                        // text
                            null,                        // sortby (null means pubTime)
                            null,
                            null,                        // locale
                            0,                           // offset
                            entryCount);                 
                    
                    sub.setLastUpdated(siteUpdated);
                    planetManager.saveSubscription(sub);
                    PlanetFactory.getPlanet().flush();
                    
                } else {
                    if (log.isDebugEnabled()) {
                        String msg = MessageFormat.format(
                                "   Skipping ({0} / {1})", new Object[] {
                            siteUpdated, sub.getLastUpdated()});
                        log.debug(msg);
                    }
                }
                
                // Populate subscription object with new entries
                PluginManager ppmgr = WebloggerFactory.getWeblogger().getPagePluginManager();
                Map pagePlugins = ppmgr.getWeblogEntryPlugins(website);
                Iterator entryIter = entries.iterator();
                while (entryIter.hasNext()) {
                    try {
                        WeblogEntry rollerEntry =
                                (WeblogEntry)entryIter.next();
                        
                        PlanetEntryData entry = new PlanetEntryData();                        
                        String content = "";
                        if (!StringUtils.isEmpty(rollerEntry.getText())) {
                            content = rollerEntry.getText();
                        } else {
                            content = rollerEntry.getSummary();
                        }
                        content = ppmgr.applyWeblogEntryPlugins(pagePlugins, rollerEntry, content);
                        
                        entry.setAuthor(rollerEntry.getCreator().getScreenName());
                        entry.setTitle(rollerEntry.getTitle());
                        entry.setPubTime(rollerEntry.getPubTime());
                        entry.setText(content);
                        entry.setPermalink(rollerEntry.getPermalink());
                        entry.setCategoriesString(rollerEntry.getCategory().getPath());                        
                        
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
