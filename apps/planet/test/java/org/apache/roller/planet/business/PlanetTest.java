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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.PlanetData;


/**
 * Test database implementation of PlanetManager.
 */
public class PlanetTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PlanetTest.class);
    
    
    public void testPlanetCRUD() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        PlanetData testPlanet = new PlanetData("testPlanet", "testPlanet");
        PlanetData planet = null;
        
        planet = mgr.getPlanet("testPlanet");
        assertNull(planet);
        
        // add
        mgr.savePlanet(testPlanet);
        TestUtils.endSession(true);
        
        // verify
        planet = null;
        planet = mgr.getPlanetById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("testPlanet", planet.getHandle());
        
        // modify
        planet.setName("foo");
        mgr.savePlanet(planet);
        TestUtils.endSession(true);
        
        // verify
        planet = null;
        planet = mgr.getPlanetById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("foo", planet.getName());
        
        // remove
        mgr.deletePlanet(planet);
        TestUtils.endSession(true);
        
        // verify
        planet = null;
        planet = mgr.getPlanet(testPlanet.getId());
        assertNull(planet);
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testPlanetLookups() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        PlanetData testPlanet = new PlanetData("testPlanet", "testPlanet");
        PlanetData planet = null;
        
        // add
        mgr.savePlanet(testPlanet);
        TestUtils.endSession(true);
        
        // verify
        planet = null;
        planet = mgr.getPlanetById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("testPlanet", planet.getHandle());
        
        // by handle
        planet = null;
        planet = mgr.getPlanet("testPlanet");
        assertNotNull(planet);
        assertEquals("testPlanet", planet.getHandle());
        
        // remove
        mgr.deletePlanet(planet);
        TestUtils.endSession(true);
    }
    
}
