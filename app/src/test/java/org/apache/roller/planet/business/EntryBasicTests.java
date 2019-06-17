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

import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Test Entry CRUD.
 */
public class EntryBasicTests {
    
    private Subscription testSub = null;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        testSub = TestUtils.setupSubscription("entryBasicTest");
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub.getId());
    }
    
    @Test
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
        SubscriptionEntry entry = null;
        entry = mgr.getEntryById(testEntry.getId());
        assertNotNull(entry);
        assertEquals("entryBasics", entry.getPermalink());
        
        // modify
        entry.setTitle("foo");
        mgr.saveEntry(entry);
        TestUtils.endSession(true);
        
        // verify
        entry = null;
        entry = mgr.getEntryById(testEntry.getId());
        assertNotNull(entry);
        assertEquals("foo", entry.getTitle());
        
        // remove
        mgr.deleteEntry(entry);
        TestUtils.endSession(true);
        
        // verify
        entry = null;
        entry = mgr.getEntryById(testEntry.getId());
        assertNull(entry);
    }

}
