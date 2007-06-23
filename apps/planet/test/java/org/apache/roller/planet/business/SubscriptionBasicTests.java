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
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Test Subscription CRUD.
 */
public class SubscriptionBasicTests extends TestCase {
    
    
    public void testSubscriptionCRUD() throws Exception {
        
        // setup planet
        TestUtils.setupPlanet();

        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        PlanetSubscriptionData testSub = new PlanetSubscriptionData();
        testSub.setFeedURL("test_title");
        testSub.setTitle("test_title");
        
        PlanetSubscriptionData sub = mgr.getSubscription(testSub.getFeedURL());
        assertNull(sub);
        
        // add
        mgr.saveSubscription(testSub);
        TestUtils.endSession(true);
        
        // verify
        sub = null;
        sub = mgr.getSubscriptionById(testSub.getId());
        assertNotNull(sub);
        assertEquals("test_title", sub.getFeedURL());
        
        // modify
        sub.setTitle("foo");
        mgr.saveSubscription(sub);
        TestUtils.endSession(true);
        
        // verify
        sub = null;
        sub = mgr.getSubscriptionById(testSub.getId());
        assertNotNull(sub);
        assertEquals("foo", sub.getTitle());
        
        // remove
        mgr.deleteSubscription(sub);
        TestUtils.endSession(true);
        
        // verify
        sub = null;
        sub = mgr.getSubscriptionById(testSub.getId());
        assertNull(sub);
        
    }
    
}
