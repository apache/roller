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

import java.io.File;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Test database implementation of PlanetManager.
 */
public class RomeFeedFetcherTest extends TestCase {
    
    public static Log log = LogFactory.getLog(RomeFeedFetcherTest.class);   
    
    private PlanetData testPlanet = null;
    private PlanetGroupData testGroup = null;
    private PlanetSubscriptionData testSub = null;
    
    String feed_url = "http://rollerweblogger.org/roller/feed/entries/rss";
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();

        testPlanet = TestUtils.setupPlanet("fetcherTestPlanet");
        testGroup = TestUtils.setupGroup(testPlanet, "fetcherTestGroup");
        
        // add test subscription
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        testSub = new PlanetSubscriptionData();
        testSub.setTitle(feed_url);
        testSub.setFeedURL(feed_url);
        mgr.saveSubscription(testSub);
        PlanetFactory.getPlanet().flush();
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub.getId());
        TestUtils.teardownGroup(testGroup.getId());
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    public void testRefreshEntries() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        FeedFetcher feedFetcher = PlanetFactory.getPlanet().getFeedFetcher();

        // refresh entries
        feedFetcher.refreshEntries("." + File.separator + "planet-cache");
        TestUtils.endSession(true);
        
        PlanetSubscriptionData sub = mgr.getSubscription(feed_url);
        assertNotNull(sub);
        assertTrue(sub.getEntries().size() > 0);
    }
    
}
