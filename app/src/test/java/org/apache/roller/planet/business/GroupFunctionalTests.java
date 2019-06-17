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

import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Test planet Group functionality.
 */
public class GroupFunctionalTests  {
    
    private Planet testPlanet = null;
    private PlanetGroup testGroup1 = null;
    private PlanetGroup testGroup2 = null;
    
    @BeforeEach
    public void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();

        testPlanet = TestUtils.setupPlanet("groupFuncTest");
        testGroup1 = TestUtils.setupGroup(testPlanet, "groupFuncTest1");
        testGroup2 = TestUtils.setupGroup(testPlanet, "groupFuncTest2");
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownGroup(testGroup1.getId());
        TestUtils.teardownGroup(testGroup2.getId());
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    @Test
    public void testGroupLookups() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
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
        Planet planet = mgr.getWebloggerById(testPlanet.getId());
        Set groups = planet.getGroups();
        assertNotNull(groups);
        assertEquals(2, groups.size());
    }
    
}
