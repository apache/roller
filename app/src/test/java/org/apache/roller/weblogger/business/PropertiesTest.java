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

package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Properties related business operations.
 */
public class PropertiesTest  {
    
    public static Log log = LogFactory.getLog(PropertiesTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        // setup weblogger
        TestUtils.setupWeblogger();
    }

    @AfterEach
    public void tearDown() throws Exception {
    }


    @Test
    public void testProperiesCRUD() throws Exception {
        
        // remember, the properties table is initialized during Roller startup
        PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        TestUtils.endSession(true);
        
        RuntimeConfigProperty prop = null;
        
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
        prop = (RuntimeConfigProperty) props.get("site.name");
        prop.setValue("foofoo");
        prop = (RuntimeConfigProperty) props.get("site.description");
        prop.setValue("blahblah");
        mgr.saveProperties(props);
        TestUtils.endSession(true);
        
        // make sure all properties were updated
        props = mgr.getProperties();
        assertNotNull(props);
        assertEquals("foofoo", ((RuntimeConfigProperty)props.get("site.name")).getValue());
        assertEquals("blahblah", ((RuntimeConfigProperty)props.get("site.description")).getValue());
    }
    
}
