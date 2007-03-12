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
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Test database implementation of PlanetManager.
 */
public class PlanetManagerTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PlanetManagerTest.class);   
    private PlanetData testPlanet = null;
    
        
    protected void setUp() throws Exception {
        testPlanet = TestUtils.setupPlanet("groupTestPlanet");
    }
        
    protected void tearDown() throws Exception {
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    public void testRefreshEntries() throws Exception {
        
        PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
        
        String feed_url1 = "http://rollerweblogger.org/roller/feed/entries/rss";
        
        {
//            PlanetConfigData config = planet.getConfiguration();
//            config.setAdminName("admin");
//            config.setSiteURL("http://localhost:8080/roller");
//            planet.saveConfiguration(config);
            
            PlanetGroupData group = new PlanetGroupData();
            group.setDescription("test_group_desc");
            group.setHandle("test_handle");
            group.setTitle("test_title");
            group.setPlanet(testPlanet);
            planet.saveGroup(group);
            
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            sub.setTitle(feed_url1);
            sub.setFeedURL(feed_url1);
            planet.saveSubscription(sub);
            
            group.getSubscriptions().add(sub);            
            planet.saveGroup(group);
            TestUtils.endSession(true);
        }
        {
            planet.refreshEntries("." + File.separator + "planet-cache");
            TestUtils.endSession(true);
            
            PlanetSubscriptionData sub = planet.getSubscription(feed_url1);
            int entriesSize = sub.getEntries().size();
            
            PlanetGroupData group = planet.getGroup(testPlanet, "test_handle");
            assertNotNull(group);
            
            planet.deleteGroup(group);
            planet.deleteSubscription(sub);
            TestUtils.endSession(true);
            
            assertTrue(entriesSize > 0);
            
            Object feed = planet.getSubscription(feed_url1);
            assertNull(feed);
        }
    }
    
    
//    public void _testAggregations() throws Exception {
//        
//        try {
//            PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
//            
//            String feed_url1 = "http://rollerweblogger.org/roller/feed/entries/rss";
//            String feed_url2 = "http://blogs.sun.com/main/feed/entries/atom";
//            
//            {
//                PlanetGroupData group = new PlanetGroupData();
//                group.setDescription("test_group_desc");
//                group.setHandle("test_handle");
//                group.setTitle("test_title");
//                group.setPlanet(testPlanet);
//                planet.saveGroup(group);
//                
//                PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
//                sub1.setTitle(feed_url1);
//                sub1.setFeedURL(feed_url1);
//                planet.saveSubscription(sub1);
//                
//                PlanetSubscriptionData sub2 = new PlanetSubscriptionData();
//                sub2.setTitle(feed_url2);
//                sub2.setFeedURL(feed_url2);
//                planet.saveSubscription(sub2);
//                
//                group.getSubscriptions().add(sub1);
//                group.getSubscriptions().add(sub2);
//                planet.saveGroup(group);
//                TestUtils.endSession(true);
//            }
//            {
//                planet.refreshEntries(null);
//                TestUtils.endSession(true);
//                
//                int count = 0;
//                Iterator subs = planet.getSubscriptions().iterator();
//                while  (subs.hasNext()) {
//                    PlanetSubscriptionData sub= (PlanetSubscriptionData)subs.next();
//                    count += sub.getEntries().size();
//                }
//                PlanetSubscriptionData sub1 = planet.getSubscription(feed_url1);
//                assertTrue(sub1.getEntries().size() > 0);
//                PlanetSubscriptionData sub2 = planet.getSubscription(feed_url2);
//                assertTrue(sub2.getEntries().size() > 0);
//                assertEquals(count, sub1.getEntries().size() + sub2.getEntries().size());
//                
//                PlanetGroupData group = planet.getGroup(testPlanet, "test_handle");
//                assertNotNull(group);
//                
//                List bigag = planet.getEntries(group, null, null, 0, 30);
//                assertEquals(30, bigag.size());
//                
//                List littleag = planet.getEntries(group, null, null, 0, 10);
//                assertEquals(10, littleag.size());
//                
//                planet.deleteGroup(group);
//                planet.deleteSubscription(sub1);
//                planet.deleteSubscription(sub2);
//                TestUtils.endSession(true);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail();
//        }
//    }
    
}
