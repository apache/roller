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

import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Utility class for unit test classes.
 */
public final class TestUtils {
    
    
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
            PlanetFactory.getPlanet().flush();
        }
        
        PlanetFactory.getPlanet().release();
    }
    
    
    /**
     * Convenience method that creates a planet and stores it.
     */
    public static PlanetData setupPlanet(String handle) throws Exception {
        
        PlanetData testPlanet = new PlanetData(handle, handle, handle);
        
        // store
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        mgr.savePlanet(testPlanet);
        
        // flush
        PlanetFactory.getPlanet().flush();
        
        // query to make sure we return the persisted object
        PlanetData planet = mgr.getPlanet(handle);
        
        if(planet == null)
            throw new PlanetException("error inserting new planet");
        
        return planet;
    }
    
    
    /**
     * Convenience method for removing a planet.
     */
    public static void teardownPlanet(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetData planet = mgr.getPlanetById(id);
        
        // remove
        mgr.deletePlanet(planet);
        
        // flush
        PlanetFactory.getPlanet().flush();
    }
    
    
    /**
     * Convenience method that creates a group and stores it.
     */
    public static PlanetGroupData setupGroup(PlanetData planet, String handle) 
            throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // make sure we are using a persistent object
        PlanetData testPlanet = mgr.getPlanetById(planet.getId());
        
        // store
        PlanetGroupData testGroup = new PlanetGroupData(testPlanet, handle, handle, handle);
        testPlanet.getGroups().add(testGroup);
        mgr.saveGroup(testGroup);
        
        // flush
        PlanetFactory.getPlanet().flush();
        
        // query to make sure we return the persisted object
        PlanetGroupData group = mgr.getGroupById(testGroup.getId());
        
        if(group == null)
            throw new PlanetException("error inserting new group");
        
        return group;
    }
    
    
    /**
     * Convenience method for removing a group.
     */
    public static void teardownGroup(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetGroupData group = mgr.getGroupById(id);
        
        // remove
        mgr.deleteGroup(group);
        group.getPlanet().getGroups().remove(group);
        
        // flush
        PlanetFactory.getPlanet().flush();
    }
    
    
    /**
     * Convenience method that creates a sub and stores it.
     */
    public static PlanetSubscriptionData setupSubscription(String feedUrl) 
            throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // store
        PlanetSubscriptionData testSub = new PlanetSubscriptionData();
        testSub.setFeedURL(feedUrl);
        testSub.setTitle(feedUrl);
        mgr.saveSubscription(testSub);
        
        // flush
        PlanetFactory.getPlanet().flush();
        
        // query to make sure we return the persisted object
        PlanetSubscriptionData sub = mgr.getSubscriptionById(testSub.getId());
        
        if(sub == null)
            throw new PlanetException("error inserting new subscription");
        
        return sub;
    }
    
    
    /**
     * Convenience method for removing a sub.
     */
    public static void teardownSubscription(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetSubscriptionData sub = mgr.getSubscriptionById(id);
        
        // remove
        mgr.deleteSubscription(sub);
        
        // flush
        PlanetFactory.getPlanet().flush();
    }
    
    
    /**
     * Convenience method that creates an entry and stores it.
     */
    public static PlanetEntryData setupEntry(PlanetSubscriptionData sub, String title) 
            throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // make sure we are using a persistent object
        PlanetSubscriptionData testSub = mgr.getSubscriptionById(sub.getId());
        
        // store
        PlanetEntryData testEntry = new PlanetEntryData();
        testEntry.setPermalink(title);
        testEntry.setTitle(title);
        testEntry.setPubTime(new java.sql.Timestamp(System.currentTimeMillis()));
        testEntry.setSubscription(testSub);
        testSub.getEntries().add(testEntry);
        mgr.saveEntry(testEntry);
        
        // flush
        PlanetFactory.getPlanet().flush();
        
        // query to make sure we return the persisted object
        PlanetEntryData entry = mgr.getEntryById(testEntry.getId());
        
        if(entry == null)
            throw new PlanetException("error inserting new entry");
        
        return entry;
    }
    
    
    /**
     * Convenience method for removing an entry.
     */
    public static void teardownEntry(String id) throws Exception {
        
        // lookup
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetEntryData entry = mgr.getEntryById(id);
        
        // remove
        mgr.deleteEntry(entry);
        entry.getSubscription().getEntries().remove(entry);
        
        // flush
        PlanetFactory.getPlanet().flush();
    }
    
}
