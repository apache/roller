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
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Test database implementation of PlanetManager.
 */
public class PlanetManagerTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PlanetManagerTest.class);
    
    
    public void testSubscriptionStorage() throws Exception {
        
        PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
        
        {   // save subscriptions and a group
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            sub.setFeedURL("test_url");
            planet.saveSubscription(sub);
            
            PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
            sub1.setFeedURL("test_url1");
            planet.saveSubscription(sub1);   
            
            PlanetGroupData group = new PlanetGroupData();
            group.setDescription("test_group_desc");
            group.setHandle("test_handle");
            group.setTitle("test_title");
            planet.saveGroup(group);
            
            TestUtils.endSession(true);
        }
        {   // retrieve subscriptions and add to group
            
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            PlanetSubscriptionData sub1 = planet.getSubscription("test_url1");
            PlanetGroupData group = planet.getGroup("test_handle");
            
            group.getSubscriptions().add(sub);
            sub.getGroups().add(group);
            
            group.getSubscriptions().add(sub1);
            sub1.getGroups().add(group);
                        
            planet.saveSubscription(sub);
            planet.saveSubscription(sub1);
            planet.saveGroup(group);
            
            TestUtils.endSession(true);
        }
        {   // get group and remove one subscription
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            PlanetGroupData group = planet.getGroup("test_handle");
            group.getSubscriptions().remove(sub);
            TestUtils.endSession(true);
        }
        {   // get group and check it's subscriptions, remove it
            PlanetGroupData group = planet.getGroup("test_handle");
            Set subs = group.getSubscriptions();
            assertEquals(1, subs.size());
            planet.deleteGroup(group);
            TestUtils.endSession(true);
        }
        {   // make sure group gone, subs still there, then remove them too
            PlanetGroupData group = planet.getGroup("test_handle");
            assertNull(group);
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNotNull(sub);
            PlanetSubscriptionData sub1 = planet.getSubscription("test_url1");
            assertNotNull(sub1);
            planet.deleteSubscription(sub);
            planet.deleteSubscription(sub1);
            TestUtils.endSession(true);
        }
        {   // make sure subscriptions are gone
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNull(sub);
            PlanetSubscriptionData sub1 = planet.getSubscription("test_url1");
            assertNull(sub1);
        }
    }
    
    
    public void testSubscriptionEntryStorage() throws Exception {
        
        PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
        
        {   // save subscription
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            sub.setFeedURL("test_url");
            planet.saveSubscription(sub);
            TestUtils.endSession(true);
        }
        {   // retrieve subscription and add entries
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNotNull(sub);
            
            PlanetEntryData entry1 = new PlanetEntryData();
            entry1.setPermalink("test_entry1");
            entry1.setCategoriesString("test,test2");
            entry1.setPubTime(new Timestamp(System.currentTimeMillis()));
            entry1.setSubscription(sub);
            planet.saveEntry(entry1);
            sub.addEntry(entry1);
            
            PlanetEntryData entry2 = new PlanetEntryData();
            entry2.setPermalink("test_entry2");
            entry2.setCategoriesString("test_cat1,test_cat2,test_cat3");
            entry2.setPubTime(new Timestamp(System.currentTimeMillis()));
            entry2.setSubscription(sub);
            planet.saveEntry(entry2);
            sub.addEntry(entry2);
            
            // save entries
            planet.saveSubscription(sub);
            TestUtils.endSession(true);
            
            // get sub and check it's entries
            sub = planet.getSubscription("test_url");
            assertEquals(2, sub.getEntries().size());
        }
        {
            // add a single entry
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNotNull(sub);
            
            PlanetEntryData entry3 = new PlanetEntryData();
            entry3.setPermalink("test_entry3");
            entry3.setCategoriesString("test,test3");
            entry3.setSubscription(sub);
            entry3.setPubTime(new Timestamp(System.currentTimeMillis()));
            planet.saveEntry(entry3);
            TestUtils.endSession(true);
            
            // verify entry was added
            sub = planet.getSubscription("test_url");
            assertEquals(3, sub.getEntries().size());
        }
        {
            // purge entries
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            sub.purgeEntries();
            planet.saveSubscription(sub);
            TestUtils.endSession(true);
            
            // make sure they were removed
            sub = planet.getSubscription("test_url");
            assertEquals(0, sub.getEntries().size());
        }
        {
            // remove test subscription
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            planet.deleteSubscription(sub);
            TestUtils.endSession(true);
            
            // make sure sub is gone
            sub = planet.getSubscription("test_url");
            assertNull(sub);
        }
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
            planet.saveGroup(group);
            
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
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
            
            PlanetGroupData group = planet.getGroup("test_handle");
            assertNotNull(group);
            
            planet.deleteGroup(group);
            planet.deleteSubscription(sub);
            TestUtils.endSession(true);
            
            assertTrue(entriesSize > 0);
            
            Object feed = planet.getSubscription(feed_url1);
            assertNull(feed);
        }
    }
    
    
    public void _testAggregations() throws Exception {
        
        try {
            PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
            
            String feed_url1 = "http://rollerweblogger.org/roller/feed/entries/rss";
            String feed_url2 = "http://blogs.sun.com/main/feed/entries/atom";
            
            {
                PlanetGroupData group = new PlanetGroupData();
                group.setDescription("test_group_desc");
                group.setHandle("test_handle");
                group.setTitle("test_title");
                planet.saveGroup(group);
                
                PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
                sub1.setFeedURL(feed_url1);
                planet.saveSubscription(sub1);
                
                PlanetSubscriptionData sub2 = new PlanetSubscriptionData();
                sub2.setFeedURL(feed_url2);
                planet.saveSubscription(sub2);
                
                group.getSubscriptions().add(sub1);
                group.getSubscriptions().add(sub2);
                planet.saveGroup(group);
                TestUtils.endSession(true);
            }
            {
                planet.refreshEntries(null);
                TestUtils.endSession(true);
                
                int count = 0;
                Iterator subs = planet.getAllSubscriptions();
                while  (subs.hasNext()) {
                    PlanetSubscriptionData sub= (PlanetSubscriptionData)subs.next();
                    count += sub.getEntries().size();
                }
                PlanetSubscriptionData sub1 = planet.getSubscription(feed_url1);
                assertTrue(sub1.getEntries().size() > 0);
                PlanetSubscriptionData sub2 = planet.getSubscription(feed_url2);
                assertTrue(sub2.getEntries().size() > 0);
                assertEquals(count, sub1.getEntries().size() + sub2.getEntries().size());
                
                PlanetGroupData group = planet.getGroup("test_handle");
                assertNotNull(group);
                
                List bigag = planet.getAggregation(group, null, null, 0, 30);
                assertEquals(30, bigag.size());
                
                List littleag = planet.getAggregation(group, null, null, 0, 10);
                assertEquals(10, littleag.size());
                
                planet.deleteGroup(group);
                planet.deleteSubscription(sub1);
                planet.deleteSubscription(sub2);
                TestUtils.endSession(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    
    public void testSubscriptionCount() throws Exception {
        
        try {
            PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
            
            String feed_url1 = "http://rollerweblogger.org/roller/feed/entries/rss";
            String feed_url2 = "http://blogs.sun.com/main/feed/entries/atom";
            
            {
                PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
                sub1.setFeedURL(feed_url1);
                planet.saveSubscription(sub1);
                PlanetSubscriptionData sub2 = new PlanetSubscriptionData();
                sub2.setFeedURL(feed_url2);
                planet.saveSubscription(sub2);
                TestUtils.endSession(true);
                
                assertEquals(2, planet.getSubscriptionCount());
                
                planet.deleteSubscription(planet.getSubscription(feed_url1));
                planet.deleteSubscription(planet.getSubscription(feed_url2));
                TestUtils.endSession(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
}

