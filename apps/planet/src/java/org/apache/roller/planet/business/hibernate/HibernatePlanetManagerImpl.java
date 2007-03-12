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

package org.apache.roller.planet.business.hibernate;

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
import org.apache.roller.planet.business.AbstractManagerImpl;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.planet.util.rome.DiskFeedInfoCache;


/**
 * Hibernate implementation of the PlanetManager.
 */
public class HibernatePlanetManagerImpl extends AbstractManagerImpl
        implements PlanetManager {
    
    private static Log log = LogFactory.getLog(HibernatePlanetManagerImpl.class);
    
    protected static final String NO_GROUP = "zzz_nogroup_zzz";
    
    private HibernatePersistenceStrategy strategy = null;
    private Map lastUpdatedByGroup = new HashMap();
    
    
    public HibernatePlanetManagerImpl(HibernatePersistenceStrategy strat) {        
        this.strategy = strat;
    }
    
    
    // save a Planet
    public void savePlanet(PlanetData planet) throws RollerException {
        strategy.store(planet);
    }
    
    
    // delete a Planet
    public void deletePlanet(PlanetData planet) 
        throws RollerException {
        strategy.remove(planet);
    }
    
    
    // lookup Planet by handle
    public PlanetData getPlanet(String handle) throws RollerException {
        PlanetData planet = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetData.class);
            criteria.add(Expression.ilike("handle", handle));
            criteria.setMaxResults(1);
            planet = (PlanetData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return planet;
    }
    
    
    // lookup Planet by id
    public PlanetData getPlanetById(String id) throws RollerException {
        return (PlanetData) strategy.load(id, PlanetData.class);
    }
    
    
    // lookup all Planets
    public List getPlanets() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetData.class);
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    // save a Group
    public void saveGroup(PlanetGroupData group)  throws RollerException {
        if (group.getId() == null || getGroupById(group.getId()) == null) {
            // If new group, make sure hadnle is unique within Planet
            if (getGroup(group.getPlanet(), group.getHandle()) != null) {
                throw new RollerException("ERROR group handle already exists in Planet");
            }
        }
        strategy.store(group);
    }
        
    
    // delete a Group
    public void deleteGroup(PlanetGroupData group) 
        throws RollerException {
        strategy.remove(group);
    }
    
    
    // TODO: remove method
    public PlanetGroupData getGroup(String handle) throws RollerException {
        return getGroup(null, handle);
    }
    
    
    // lookup a Group by Planet & handle
    public PlanetGroupData getGroup(PlanetData planet, String handle) throws RollerException {
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            criteria.setMaxResults(1);
            criteria.add(Expression.eq("handle", handle));
            if(planet != null) {
                criteria.add(Expression.eq("planet", planet));
            }
            return (PlanetGroupData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    // lookup a Planet by id
    public PlanetGroupData getGroupById(String id) throws RollerException {
        return (PlanetGroupData) strategy.load(id, PlanetGroupData.class);
    }
        
    
    // lookup all Groups
    // TODO: remove method
    public List getGroups() throws RollerException {
        return getGroups(null);
    }
    
    
    // lookup all Groups by Planet
    public List getGroups(PlanetData planet) throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetGroupData.class);
            if(planet != null) {
                criteria.add(Expression.eq("planet", planet));
            }
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    // TODO: remove method
    public List getGroupHandles() throws RollerException {
        return getGroupHandles(null);
    }
    
    
    // TODO: remove method
    public List getGroupHandles(PlanetData planet) throws RollerException {
        List handles = new ArrayList();
        Iterator list = getGroups(planet).iterator();
        while (list.hasNext()) {
            PlanetGroupData group = (PlanetGroupData)list.next();
            handles.add(group.getHandle());
        }
        return handles;
    }
    
    
    // save a Subscription
    public void saveSubscription(PlanetSubscriptionData sub) 
        throws RollerException {
        PlanetSubscriptionData existing = getSubscription(sub.getFeedURL());
        if (existing == null || (existing.getId().equals(sub.getId()))) {
            this.strategy.store(sub);
        } else {
            throw new RollerException("ERROR: duplicate feed URLs not allowed");
        }
    }
    
    
    // delete a Subscription
    public void deleteSubscription(PlanetSubscriptionData sub) 
        throws RollerException {
        strategy.remove(sub);
    }
    
    
    // lookup a Subscription by feed url
    public PlanetSubscriptionData getSubscription(String feedURL) 
        throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.setMaxResults(1);
            criteria.add(Expression.eq("feedURL", feedURL));
            List list = criteria.list();
            return list.size()!=0 ? (PlanetSubscriptionData)list.get(0) : null;
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    // lookup a Subscription by id
    public PlanetSubscriptionData getSubscriptionById(String id) 
        throws RollerException {
        return (PlanetSubscriptionData) strategy.load(id, PlanetSubscriptionData.class);
    }
    
    
    // lookup all Subscriptions
    // TODO: return List, not Iterator
    // TODO: make pageable
    public Iterator getAllSubscriptions() {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria =
                    session.createCriteria(PlanetSubscriptionData.class);
            criteria.addOrder(Order.asc("feedURL"));
            List list = criteria.list();
            return list.iterator();
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription collection", e);
        }
    }
    
    
    // get subscriptions count
    public int getSubscriptionCount() throws RollerException {
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Integer count = (Integer)session.createQuery(
                    "select count(*) from org.apache.roller.planet.pojos.PlanetSubscriptionData").uniqueResult();
            return count.intValue();
        } catch (Throwable e) {
            throw new RuntimeException(
                    "ERROR fetching subscription count", e);
        }
    }
    
    
    // get popular subscriptions
    public synchronized List getTopSubscriptions(int offset, int length) 
            throws RollerException {
        
        return getTopSubscriptions(null, offset, length);
    }
    
    
    // get popular subscriptions
    public synchronized List getTopSubscriptions(String groupHandle, int offset, int length) 
            throws RollerException {
        
        List ret = null;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Query query = null;
            if (groupHandle != null) {
                query = session.createQuery(
                    "select sub from org.apache.roller.planet.pojos.PlanetSubscriptionData sub "
                    +"join sub.groups group "
                    +"where "
                    +"group.handle=:groupHandle "
                    +"order by sub.inboundblogs desc");
                query.setString("groupHandle", groupHandle);
            } else {
                query = session.createQuery(
                    "select sub from org.apache.roller.planet.pojos.PlanetSubscriptionData sub "
                    +"order by sub.inboundblogs desc");
            }
            if (offset != 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            ret = query.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        return ret;
    }
    
    
    // save an Entry
    public void saveEntry(PlanetEntryData entry) 
        throws RollerException {
        strategy.store(entry);
    }
    
    
    // delete an Entry
    public void deleteEntry(PlanetEntryData entry) 
        throws RollerException {
        strategy.remove(entry);
    }
    
    
    // delete all Entries from a Subscription
    public void deleteEntries(PlanetSubscriptionData sub) 
        throws RollerException {
        Iterator entries = sub.getEntries().iterator();
        while(entries.hasNext()) {
            strategy.remove(entries.next());
        }
        
        // make sure and clear the other side of the assocation
        sub.getEntries().clear();
    }
    
    
    // TODO: remove method
    public List getFeedEntries(String feedURL, int offset, int length)
            throws RollerException {
            
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(PlanetEntryData.class);
            criteria.createAlias("subscription", "sub");
            criteria.add(Expression.eq("sub.feedURL", feedURL));
            criteria.addOrder(Order.desc("pubTime"));
            criteria.setFirstResult(offset);
            if (length != -1) criteria.setMaxResults(length);
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    // TODO: rename method getEntries()
    public synchronized List getAggregation(int offset, int len)
            throws RollerException {
        return getAggregation(null, null, null, offset, len);
    }
    
    
    // TODO: rename method getEntries()
    public synchronized List getAggregation(Date startDate, Date endDate, int offset, int len)
            throws RollerException {
        return getAggregation(null, startDate, endDate, offset, len);
    }
    
    
    // TODO: rename method getEntries()
    public synchronized List getAggregation(PlanetGroupData group, int offset, int len) 
        throws RollerException {
        return getAggregation(group, null, null, offset, len);
    } 
    
    
    // TODO: rename method getEntries()
    public synchronized List getAggregation(
        PlanetGroupData group, Date startDate, Date endDate, int offset, int length)
        throws RollerException {
        // TODO: ATLAS getAggregation DONE TESTED
        List ret = null;
        if (endDate == null) endDate = new Date();
        try {
            String groupHandle = (group == null) ? NO_GROUP : group.getHandle();
            long startTime = System.currentTimeMillis();
            Session session =
                    ((HibernatePersistenceStrategy)strategy).getSession();
            
            if (group != null) {
                StringBuffer sb = new StringBuffer();
                sb.append("select e from org.apache.roller.planet.pojos.PlanetEntryData e ");
                sb.append("join e.subscription.groups g ");
                sb.append("where g.handle=:groupHandle and e.pubTime < :endDate ");
                if (startDate != null) {
                    sb.append("and e.pubTime > :startDate ");
                }
                sb.append("order by e.pubTime desc");
                Query query = session.createQuery(sb.toString());
                query.setParameter("groupHandle", group.getHandle());
                query.setFirstResult(offset);
                if (length != -1) query.setMaxResults(length);
                query.setParameter("endDate", endDate);
                if (startDate != null) {
                    query.setParameter("startDate", startDate);
                }
                ret = query.list();
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append("select e from org.apache.roller.planet.pojos.PlanetEntryData e ");
                sb.append("join e.subscription.groups g ");
                sb.append("where (g.handle='external' or g.handle='all') ");
                sb.append("and e.pubTime < :endDate ");
                if (startDate != null) {
                    sb.append("and e.pubTime > :startDate ");
                }
                sb.append("order by e.pubTime desc");
                Query query = session.createQuery(sb.toString());
                query.setFirstResult(offset);
                if (length != -1) query.setMaxResults(length);
                query.setParameter("endDate", endDate);
                if (startDate != null) {
                    query.setParameter("startDate", startDate);
                }
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
            log.debug("Generated aggregation in "
                    +((endTime-startTime)/1000.0)+" seconds");
            
        } catch (Throwable e) {
            log.error("ERROR: building aggregation for: "+group.getHandle(), e);
            throw new RollerException(e);
        }
        return ret;
    } 
    
    
    // TODO: is this needed?
    public synchronized void clearCachedAggregations() {
        lastUpdatedByGroup.clear();
    }
    
    
    // TODO: is this needed?
    public Date getLastUpdated() {
        return (Date)lastUpdatedByGroup.get(NO_GROUP);
    }
    
    
    // TODO: is this needed?
    public Date getLastUpdated(PlanetGroupData group) {
        return (Date)lastUpdatedByGroup.get(group);
    }
    
    
    // refresh Entries for all Subscriptions
    public void refreshEntries(String cacheDirPath) throws RollerException {
        
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
                this.deleteEntries(sub);
                sub.addEntries(newEntries);
                this.saveSubscription(sub);
                this.strategy.flush();
            }
            long subEndTime = System.currentTimeMillis();
            log.debug("   " + count + " - "
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
    
    
    // get new Entries for a specific Subscription
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
