package org.apache.roller.business.datamapper;
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

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetConfigData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.util.rome.DiskFeedInfoCache;


/**
 * Manages Planet Roller objects and entry aggregations in a database.
 * 
 * @author Dave Johnson
 */
public class DatamapperPlanetManagerImpl implements PlanetManager {

    private static Log log = LogFactory.getLog(
        DatamapperPlanetManagerImpl.class);

    /** The strategy for this manager. */
    private DatamapperPersistenceStrategy strategy;

    protected Map lastUpdatedByGroup = new HashMap();
    protected static final String NO_GROUP = "zzz_nogroup_zzz";

    public DatamapperPlanetManagerImpl 
            (DatamapperPersistenceStrategy strategy) {
        log.debug("Instantiating Datamapper Planet Manager");

        this.strategy = strategy;
    }

    public void saveConfiguration(PlanetConfigData config)
            throws RollerException {
        strategy.store(config);
    }

    public void saveGroup(PlanetGroupData group) throws RollerException {
        strategy.store(group);
    }

    public void saveEntry(PlanetEntryData entry) throws RollerException {
        strategy.store(entry);
    }

    public void saveSubscription(PlanetSubscriptionData sub)
            throws RollerException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            strategy.store(sub);
        }
        else {
            throw new RollerException("ERROR: duplicate feed URLs not allowed");
        }
    }

    public void deleteEntry(PlanetEntryData entry) throws RollerException {
        strategy.remove(entry);
    }

    public void deleteGroup(PlanetGroupData group) throws RollerException {
        strategy.remove(group);
    }

    public void deleteSubscription(PlanetSubscriptionData sub)
            throws RollerException {
        strategy.remove(sub);
    }

    public PlanetConfigData getConfiguration() throws RollerException {
        List results = (List) strategy.newQuery(PlanetConfigData.class, 
                "PlanetConfigData.getAll"); 
        PlanetConfigData config = results.size()!=0 ? 
            (PlanetConfigData)results.get(0) : null;
            
        // We inject the cache dir into the config object here to maintain
        // compatibility with the standaline version of the aggregator.
//        if (config != null) {
//            config.setCacheDir(
//                PlanetConfig.getProperty("planet.aggregator.cache.dir"));
//        }
        return config;
    }

    public PlanetSubscriptionData getSubscription(String feedUrl)
            throws RollerException {
        List results = (List) strategy.newQuery(PlanetSubscriptionData.class, 
                "PlanetSubscriptionData.getByFeedURL"); 
        return results.size()!=0 ? 
            (PlanetSubscriptionData)results.get(0) : null;
    }

    public PlanetSubscriptionData getSubscriptionById(String id)
            throws RollerException {
        return (PlanetSubscriptionData) strategy.load(
                PlanetSubscriptionData.class, id);
    }

    public Iterator getAllSubscriptions() {
        try {
            return ((List)strategy.newQuery(PlanetSubscriptionData.class, 
                    "PlanetSubscriptionData.getAll")).iterator(); 
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }

    public int getSubscriptionCount() throws RollerException {
        return ((List)strategy.newQuery(PlanetSubscriptionData.class, 
                "PlanetSubscriptionData.getAll")).size(); 
    }

    public List getTopSubscriptions(int offset, int length) 
            throws RollerException {
        return getTopSubscriptions(null, offset, length);
    }
    
    /**
     * Get top X subscriptions, restricted by group.
     */
    public List getTopSubscriptions(
            String groupHandle, int offset, int len) throws RollerException {
        List result = null;
        if (groupHandle != null) {
            result = (List) strategy.newQuery(PlanetSubscriptionData.class,
                "PlanetSubscriptionData.getByGroupHandleOrderByInboundBlogsDesc")
            .execute(groupHandle);
        } else {
            result = (List) strategy.newQuery(PlanetSubscriptionData.class,
                "PlanetSubscriptionData.getAllOrderByInboundBlogsDesc")
                .execute();
        }
        // TODO handle offset and length
        return result;
    }
    
    public PlanetGroupData getGroup(String handle) throws RollerException {
        List results = (List) strategy.newQuery(PlanetGroupData.class, 
                "PlanetGroupData.getByHandle").execute(handle); 
        // TODO handle max result == 1
        PlanetGroupData group = results.size()!=0 ? 
            (PlanetGroupData)results.get(0) : null;
        return group;
    }

    public PlanetGroupData getGroupById(String id) throws RollerException {
        return (PlanetGroupData) strategy.load(PlanetGroupData.class, id);
    }

    public List getGroups() throws RollerException {
        return (List) strategy.newQuery(PlanetGroupData.class, 
                "PlanetGroupData.getAll").execute(); 
    }

    public List getGroupHandles() throws RollerException {
        List handles = new ArrayList();
        Iterator list = getGroups().iterator();
        while (list.hasNext()) {
            PlanetGroupData group = (PlanetGroupData) list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }

    /**
     * Get entries in a single feed as list of PlanetEntryData objects.
     */
    public List getFeedEntries(
            String feedUrl, int offset, int len) throws RollerException {
        // TODO: ATLAS getFeedEntries DONE       
        List result = (List) strategy.newQuery(PlanetEntryData.class, 
                "PlanetEntryData.getByFeedURL").execute(feedUrl); 
        // TODO handle offset and length
        return result;
    }

    public List getAggregation(
            int offset, int len) throws RollerException {
        return getAggregation(null, null, null, offset, len);
    }
    
    /**
     * Get agggration from cache, enries in reverse chonological order.
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging)
     */
    public List getAggregation(Date startDate, Date endDate,
            int offset, int len) throws RollerException {
        return getAggregation(null, startDate, endDate, offset, len);
    }
    
    public List getAggregation(
            PlanetGroupData group, int offset, int len) 
            throws RollerException {
        return getAggregation(group, null, null, offset, len);
    }
    
    /**
     * Get agggration for group from cache, enries in reverse chonological order.
     * Respects category constraints of group.
     * @param group Restrict to entries from one subscription group.
     * @param offset    Offset into results (for paging)
     * @param length    Maximum number of results to return (for paging)
     */
    public List getAggregation(
            PlanetGroupData group, Date startDate, Date endDate,
            int offset, int length) throws RollerException {
        // TODO: ATLAS getAggregation DONE TESTED
        List result = null;
        if (endDate == null) endDate = new Date();
        try {
            String groupHandle = (group == null) ? NO_GROUP : group.getHandle();
            long startTime = System.currentTimeMillis();
            DatamapperQuery query;
            Object[] params;
            if (group != null) {
                if (startDate != null) {
                    params = new Object[] {groupHandle, endDate, startDate};
                    query = strategy.newQuery(PlanetEntryData.class,
                            "PlanetEntryData.getByGroup&EndDate&StartDateOrderByPubTimeDesc");
                } else {
                    params = new Object[] {groupHandle, endDate};
                    query = strategy.newQuery(PlanetEntryData.class,
                            "PlanetEntryData.getByGroup&EndDateOrderByPubTimeDesc");
                }
                // TODO handle offset and length
            } else {
                if (startDate != null) {
                    params = new Object[] {endDate, startDate};
                    query = strategy.newQuery(PlanetEntryData.class,
                            "PlanetEntryData.getByExternalOrInternalGroup&amp;EndDate&amp;StartDateOrderByPubTimeDesc");
                } else {
                    params = new Object[] {endDate};
                    query = strategy.newQuery(PlanetEntryData.class,
                            "PlanetEntryData.getByExternalOrInternalGroup&amp;EndDateOrderByPubTimeDesc");
                }
                // TODO handle offset and length
            }
            result = (List) query.execute(params);
            Date retLastUpdated;
            if (result.size() > 0) {
                PlanetEntryData entry = (PlanetEntryData)result.get(0);
                retLastUpdated = entry.getPubTime();
            } else {
                retLastUpdated = new Date();
            }
            lastUpdatedByGroup.put(groupHandle, retLastUpdated);
            
            long endTime = System.currentTimeMillis();
            log.debug("Generated aggregation in "
                    + ((endTime-startTime)/1000.0) + " seconds");
            
        } catch (Throwable e) {
            log.error("ERROR: building aggregation for: " + group, e);
            throw new RollerException(e);
        }
        return result;
    }
    
    public synchronized void clearCachedAggregations() {
        lastUpdatedByGroup.clear();
    }
                                                                                        
    public Date getLastUpdated() {
        return (Date) lastUpdatedByGroup.get(NO_GROUP);
    }

    public Date getLastUpdated(PlanetGroupData group) {
        return (Date) lastUpdatedByGroup.get(group);
    }

    public void refreshEntries(String cacheDirPath) throws RollerException {
        
        Date now = new Date();
        long startTime = System.currentTimeMillis();
        PlanetConfigData config = getConfiguration();
        
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
            log.error("Unable to create planet cache directory");
            return;
        }
        
        // abort if cache dir is not writable
        if (!cacheDir.canWrite()) {
            log.error("Planet cache directory is not writable");
            return;
        }
        
        FeedFetcherCache feedInfoCache =
                new DiskFeedInfoCache(cacheDirName);
        
        if (config.getProxyHost()!=null && config.getProxyPort() > 0) {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", config.getProxyHost());
            System.setProperty("http.proxyPort",
                    Integer.toString(config.getProxyPort()));
        }
        /** a hack to set 15 sec timeouts for java.net.HttpURLConnection */
        System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
        System.setProperty("sun.net.client.defaultReadTimeout", "15000");
        
        FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
        //FeedFetcher feedFetcher = new HttpClientFeedFetcher(feedInfoCache);
        feedFetcher.setUsingDeltaEncoding(false);
        feedFetcher.setUserAgent("RollerPlanetAggregator");
        
        // Loop through all subscriptions in the system
        Iterator subs = getAllSubscriptions();
        while (subs.hasNext()) {
            
            long subStartTime = System.currentTimeMillis();
            
            PlanetSubscriptionData sub = (PlanetSubscriptionData)subs.next();
            
            // reattach sub.  sub gets detached as we iterate
            sub = this.getSubscriptionById(sub.getId());
            
            Set newEntries = this.getNewEntries(sub, feedFetcher, feedInfoCache);
            int count = newEntries.size();
            
            log.debug("   Entry count: " + count);
            if (count > 0) {
                sub.purgeEntries();
                sub.addEntries(newEntries);
                this.saveSubscription(sub);
                this.strategy.flush();
            }
            long subEndTime = System.currentTimeMillis();
            log.info("   " + count + " - "
                    + ((subEndTime-subStartTime)/1000.0)
                    + " seconds to process (" + count + ") entries of "
                    + sub.getFeedURL());
        }
        // Clear the aggregation cache
        clearCachedAggregations();
        
        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Refreshed entries in "
                + ((endTime-startTime)/1000.0) + " seconds");
    }
        
    protected Set getNewEntries(PlanetSubscriptionData sub,
                                FeedFetcher feedFetcher,
                                FeedFetcherCache feedInfoCache)
            throws RollerException {
        
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
                PlanetEntryData entry =
                        new PlanetEntryData(feed, romeEntry, sub);
                log.debug("Entry title=" + entry.getTitle() + 
                    " content size=" + entry.getContent().length());
                if (entry.getPubTime() == null) {
                    log.debug("No published date, assigning fake date for " +
                        feedURL);
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

    public void release() {}
}

