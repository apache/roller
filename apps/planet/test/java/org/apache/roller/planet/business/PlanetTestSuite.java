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

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test core business services.
 *
 * The core business services are the things which allow the business layer as
 * a whole to function.  Examples would be the PropertiesManager which is
 * involved in servicing the core application config.
 *
 * Tests from from this suite should only include things that are not part of
 * or dependent on the core weblog platform, i.e. you don't need a user or a
 * weblog to do them.
 */
public class PlanetTestSuite {
    
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        // TODO: add a test for PlanetConfig
        
        suite.addTestSuite(PropertiesTest.class);
        
        // planets
        suite.addTestSuite(PlanetBasicTests.class);
        suite.addTestSuite(PlanetFunctionalTests.class);
        
        // groups
        suite.addTestSuite(GroupBasicTests.class);
        suite.addTestSuite(GroupFunctionalTests.class);
        
        // subscriptions
        suite.addTestSuite(SubscriptionBasicTests.class);
        suite.addTestSuite(SubscriptionFunctionalTests.class);
        
        // entries
        suite.addTestSuite(EntryBasicTests.class);
        suite.addTestSuite(EntryFunctionalTests.class);
        
        suite.addTestSuite(RomeFeedFetcherTest.class);

        return suite;
    }
    
}
