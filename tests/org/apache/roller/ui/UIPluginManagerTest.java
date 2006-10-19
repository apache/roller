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

package org.apache.roller.ui;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.plugins.UIPluginManager;


/**
 * Test Plugin Management business layer operations.
 */
public class UIPluginManagerTest extends TestCase {
    
    private static Log log = LogFactory.getLog(UIPluginManagerTest.class);
    
    
    public UIPluginManagerTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(UIPluginManagerTest.class);
    }
    
    
    public void setUp() throws Exception { }
    public void tearDown() throws Exception { }
    
    
    public void testEntryEditors() throws Exception {
        
        UIPluginManager pmgr = RollerContext.getUIPluginManager();
        
        // test getEditors() lis
        assertEquals(2, pmgr.getWeblogEntryEditors().size());
        
        // test getting a single editor
        assertEquals("TextEditor", pmgr.getWeblogEntryEditor("TextEditor").getId());
        
        // make sure we return default editor if editor id is not found
        assertEquals("TextEditor", pmgr.getWeblogEntryEditor(null).getId());
    }
    
}
