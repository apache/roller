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

package org.apache.roller.business.hibernate;

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
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetEntryData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetGroupSubscriptionAssoc;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.util.rome.DiskFeedInfoCache;

/**
 * Hibernate implementation of the PlanetManager.
 */
public class HibernatePlanetManagerImpl implements PlanetManager {
    
    private static Log log = LogFactory.getLog(HibernatePlanetManagerImpl.class);
    
    protected static final String NO_GROUP = "zzz_nogroup_zzz";
    
    private HibernatePersistenceStrategy strategy = null;
    private String localURL = null;
    private Map lastUpdatedByGroup = new HashMap();
       
    public HibernatePlanetManagerImpl(HibernatePersistenceStrategy strat) {
        
        this.strategy = strat;
        
        // TODO: this is bad.  this property should be in the planet config.
        localURL = RollerRuntimeConfig.getProperty("site.absoluteurl");
    }
        
    public void saveConfiguration(PlanetConfigData config) 
        throws RollerException {
        strategy.store(config);
    }
        
    public void saveGroup(PlanetGroupData group) 
        throws RollerException {
        
        // save each sub assoc first, then the group
        Iterator assocs = group.getGroupSubscriptionAssocs().iterator();
        while (assocs.hasNext()) {
            PlanetGroupSubscriptionAssoc assoc =
                    (PlanetGroupSubscriptionAssoc)assocs.next();
            strategy.store(assoc);
        }
        strategy.store(group);
    }
        
    public void saveEntry(PlanetEntryData entry) 
        throws RollerException {
        strategy.store(entry);
    }
        
