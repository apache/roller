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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rometools.fetcher.impl.DiskFeedInfoCache;
import com.rometools.fetcher.impl.FeedFetcherCache;
import com.rometools.fetcher.impl.HttpURLFeedFetcher;
import com.rometools.fetcher.impl.SyndFeedInfo;
import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;

public class FeedProcessorImpl implements FeedProcessor {
    
    private static Log log = LogFactory.getLog(FeedProcessorImpl.class);
    
    public FeedProcessorImpl() {
        // no-op
    }

    /**
     * @inheritDoc
     */
    public Subscription fetchSubscription(String feedURL)
            throws WebloggerException {
        return fetchSubscription(feedURL, null);
    }

    /**
     * @inheritDoc
     */
    @Override
    public Subscription fetchSubscription(String feedURL, Date lastModified)
            throws WebloggerException {

        if(feedURL == null) {
            throw new IllegalArgumentException("feed url cannot be null");
        }

        // we handle special weblogger planet integrated subscriptions which have
        // feedURLs defined as ... weblogger:<blog handle>
        if(feedURL.startsWith("weblogger:")) {
            log.debug("Feed is a local blog, handling via API - "+feedURL);
            return fetchWebloggerSubscription(feedURL, lastModified);
        }

        // setup Rome feed fetcher
        com.rometools.fetcher.FeedFetcher feedFetcher = getRomeFetcher();

        // fetch the feed
        log.debug("Fetching feed: "+feedURL);
        SyndFeed feed;
        try {
            feed = feedFetcher.retrieveFeed(new URL(feedURL));
        } catch (Exception ex) {
            throw new WebloggerException("Error fetching subscription - "+feedURL, ex);
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

    /**
     * Fetch local feeds directly from Weblogger so we don't waste time with lots of
     * feed processing.
     * We expect local feeds to have urls of the style ... weblogger:<blog handle>
     */
    private Subscription fetchWebloggerSubscription(String feedURL, Date lastModified)
            throws WebloggerException {

        // extract blog handle from our special feed url
        String weblogHandle = null;
        String[] items = feedURL.split(":", 2);
        if(items != null && items.length > 1) {
            weblogHandle = items[1];
        }
        
        log.debug("Handling LOCAL feed - "+feedURL);
        
        Weblog localWeblog;
        try {
            localWeblog = WebloggerFactory.getWeblogger().getWeblogManager()
                    .getWeblogByHandle(weblogHandle);
            if (localWeblog == null) {
                throw new WebloggerException("Local feed - "+feedURL+" no longer exists in weblogger");
            }
            
        } catch (WebloggerException ex) {
            throw new WebloggerException("Problem looking up local weblog - "+weblogHandle, ex);
        }
        
        // if weblog hasn't changed since last fetch then bail
        if(lastModified != null && !localWeblog.getLastModified().after(lastModified)) {
            log.debug("Skipping unmodified LOCAL weblog");
            return null;
        }
        
        // build planet subscription from weblog
        Subscription newSub = new Subscription();
        newSub.setFeedURL(feedURL);
        newSub.setSiteURL(WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(localWeblog, null, true));
        newSub.setTitle(localWeblog.getName());
        newSub.setAuthor(localWeblog.getName());
        newSub.setLastUpdated(localWeblog.getLastModified());
        
        // must have a last updated time
        if(newSub.getLastUpdated() == null) {
            newSub.setLastUpdated(new Date());
        }
        
        // lookup recent entries from weblog and add them to the subscription
        try {
            int entryCount = WebloggerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");

            if (log.isDebugEnabled()) {
                log.debug("Seeking up to " + entryCount + " entries from " + localWeblog.getHandle());
            }
            
            // grab recent entries for this weblog
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(localWeblog);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setMaxResults(entryCount);
            List<WeblogEntry> entries = wmgr.getWeblogEntries(wesc);
            log.debug("Found " + entries.size());

            // Populate subscription object with new entries
            PluginManager ppmgr = WebloggerFactory.getWeblogger().getPluginManager();
            Map pagePlugins = ppmgr.getWeblogEntryPlugins(localWeblog);
            for ( WeblogEntry rollerEntry : entries ) {
                SubscriptionEntry entry = new SubscriptionEntry();
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
                entry.setCategoriesString(rollerEntry.getCategory().getName());
                
                newSub.addEntry(entry);
            }
            
        } catch (WebloggerException ex) {
            throw new WebloggerException("Error processing entries for local weblog - "+weblogHandle, ex);
        }
        
        // all done
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
        if (StringUtils.isBlank(newEntry.getText()) && romeEntry.getDescription() != null)  {
            newEntry.setText(romeEntry.getDescription().getValue());
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

        String cacheDirPath = WebloggerConfig.getProperty("planet.aggregator.cache.dir");

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
    private com.rometools.fetcher.FeedFetcher getRomeFetcher() {

        FeedFetcherCache feedCache = getRomeFetcherCache();

        com.rometools.fetcher.FeedFetcher feedFetcher;
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

    /**
     * @inheritDoc
     */
    private void updateSubscription(Subscription sub) throws WebloggerException {

        if (sub == null) {
            throw new IllegalArgumentException("cannot update null subscription");
        }

        log.debug("updating feed: "+sub.getFeedURL());

        long subStartTime = System.currentTimeMillis();

        Subscription updatedSub;
        try {
            // fetch the latest version of the subscription
            log.debug("Getting fetcher");
            FeedProcessor fetcher = WebloggerFactory.getWeblogger().getFeedFetcher();
            log.debug("Using fetcher class: " + fetcher.getClass().getName());
            updatedSub = fetcher.fetchSubscription(sub.getFeedURL(), sub.getLastUpdated());

        } catch (WebloggerException ex) {
            throw new WebloggerException("Error fetching updated subscription", ex);
        }

        log.debug("Got updatedSub = " + updatedSub);

        // if sub was unchanged then we are done
        if (updatedSub == null) {
            return;
        }

        // if this subscription hasn't changed since last update then we're done
        if (sub.getLastUpdated() != null && updatedSub.getLastUpdated() != null &&
                !updatedSub.getLastUpdated().after(sub.getLastUpdated())) {
            log.debug("Skipping update, feed hasn't changed - "+sub.getFeedURL());
        }

        // update subscription attributes
        sub.setSiteURL(updatedSub.getSiteURL());
        sub.setTitle(updatedSub.getTitle());
        sub.setAuthor(updatedSub.getAuthor());
        sub.setLastUpdated(updatedSub.getLastUpdated());

        // update subscription entries
        int entries = 0;
        Set<SubscriptionEntry> newEntries = updatedSub.getEntries();
        log.debug("newEntries.size() = " + newEntries.size());
        if (newEntries.size() > 0) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();

                // clear out old entries
                pmgr.deleteEntries(sub);

                // add fresh entries
                sub.getEntries().clear();
                sub.addEntries(newEntries);

                // save and flush
                pmgr.saveSubscription(sub);
                WebloggerFactory.getWeblogger().flush();

                log.debug("Added entries");
                entries += newEntries.size();

            } catch(WebloggerException ex) {
                throw new WebloggerException("Error persisting updated subscription", ex);
            }
        }

        long subEndTime = System.currentTimeMillis();
        log.debug("updated feed -- "+sub.getFeedURL()+" -- in " +
                ((subEndTime-subStartTime) / DateUtils.MILLIS_PER_SECOND) + " seconds.  " + entries +
                " entries updated.");
    }

    /**
     * @inheritDoc
     */
    public void updateSubscriptions(Planet group) throws WebloggerException {

        if(group == null) {
            throw new IllegalArgumentException("cannot update null group");
        }

        updateProxySettings();

        log.debug("--- BEGIN --- Updating subscriptions in group = "+group.getHandle());

        long startTime = System.currentTimeMillis();

        updateSubscriptions(group.getSubscriptions());

        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Updated subscriptions in "
                + ((endTime-startTime) / DateUtils.MILLIS_PER_SECOND) + " seconds");
    }

    public void updateSubscriptions(Collection<Subscription> subscriptions) {
        updateProxySettings();

        PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
        for (Subscription sub : subscriptions) {
            try {
                // reattach sub.  sub gets detached as we iterate
                sub = pmgr.getSubscriptionById(sub.getId());
            } catch (WebloggerException ex) {
                log.warn("Subscription went missing while doing update: "+ex.getMessage());
            }

            // this updates and saves
            try {
                updateSubscription(sub);
            } catch(WebloggerException ex) {
                // do a little work to get at the source of the problem
                Throwable cause = ex;
                if(ex.getRootCause() != null) {
                    cause = ex.getRootCause();
                }
                if(cause.getCause() != null) {
                    cause = cause.getCause();
                }

                if (log.isDebugEnabled()) {
                    log.debug("Error updating subscription - "+sub.getFeedURL(), cause);
                } else {
                    log.warn("Error updating subscription - "+sub.getFeedURL()
                            + " turn on debug logging for more info");
                }

            } catch(Exception ex) {
                if (log.isDebugEnabled()) {
                    log.warn("Error updating subscription - "+sub.getFeedURL(), ex);
                } else {
                    log.warn("Error updating subscription - "+sub.getFeedURL()
                            + " turn on debug logging for more info");
                }
            }
        }
    }


    // upate proxy settings for jvm based on planet configuration
    private void updateProxySettings() {
        String proxyHost = WebloggerRuntimeConfig.getProperty("planet.site.proxyhost");
        int proxyPort = WebloggerRuntimeConfig.getIntProperty("planet.site.proxyport");
        if (proxyHost != null && proxyPort > 0) {
            System.setProperty("proxySet", "true");
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", Integer.toString(proxyPort));
        }
        /** a hack to set 15 sec timeouts for java.net.HttpURLConnection */
        System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
        System.setProperty("sun.net.client.defaultReadTimeout", "15000");
    }

}
