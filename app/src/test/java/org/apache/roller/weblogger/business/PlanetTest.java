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
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.Planet;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test planet Group functionality.
 */
public class PlanetTest extends WebloggerTest {

    @Test
    public void testPlanetLookups() throws Exception {
        Planet testPlanet = setupPlanet("planetFuncTest1");

        // lookup planet by id
        Planet planet = planetManager.getPlanet(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("planetFuncTest1", planet.getHandle());
        
        // lookup planet by handle
        planet = planetManager.getPlanetByHandle(testPlanet.getHandle());
        assertNotNull(planet);
        assertEquals("planetFuncTest1", planet.getHandle());
        
        // lookup all planets in system
        List<Planet> planets = planetManager.getPlanets();
        assertNotNull(planets);
        assertEquals(1, planets.size());

        teardownPlanet(testPlanet.getHandle());
    }

    @Test
    public void testPlanetCRUD() throws Exception {
        Planet localPlanet = new Planet("testPlanetHandle", "testPlanetTitle", "testPlanetDesc");
        Planet planet;

        planet = planetManager.getPlanetByHandle("testPlanetHandle");
        assertNull(planet);

        // add
        planetManager.savePlanet(localPlanet);
        endSession(true);

        // verify
        planet = planetManager.getPlanet(localPlanet.getId());
        assertNotNull(planet);
        assertEquals("testPlanetHandle", planet.getHandle());

        // modify
        planet.setTitle("foo");
        planetManager.savePlanet(planet);
        endSession(true);

        // verify
        planet = planetManager.getPlanet(localPlanet.getId());
        assertNotNull(planet);
        assertEquals("foo", planet.getTitle());

        // remove
        planetManager.deletePlanet(planet);
        endSession(true);

        // verify
        planet = planetManager.getPlanet(localPlanet.getId());
        assertNull(planet);
    }
}
