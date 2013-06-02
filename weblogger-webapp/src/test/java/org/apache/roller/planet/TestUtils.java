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
*/

package org.apache.roller.planet;

import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Utility class for unit test classes.
 */
public final class TestUtils {
    
    
    public static void setupPlanet() throws Exception {
        
        if(!WebloggerFactory.isBootstrapped()) {
            
            // do application bootstrapping and init
            WebloggerFactory.bootstrap();
            
            // always initialize the properties manager and flush
            WebloggerFactory.getWeblogger().getPropertiesManager().initialize();
            WebloggerFactory.getWeblogger().flush();
        }
    }
    
    
    public static void shutdownPlanet() throws Exception {
        
        // trigger shutdown
        WebloggerFactory.getWeblogger().shutdown();
    }
    
    
    /**
     * Convenience method that simulates the end of a typical session.
     *
     * Normally this would be triggered by the end of the response in the webapp
     * but for unit tests we need to do this explicitly.
     *
     * @param flush true if you want to flush changes to db before releasing
     */
    public static void endSession(boolean flush) throws Exception {
        
        if(flush) {
            WebloggerFactory.getWeblogger().flush();
        }
        
        WebloggerFactory.getWeblogger().release();
    }
    
    
    /**
     * Convenience method that creates a planet and stores it.
     */
    public static Planet setupPlanet(String handle) throws Exception {
        
        Planet testPlanet = new Planet(handle, handle, handle);
        
        // store
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        mgr.savePlanet(testPlanet);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
        
        // query to make sure we return the persisted object
        Planet planet = mgr.getWeblogger(handle);
        
        if(planet == null)
            throw new WebloggerException("error inserting new planet");
        
        return planet;
    }
    
    
    /**
     * Convenience method for removing a planet.
     */
    public static void teardownPlanet(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Planet planet = mgr.getWebloggerById(id);
        
        // remove
        mgr.deletePlanet(planet);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method that creates a group and stores it.
     */
    public static PlanetGroup setupGroup(Planet planet, String handle) 
            throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // make sure we are using a persistent object
        Planet testPlanet = mgr.getWebloggerById(planet.getId());
        
        // store
        PlanetGroup testGroup = new PlanetGroup(testPlanet, handle, handle, handle);
        testPlanet.getGroups().add(testGroup);
        mgr.saveGroup(testGroup);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
        
        // query to make sure we return the persisted object
        PlanetGroup group = mgr.getGroupById(testGroup.getId());
        
        if(group == null)
            throw new WebloggerException("error inserting new group");
        
        return group;
    }
    
    
    /**
     * Convenience method for removing a group.
     */
    public static void teardownGroup(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        PlanetGroup group = mgr.getGroupById(id);
        
        // remove
        mgr.deleteGroup(group);
        group.getWeblogger().getGroups().remove(group);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method that creates a sub and stores it.
     */
    public static Subscription setupSubscription(String feedUrl) 
            throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // store
        Subscription testSub = new Subscription();
        testSub.setFeedURL(feedUrl);
        testSub.setTitle(feedUrl);
        mgr.saveSubscription(testSub);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
        
        // query to make sure we return the persisted object
        Subscription sub = mgr.getSubscriptionById(testSub.getId());
        
        if(sub == null)
            throw new WebloggerException("error inserting new subscription");
        
        return sub;
    }
    
    
    /**
     * Convenience method for removing a sub.
     */
    public static void teardownSubscription(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(id);
        
        // remove
        mgr.deleteSubscription(sub);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
    }
    
    
    /**
     * Convenience method that creates an entry and stores it.
     */
    public static SubscriptionEntry setupEntry(Subscription sub, String title) 
            throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // make sure we are using a persistent object
        Subscription testSub = mgr.getSubscriptionById(sub.getId());
        
        // store
        SubscriptionEntry testEntry = new SubscriptionEntry();
        testEntry.setPermalink(title);
        testEntry.setTitle(title);
        testEntry.setPubTime(new java.sql.Timestamp(System.currentTimeMillis()));
        testEntry.setSubscription(testSub);
        testSub.getEntries().add(testEntry);
        mgr.saveEntry(testEntry);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
        
        // query to make sure we return the persisted object
        SubscriptionEntry entry = mgr.getEntryById(testEntry.getId());
        
        if(entry == null)
            throw new WebloggerException("error inserting new entry");
        
        return entry;
    }
    
    
    /**
     * Convenience method for removing an entry.
     */
    public static void teardownEntry(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        SubscriptionEntry entry = mgr.getEntryById(id);
        
        // remove
        mgr.deleteEntry(entry);
        entry.getSubscription().getEntries().remove(entry);
        
        // flush
        WebloggerFactory.getWeblogger().flush();
    }
    
    public void testNothing() {
        // TODO: remove this method
    }
    
}
