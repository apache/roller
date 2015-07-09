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
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test Group CRUD.
 */
public class GroupBasicTests extends TestCase {
    
    protected void setUp() throws Exception {
        // setup planet
        TestUtils.setupWeblogger();
    }
    
    
    public void testGroupCRUD() throws Exception {
        
        PlanetManager mgr = WebloggerFactory.getWeblogger().getPlanetManager();
        
        PlanetGroup testGroup = new PlanetGroup();
        testGroup.setDescription("test_group_desc");
        testGroup.setHandle("test_handle");
        testGroup.setTitle("test_title");
        PlanetGroup group = null;
        
        group = mgr.getGroup("test_handle");
        assertNull(group);
        
        // add
        mgr.saveGroup(testGroup);
        TestUtils.endSession(true);
        
        // verify
        group = mgr.getGroupById(testGroup.getId());
        assertNotNull(group);
        assertEquals("test_handle", group.getHandle());

        // modify
        group.setTitle("foo");
        mgr.saveGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = mgr.getGroupById(testGroup.getId());
        assertNotNull(group);
        assertEquals("foo", group.getTitle());
        
        // remove
        mgr.deleteGroup(group);
        TestUtils.endSession(true);
        
        // verify
        group = mgr.getGroupById(testGroup.getId());
        assertNull(group);
    }
    
}
