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

import java.util.List;
import junit.framework.TestCase;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.TestUtils;


/**
 * Test planet Group functionality.
 */
public class PlanetTest extends TestCase {
    
    protected void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    protected void tearDown() throws Exception {
    }

    public void testPlanetLookups() throws Exception {
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        Planet testPlanet = TestUtils.setupPlanet("planetFuncTest1");

        // lookup planet by id
        Planet planet = mgr.getPlanetById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("planetFuncTest1", planet.getHandle());
        
        // lookup planet by handle
        planet = mgr.getPlanet(testPlanet.getHandle());
        assertNotNull(planet);
        assertEquals("planetFuncTest1", planet.getHandle());
        
        // lookup all planets in system
        List<Planet> planets = mgr.getPlanets();
        assertNotNull(planets);
        assertEquals(1, planets.size());

        TestUtils.teardownPlanet(testPlanet.getHandle());
    }

    public void testPlanetCRUD() throws Exception {
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();

        Planet localPlanet = new Planet("testPlanetHandle", "testPlanetTitle", "testPlanetDesc");
        Planet planet;

        planet = mgr.getPlanet("testPlanetHandle");
        assertNull(planet);

        // add
        mgr.savePlanet(localPlanet);
        TestUtils.endSession(true);

        // verify
        planet = mgr.getPlanetById(localPlanet.getId());
        assertNotNull(planet);
        assertEquals("testPlanetHandle", planet.getHandle());

        // modify
        planet.setTitle("foo");
        mgr.savePlanet(planet);
        TestUtils.endSession(true);

        // verify
        planet = mgr.getPlanetById(localPlanet.getId());
        assertNotNull(planet);
        assertEquals("foo", planet.getTitle());

        // remove
        mgr.deletePlanet(planet);
        TestUtils.endSession(true);

        // verify
        planet = mgr.getPlanetById(localPlanet.getId());
        assertNull(planet);
    }

}
