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

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import static org.junit.Assert.*;

/**
 * Test custom weblogger feed fetcher.
 */
public class FeedManagerImplTest extends WebloggerTest {

    String rollerFeedUrl = "weblogger:weblogger-fetcher-test-weblog";
    String expectedTitle = "Slashdot";
    String expectedSiteUrl = "https://slashdot.org/";
    String externalFeedUrl = "http://rss.slashdot.org/Slashdot/slashdotMainatom";
    private Subscription testSub = null;
    private User testUser = null;
    private Weblog testWeblog = null;

    @Resource
    protected FeedManager feedManager;

    public void setFeedManager(FeedManager feedManager) {
        this.feedManager = feedManager;
    }

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("webloggerFetcherTestUser");
        testWeblog = setupWeblog("weblogger-fetcher-test-weblog", testUser);

        // add test planet
        Planet planet = new Planet("testPlanetHandle", "testPlanetTitle", "testPlanetDesc");

        // add test subscription
        testSub = new Subscription();
        testSub.setTitle(externalFeedUrl);
        testSub.setFeedURL(externalFeedUrl);
        testSub.setPlanet(planet);
        planet.getSubscriptions().add(testSub);
        planetManager.savePlanet(planet);
        planetManager.saveSubscription(testSub);
        endSession(true);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownSubscription(testSub.getId());
        teardownPlanet("testPlanetHandle");
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
    }

    @Test
    public void testFetchFeed() {
        // fetch feed
        Subscription sub = feedManager.fetchSubscription(externalFeedUrl);
        assertNotNull(sub);
        assertEquals(externalFeedUrl, sub.getFeedURL());
        assertEquals(expectedSiteUrl, sub.getSiteURL());
        assertEquals(expectedTitle, sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
    }

    @Test
    public void testFetchFeedConditionally() {
        // fetch feed
        Subscription sub = feedManager.fetchSubscription(externalFeedUrl);
        assertNotNull(sub);
        assertEquals(externalFeedUrl, sub.getFeedURL());
        assertEquals(expectedSiteUrl, sub.getSiteURL());
        assertEquals(expectedTitle, sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);

        // now do a conditional fetch and we should get back null
        Subscription updatedSub = feedManager.fetchSubscription(externalFeedUrl, sub.getLastUpdated());
        assertNull(updatedSub);
    }

    @Test
    public void testFetchInternalSubscription() throws Exception {
        try {
            // first fetch non-conditionally so we know we should get a Sub
            Subscription sub = feedManager.fetchSubscription(rollerFeedUrl);
            assertNotNull(sub);
            assertEquals(rollerFeedUrl, sub.getFeedURL());
            assertNotNull(sub.getLastUpdated());

            // now do a conditional fetch and we should get back null
            Subscription updatedSub = feedManager.fetchSubscription(rollerFeedUrl, sub.getLastUpdated());
            assertNull(updatedSub);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdatePlanet() throws Exception {
        Planet planet = planetManager.getPlanetByHandle("testPlanetHandle");

        // update the planet
        feedManager.updateSubscriptions(planet);
        endSession(true);

        // verify the results
        Subscription sub = planetManager.getSubscription(planet, externalFeedUrl);
        assertNotNull(sub);
        assertEquals(externalFeedUrl, sub.getFeedURL());
        assertEquals(expectedSiteUrl, sub.getSiteURL());
        assertEquals(expectedTitle, sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
    }

}
