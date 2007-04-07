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

package org.apache.roller.ui.rendering.plugins;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.ServletTestBase;
import org.apache.roller.ui.authoring.struts.actions.WeblogEntryActionTest;

/**
 * Test smileys plugin.
 */
public class SmileysTest extends ServletTestBase {
    
    private static Log log = LogFactory.getLog(WeblogEntryActionTest.class);
    
    private WebsiteData testWeblog = null;
    private UserData testUser = null;
    
    
    public SmileysTest() {
        super();
    }
    
    
    public static Test suite() {
        return new TestSuite(SmileysTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        super.setUp();
        try {
            testUser = TestUtils.setupUser("bkmrkTestUser");
            testWeblog = TestUtils.setupWeblog("bkmrkTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    public void testSmileEmoticon() throws Exception {
        doFilters();
        
        SmileysPlugin plugin = new SmileysPlugin();
        plugin.init(testWeblog);
        assertTrue( SmileysPlugin.smileyPatterns.length > 0 );
        
        String test = "put on a happy :-) face";
        String expected =
                "put on a happy <img src=\"http://localhost:8080/roller/images/smileys/smile.gif" +
                "\" class=\"smiley\" alt=\":-)\" title=\":-)\" /> face";
        String result = plugin.render(null, test);
        assertEquals(expected, result);
    }
    
}
