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

import java.util.List;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test Planet functionality.
 */
public class PlanetFunctionalTests extends TestCase {
    
    public static Log log = LogFactory.getLog(PlanetFunctionalTests.class);
    
    private Planet testPlanet = null;
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();

        testPlanet = TestUtils.setupPlanet("planetFuncTest");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    /**
     * Test lookup mechanisms.
     */
    public void testPlanetLookups() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getWebloggerManager();
        
        Planet planet = null;
        
        // by id
        planet = mgr.getWebloggerById(testPlanet.getId());
        assertNotNull(planet);
        assertEquals("planetFuncTest", planet.getHandle());
        
        // by handle
        planet = null;
        planet = mgr.getWeblogger("planetFuncTest");
        assertNotNull(planet);
        assertEquals("planetFuncTest", planet.getHandle());
        
        // all planets
        List planets = mgr.getWebloggers();
        assertNotNull(planets);
        assertEquals(1, planets.size());
    }
    
}
