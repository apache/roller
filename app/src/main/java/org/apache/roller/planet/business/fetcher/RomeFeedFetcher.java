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

package org.apache.roller.planet.business.fetcher;

import com.sun.syndication.feed.module.DCModule;
import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.SyndFeedInfo;
import com.sun.syndication.fetcher.impl.DiskFeedInfoCache;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * A FeedFetcher based on the ROME RSS/Atom feed parser (http://rome.dev.java.net).
 */
public class RomeFeedFetcher implements org.apache.roller.planet.business.fetcher.FeedFetcher {
    
    private static Log log = LogFactory.getLog(RomeFeedFetcher.class);
    
    
    public RomeFeedFetcher() {
        // no-op
    }
    
    
    /**
     * @inheritDoc
     */
    public Subscription fetchSubscription(String feedURL) 
            throws FetcherException {
        return fetchSubscription(feedURL, null);
    }
    
    
    /**
     * @inheritDoc
     */
    public Subscription fetchSubscription(String feedURL, Date lastModified) 
            throws FetcherException {
        
        if(feedURL == null) {
            throw new IllegalArgumentException("feed url cannot be null");
        }
        
        // setup Rome feed fetcher
        FeedFetcher feedFetcher = getRomeFetcher();
        
        // fetch the feed
        log.debug("Fetching feed: "+feedURL);
        SyndFeed feed;
        try {
            feed = feedFetcher.retrieveFeed(new URL(feedURL));
        } catch (Exception ex) {
            throw new FetcherException("Error fetching subscription - "+feedURL, ex);
        }
        
        log.debug("Feed pulled, extracting data into Subscription");
        
        // build planet subscription from fetched feed
        Subscription newSub = new Subscription();
        newSub.setFeedURL(feedURL);
        newSub.setSiteURL(feed.getLink());
        newSub.setTitle(feed.getTitle());
        newSub.setAuthor(feed.getAuthor());
        newSub.setLastUpdated(feed.getPublishedDate());
        
        
        // normalize any data that couldn't be properly extracted
        if(newSub.getSiteURL() == null) {
            // set the site url to the feed url then
            newSub.setSiteURL(newSub.getFeedURL());
        }
        if(newSub.getAuthor() == null) {
            // set the author to the title
            newSub.setAuthor(newSub.getTitle());
        }
        if(newSub.getLastUpdated() == null) {
            // no update time specified in feed, so try consulting feed info cache
            FeedFetcherCache feedCache = getRomeFetcherCache();
            try {
                SyndFeedInfo feedInfo = feedCache.getFeedInfo(new URL(newSub.getFeedURL()));
                if(feedInfo.getLastModified() != null) {
                    long lastUpdatedLong = (Long) feedInfo.getLastModified();
                    if (lastUpdatedLong != 0) {
                        newSub.setLastUpdated(new Date(lastUpdatedLong));
                    }
                }
            } catch (MalformedURLException ex) {
                // should never happen since we check this above
            }
        }
        
        // check if feed is unchanged and bail now if so
        if(lastModified != null && newSub.getLastUpdated() != null &&
                !newSub.getLastUpdated().after(lastModified)) {
            return null;
        }
        
        if(log.isDebugEnabled()) {
            log.debug("Subscription is: " + newSub.toString());
        }
        
        
        // some kludge to deal with feeds w/ no entry dates
        // we assign arbitrary dates chronologically by entry starting either
        // from the current time or the last update time of the subscription
        Calendar cal = Calendar.getInstance();
        if (newSub.getLastUpdated() != null) {
            cal.setTime(newSub.getLastUpdated());
        } else {
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1);
        }
        
        // add entries
        List<SyndEntry> feedEntries = feed.getEntries();
        for (SyndEntry feedEntry : feedEntries) {
            SubscriptionEntry newEntry = buildEntry(feedEntry);
            
            // some kludge to handle feeds with no entry dates
            if (newEntry.getPubTime() == null) {
                log.debug("No published date, assigning fake date for "+feedURL);
                newEntry.setPubTime(new Timestamp(cal.getTimeInMillis()));
                cal.add(Calendar.DATE, -1);
            }
            
            if(newEntry != null) {
                newSub.addEntry(newEntry);
            }
        }
        
        log.debug(feedEntries.size()+" entries included");
        
        return newSub;
    }
    
    
    // build a SubscriptionEntry from Rome SyndEntry and SyndFeed
    private SubscriptionEntry buildEntry(SyndEntry romeEntry) {
        
        // if we don't have a permalink then we can't continue
        if(romeEntry.getLink() == null) {
            return null;
        }
        
        SubscriptionEntry newEntry = new SubscriptionEntry();
        
        newEntry.setTitle(romeEntry.getTitle());
        newEntry.setPermalink(romeEntry.getLink());
        
        // Play some games to get the author
        DCModule entrydc = (DCModule)romeEntry.getModule(DCModule.URI);
        if (romeEntry.getAuthor() != null) {
            newEntry.setAuthor(romeEntry.getAuthor());
        } else {
            // use <dc:creator>
            newEntry.setAuthor(entrydc.getCreator());
        }
        
        // Play some games to get the updated date
        if (romeEntry.getUpdatedDate() != null) {
            newEntry.setUpdateTime(new Timestamp(romeEntry.getUpdatedDate().getTime()));
        }
        // TODO: should we set a default update time here?
        
        // And more games getting publish date
        if (romeEntry.getPublishedDate() != null) {
            // use <pubDate>
            newEntry.setPubTime(new Timestamp(romeEntry.getPublishedDate().getTime()));
        } else if (entrydc != null && entrydc.getDate() != null) {
            // use <dc:date>
            newEntry.setPubTime(new Timestamp(entrydc.getDate().getTime()));
        } else {
            newEntry.setPubTime(newEntry.getUpdateTime());
        }
        
        // get content and unescape if it is 'text/plain'
        if (romeEntry.getContents().size() > 0) {
            SyndContent content= (SyndContent)romeEntry.getContents().get(0);
            if (content != null && content.getType().equals("text/plain")) {
                newEntry.setText(StringEscapeUtils.unescapeHtml4(content.getValue()));
            } else if (content != null) {
                newEntry.setText(content.getValue());
            }
        }
        
        // no content, try summary
        if (newEntry.getText() == null || newEntry.getText().trim().length() == 0) {
            if (romeEntry.getDescription() != null) {
                newEntry.setText(romeEntry.getDescription().getValue());
            }
        }
        
        // copy categories
        if (romeEntry.getCategories().size() > 0) {
            List<String> list = new ArrayList<String>();
            for (Object cat : romeEntry.getCategories()) {
                list.add(((SyndCategory) cat).getName());
            }
            newEntry.setCategoriesString(list);
        }
        
        return newEntry;
    }
    
    
    // get a feed fetcher cache, if possible
    private FeedFetcherCache getRomeFetcherCache() {
        
        String cacheDirPath = WebloggerConfig.getProperty("cache.dir");
        
        // can't continue without cache dir
        if (cacheDirPath == null) {
            log.warn("Planet cache directory not set, feeds cannot be cached.");
            return null;
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
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
        } catch (Exception e) {
            log.error("Unable to create planet cache directory: " +
                    ((cacheDir != null) ? cacheDir.getPath() : null), e);
            return null;
        }
        
        // abort if cache dir is not writable
        if (!cacheDir.canWrite()) {
            log.error("Planet cache directory is not writable: " + cacheDir.getPath());
            return null;
        }
        
        return new DiskFeedInfoCache(cacheDirName);
    }


    // get a feed fetcher
    private FeedFetcher getRomeFetcher() {
        
        FeedFetcherCache feedCache = getRomeFetcherCache();
        
        FeedFetcher feedFetcher;
        if(feedCache != null) {
            feedFetcher = new HttpURLFeedFetcher(feedCache);
        } else {
            feedFetcher = new HttpURLFeedFetcher();
        }
        
        // set options
        feedFetcher.setUsingDeltaEncoding(false);
        feedFetcher.setUserAgent("RollerPlanetAggregator");
        
        return feedFetcher;
    }
    
}
