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

import java.util.HashSet;
import java.util.Set;


/**
 * Test feed updater.
 */
public class SingleThreadedFeedUpdaterTest extends TestCase {
    
    public static Log log = LogFactory.getLog(SingleThreadedFeedUpdaterTest.class);
    
    private Subscription testSub = null;
    
    private Planet planet = null;

    private String feed_url = "http://rollerweblogger.org/roller/feed/entries/atom";
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        // add test planet
        planet = new Planet("testPlanetHandle", "testPlanetTitle", "testPlanetDesc");

        // add test subscription
        testSub = new Subscription();
        testSub.setTitle(feed_url);
        testSub.setFeedURL(feed_url);
        testSub.setPlanet(planet);
        planet.getSubscriptions().add(testSub);
        mgr.savePlanet(planet);
        mgr.saveSubscription(testSub);
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub.getId());
    }
    
    
    public void testUpdateSubscription() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(testSub.getId());
        
        // update the subscription
        FeedUpdater updater = new SingleThreadedFeedUpdater();
        Set<Subscription> subscriptionSet = new HashSet<>();
        subscriptionSet.add(sub);
        updater.updateSubscriptions(subscriptionSet);
        TestUtils.endSession(true);
        
        // verify the results
        sub = mgr.getSubscription(planet, feed_url);
        assertNotNull(sub);
        assertEquals(feed_url, sub.getFeedURL());
        assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
        assertEquals("Blogging Roller", sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
    }
    
}
