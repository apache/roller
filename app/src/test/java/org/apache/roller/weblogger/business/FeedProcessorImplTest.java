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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;

import java.util.HashSet;
import java.util.Set;

/**
 * Test custom weblogger feed fetcher.
 */
public class FeedProcessorImplTest extends TestCase {
    
    public static Log log = LogFactory.getLog(FeedProcessorImplTest.class);
    
    String rollerFeedUrl = "weblogger:webloggerFetcherTestWeblog";
    String externalFeedUrl = "http://rollerweblogger.org/roller/feed/entries/atom";
    private Subscription testSub = null;
    private Planet planet = null;
    private User testUser = null;
    private Weblog testWeblog = null;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Override
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        testUser = TestUtils.setupUser("webloggerFetcherTestUser");
        testWeblog = TestUtils.setupWeblog("webloggerFetcherTestWeblog", testUser);

        // add test planet
        planet = new Planet("testPlanetHandle", "testPlanetTitle", "testPlanetDesc");

        // add test subscription
        testSub = new Subscription();
        testSub.setTitle(externalFeedUrl);
        testSub.setFeedURL(externalFeedUrl);
        testSub.setPlanet(planet);
        planet.getSubscriptions().add(testSub);
        mgr.savePlanet(planet);
        mgr.saveSubscription(testSub);
        TestUtils.endSession(true);
    }
    
    @Override
    public void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub.getId());
        TestUtils.teardownPlanet("testPlanetHandle");
        TestUtils.teardownWeblog(testWeblog.getId());
        TestUtils.teardownUser(testUser.getUserName());
    }

    public void testFetchFeed() throws WebloggerException {
        try {
            FeedProcessor feedFetcher = WebloggerFactory.getWeblogger().getFeedFetcher();

            // fetch feed
            Subscription sub = feedFetcher.fetchSubscription(externalFeedUrl);
            assertNotNull(sub);
            assertEquals(externalFeedUrl, sub.getFeedURL());
            assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
            assertEquals("Blogging Roller", sub.getTitle());
            assertNotNull(sub.getLastUpdated());
            assertTrue(sub.getEntries().size() > 0);

        } catch (WebloggerException ex) {
            log.error("Error fetching feed", ex);
            throw ex;
        }
    }


    public void testFetchFeedConditionally() throws WebloggerException {
        try {
            FeedProcessor feedFetcher = WebloggerFactory.getWeblogger().getFeedFetcher();

            // fetch feed
            Subscription sub = feedFetcher.fetchSubscription(externalFeedUrl);
            assertNotNull(sub);
            assertEquals(externalFeedUrl, sub.getFeedURL());
            assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
            assertEquals("Blogging Roller", sub.getTitle());
            assertNotNull(sub.getLastUpdated());
            assertTrue(sub.getEntries().size() > 0);

            // now do a conditional fetch and we should get back null
            Subscription updatedSub = feedFetcher.fetchSubscription(externalFeedUrl, sub.getLastUpdated());
            assertNull(updatedSub);

        } catch (WebloggerException ex) {
            log.error("Error fetching feed", ex);
            throw ex;
        }
    }

    public void testFetchInternalSubscription() throws Exception {
        try {
            FeedProcessor feedFetcher = WebloggerFactory.getWeblogger().getFeedFetcher();

            // first fetch non-conditionally so we know we should get a Sub
            Subscription sub = feedFetcher.fetchSubscription(rollerFeedUrl);
            assertNotNull(sub);
            assertEquals(rollerFeedUrl, sub.getFeedURL());
            assertNotNull(sub.getLastUpdated());

            // now do a conditional fetch and we should get back null
            Subscription updatedSub = feedFetcher.fetchSubscription(rollerFeedUrl, sub.getLastUpdated());
            assertNull(updatedSub);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void testUpdateSubscription() throws Exception {
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(testSub.getId());

        // update the subscription
        FeedProcessor updater = new FeedProcessorImpl();
        Set<Subscription> subscriptionSet = new HashSet<>();
        subscriptionSet.add(sub);
        updater.updateSubscriptions(subscriptionSet);
        TestUtils.endSession(true);

        // verify the results
        sub = mgr.getSubscription(planet, externalFeedUrl);
        assertNotNull(sub);
        assertEquals(externalFeedUrl, sub.getFeedURL());
        assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
        assertEquals("Blogging Roller", sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
    }

}
