/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.planet.business;

import java.util.Map;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.TestUtils;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.pojos.PropertyData;


/**
 * Test Properties related business operations.
 */
public class PropertiesTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PropertiesTest.class);
    
    
    public void setUp() throws Exception {
        // setup planet
        TestUtils.setupPlanet();

    }
    
    public void tearDown() throws Exception {}
    
    
    public void testProperiesCRUD() throws Exception {
        
        // remember, the properties table is initialized during Roller startup
        PropertiesManager mgr = PlanetFactory.getPlanet().getPropertiesManager();
        TestUtils.endSession(true);
        
        PropertyData prop = null;
        
        // get a property by name
        prop = mgr.getProperty("site.name");
        assertNotNull(prop);
        
        // update a property
        prop.setValue("testtest");
        mgr.saveProperty(prop);
        TestUtils.endSession(true);
        
        // make sure property was updated
        prop = null;
        prop = mgr.getProperty("site.name");
        assertNotNull(prop);
        assertEquals("testtest", prop.getValue());
        
        // get all properties
        Map props = mgr.getProperties();
        assertNotNull(props);
        assertTrue(props.containsKey("site.name"));
        
        // update multiple properties
        prop = (PropertyData) props.get("site.name");
        prop.setValue("foofoo");
        prop = (PropertyData) props.get("site.description");
        prop.setValue("blahblah");
        mgr.saveProperties(props);
        TestUtils.endSession(true);
        
        // make sure all properties were updated
        props = mgr.getProperties();
        assertNotNull(props);
        assertEquals("foofoo", ((PropertyData)props.get("site.name")).getValue());
        assertEquals("blahblah", ((PropertyData)props.get("site.description")).getValue());
    }
    
}
