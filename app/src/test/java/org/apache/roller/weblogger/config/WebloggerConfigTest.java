/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
package org.apache.roller.weblogger.config;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

public class WebloggerConfigTest extends TestCase {
    
    private static final Map<String, String> props = new HashMap<>();
    
    static
    {
        props.put("defined.property.one", "value one");
        props.put("defined.property.two", "value two");
        props.put("defined.property.with.dollar.sign.in.value", "$2");
    }
    
    public void testExpansion() throws Exception {
        String expanded =
                WebloggerConfig.expandProperties("String with ${defined.property.one} and ${defined.property.two} " +
                        "and ${defined.property.with.dollar.sign.in.value} and ${undefined.property} and some stuff.",
                        props);
        
        assertEquals("Expanded string doesn't match expected",
                "String with value one and value two and $2 and ${undefined.property} and some stuff.",
                expanded);
    }
    
    public void testSystemProperty() throws Exception {
        String expanded =
                WebloggerConfig.expandProperties("${java.home}", System.getProperties());
        assertEquals("Expanded string doesn't match expected",
                System.getProperty("java.home"),
                expanded);
    }
}
