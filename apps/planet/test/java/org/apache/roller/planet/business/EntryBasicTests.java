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

import java.util.Date;
import junit.framework.TestCase;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.PlanetEntryData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * Test Entry CRUD.
 */
public class EntryBasicTests extends TestCase {
    
    private PlanetSubscriptionData testSub = null;
    
    
    protected void setUp() throws Exception {
        testSub = TestUtils.setupSubscription("entryBasicTest");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownSubscription(testSub.getId());
    }
    
    
    public void testEntryCRUD() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetSubscriptionData sub = mgr.getSubscriptionById(testSub.getId());
        
        PlanetEntryData testEntry = new PlanetEntryData();
        testEntry.setPermalink("entryBasics");
        testEntry.setTitle("entryBasics");
        testEntry.setPubTime(new java.sql.Timestamp(System.currentTimeMillis()));
        testEntry.setSubscription(sub);
        
        // add
        mgr.saveEntry(testEntry);
        TestUtils.endSession(true);
        
        // verify
        PlanetEntryData entry = null;
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
