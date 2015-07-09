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

package org.apache.roller.planet.business;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test Planet CRUD.
 */
public class PlanetBasicTests extends TestCase {
    
    public static Log log = LogFactory.getLog(PlanetBasicTests.class);
    
    
    public void testPlanetCRUD() throws Exception {
        
        // setup planet
        TestUtils.setupWeblogger();

        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        PlanetGroup testPlanet = new PlanetGroup("testPlanetHandle", "testPlanetTitle", "testPlanetDesc");
        PlanetGroup planet;
        
        planet = mgr.getGroup("testPlanetHandle");
        assertNull(planet);
        
        // add
        mgr.saveGroup(testPlanet);
        TestUtils.endSession(true);

        // verify
        planet = mgr.getGroupById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("testPlanetHandle", planet.getHandle());

        // modify
        planet.setTitle("foo");
        mgr.saveGroup(planet);
        TestUtils.endSession(true);

        // verify
        planet = mgr.getGroupById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("foo", planet.getTitle());
        
        // remove
        mgr.deleteGroup(planet);
        TestUtils.endSession(true);
        
        // verify
        planet = mgr.getGroupById(testPlanet.getId());
        assertNull(planet);
    }
    
}
