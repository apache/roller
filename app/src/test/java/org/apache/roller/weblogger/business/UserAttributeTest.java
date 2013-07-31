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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;

/**
 * Test User related business operations.
 */
public class UserAttributeTest extends TestCase {    
    public static Log log = LogFactory.getLog(UserAttributeTest.class);
    
    
    public UserAttributeTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(UserAttributeTest.class);
    }
    
    
    public void setUp() throws Exception {
        // setup weblogger
        TestUtils.setupWeblogger();
    }
    
    public void tearDown() throws Exception {
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testUserAttributeCRUD() throws Exception {
        log.debug("BEGIN");
        User user = TestUtils.setupUser("auser");

        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        umgr.setUserAttribute(user.getUserName(), "testattname", "testattvalue");
        TestUtils.endSession(true);
        
        assertEquals(user.getId(), umgr.getUserByAttribute("testattname", "testattvalue").getId());
        
        user = TestUtils.getManagedUser(user);
        user.setEnabled(Boolean.FALSE);
        umgr.saveUser(user);
        TestUtils.endSession(true);
        
        assertNull(umgr.getUserByAttribute("testattname", "testattvalue"));
        
        TestUtils.teardownUser(user.getUserName());

        assertEquals(0, umgr.getUserAttributes("auser").size());
        
        log.debug("END");
    }
}


