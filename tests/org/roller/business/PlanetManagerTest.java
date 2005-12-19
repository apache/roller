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
package org.roller.business;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.PlanetManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.PlanetConfigData;
import org.roller.pojos.PlanetEntryData;
import org.roller.pojos.PlanetGroupData;
import org.roller.pojos.PlanetSubscriptionData;
import org.roller.pojos.UserData;
import org.roller.util.Blacklist;

/**
 * Test database implementation of PlanetManager.
 * @author Dave Johnson
 */
public class PlanetManagerTest extends TestCase
{
    private Roller roller = null;
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PlanetManagerTest.class);
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();        
        RollerConfig.setPlanetCachePath(
            System.getProperty("ro.build") 
            + File.separator + "tests" 
            + File.separator + "planet-cache");
    }
    
    public void testConfigurationStorage() throws Exception
    {
        Roller roller = getRoller();
        assertNotNull(roller);       
        PlanetManager planet = roller.getPlanetManager();
        assertNotNull(planet);    
        
        {   // save config with default group
            roller.begin();
            
            PlanetConfigData config = new PlanetConfigData();
            //config.setCacheDir(cacheDir);
            config.setTitle("test_title");
            config.setAdminEmail("test_admin_email");

            PlanetGroupData group = new PlanetGroupData();
            group.setDescription("test_group_desc");
            group.setHandle("test_handle");
            group.setTitle("test_title");
            planet.saveGroup(group);
            
            config.setDefaultGroup(group);
            planet.saveConfiguration(config);
            
            roller.commit();
        }
        {   // retrieve config and default group
            roller.begin();
            PlanetConfigData config = planet.getConfiguration();
            assertEquals("test_title", config.getTitle());
            assertEquals("test_admin_email", config.getAdminEmail());
            
            PlanetGroupData group = config.getDefaultGroup();
            assertEquals("test_group_desc",group.getDescription());
            assertEquals("test_title",group.getTitle());
            
            roller.rollback();
        }
        {   // remove config
            roller.begin();
            PlanetConfigData config = planet.getConfiguration();
            config.remove();
            roller.commit();
        }
        {
            // make sure config and group are gone
            roller.begin();
            PlanetConfigData config = planet.getConfiguration();
            assertNull(config);
            PlanetGroupData group = planet.getGroup("test_handle");
            assertNull(group);
            roller.rollback();
        }
    }
    public void testGroupStorage() throws Exception
    {
        Roller roller = getRoller();
        assertNotNull(roller);       
        PlanetManager planet = roller.getPlanetManager();
        assertNotNull(planet);  
        
        {   // save group
            roller.begin(UserData.SYSTEM_USER);
            PlanetGroupData group = new PlanetGroupData();
            group.setDescription("test_group_desc");
            group.setHandle("test_handle");
            group.setTitle("test_title");
            planet.saveGroup(group);
            roller.commit();
        }
        {   // retrieve group 
            roller.begin();
            PlanetGroupData group = planet.getGroup("test_handle");
            assertEquals("test_group_desc",group.getDescription());
            assertEquals("test_title",group.getTitle());
            assertEquals(1, planet.getGroupHandles().size());
            roller.rollback();
        }
        {   // remove group
            roller.begin();
            PlanetGroupData group = planet.getGroup("test_handle");
            planet.deleteGroup(group);
            roller.commit();
        }
        {   // verify that it is gone
            roller.begin();
            PlanetGroupData group = planet.getGroup("test_handle");
            assertNull(group);
            roller.rollback();
        }
    }
    public void testSubscriptionStorage() throws Exception
    {
        Roller roller = getRoller();
        assertNotNull(roller);       
        PlanetManager planet = roller.getPlanetManager();
        assertNotNull(planet);        
        
        {   // save subscription
            roller.begin();
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            sub.setFeedUrl("test_url");
            planet.saveSubscription(sub);
            roller.commit();
        }
        {   // retrieve subscription and add to group
            roller.begin();
            
            PlanetGroupData group = new PlanetGroupData();
            group.setDescription("test_group_desc");
            group.setHandle("test_handle");
            group.setTitle("test_title");
            planet.saveGroup(group);
            
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNotNull(sub);
            group.addSubscription(sub);
            
            PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
            sub1.setFeedUrl("test_url1");
            planet.saveSubscription(sub1);
            
            List subs = new ArrayList();
            subs.add(sub1);
            group.addSubscriptions(subs); 
           
            planet.saveGroup(group);

            roller.commit();
        }
        {   // get group and check it's subscriptions, remove it
            roller.begin();
            PlanetGroupData group = planet.getGroup("test_handle");
            Set subs = group.getSubscriptions();
            assertEquals(2, subs.size());            
            planet.deleteGroup(group);
            roller.commit();
        }
        {   // make sure group gone, subs still there, then remove them too
            roller.begin();
            PlanetGroupData group = planet.getGroup("test_handle");
            assertNull(group);
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNotNull(sub);
            PlanetSubscriptionData sub1 = planet.getSubscription("test_url1");
            assertNotNull(sub1);
            planet.deleteSubscription(sub);
            planet.deleteSubscription(sub1);
            roller.commit();
        }
        {   // make sure subscriptions are gone
            roller.begin();
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNull(sub);
            PlanetSubscriptionData sub1 = planet.getSubscription("test_url1");
            assertNull(sub1);
            roller.rollback();
        }
    }
    public void testSubscriptionEntryStorage() throws Exception
    {
        Roller roller = getRoller();
        assertNotNull(roller);       
        PlanetManager planet = roller.getPlanetManager();
        assertNotNull(planet);        
        
        {   // save subscription
            roller.begin();
            PlanetSubscriptionData sub = new PlanetSubscriptionData();
            sub.setFeedUrl("test_url");
            planet.saveSubscription(sub);
            roller.commit();
        }
        {   // retrieve subscription and add entries
            roller.begin();
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNotNull(sub);
            
            PlanetEntryData entry1 = new PlanetEntryData();
            entry1.setPermalink("test_entry1");
            entry1.setCategoriesString("test,test2");
            entry1.setSubscription(sub);
            entry1.setPublished(new Date());
            entry1.save();
            sub.addEntry(entry1);
            
            PlanetEntryData entry2 = new PlanetEntryData();
            entry2.setPermalink("test_entry2");
            entry2.setCategoriesString("test_cat1,test_cat2,test_cat3");
            entry2.setSubscription(sub);
            entry2.setPublished(new Date());
            entry2.save();
            sub.addEntry(entry2);
            
            roller.commit();
        }
        {   // get sub and check it's entries, remove it
            roller.begin();
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertEquals(2, sub.getEntries().size());
            planet.deleteSubscription(sub);
            roller.commit();
        }
        {   // make sure sub is gone
            roller.begin();
            PlanetSubscriptionData sub = planet.getSubscription("test_url");
            assertNull(sub);
            roller.rollback();
        }
    }
    public void testRefreshEntries()
    {
        try 
        {
            Roller roller = getRoller();
            assertNotNull(roller);       
            PlanetManager planet = roller.getPlanetManager();
            assertNotNull(planet);     
            String feed_url1 = "http://rollerweblogger.org/rss/roller";
            {   
                roller.begin();
                PlanetConfigData config = new PlanetConfigData();
                //config.setCacheDir(cacheDir); 
                config.setTitle("test_title");
                config.setAdminEmail("test_admin_email");
                planet.saveConfiguration(config);
                
                PlanetGroupData group = new PlanetGroupData();
                group.setDescription("test_group_desc");
                group.setHandle("test_handle");
                group.setTitle("test_title");
                planet.saveGroup(group);
                
                PlanetSubscriptionData sub = new PlanetSubscriptionData();
                sub.setFeedUrl(feed_url1);
                planet.saveSubscription(sub);
                
                group.addSubscription(sub); 
                planet.saveGroup(group);
                
                roller.commit();
            }        
            {   
                roller.begin();
                planet.refreshEntries();
                roller.commit();
                
                roller.begin();
                PlanetSubscriptionData sub = planet.getSubscription(feed_url1);
                int entriesSize = sub.getEntries().size();
          
                PlanetGroupData group = planet.getGroup("test_handle");
                assertNotNull(group);
                
                planet.deleteGroup(group);
                planet.deleteSubscription(sub);
                
                PlanetConfigData config = planet.getConfiguration();
                config.remove();
                roller.commit();

                assertTrue(entriesSize > 0);
            }
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            fail();
        }
    }
    public void testAggregations() throws Exception
    {
        try 
        {
            Roller roller = getRoller();
            assertNotNull(roller);       
            PlanetManager planet = roller.getPlanetManager();
            assertNotNull(planet);     
            String feed_url1 = "http://rollerweblogger.org/rss/roller";
            String feed_url2 = "http://linuxintegrators.com/acoliver/?flavor=rss2";
            {   
                roller.begin();
                PlanetConfigData config = new PlanetConfigData();
                //config.setCacheDir(cacheDir); 
                config.setTitle("test_title");
                config.setAdminEmail("test_admin_email");
                planet.saveConfiguration(config);
                
                PlanetGroupData group = new PlanetGroupData();
                group.setDescription("test_group_desc");
                group.setHandle("test_handle");
                group.setTitle("test_title");
                planet.saveGroup(group);
                
                PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
                sub1.setFeedUrl(feed_url1);
                planet.saveSubscription(sub1);
                
                PlanetSubscriptionData sub2 = new PlanetSubscriptionData();
                sub2.setFeedUrl(feed_url2);
                planet.saveSubscription(sub2);
                
                group.addSubscription(sub1); 
                group.addSubscription(sub2); 
                planet.saveGroup(group);
                
                roller.commit();
            }        
            {   
                roller.begin();
                planet.refreshEntries();
                roller.commit();
                
                roller.begin();
                int count = 0;
                Iterator subs = planet.getAllSubscriptions();
                while  (subs.hasNext()) 
                {
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
    
                List bigag = planet.getAggregation(group, 30);
                assertEquals(30, bigag.size());
                      
                List littleag = planet.getAggregation(group, 10);
                assertEquals(10, littleag.size());
                                  
                planet.deleteGroup(group);
                planet.deleteSubscription(sub1);
                planet.deleteSubscription(sub2);
                
                PlanetConfigData config = planet.getConfiguration();
                config.remove();
                roller.commit(); 
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }
    }
    public void testSubscriptionCount() throws Exception
    {
        try 
        {
            Roller roller = getRoller();
            assertNotNull(roller);       
            PlanetManager planet = roller.getPlanetManager();
            assertNotNull(planet);     
            String feed_url1 = "http://rollerweblogger.org/rss/roller";
            String feed_url2 = "http://linuxintegrators.com/acoliver/?flavor=rss2";
            {   
                roller.begin();            
                PlanetSubscriptionData sub1 = new PlanetSubscriptionData();
                sub1.setFeedUrl(feed_url1);
                planet.saveSubscription(sub1);            
                PlanetSubscriptionData sub2 = new PlanetSubscriptionData();
                sub2.setFeedUrl(feed_url2);
                planet.saveSubscription(sub2);
                roller.commit();
    
                roller.begin();
                assertEquals(2, planet.getSubscriptionCount());
                roller.rollback();
                
                roller.begin();
                planet.getSubscription(feed_url1).remove();
                planet.getSubscription(feed_url2).remove();
                roller.commit();
            }     
        }
        catch (Exception e) 
        {
            e.printStackTrace();
            fail();
        }
    }
    /*
    public void testRankingRange()
    {
        int limit = 1000;
        int count = 5500;
        int mod = (count / limit) + 1;        
        for (int i=0; i<20; i++)
        {
            int start = (i % mod) * limit;
            int end = start + 999;
            end = end > count ? count : end; 
            System.out.println("start="+start+" end="+end);
        }
    }
    */
    public Roller getRoller() throws RollerException
    {
        return RollerFactory.getRoller();
    }

    public static Test suite()
    {
        return new TestSuite(PlanetManagerTest.class);
    }
}
    
