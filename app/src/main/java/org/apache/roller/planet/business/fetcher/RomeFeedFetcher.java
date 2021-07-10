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

import com.rometools.rome.feed.module.DCModule;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.Subscription;

import static java.net.http.HttpResponse.BodyHandlers.ofInputStream;


/**
 * A FeedFetcher based on Apache ROME and {@link java.net.http.HttpClient}.
 */
public class RomeFeedFetcher implements FeedFetcher {
    
    private static final Log log = LogFactory.getLog(RomeFeedFetcher.class);
    
    // mutable, copy() first
    private static final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                                .timeout(Duration.ofSeconds(3))
                                .header("User-Agent", "RollerPlanetAggregator");
    
    private final HttpClient client;
    
    public RomeFeedFetcher() {
        // immutable + thread safe, prefers HTTP/2, no redirects
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3)).build();
    }
    
    /**
     * @inheritDoc
     */
    @Override
    public Subscription fetchSubscription(String feedURL) throws FetcherException {
        return fetchSubscription(feedURL, null);
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public Subscription fetchSubscription(String feedURL, Date lastModified) throws FetcherException {

        if(feedURL == null) {
            throw new IllegalArgumentException("feed url cannot be null");
        }
        
        // fetch the feed
        log.debug("Fetching feed: "+feedURL);
        SyndFeed feed;
        try {
            feed = fetchFeed(feedURL);
        } catch (FeedException | IOException | InterruptedException ex) {
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
            
            newSub.addEntry(newEntry);
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
        if (!romeEntry.getContents().isEmpty()) {
            SyndContent content= romeEntry.getContents().get(0);
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
        if (!romeEntry.getCategories().isEmpty()) {
            List<String> list = new ArrayList<>();
            for (SyndCategory cat : romeEntry.getCategories()) {
                list.add(cat.getName());
            }
            newEntry.setCategoriesString(list);
        }
        
        return newEntry;
    }
    
    private SyndFeed fetchFeed(String url) throws IOException, InterruptedException, FeedException {
        
        HttpRequest request = requestBuilder.copy().uri(URI.create(url)).build();
        
        try(XmlReader reader = new XmlReader(client.send(request, ofInputStream()).body())) {
            return new SyndFeedInput().build(reader);
        }
       
    }
    
}
