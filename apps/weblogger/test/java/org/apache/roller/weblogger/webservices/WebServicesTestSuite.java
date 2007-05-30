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

package org.apache.roller.weblogger.webservices;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.roller.weblogger.webservices.adminapi.AappTest;
import org.apache.roller.weblogger.webservices.adminapi.HandlerBaseTest;
import org.apache.roller.weblogger.webservices.adminapi.MemberHandlerTest;
import org.apache.roller.weblogger.webservices.adminapi.UserHandlerTest;
import org.apache.roller.weblogger.webservices.adminapi.WeblogHandlerTest;
import org.apache.roller.weblogger.webservices.adminapi.sdk.MemberEntryTest;
import org.apache.roller.weblogger.webservices.adminapi.sdk.UserEntryTest;
import org.apache.roller.weblogger.webservices.adminapi.sdk.WeblogEntryTest;
import org.apache.roller.weblogger.webservices.xmlrpc.RollerXmlRpcServerTest;


/**
 * Test web services classes, some tests require mock web container.
 */
public class WebServicesTestSuite {
    
    public static Test suite() {

        TestSuite suite = new TestSuite();
	
        suite.addTestSuite(RollerXmlRpcServerTest.class);
        
        suite.addTestSuite(AappTest.class);
        suite.addTestSuite(HandlerBaseTest.class);
        suite.addTestSuite(MemberHandlerTest.class);
        suite.addTestSuite(UserHandlerTest.class);
        suite.addTestSuite(WeblogHandlerTest.class);
        
        suite.addTestSuite(MemberEntryTest.class);
        suite.addTestSuite(UserEntryTest.class);
        suite.addTestSuite(WeblogEntryTest.class);
        
        return suite;
    }
    
}
