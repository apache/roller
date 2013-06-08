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
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test Group CRUD.
 */
public class GroupBasicTests extends TestCase {
    
    private Planet testPlanet = null;
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    public void testGroupCRUD() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        PlanetGroup testGroup = new PlanetGroup();
        testGroup.setDescription("test_group_desc");
        testGroup.setHandle("test_handle");
        testGroup.setTitle("test_title");
        testGroup.setPlanet(testPlanet);
        PlanetGroup group = null;
        
        group = mgr.getGroup(testPlanet, "test_handle");
        assertNull(group);
        
        // add
        mgr.saveGroup(testGroup);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = mgr.getGroupById(testGroup.getId());
        assertNotNull(group);
        assertEquals("test_handle", group.getHandle());
        assertEquals(testPlanet.getId(), group.getWeblogger().getId());
        
        // modify
        group.setTitle("foo");
        mgr.saveGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = mgr.getGroupById(testGroup.getId());
        assertNotNull(group);
        assertEquals("foo", group.getTitle());
        
        // remove
        mgr.deleteGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = null;
        group = mgr.getGroupById(testGroup.getId());
        assertNull(group);
    }
    
}
