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

import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.io.EmptyInputStream;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FeedManagerImpl implements FeedManager {

    private WeblogManager weblogManager;
    private WeblogEntryManager weblogEntryManager;
    private PlanetManager planetManager;
    private URLStrategy urlStrategy;
    private JPAPersistenceStrategy strategy;
    private PropertiesManager propertiesManager;

    private static Logger log = LoggerFactory.getLogger(FeedManagerImpl.class);

    public FeedManagerImpl() {
    }

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    public void setStrategy(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Override
    public Subscription fetchSubscription(String feedURL) {
        return fetchSubscription(feedURL, null);
    }

    @Override
    public Subscription fetchSubscription(String feedURL, Instant lastModified) {

        if (feedURL == null) {
            throw new IllegalArgumentException("feed url cannot be null");
        }

        // we handle special weblogger planet integrated subscriptions which have
        // feedURLs defined as ... weblogger:<blog handle>
        if (feedURL.startsWith("weblogger:")) {
            log.debug("Feed is a local blog, handling via API - {}", feedURL);
            return fetchWebloggerSubscription(feedURL, lastModified);
        }

        // fetch the feed
        log.debug("Fetching feed: " + feedURL);
        SyndFeed feed;
        try (CloseableHttpClient client = HttpClients.createMinimal()) {
            HttpUriRequest method = new HttpGet(feedURL);
            method.setHeader("User-Agent", "TightBlogPlanetAggregator");
            try (CloseableHttpResponse response = client.execute(method);
                 InputStream stream = response.getEntity().getContent()) {
                if (!(stream instanceof EmptyInputStream)) {
                    SyndFeedInput input = new SyndFeedInput();
                    feed = input.build(new XmlReader(stream));
                } else {
                    log.info("Feed: {} returned an EmptyInputStream. Status Code: {}, Response Text: ",
                            feedURL, response.getStatusLine().getStatusCode(),
                            response.getStatusLine().getReasonPhrase());
                    return null;
                }
            }
        } catch (FeedException | IOException ex) {
            log.warn("Error fetching subscription - {}", feedURL, ex);
            return null;
        }

        log.debug("Feed pulled, extracting data into Subscription");

        // build planet subscription from fetched feed
        Subscription newSub = new Subscription();
        newSub.setFeedURL(feedURL);
        newSub.setSiteURL(feed.getLink());
        newSub.setTitle(feed.getTitle());
        newSub.setLastUpdated(feed.getPublishedDate().toInstant());

        // check if feed is unchanged and bail now if so
        if (lastModified != null && newSub.getLastUpdated() != null &&
                !newSub.getLastUpdated().isAfter(lastModified)) {
            return null;
        }

        log.debug("Subscription is: {}", newSub.toString());

        // add entries
        List<SyndEntry> feedEntries = feed.getEntries();
        int numIncluded = 0;
        for (SyndEntry feedEntry : feedEntries) {
            SubscriptionEntry newEntry = buildEntry(feedEntry);

            // some kludge to handle feeds with no entry dates
            if (newEntry != null) {
                if (newEntry.getPubTime() == null) {
                    log.debug("No published date, assigning today's date for {}", feedURL);
                    newEntry.setPubTime(Instant.now());
                }
                newSub.addEntry(newEntry);
                numIncluded++;
            }
        }
        log.debug(numIncluded + " entries included");
        return newSub;
    }

    /**
     * Fetch local feeds directly from Weblogger so we don't waste time with lots of
     * feed processing.
     * We expect local feeds to have urls of the style ... weblogger:<blog handle>
     */
    private Subscription fetchWebloggerSubscription(String feedURL, Instant lastModified) {

        // extract blog handle from our special feed url
        String weblogHandle = null;
        String[] items = feedURL.split(":", 2);
        if (items.length > 1) {
            weblogHandle = items[1];
        }

        log.debug("Handling LOCAL feed - {}", feedURL);

        Weblog localWeblog;
        localWeblog = weblogManager.getWeblogByHandle(weblogHandle);
        if (localWeblog == null) {
            log.info("Skipping feed {}, blog no longer exists", feedURL);
            return null;
        }

        // if weblog hasn't changed since last fetch then bail
        if (lastModified != null && !localWeblog.getLastModified().isAfter(lastModified)) {
            log.debug("Skipping unmodified local blog {}", feedURL);
            return null;
        }

        // build planet subscription from weblog
        Subscription newSub = new Subscription();
        newSub.setFeedURL(feedURL);
        newSub.setSiteURL(urlStrategy.getWeblogURL(localWeblog, true));
        newSub.setTitle(localWeblog.getName());
        newSub.setLastUpdated(localWeblog.getLastModified());

        // must have a last updated time
        if (newSub.getLastUpdated() == null) {
            newSub.setLastUpdated(Instant.now());
        }

        // lookup recent entries from weblog and add them to the subscription
        int entryCount = propertiesManager.getIntProperty("site.newsfeeds.maxEntries");

        if (log.isDebugEnabled()) {
            log.debug("Seeking up to {} entries from {}", entryCount, localWeblog.getHandle());
        }

        // grab recent entries for this weblog
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(localWeblog);
        wesc.setStatus(PubStatus.PUBLISHED);
        wesc.setMaxResults(entryCount);
        List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);
        log.debug("Found {}", entries.size());

        // Populate subscription object with new entries
        for (WeblogEntry blogEntry : entries) {
            SubscriptionEntry entry = new SubscriptionEntry();
            String content;
            if (!StringUtils.isEmpty(blogEntry.getText())) {
                content = blogEntry.getText();
            } else {
                content = blogEntry.getSummary();
            }
            content = weblogEntryManager.processBlogText(blogEntry, content);

            entry.setAuthor(blogEntry.getCreator().getScreenName());
            entry.setTitle(blogEntry.getTitle());
            entry.setPubTime(blogEntry.getPubTime());
            entry.setContent(content);
            entry.setPermalink(blogEntry.getPermalink());
            entry.setUri(blogEntry.getPermalink());
            entry.setCategoriesString(blogEntry.getCategory().getName());
            entry.setUploaded(Instant.now());
            newSub.addEntry(entry);
        }

        // all done
        return newSub;
    }

    // build a SubscriptionEntry from Rome SyndEntry and SyndFeed
    private SubscriptionEntry buildEntry(SyndEntry romeEntry) {

        // link and uri's are required
        if (romeEntry.getLink() == null || romeEntry.getUri() == null) {
            return null;
        }

        // max 255 length for links
        if (romeEntry.getLink().length() > 255 || romeEntry.getUri().length() > 255) {
            return null;
        }

        SubscriptionEntry newEntry = new SubscriptionEntry();

        newEntry.setTitle(romeEntry.getTitle());
        newEntry.setPermalink(romeEntry.getLink());
        newEntry.setUri(romeEntry.getUri());

        // Play some games to get the author
        DCModule entrydc = (DCModule) romeEntry.getModule(DCModule.URI);
        if (romeEntry.getAuthor() != null) {
            newEntry.setAuthor(romeEntry.getAuthor());
        } else {
            // use <dc:creator>
            newEntry.setAuthor(entrydc.getCreator());
        }

        // Play some games to get the updated date
        if (romeEntry.getUpdatedDate() != null) {
            newEntry.setUpdateTime(romeEntry.getUpdatedDate().toInstant());
        }

        // And more games getting publish date
        if (romeEntry.getPublishedDate() != null) {
            // use <pubDate>
            newEntry.setPubTime(romeEntry.getPublishedDate().toInstant());
        } else if (entrydc != null && entrydc.getDate() != null) {
            // use <dc:date>
            newEntry.setPubTime(entrydc.getDate().toInstant());
        } else {
            newEntry.setPubTime(newEntry.getUpdateTime());
        }

        // get content and unescape if it is 'text/plain'
        if (romeEntry.getContents().size() > 0) {
            SyndContent content = romeEntry.getContents().get(0);
            if (content != null && content.getType().equals("text/plain")) {
                newEntry.setContent(StringEscapeUtils.unescapeHtml4(content.getValue()));
            } else if (content != null) {
                newEntry.setContent(content.getValue());
            }
        }

        // no content, try summary
        if (StringUtils.isBlank(newEntry.getContent()) && romeEntry.getDescription() != null) {
            newEntry.setContent(romeEntry.getDescription().getValue());
        }

        // copy categories
        if (romeEntry.getCategories().size() > 0) {
            List<String> list = new ArrayList<>();
            for (Object cat : romeEntry.getCategories()) {
                list.add(((SyndCategory) cat).getName());
            }
            newEntry.setCategoriesString(list);
        }
        newEntry.setUploaded(Instant.now());
        return newEntry;
    }

    private void updateSubscription(Subscription sub) {

        if (sub == null) {
            throw new IllegalArgumentException("cannot update null subscription");
        }

        log.debug("updating feed: " + sub.getFeedURL());
        long subStartTime = System.currentTimeMillis();

        Subscription updatedSub = fetchSubscription(sub.getFeedURL(), sub.getLastUpdated());
        log.debug("Got updatedSub = {}", updatedSub);

        // if sub was unchanged then we are done
        if (updatedSub == null) {
            return;
        }

        // if this subscription hasn't changed since last update then we're done
        if (sub.getLastUpdated() != null && updatedSub.getLastUpdated() != null &&
                !updatedSub.getLastUpdated().isAfter(sub.getLastUpdated())) {
            log.debug("Skipping update, feed hasn't changed - {}", sub.getFeedURL());
        }

        // update subscription attributes
        sub.setSiteURL(updatedSub.getSiteURL());
        sub.setTitle(updatedSub.getTitle());
        sub.setLastUpdated(updatedSub.getLastUpdated());

        // update subscription entries
        int entries = 0;
        Set<SubscriptionEntry> newEntries = updatedSub.getEntries();
        log.debug("newEntries.size() = {}", newEntries.size());
        if (newEntries.size() > 0) {

            // add fresh entries
            sub.getEntries().clear();
            sub.addEntries(newEntries);

            // save and flush
            planetManager.saveSubscription(sub);
            strategy.flush();

            log.debug("Added entries");
            entries += newEntries.size();
        }

        long subEndTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("updated feed -- {} -- in {} seconds.  {} entries updated.",
                    sub.getFeedURL(), (subEndTime - subStartTime) / DateUtils.MILLIS_PER_SECOND, entries);
        }
    }

    @Override
    public void updateSubscriptions(Planet group) {
        if (group == null) {
            throw new IllegalArgumentException("cannot update null group");
        }

        log.debug("--- BEGIN --- Updating subscriptions in group = {}", group.getHandle());
        long startTime = System.currentTimeMillis();

        updateSubscriptions(group.getSubscriptions());

        if (log.isInfoEnabled()) {
            long endTime = System.currentTimeMillis();
            log.info("--- DONE --- Updated subscriptions in {} seconds",
                    (endTime - startTime) / DateUtils.MILLIS_PER_SECOND);
        }
    }

    @Override
    public void updateAllSubscriptions() {
        log.debug("--- BEGIN --- Updating all subscriptions");
        long startTime = System.currentTimeMillis();

        List<Subscription> subs = planetManager.getSubscriptions();
        updateSubscriptions(subs);
        long endTime = System.currentTimeMillis();
        log.info("--- DONE --- Updated subscriptions in {} seconds",
                (endTime - startTime) / DateUtils.MILLIS_PER_SECOND);
    }

    private void updateSubscriptions(Collection<Subscription> subscriptions) {
        for (Subscription sub : subscriptions) {
            // reattach sub.  sub gets detached as we iterate
            sub = planetManager.getSubscription(sub.getId());

            // this updates and saves
            if (sub != null) {
                try {
                    updateSubscription(sub);
                } catch (Exception ex) {
                    if (log.isDebugEnabled()) {
                        log.warn("Error updating subscription - {}", sub.getFeedURL(), ex);
                    } else {
                        log.warn("Error updating subscription - {} turn on debug logging for more info",
                                sub.getFeedURL());
                    }
                }
            }
        }
    }
}
