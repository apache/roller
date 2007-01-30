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

package org.apache.roller.business;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test core weblog services.
 *
 * This test suite should test all of the most essential and core functions of
 * a weblog, such as managing entries, categories, comments, etc.  These items
 * are expected to be dependent on the core weblog platform to function.
 */
public class CoreWeblogServicesTestSuite {
    
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        // test categories
        suite.addTestSuite(WeblogCategoryCRUDTest.class);
        suite.addTestSuite(WeblogCategoryParentDeletesTest.class);
        suite.addTestSuite(WeblogCategoryTest.class);
        
        // test templates
        suite.addTestSuite(WeblogPageTest.class);
        
        // test entries
        suite.addTestSuite(WeblogEntryTest.class);
        
        // test comments
        suite.addTestSuite(CommentTest.class);

        return suite;
    }
    
}
