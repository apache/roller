/*
 * GroupTest.java
 *
 * Created on December 15, 2006, 11:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.planet.business;

import java.util.List;
import junit.framework.TestCase;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.planet.pojos.PlanetGroupData;


/**
 * Test planet Group functionality.
 */
public class GroupTest extends TestCase {
    
    private PlanetData testPlanet = null;
    
    
    protected void setUp() throws Exception {
        testPlanet = TestUtils.setupPlanet("groupTestPlanet");
    }
    
    
    protected void tearDown() throws Exception {
        TestUtils.teardownPlanet(testPlanet.getId());
    }
    
    
    public void testGroupCRUD() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        
        PlanetGroupData testGroup = new PlanetGroupData();
        testGroup.setDescription("test_group_desc");
        testGroup.setHandle("test_handle");
        testGroup.setTitle("test_title");
        testGroup.setPlanet(testPlanet);
        PlanetGroupData group = null;
        
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
        assertEquals(testPlanet.getId(), group.getPlanet().getId());
        
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
    
    
    public void testGroupLookups() throws Exception {
        
        PlanetManager mgr = PlanetFactory.getPlanet().getPlanetManager();
        PlanetGroupData testGroup1 = null;
        PlanetGroupData testGroup2 = null;
        try {    
            testGroup1 = new PlanetGroupData();
            testGroup1.setDescription("group1");
            testGroup1.setHandle("test_group1");
            testGroup1.setTitle("test_group1");
            testGroup1.setPlanet(testPlanet);

            testGroup2 = new PlanetGroupData();
            testGroup2.setDescription("group2");
            testGroup2.setHandle("test_group2");
            testGroup2.setTitle("test_group2");
            testGroup2.setPlanet(testPlanet);

            PlanetGroupData group = null;
            List groups = null;

            // add
            mgr.saveGroup(testGroup1);
            mgr.saveGroup(testGroup2);
            TestUtils.endSession(true);

            // lookup group by id
            group = null;
            group = mgr.getGroupById(testGroup1.getId());
            assertNotNull(group);
            assertEquals("test_group1", group.getHandle());

            // lookup group in test planet by handle
            group = null;
            group = mgr.getGroup(testPlanet, testGroup1.getHandle());
            assertNotNull(group);
            assertEquals("test_group1", group.getHandle());

        } finally {
            testGroup1 = mgr.getGroupById(testGroup1.getId());
            testGroup2 = mgr.getGroupById(testGroup2.getId());
            mgr.deleteGroup(testGroup1);
            mgr.deleteGroup(testGroup2);
            TestUtils.endSession(true);
        }
    }
    
}
