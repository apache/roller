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

package org.apache.roller.weblogger.business.jpa;

import junit.framework.TestCase;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Test Weblogger Bookmark Management.
 */
public class JPAOAuthManagerTest extends TestCase {    
    public static Log log = LogFactory.getLog(JPAOAuthManagerTest.class);

    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tearDown", ex);
            throw new Exception("Test teardown failed", ex);
        }
    }

    public void testCRUD() throws Exception {
        JPAOAuthManagerImpl omgr = (JPAOAuthManagerImpl)
            WebloggerFactory.getWeblogger().getOAuthManager();

        String consumerKey = "1111";
        OAuthConsumer consumer = omgr.addConsumer("dummyusername", consumerKey);
        TestUtils.endSession(true);

        consumer = omgr.getConsumerByKey(consumer.consumerKey);
        assertNotNull(consumer);
        assertEquals(consumerKey, consumer.consumerKey);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.setProperty("userId", "dummyusername");
        omgr.addAccessor(accessor);
        TestUtils.endSession(true);

        accessor = omgr.getAccessorByKey(consumerKey);
        assertNotNull(accessor);

        omgr.removeAccessor(accessor);
        TestUtils.endSession(true);
        assertNull(omgr.getAccessorByKey(consumerKey));

        omgr.removeConsumer(consumer);
        TestUtils.endSession(true);
        assertNull(omgr.getConsumerByKey(consumerKey));
    }
}
