package org.roller.business;

import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.PlanetManager;
import org.roller.model.Roller;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetEntryData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;
import org.roller.util.rome.DiskFeedInfoCache;
import org.roller.util.LRUCache2;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;

/**
 * Base class for PlanetManager implementations.
 * @author Dave Johnson
 */
public abstract class PlanetManagerImpl implements PlanetManager
{
    protected Roller roller = null;
    protected PersistenceStrategy strategy;
    
    // Cache up to 20 aggregations, each for up to 30 minutes
    // TODO: make this aggregation cache configurable
    //protected LRUCache2 aggregationsByGroup = 
        //new LRUCache2(20, 30 * 60 * 1000);
    
    // Cache up to 20 aggregations, each for up to 30 minutes
    // TODO: make this top-subscriptions cache configurable
    //protected LRUCache2 topSubscriptionsByGroup = 
        //new LRUCache2(20, 30 * 60 * 1000);

    private static Log logger =
        LogFactory.getFactory().getInstance(PlanetManagerImpl.class);
        
    public PlanetManagerImpl()
    {   
    }

    public PlanetManagerImpl(PersistenceStrategy strategy, Roller roller)
    {
        this.strategy = strategy;
        this.roller = roller;    
    }
    
    public void refreshEntries() throws RollerException
    {
        Date now = new Date();
        long startTime = System.currentTimeMillis();
        PlanetConfigData config = getConfiguration();
        if (config == null || config.getCacheDir() == null)
        {
            logger.warn("Planet cache directory not set, aborting refresh");
            return;
        }
        FeedFetcherCache feedInfoCache = 
                new DiskFeedInfoCache(config.getCacheDir());
               
        if (config.getProxyHost()!=null && config.getProxyPort() > 0)
        {
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
        while (subs.hasNext())
        {
            long subStartTime = System.currentTimeMillis();           
            
            // Fetch latest entries for each subscription
            Set newEntries = new TreeSet();
            PlanetSubscriptionData sub = (PlanetSubscriptionData)subs.next();
            SyndFeed feed = null;
            URL feedUrl = null;
            Date lastUpdated = now;
            try
            {
                feedUrl = new URL(sub.getFeedUrl());
                logger.debug("Get feed from cache "+sub.getFeedUrl());
                feed = feedFetcher.retrieveFeed(feedUrl);
                SyndFeedInfo feedInfo = feedInfoCache.getFeedInfo(feedUrl);
                if (feedInfo.getLastModified() != null) 
                {
                    long lastUpdatedLong = 
                        ((Long)feedInfo.getLastModified()).longValue();
                    if (lastUpdatedLong != 0)
                    {
                        lastUpdated = new Date(lastUpdatedLong);  
                    }
                }
                Thread.sleep(100); // be nice
            }
            catch (Exception e)
            {
                logger.warn("ERROR parsing " + sub.getFeedUrl() 
                    + " : " + e.getClass().getName() + " : " + e.getMessage());
                logger.debug(e);
                continue;
            }
            if (lastUpdated!=null && sub.getLastUpdated()!=null)
            {
                Calendar feedCal = Calendar.getInstance();
                feedCal.setTime(lastUpdated);
                
                Calendar subCal = Calendar.getInstance();
                subCal.setTime(sub.getLastUpdated());
                
                if (!feedCal.after(subCal)) 
                {
                    if (logger.isDebugEnabled())
                    {
                        String msg = MessageFormat.format(
                            "   Skipping ({0} / {1})",
                            new Object[] {
                               lastUpdated, sub.getLastUpdated()});
                       logger.debug(msg);
                    }
                    continue;
                }
            }
            if (feed.getPublishedDate() != null)
            {
                sub.setLastUpdated(feed.getPublishedDate());
                saveSubscription(sub);
            }
            
            // Kludge for Feeds without entry dates: most recent entry is given
            // feed's last publish date (or yesterday if none exists) and earler
            // entries are placed at once day intervals before that.
            Calendar cal = Calendar.getInstance();
            if (sub.getLastUpdated() != null)
            {
                cal.setTime(sub.getLastUpdated());
            }
            else
            {
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1);
            }

            // Populate subscription object with new entries
            int count = 0;
            Iterator entries = feed.getEntries().iterator();
            while (entries.hasNext())
            {
                try 
                {
                    SyndEntry romeEntry = (SyndEntry) entries.next();
                    PlanetEntryData entry = 
                            new PlanetEntryData(feed, romeEntry, sub);
                    if (entry.getPublished() == null)
                    {                    
                        logger.debug(
                         "No published date, assigning fake date for "+feedUrl);
                        entry.setPublished(cal.getTime());
                    }
                    if (entry.getPermalink() == null)
                    {
                     logger.warn("No permalink, rejecting entry from "+feedUrl);
                    }
                    else 
                    {
                        saveEntry(entry);
                        newEntries.add(entry);
                    }
                    cal.add(Calendar.DATE, -1);
                    count++;
                } 
                catch (Exception e)
                {
                    logger.error("ERROR processing subscription entry", e);
                }
            }
            logger.debug("   Entry count: " + count);
            if (count > 0) 
            {
                Iterator entryIter = sub.getEntries().iterator();
                while (entryIter.hasNext())
                {
                    deleteEntry((PlanetEntryData)entryIter.next());
                }
                sub.purgeEntries();
                sub.addEntries(newEntries); 
                if (roller != null) roller.commit();
            }
            long subEndTime = System.currentTimeMillis();  
            logger.info("   " + count + " - " 
                    + ((subEndTime-subStartTime)/1000.0) 
                    + " seconds to process (" + count + ") entries of " 
                    + sub.getFeedUrl());
        }
        // Clear the aggregation cache
        clearCachedAggregations();
        
        long endTime = System.currentTimeMillis();
        logger.info("--- DONE --- Refreshed entries in " 
                + ((endTime-startTime)/1000.0) + " seconds");
    }

}
