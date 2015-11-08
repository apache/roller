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

import java.util.Date;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.TestUtils;


/**
 * Test planet Entry functionality.
 */
public class SubscriptionEntryTest extends TestCase {
    
    public static Log log = LogFactory.getLog(SubscriptionEntryTest.class);
    
    private Planet testPlanet = null;
    private Subscription testSub = null;
    private SubscriptionEntry testEntry = null;

    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();

        testPlanet = TestUtils.setupPlanet("testPlanet");
        testSub = TestUtils.setupSubscription(testPlanet, "testSubscription");
        testEntry = TestUtils.setupEntry(testSub, "testEntry");
        TestUtils.setupEntry(testSub, "testEntry2");

        WebloggerFactory.getWeblogger().getPlanetManager().savePlanet(testPlanet);
        WebloggerFactory.flush();
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownPlanet(testPlanet.getHandle());
    }


    public void testEntryCRUD() throws Exception {

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(testSub.getId());

        SubscriptionEntry testEntry = new SubscriptionEntry();
        testEntry.setPermalink("entryBasics");
        testEntry.setTitle("entryBasics");
        testEntry.setPubTime(new java.sql.Timestamp(System.currentTimeMillis()));
        testEntry.setSubscription(sub);

        // add
        mgr.saveEntry(testEntry);
        TestUtils.endSession(true);

        // verify
        SubscriptionEntry entry;
        entry = mgr.getEntryById(testEntry.getId());
        assertNotNull(entry);
        assertEquals("entryBasics", entry.getPermalink());

        // modify
        entry.setTitle("foo");
        mgr.saveEntry(entry);
        TestUtils.endSession(true);

        // verify
        entry = mgr.getEntryById(testEntry.getId());
        assertNotNull(entry);
        assertEquals("foo", entry.getTitle());

        // remove
        mgr.deleteEntry(entry);
        TestUtils.endSession(true);

        // verify
        entry = mgr.getEntryById(testEntry.getId());
        assertNull(entry);
    }

    public void testEntryLookups() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // by id
        SubscriptionEntry entry = mgr.getEntryById(testEntry.getId());
        assertNotNull(entry);
        assertEquals("testEntry", entry.getPermalink());
        
        // by subscription
        Subscription sub = mgr.getSubscriptionById(testSub.getId());
        assertEquals(2, sub.getEntries().size());
        
        // by subscription through manager
        assertEquals(2, mgr.getEntries(sub, 0, 10).size());
        
        // by planet
        Planet planet = mgr.getPlanetById(testPlanet.getId());
        assertEquals(2, mgr.getEntries(planet, 0, 10).size());
        
        // by planet with timeframe constraint
        assertEquals(0, mgr.getEntries(planet, new Date(), null, 0, 10).size());
    }
    
    
    public void testDeleteEntries() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Subscription sub = mgr.getSubscriptionById(testSub.getId());
        
        // make sure entries are there
        assertEquals(2, sub.getEntries().size());
        
        // purge entries
        mgr.deleteEntries(sub);
        TestUtils.endSession(true);
        
        // verify
        sub = mgr.getSubscriptionById(testSub.getId());
        assertEquals(0, sub.getEntries().size());
    }
    
}
