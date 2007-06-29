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

import java.util.Set;
import junit.framework.TestCase;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;


/**
 * Test planet Group functionality.
 */
public class GroupFunctionalTests extends TestCase {
    
    private Planet testPlanet = null;
    private PlanetGroup testGroup1 = null;
    private PlanetGroup testGroup2 = null;
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();

        testPlanet = TestUtils.setupPlanet("groupFuncTest");
        testGroup1 = TestUtils.setupGroup(testPlanet, "groupFuncTest1");
        testGroup2 = TestUtils.setupGroup(testPlanet, "groupFuncTest2");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownGroup(testGroup1.getId());
        TestUtils.teardownGroup(testGroup2.getId());
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    public void testGroupLookups() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        // lookup group by id
        PlanetGroup group = mgr.getGroupById(testGroup1.getId());
        assertNotNull(group);
        assertEquals("groupFuncTest1", group.getHandle());
        
        // lookup group by planet & handle
        group = null;
        group = mgr.getGroup(testPlanet, testGroup1.getHandle());
        assertNotNull(group);
        assertEquals("groupFuncTest1", group.getHandle());
        
        // lookup all groups in planet
        Planet planet = mgr.getPlanetById(testPlanet.getId());
        Set groups = planet.getGroups();
        assertNotNull(groups);
        assertEquals(2, groups.size());
    }
    
}
