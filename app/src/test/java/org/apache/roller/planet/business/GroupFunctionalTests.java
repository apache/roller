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

import java.util.List;
import junit.framework.TestCase;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test planet Group functionality.
 */
public class GroupFunctionalTests extends TestCase {
    
    private PlanetGroup testGroup1 = null;
    private PlanetGroup testGroup2 = null;
    
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();

        testGroup1 = TestUtils.setupGroup("groupFuncTest1");
        testGroup2 = TestUtils.setupGroup("groupFuncTest2");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownGroup(testGroup1.getId());
        TestUtils.teardownGroup(testGroup2.getId());
    }
    
    
    public void testGroupLookups() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        // lookup group by id
        PlanetGroup group = mgr.getGroupById(testGroup1.getId());
        assertNotNull(group);
        assertEquals("groupFuncTest1", group.getHandle());
        
        // lookup group by planet & handle
        group = null;
        group = mgr.getGroup(testGroup1.getHandle());
        assertNotNull(group);
        assertEquals("groupFuncTest1", group.getHandle());
        
        // lookup all groups in planet
        List<PlanetGroup> groups = mgr.getPlanetGroups();
        assertNotNull(groups);
        assertEquals(2, groups.size());
    }
    
}
