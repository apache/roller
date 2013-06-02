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
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test Subscription functionality.
 */
public class SubscriptionFunctionalTests extends TestCase {
    
    private Planet testPlanet = null;
    private PlanetGroup testGroup1 = null;
    private PlanetGroup testGroup2 = null;
    private Subscription testSub1 = null;
    private Subscription testSub2 = null;
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();

        testPlanet = TestUtils.setupPlanet("subFuncTest");
        testGroup1 = TestUtils.setupGroup(testPlanet, "subFuncTest1");
        testGroup2 = TestUtils.setupGroup(testPlanet, "subFuncTest2");
        testSub1 = TestUtils.setupSubscription("subFuncTest1");
        testSub2 = TestUtils.setupSubscription("subFuncTest2");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub1.getId());
        TestUtils.teardownSubscription(testSub2.getId());
        TestUtils.teardownGroup(testGroup1.getId());
        TestUtils.teardownGroup(testGroup2.getId());
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    public void testSubscriptionLookups() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // by id
        Subscription sub = mgr.getSubscriptionById(testSub1.getId());
        assertNotNull(sub);
        assertEquals("subFuncTest1", sub.getFeedURL());
        
        // by feed url
        sub = null;
        sub = mgr.getSubscription(testSub2.getFeedURL());
        assertNotNull(sub);
        assertEquals("subFuncTest2", sub.getFeedURL());
        
        // count
        assertEquals(2, mgr.getSubscriptionCount());
    }
    
    
    public void testSubscriptionGroupCRUD() throws Exception {
        
        PlanetManager planet = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // retrieve subscriptions and add to group
        Subscription sub1 = planet.getSubscriptionById(testSub1.getId());
        Subscription sub2 = planet.getSubscriptionById(testSub2.getId());
        PlanetGroup group = planet.getGroupById(testGroup1.getId());
        
        // make sure no subs in group yet
        assertEquals(0, group.getSubscriptions().size());
        
        // add
        group.getSubscriptions().add(sub1);
        sub1.getGroups().add(group);

        group.getSubscriptions().add(sub2);
        sub2.getGroups().add(group);
        
        planet.saveGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = planet.getGroupById(testGroup1.getId());
        sub1 = planet.getSubscriptionById(testSub1.getId());
        sub2 = planet.getSubscriptionById(testSub2.getId());
        assertEquals(2, group.getSubscriptions().size());
        
        // remove
        group.getSubscriptions().remove(sub1);
        sub1.getGroups().remove(group);
        
        group.getSubscriptions().remove(sub2);
        sub2.getGroups().remove(group);

        planet.saveGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = planet.getGroupById(testGroup1.getId());
        assertEquals(0, group.getSubscriptions().size());
    }
    
}
