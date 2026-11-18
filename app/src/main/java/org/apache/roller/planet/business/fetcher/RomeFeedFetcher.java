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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.config.WebloggerConfig;


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

    // limits on a fetched feed's size and total download time, so a feed
    // that is huge or arrives slowly can't hold up the feed updater
    private final long maxBytes;
    private final Duration maxTime;
    
    public RomeFeedFetcher() {
        this(positiveProperty("planet.aggregator.fetch.maxBytes", 5 * 1024 * 1024),
                Duration.ofSeconds(positiveProperty("planet.aggregator.fetch.maxSeconds", 30)));
    }

    RomeFeedFetcher(long maxBytes, Duration maxTime) {
        this.maxBytes = maxBytes;
        this.maxTime = maxTime;
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
        
        // only fetch over http and https
        String scheme = feedURL.contains(":")
                ? feedURL.substring(0, feedURL.indexOf(':')) : "";
        if(!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new FetcherException("Feed URL must start with http or https - "+feedURL);
        }
        
        // fetch the feed
        log.debug("Fetching feed: "+feedURL);
        SyndFeed feed;
        try {
            feed = fetchFeed(feedURL);
        } catch (InterruptedException ex) {
            // let the caller see that it was asked to stop
            Thread.currentThread().interrupt();
            throw new FetcherException("Interrupted fetching subscription - "+feedURL, ex);
        } catch (FeedException | IOException ex) {
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
        
        HttpRequest request;
        try {
            request = requestBuilder.copy().uri(URI.create(url)).build();
        } catch (IllegalArgumentException e) {
            throw new IOException("Feed URL is not valid - " + url, e);
        }
        
        // the request timeout only covers the response headers, so bound the
        // whole download, including the body, here
        AtomicBoolean abandoned = new AtomicBoolean();
        AtomicReference<LimitedBodySubscriber> subscriber = new AtomicReference<>();
        CompletableFuture<HttpResponse<byte[]>> response = client.sendAsync(request,
                info -> {
                    LimitedBodySubscriber s = new LimitedBodySubscriber(maxBytes,
                            info.headers().firstValueAsLong("Content-Length").orElse(-1),
                            info.statusCode(), abandoned);
                    subscriber.set(s);
                    return s;
                });
        byte[] body;
        try {
            body = response.get(maxTime.toMillis(), TimeUnit.MILLISECONDS).body();
        } catch (TimeoutException e) {
            abandon(response, abandoned, subscriber);
            throw new IOException("Feed was not received within "
                    + maxTime.toSeconds() + " seconds");
        } catch (InterruptedException e) {
            abandon(response, abandoned, subscriber);
            throw e;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new IOException(e.getCause());
        }
        
        try(XmlReader reader = new XmlReader(new ByteArrayInputStream(body))) {
            return new SyndFeedInput().build(reader);
        }
       
    }
    
    
    // stops a download we no longer wait for. On Java 11, cancelling the
    // future doesn't stop the exchange, so the body subscriber also cancels
    // its subscription, which closes the connection.
    private static void abandon(CompletableFuture<?> response, AtomicBoolean abandoned,
            AtomicReference<LimitedBodySubscriber> subscriber) {
        abandoned.set(true);
        LimitedBodySubscriber s = subscriber.get();
        if (s != null) {
            s.cancel(new IOException("Feed download abandoned"));
        }
        response.cancel(true);
    }
    
    
    // a property from roller.properties, or the default if it isn't positive
    private static int positiveProperty(String name, int defaultValue) {
        try {
            int value = WebloggerConfig.getIntProperty(name, defaultValue);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException e) {
            log.warn("Invalid value for " + name + ", using " + defaultValue);
            return defaultValue;
        }
    }
    
    
    // collects a successful response's body, failing once it grows past
    // maxBytes or once the fetcher stops waiting for it
    private static final class LimitedBodySubscriber
            implements HttpResponse.BodySubscriber<byte[]> {
        
        private final CompletableFuture<byte[]> result = new CompletableFuture<>();
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private final long maxBytes;
        private final long contentLength;
        private final int statusCode;
        private final AtomicBoolean abandoned;
        private Flow.Subscription subscription;
        private boolean cancelled;
        
        LimitedBodySubscriber(long maxBytes, long contentLength, int statusCode,
                AtomicBoolean abandoned) {
            this.maxBytes = maxBytes;
            this.contentLength = contentLength;
            this.statusCode = statusCode;
            this.abandoned = abandoned;
        }
        
        @Override
        public synchronized void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            if (abandoned.get()) {
                cancel(new IOException("Feed download abandoned"));
            } else if (statusCode < 200 || statusCode > 299) {
                cancel(new IOException("Feed request returned HTTP status " + statusCode));
            } else if (contentLength > maxBytes) {
                tooLarge();
            } else {
                subscription.request(Long.MAX_VALUE);
            }
        }
        
        @Override
        public synchronized void onNext(List<ByteBuffer> buffers) {
            if (cancelled || result.isDone()) {
                return;
            }
            for (ByteBuffer buffer : buffers) {
                if (body.size() + (long) buffer.remaining() > maxBytes) {
                    tooLarge();
                    return;
                }
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                body.writeBytes(bytes);
            }
        }
        
        @Override
        public void onError(Throwable throwable) {
            result.completeExceptionally(throwable);
        }
        
        @Override
        public void onComplete() {
            result.complete(body.toByteArray());
        }
        
        @Override
        public CompletionStage<byte[]> getBody() {
            return result;
        }
        
        private void tooLarge() {
            cancel(new IOException("Feed is larger than " + maxBytes + " bytes"));
        }
        
        // stops reading the body; safe to call from any thread
        synchronized void cancel(IOException reason) {
            if (!cancelled && subscription != null) {
                cancelled = true;
                subscription.cancel();
            }
            result.completeExceptionally(reason);
        }
    }
    
}
