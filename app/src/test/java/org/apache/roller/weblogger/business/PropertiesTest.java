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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business;

import java.util.Map;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PropertiesTest extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testPropertiesCRUD() throws Exception {
        RuntimeConfigProperty prop;
        
        // get a property by name
        prop = propertiesManager.getProperty("site.name");
        assertNotNull(prop);
        
        // update a property
        prop.setValue("testtest");
        propertiesManager.saveProperty(prop);
        endSession(true);
        
        // make sure property was updated
        prop = propertiesManager.getProperty("site.name");
        assertNotNull(prop);
        assertEquals("testtest", prop.getValue());
        
        // get all properties
        Map<String, RuntimeConfigProperty> props = propertiesManager.getProperties();
        assertNotNull(props);
        assertTrue(props.containsKey("site.name"));
        
        // update multiple properties
        prop = props.get("site.name");
        prop.setValue("foofoo");
        prop = props.get("users.themes.path");
        prop.setValue("/my/new/themedir");
        propertiesManager.saveProperties(props);
        endSession(true);
        
        // make sure all properties were updated
        props = propertiesManager.getProperties();
        assertNotNull(props);
        assertEquals("foofoo", props.get("site.name").getValue());
        assertEquals("/my/new/themedir", props.get("users.themes.path").getValue());
    }
}
