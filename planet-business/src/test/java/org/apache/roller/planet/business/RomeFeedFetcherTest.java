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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.pojos.Subscription;


/**
 * Test database implementation of PlanetManager.
 */
public class RomeFeedFetcherTest extends TestCase {
    
    public static Log log = LogFactory.getLog(RomeFeedFetcherTest.class);
    
    String feed_url = "http://rollerweblogger.org/roller/feed/entries/atom";
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();
    }
    
    
    protected void tearDown() throws Exception {
    }
    
    
    public void testFetchFeed() throws Exception {
        
        FeedFetcher feedFetcher = PlanetFactory.getPlanet().getFeedFetcher();

        // fetch feed
        Subscription sub = feedFetcher.fetchSubscription(feed_url);
        assertNotNull(sub);
        assertEquals(feed_url, sub.getFeedURL());
        assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
        assertEquals("Blogging Roller", sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
    }
    
    
    public void testFetchFeedConditionally() throws Exception {
        
        FeedFetcher feedFetcher = PlanetFactory.getPlanet().getFeedFetcher();

        // fetch feed
        Subscription sub = feedFetcher.fetchSubscription(feed_url);
        assertNotNull(sub);
        assertEquals(feed_url, sub.getFeedURL());
        assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
        assertEquals("Blogging Roller", sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
        
        // now do a conditional fetch and we should get back null
        Subscription updatedSub = feedFetcher.fetchSubscription(feed_url, sub.getLastUpdated());
        assertNull(updatedSub);
    }
    
}
