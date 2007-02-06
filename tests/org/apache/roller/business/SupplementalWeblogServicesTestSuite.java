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
 * Test supplemental weblog services.
 *
 * This test suite should test all of the supplemental functions of a weblog. 
 * These are the things that aren't quite as much of a core component as some
 * of the other services.  The main point of this is to break up the testing
 * into reasonable groupings though.
 */
public class SupplementalWeblogServicesTestSuite {
    
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        // file uploads
        suite.addTestSuite(FileManagerTest.class);
        
        // hit counts
        suite.addTestSuite(HitCountTest.class);
        
        // pings
        suite.addTestSuite(PingsTest.class);
        
        // folders and bookmarks
        suite.addTestSuite(FolderCRUDTest.class);
        suite.addTestSuite(FolderFunctionalityTest.class);
        suite.addTestSuite(FolderParentDeletesTest.class);
        suite.addTestSuite(BookmarkTest.class);
        
        // referrers
        suite.addTestSuite(RefererTest.class);

        return suite;
    }
    
}
