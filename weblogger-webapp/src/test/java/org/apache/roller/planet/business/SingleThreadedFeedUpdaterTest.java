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
import org.apache.roller.planet.business.updater.FeedUpdater;
import org.apache.roller.planet.business.updater.SingleThreadedFeedUpdater;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test feed updater.
 */
public class SingleThreadedFeedUpdaterTest extends TestCase {
    
    public static Log log = LogFactory.getLog(SingleThreadedFeedUpdaterTest.class);
    
    private Subscription testSub = null;
    
    private String feed_url = "http://rollerweblogger.org/roller/feed/entries/atom";
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();
        
        // add test subscription
        PlanetManager mgr = WebloggerFactory.getWeblogger().getWebloggerManager();
        testSub = new Subscription();
        testSub.setTitle(feed_url);
        testSub.setFeedURL(feed_url);
        mgr.saveSubscription(testSub);
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub.getId());
    }
    
    
    public void testUpdateSubscription() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getWebloggerManager();
        Subscription sub = mgr.getSubscriptionById(testSub.getId());
        
        // update the subscription
        FeedUpdater updater = new SingleThreadedFeedUpdater();
        updater.updateSubscription(sub);
        TestUtils.endSession(true);
        
        // verify the results
        sub = mgr.getSubscription(feed_url);
        assertNotNull(sub);
        assertEquals(feed_url, sub.getFeedURL());
        assertEquals("http://rollerweblogger.org/roller/", sub.getSiteURL());
        assertEquals("Blogging Roller", sub.getTitle());
        assertNotNull(sub.getLastUpdated());
        assertTrue(sub.getEntries().size() > 0);
    }
    
}
