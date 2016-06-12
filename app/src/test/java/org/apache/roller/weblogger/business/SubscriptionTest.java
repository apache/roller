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

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test Subscription functionality.
 */
public class SubscriptionTest extends WebloggerTest {
    private Planet testPlanet1 = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testPlanet1 = setupPlanet("subFuncPlanet1");
    }

    @After
    public void tearDown() throws Exception {
        teardownPlanet(testPlanet1.getHandle());
    }

    @Test
    public void testSubscriptionCRUD() throws Exception {
        Subscription testSub = setupSubscription(testPlanet1, "feed_url");
        testSub.setTitle("test_title");

        // verify
        Subscription sub = planetManager.getSubscription(testSub.getId());
        assertNotNull(sub);
        assertEquals("feed_url", sub.getFeedURL());

        // modify
        sub.setTitle("foo");
        planetManager.saveSubscription(sub);
        endSession(true);

        // verify
        sub = planetManager.getSubscription(testSub.getId());
        assertNotNull(sub);
        assertEquals("foo", sub.getTitle());

        // remove
        teardownSubscription(testSub.getId());

        // verify
        sub = planetManager.getSubscription(testSub.getId());
        assertNull(sub);
    }

    @Test
    public void testSubscriptionLookups() throws Exception {
        Subscription testSub1 = setupSubscription(testPlanet1, "subFuncSub1");
        Subscription testSub2 = setupSubscription(testPlanet1, "subFuncSub2");

        // by id
        Subscription sub = planetManager.getSubscription(testSub1.getId());
        assertNotNull(sub);
        assertEquals("subFuncSub1", sub.getFeedURL());
        
        // by feed url
        sub = planetManager.getSubscription(testPlanet1, testSub2.getFeedURL());
        assertNotNull(sub);
        assertEquals("subFuncSub2", sub.getFeedURL());
        
        // count by planet retrieval
        Planet testPlanet = planetManager.getPlanetByHandle(testPlanet1.getHandle());
        assertEquals(2, testPlanet.getSubscriptions().size());
    }

}