    public void saveSubscription(PlanetSubscriptionData sub) 
        throws RollerException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedUrl());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            this.strategy.store(sub);
        } else {
            throw new RollerException("ERROR: duplicate feed URLs not allowed");
        }
    }
        
    public void deleteEntry(PlanetEntryData entry) 
        throws RollerException {
        strategy.remove(entry);
    }
        
    public void deleteGroup(PlanetGroupData group) 
        throws RollerException {
        strategy.remove(group);
    }
        
    public void deleteSubscription(PlanetSubscriptionData sub) 
        throws RollerException {
        strategy.remove(sub);
    }
        
    public PlanetConfigData getConfiguration() throws RollerException {
        PlanetConfigData config = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetConfigData.class);
            criteria.setMaxResults(1);
            List list = criteria.list();
            config = list.size()!=0 ? (PlanetConfigData)list.get(0) : null;
            
            // We inject the cache dir into the config object here to maintain
            // compatibility with the standaline version of the aggregator.
            if (config != null) {
                config.setCacheDir(
                        RollerConfig.getProperty("planet.aggregator.cache.dir"));
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return config;
    }    
    
    public PlanetSubscriptionData getSubscription(String feedUrl) 
        throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.setMaxResults(1);
            criteria.add(Expression.eq("feedUrl", feedUrl));
            List list = criteria.list();
            return list.size()!=0 ? (PlanetSubscriptionData)list.get(0) : null;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    public PlanetSubscriptionData getSubscriptionById(String id) 
        throws RollerException {
        return (PlanetSubscriptionData) strategy.load(id, PlanetSubscriptionData.class);
    }
        
    public Iterator getAllSubscriptions() {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.addOrder(Order.asc("feedUrl"));
            List list = criteria.list();
            return list.iterator();
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }
    
    public int getSubscriptionCount() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Integer count = (Integer)session.createQuery(
                    "select count(*) from org.apache.roller.pojos.PlanetSubscriptionData").uniqueResult();
            return count.intValue();
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription count", e);
        }
    }
    
    public synchronized List getTopSubscriptions(int offset, int length) 
        throws RollerException {
        // TODO: ATLAS getTopSubscriptions DONE
        String groupHandle = NO_GROUP;
        List ret = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.addOrder(Order.desc("inboundblogs"));
            criteria.setFirstResult(offset);
            criteria.setMaxResults(length);
            ret = criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return ret;
    }
    
    public synchronized List getTopSubscriptions(
            PlanetGroupData group, int offset, int length) 
            throws RollerException {
        // TODO: ATLAS getTopSubscriptions DONE
        String groupHandle = (group == null) ? NO_GROUP : group.getHandle();
        List ret = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Query query = session.createQuery(
                    "select sub from org.apache.roller.pojos.PlanetSubscriptionData sub "
                    +"join sub.groupSubscriptionAssocs assoc "
                    +"where "
                    +"assoc.group.handle=:groupHandle "
                    +"order by sub.inboundblogs desc");
            query.setString("groupHandle", group.getHandle());
            query.setFirstResult(offset);
            query.setMaxResults(length);
            ret = query.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return ret;
    }
        
    public PlanetGroupData getGroup(String handle) throws RollerException {
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            criteria.setMaxResults(1);
            criteria.add(Expression.eq("handle", handle));
            return (PlanetGroupData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    public PlanetGroupData getGroupById(String id) throws RollerException {
        return (PlanetGroupData) strategy.load(id, PlanetGroupData.class);
    }
        
    public List getGroups() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
       
    public List getGroupHandles() throws RollerException {
        List handles = new ArrayList();
        Iterator list = getGroups().iterator();
        while (list.hasNext()) {
            PlanetGroupData group = (PlanetGroupData)list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }
    
     public List getFeedEntries(String feedUrl, int offset, int length)
        throws RollerException {
        // TODO: ATLAS getFeedEntries         
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetEntryData.class);
            criteria.setFirstResult(offset);
            criteria.setMaxResults(length);
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }    
   
    public synchronized List getAggregation(int offset, int len) 
        throws RollerException {
        return getAggregation(null, offset, len);
    } 
    
    public synchronized List getAggregation(
        PlanetGroupData group, int offset, int length)
        throws RollerException {
        // TODO: ATLAS getAggregation DONE TESTED
        List ret = null;
        try {
            String groupHandle = (group == null) ? NO_GROUP : group.getHandle();
            long startTime = System.currentTimeMillis();
            Session session =
                    ((HibernatePersistenceStrategy)strategy).getSession();
            
            if (group != null) {
                Query query = session.createQuery(
                    "select entry from org.apache.roller.pojos.PlanetEntryData entry "
                    +"join entry.subscription.groupSubscriptionAssocs assoc "
                    +"where assoc.group=:group order by entry.pubTime desc");
                query.setEntity("group", group);
                query.setFirstResult(offset);
                query.setMaxResults(length);
                ret = query.list();
            } else {
                Query query = session.createQuery(
                    "select entry from org.apache.roller.pojos.PlanetEntryData entry "
                    +"join entry.subscription.groupSubscriptionAssocs assoc "
                    +"where "
                    +"assoc.group.handle='external' or assoc.group.handle='all'"
                    +" order by entry.pubTime desc");
                query.setFirstResult(offset);
                query.setMaxResults(length);
                ret = query.list();
            }
            Date retLastUpdated = null;
            if (ret.size() > 0) {
                PlanetEntryData entry = (PlanetEntryData)ret.get(0);
                retLastUpdated = entry.getPubTime();
            } else {
                retLastUpdated = new Date();
            }
            lastUpdatedByGroup.put(groupHandle, retLastUpdated);
            
            long endTime = System.currentTimeMillis();
            log.info("Generated aggregation in "
                    +((endTime-startTime)/1000.0)+" seconds");
            
        } catch (Throwable e) {
            log.error("ERROR: building aggregation for: "+group, e);
            throw new RollerException(e);
        }
        return ret;
    } 
    
    public synchronized void clearCachedAggregations() {
        lastUpdatedByGroup.clear();
    }
    
    public Date getLastUpdated() {
        return (Date)lastUpdatedByGroup.get(NO_GROUP);
    }
    
    public Date getLastUpdated(PlanetGroupData group) {
        return (Date)lastUpdatedByGroup.get(group);
    }
    
    
    public void refreshEntries() throws RollerException {
        
        Roller roller = RollerFactory.getRoller();
        
        Date now = new Date();
        long startTime = System.currentTimeMillis();
        PlanetConfigData config = getConfiguration();
        
        // can't continue without cache dir
        if (config == null || config.getCacheDir() == null) {
            log.warn("Planet cache directory not set, aborting refresh");
            return;
        }
        
        // allow ${user.home} in cache dir property
        String cacheDirName = config.getCacheDir().replaceFirst(
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
            
            // Fetch latest entries for each subscription
//            Set newEntries = null;
//            int count = 0;
//            if (!StringUtils.isEmpty(localURL) && sub.getFeedUrl().startsWith(localURL)) {
//                newEntries = getNewEntriesLocal(sub, feedFetcher, feedInfoCache);
//            } else {
//                newEntries = getNewEntriesRemote(sub, feedFetcher, feedInfoCache);
//            }
            Set newEntries = this.getNewEntries(sub, feedFetcher, feedInfoCache);
            int count = newEntries.size();
            
            log.debug("   Entry count: " + count);
            if (count > 0) {
                sub.purgeEntries();
                sub.addEntries(newEntries);
                this.saveSubscription(sub);
                if(roller != null) roller.flush();
            }
            long subEndTime = System.currentTimeMillis();
            log.info("   " + count + " - "
                    + ((subEndTime-subStartTime)/1000.0)
                    + " seconds to process (" + count + ") entries of "
                    + sub.getFeedUrl());
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
        URL feedUrl = null;
        Date lastUpdated = new Date();
        try {
            feedUrl = new URL(sub.getFeedUrl());
            log.debug("Get feed from cache "+sub.getFeedUrl());
            feed = feedFetcher.retrieveFeed(feedUrl);
            SyndFeedInfo feedInfo = feedInfoCache.getFeedInfo(feedUrl);
            if (feedInfo.getLastModified() != null) {
                long lastUpdatedLong =
                        ((Long)feedInfo.getLastModified()).longValue();
                if (lastUpdatedLong != 0) {
                    lastUpdated = new Date(lastUpdatedLong);
                }
            }
            Thread.sleep(100); // be nice
        } catch (Exception e) {
            log.warn("ERROR parsing " + sub.getFeedUrl()
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
                if (entry.getPubTime() == null) {
                    log.debug(
                            "No published date, assigning fake date for "+feedUrl);
                    entry.setPubTime(new Timestamp(cal.getTimeInMillis()));
                }
                if (entry.getPermaLink() == null) {
                    log.warn("No permalink, rejecting entry from "+feedUrl);
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

    protected String getLocalURL() {
        return localURL;
    }

}

