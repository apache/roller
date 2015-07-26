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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import junit.framework.TestCase;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.Subscription;


/**
 * Test Subscription functionality.
 */
public class SubscriptionTest extends TestCase {
    private Planet testPlanet1 = null;

    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();
        testPlanet1 = TestUtils.setupPlanet("subFuncPlanet1");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownPlanet(testPlanet1.getHandle());
    }


    public void testSubscriptionCRUD() throws Exception {

        // setup planet
        TestUtils.setupWeblogger();

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        Subscription testSub = TestUtils.setupSubscription(testPlanet1, "feed_url");
        testSub.setTitle("test_title");

        // verify
        Subscription sub = mgr.getSubscriptionById(testSub.getId());
        assertNotNull(sub);
        assertEquals("feed_url", sub.getFeedURL());

        // modify
        sub.setTitle("foo");
        mgr.saveSubscription(sub);
        TestUtils.endSession(true);

        // verify
        sub = mgr.getSubscriptionById(testSub.getId());
        assertNotNull(sub);
        assertEquals("foo", sub.getTitle());

        // remove
        TestUtils.teardownSubscription(testSub.getId());

        // verify
        sub = mgr.getSubscriptionById(testSub.getId());
        assertNull(sub);
    }

    public void testSubscriptionLookups() throws Exception {
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription testSub1 = TestUtils.setupSubscription(testPlanet1, "subFuncSub1");
        Subscription testSub2 = TestUtils.setupSubscription(testPlanet1, "subFuncSub2");

        // by id
        Subscription sub = mgr.getSubscriptionById(testSub1.getId());
        assertNotNull(sub);
        assertEquals("subFuncSub1", sub.getFeedURL());
        
        // by feed url
        sub = mgr.getSubscription(testPlanet1, testSub2.getFeedURL());
        assertNotNull(sub);
        assertEquals("subFuncSub2", sub.getFeedURL());
        
        // count
        assertEquals(2, mgr.getSubscriptionCount());
    }

}
