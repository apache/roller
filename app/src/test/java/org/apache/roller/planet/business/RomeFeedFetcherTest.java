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

package org.apache.roller.planet.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.business.fetcher.FetcherException;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test database implementation of PlanetManager.
 */
public class RomeFeedFetcherTest  {
    
    public static Log log = LogFactory.getLog(RomeFeedFetcherTest.class);
    
    String feed_url = "https://rollerweblogger.org/roller/feed/entries/atom";
    
    @BeforeEach
    public void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testFetchFeed() throws FetcherException {
        try {
            FeedFetcher feedFetcher = WebloggerFactory.getWeblogger().getFeedFetcher();
            
            // fetch feed
            Subscription sub = feedFetcher.fetchSubscription(feed_url);
            assertNotNull(sub);
            assertEquals(feed_url, sub.getFeedURL());
            assertEquals("https://rollerweblogger.org/roller/", sub.getSiteURL());
            assertEquals("Blogging Roller", sub.getTitle());
            assertNotNull(sub.getLastUpdated());
            assertTrue(sub.getEntries().size() > 0);

        } catch (FetcherException ex) {
            log.error("Error fetching feed", ex);
            throw ex;
        }
    }


    @Test
    public void testFetchFeedConditionally() throws FetcherException {
        try {
            FeedFetcher feedFetcher = WebloggerFactory.getWeblogger().getFeedFetcher();
            
            // fetch feed
            Subscription sub = feedFetcher.fetchSubscription(feed_url);
            assertNotNull(sub);
            assertEquals(feed_url, sub.getFeedURL());
            assertEquals("https://rollerweblogger.org/roller/", sub.getSiteURL());
            assertEquals("Blogging Roller", sub.getTitle());
            assertNotNull(sub.getLastUpdated());
            assertTrue(sub.getEntries().size() > 0);
            
            // now do a conditional fetch and we should get back null
            Subscription updatedSub = feedFetcher.fetchSubscription(feed_url, sub.getLastUpdated());
            assertNull(updatedSub);

        } catch (FetcherException ex) {
            log.error("Error fetching feed", ex);
            throw ex;
        }
    }
    
}
