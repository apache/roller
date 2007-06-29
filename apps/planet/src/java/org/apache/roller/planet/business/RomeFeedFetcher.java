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

package org.apache.roller.planet.business;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.hibernate.HibernatePlanetManagerImpl;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.util.rome.DiskFeedInfoCache;


/**
 * A FeedFetcher based on the ROME RSS/Atom feed parser (http://rome.dev.java.net).
 */
public class RomeFeedFetcher implements FeedFetcher {
    
    private static Log log = LogFactory.getLog(RomeFeedFetcher.class);
    
    
    public RomeFeedFetcher() {}
    
    
    // refresh Entries for all Subscriptions
    public void refreshEntries(String cacheDirPath) throws PlanetException {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        Date now = new Date();
        long startTime = System.currentTimeMillis();
        
        // can't continue without cache dir
        if (cacheDirPath == null) {
            log.warn("Planet cache directory not set, aborting refresh");
            return;
        }
        
        // allow ${user.home} in cache dir property
        String cacheDirName = cacheDirPath.replaceFirst(
                "\\$\\{user.home}",System.getProperty("user.home"));
        
        // allow ${catalina.home} in cache dir property
        if (System.getProperty("catalina.home") != null) {
            cacheDirName = cacheDirName.replaceFirst(
                "\\$\\{catalina.home}",System.getProperty("catalina.home"));
        }
        
        // create cache  dir if it does not exist
        File cacheDir = null;
        try {
            cacheDir = new File(cacheDirName);
            if (!cacheDir.exists()) cacheDir.mkdirs();
        } catch (Exception e) {
            log.error("Unable to create planet cache directory: " + cacheDir.getPath(), e);
            return;
        }
        
        // abort if cache dir is not writable
        if (!cacheDir.canWrite()) {
            log.error("Planet cache directory is not writable: " + cacheDir.getPath());
            return;
        }
        
        FeedFetcherCache feedInfoCache =
                new DiskFeedInfoCache(cacheDirName);
        
        String proxyHost = PlanetRuntimeConfig.getProperty("site.proxyhost");
        int proxyPort = PlanetRuntimeConfig.getIntProperty("site.proxyport");
        if (proxyHost != null && proxyPort > 0) {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxyPort));
        }
        /** a hack to set 15 sec timeouts for java.net.HttpURLConnection */
        System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
        System.setProperty("sun.net.client.defaultReadTimeout", "15000");
        
        com.sun.syndication.fetcher.FeedFetcher feedFetcher = 
                new HttpURLFeedFetcher(feedInfoCache);
        //FeedFetcher feedFetcher = new HttpClientFeedFetcher(feedInfoCache);
        feedFetcher.setUsingDeltaEncoding(false);
        feedFetcher.setUserAgent("RollerPlanetAggregator");
        
        // Loop through all subscriptions in the system
        Iterator subs = mgr.getSubscriptions().iterator();
        while (subs.hasNext()) {
            
            long subStartTime = System.currentTimeMillis();
            
            Subscription sub = (Subscription)subs.next();
            
            // reattach sub.  sub gets detached as we iterate
            sub = mgr.getSubscriptionById(sub.getId());
            
            Set newEntries = this.getNewEntries(sub, feedFetcher, feedInfoCache);
            int count = newEntries.size();
            
            log.debug("   Entry count: " + count);
            if (count > 0) {
                mgr.deleteEntries(sub);
                sub.addEntries(newEntries);
                mgr.saveSubscription(sub);
            }
            
            long subEndTime = System.currentTimeMillis();
            log.debug("   " + count + " - "
                    + ((subEndTime-subStartTime)/1000.0)
                    + " seconds to process (" + count + ") entries of "
                    + sub.getFeedURL());
        }
        
        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Refreshed entries in "
                + ((endTime-startTime)/1000.0) + " seconds");
    }
    
    
    // get new Entries for a specific Subscription
    protected Set getNewEntries(Subscription sub,
                                com.sun.syndication.fetcher.FeedFetcher feedFetcher,
                                FeedFetcherCache feedInfoCache)
            throws PlanetException {
        
        Set newEntries = new TreeSet();
        SyndFeed feed = null;
        URL feedURL = null;
        Date lastUpdated = new Date();
        try {
            feedURL = new URL(sub.getFeedURL());
            log.debug("Get feed from cache "+sub.getFeedURL());
            feed = feedFetcher.retrieveFeed(feedURL);
            SyndFeedInfo feedInfo = feedInfoCache.getFeedInfo(feedURL);
            if (feedInfo.getLastModified() != null) {
                long lastUpdatedLong =
                        ((Long)feedInfo.getLastModified()).longValue();
                if (lastUpdatedLong != 0) {
                    lastUpdated = new Date(lastUpdatedLong);
                }
            }
            Thread.sleep(100); // be nice
        } catch (Exception e) {
            log.warn("ERROR parsing " + sub.getFeedURL()
            + " : " + e.getClass().getName() + " : " + e.getMessage());
            log.debug(e);
            return newEntries; // bail out
        }
        if (lastUpdated!=null && sub.getLastUpdated()!=null) {
            Calendar feedCal = Calendar.getInstance();
            feedCal.setTime(lastUpdated);
            
            Calendar subCal = Calendar.getInstance();
            subCal.setTime(sub.getLastUpdated());
            
            if (!feedCal.after(subCal)) {
                if (log.isDebugEnabled()) {
                    String msg = MessageFormat.format(
                            "   Skipping ({0} / {1})",
                            new Object[] {
                        lastUpdated, sub.getLastUpdated()});
                    log.debug(msg);
                }
                return newEntries; // bail out
            }
        }
        if (feed.getPublishedDate() != null) {
            sub.setLastUpdated(feed.getPublishedDate());
            // saving sub here causes detachment issues, so we save it later
        }
        
        // Horrible kludge for Feeds without entry dates: most recent entry is 
        // given feed's last publish date (or yesterday if none exists) and 
        // earler entries are placed at once day intervals before that.
        Calendar cal = Calendar.getInstance();
        if (sub.getLastUpdated() != null) {
            cal.setTime(sub.getLastUpdated());
        } else {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);
        }
        
        // Populate subscription object with new entries
        Iterator entries = feed.getEntries().iterator();
        while (entries.hasNext()) {
            try {
                SyndEntry romeEntry = (SyndEntry) entries.next();
                SubscriptionEntry entry =
                        new SubscriptionEntry(feed, romeEntry, sub);
                log.debug("Entry title=" + entry.getTitle() + " content size=" + entry.getContent().length());
                if (entry.getPubTime() == null) {
                    log.debug("No published date, assigning fake date for "+feedURL);
                    entry.setPubTime(new Timestamp(cal.getTimeInMillis()));
                }
                if (entry.getPermalink() == null) {
                    log.warn("No permalink, rejecting entry from "+feedURL);
                } else {
                    newEntries.add(entry);
                }
                cal.add(Calendar.DATE, -1);
            } catch (Exception e) {
                log.error("ERROR processing subscription entry", e);
            }
        }
        return newEntries;
    }
    
}
