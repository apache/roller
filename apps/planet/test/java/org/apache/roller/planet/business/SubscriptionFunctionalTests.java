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

import java.util.Set;
import junit.framework.TestCase;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Test Subscription functionality.
 */
public class SubscriptionFunctionalTests extends TestCase {
    
    private PlanetData testPlanet = null;
    private PlanetGroupData testGroup1 = null;
    private PlanetGroupData testGroup2 = null;
    private PlanetSubscriptionData testSub1 = null;
    private PlanetSubscriptionData testSub2 = null;
    
    
    protected void setUp() throws Exception {
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
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // by id
        PlanetSubscriptionData sub = mgr.getSubscriptionById(testSub1.getId());
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
        
        PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
        
        // retrieve subscriptions and add to group
        PlanetSubscriptionData sub1 = planet.getSubscriptionById(testSub1.getId());
        PlanetSubscriptionData sub2 = planet.getSubscriptionById(testSub2.getId());
        PlanetGroupData group = planet.getGroupById(testGroup1.getId());
        
        // make sure no subs in group yet
        assertEquals(0, group.getSubscriptions().size());
        
        // add
        group.getSubscriptions().add(sub1);
        group.getSubscriptions().add(sub2);
        planet.saveGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = planet.getGroupById(testGroup1.getId());
        assertEquals(2, group.getSubscriptions().size());
        
        // remove
        group.getSubscriptions().remove(sub1);
        group.getSubscriptions().remove(sub2);
        planet.saveGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = planet.getGroupById(testGroup1.getId());
        assertEquals(0, group.getSubscriptions().size());
    }
    
}
