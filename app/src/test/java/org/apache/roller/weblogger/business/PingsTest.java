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
/*
 * PingsTest.java
 *
 * Created on April 9, 2006, 1:27 PM
 */

package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Pings related business operations.
 */
public class PingsTest  {
    
    public static Log log = LogFactory.getLog(PingsTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    PingTarget testCommonPing = null;


    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("wtTestUser");
            testWeblog = TestUtils.setupWeblog("wtTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        testCommonPing = new PingTarget();
        testCommonPing.setName("testCommonPing");
        testCommonPing.setPingUrl("http://localhost/testCommonPing");
    }

    @AfterEach
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        testCommonPing = null;
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testPingTargetCRUD() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        PingTarget ping = null;
        
        // create common ping
        mgr.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        TestUtils.endSession(true);
        
        // make sure common ping was stored
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getPingUrl(), ping.getPingUrl());
        
        // update common ping
        ping = null;
        ping = mgr.getPingTarget(commonId);
        ping.setName("testtestCommon");
        mgr.savePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure common ping was updated
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals("testtestCommon", ping.getName());
        
        // delete common ping
        ping = null;
        ping = mgr.getPingTarget(commonId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure common ping was deleted
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNull(ping);
    }
    
    
    /**
     * Test lookup mechanisms ... id, all common for weblog
     */
    @Test
    public void testPingTargetLookups() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        PingTarget ping = null;
        
        // create common ping
        mgr.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getName(), ping.getName());
        
        // lookup all common pings
        List<PingTarget> commonPings = mgr.getCommonPingTargets();
        assertNotNull(commonPings);
        // correct answer is: 4 pings in config + 1 new one = 5
        assertEquals(5, commonPings.size());
        
        // delete common ping
        ping = null;
        ping = mgr.getPingTarget(commonId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testAutoPingCRUD() throws Exception {
        
        AutoPingManager mgr = WebloggerFactory.getWeblogger().getAutopingManager();
        PingTargetManager pingMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        AutoPing autoPing = null;
        
        // create ping target to use for tests
        PingTarget pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        PingTarget pingTarget2 = TestUtils.setupPingTarget("blahPing", "http://blah/null");
        TestUtils.endSession(true);
        
        // create autoPing
        autoPing = new AutoPing(null, pingTarget, testWeblog);
        mgr.saveAutoPing(autoPing);
        String id = autoPing.getId();
        TestUtils.endSession(true);
        
        // make sure autoPing was stored
        autoPing = null;
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // update autoPing
        autoPing.setPingTarget(pingMgr.getPingTarget(pingTarget2.getId()));
        mgr.saveAutoPing(autoPing);
        TestUtils.endSession(true);
        
        // make sure autoPing was updated
        autoPing = null;
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget2, autoPing.getPingTarget());
        
        // delete autoPing
        mgr.removeAutoPing(autoPing);
        TestUtils.endSession(true);
        
        // make sure common autoPing was deleted
        autoPing = null;
        autoPing = mgr.getAutoPing(id);
        assertNull(autoPing);
        
        // teardown test ping target
        TestUtils.teardownPingTarget(pingTarget.getId());
        TestUtils.teardownPingTarget(pingTarget2.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test special ping target removal methods ... by weblog/target, collection, all
     */
    @Test
    public void testPingTargetRemovals() throws Exception {
        
        AutoPingManager mgr = WebloggerFactory.getWeblogger().getAutopingManager();
        PingTargetManager ptmgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        AutoPing testAutoPing = null;
        
        // create ping target to use for tests
        PingTarget pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        PingTarget pingTarget2 = TestUtils.setupPingTarget("blahPing", "http://blah/null");
        PingTarget pingTarget3 = TestUtils.setupPingTarget("gahPing", "http://gah/null");
        
        try {
        
            // create auto pings for test
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            AutoPing autoPing = TestUtils.setupAutoPing(pingTarget, testWeblog);
            AutoPing autoPing2 = TestUtils.setupAutoPing(pingTarget2, testWeblog);
            AutoPing autoPing3 = TestUtils.setupAutoPing(pingTarget3, testWeblog);
            TestUtils.endSession(true);

            // remove by weblog/target
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            pingTarget = ptmgr.getPingTarget(pingTarget.getId());
            mgr.removeAutoPing(pingTarget, testWeblog);
            TestUtils.endSession(true);

            // make sure remove succeeded
            testAutoPing = null;
            testAutoPing = mgr.getAutoPing(autoPing.getId());
            assertNull(testAutoPing);

            // remove a collection
            List<AutoPing> autoPings = new ArrayList<>();
            autoPing2 = mgr.getAutoPing(autoPing2.getId()); //Get managed version of autoPing2
            autoPings.add(autoPing2);
            autoPing3 = mgr.getAutoPing(autoPing3.getId()); //Get managed version of autoPing2
            autoPings.add(autoPing3);
            mgr.removeAutoPings(autoPings);
            TestUtils.endSession(true);

            // make sure delete was successful
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            autoPings = mgr.getAutoPingsByWebsite(testWeblog);
            assertNotNull(autoPings);
            assertEquals(0, autoPings.size());

            // need to create more test pings
            autoPing = TestUtils.setupAutoPing(pingTarget, testWeblog);
            autoPing2 = TestUtils.setupAutoPing(pingTarget2, testWeblog);
            autoPing3 = TestUtils.setupAutoPing(pingTarget3, testWeblog);
            TestUtils.endSession(true);

            // remove all
            mgr.removeAllAutoPings();
            TestUtils.endSession(true);

            // make sure remove succeeded
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            autoPings = mgr.getAutoPingsByWebsite(testWeblog);
            assertNotNull(autoPings);
            assertEquals(0, autoPings.size());
        
        } finally {
            // teardown test ping target
            TestUtils.teardownPingTarget(pingTarget.getId());
            TestUtils.teardownPingTarget(pingTarget2.getId());
            TestUtils.endSession(true);
        }
    }
    
    
    /**
     * Test lookup mechanisms ... id, ping target, weblog
     */
    @Test
    public void testAutoPingLookups() throws Exception {
        
        AutoPingManager mgr = WebloggerFactory.getWeblogger().getAutopingManager();
        PingTargetManager ptmgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        AutoPing autoPing = null;
        
        // create autoPing target to use for tests
        PingTarget pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        TestUtils.endSession(true);
        
        // create autoPing
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        pingTarget = ptmgr.getPingTarget(pingTarget.getId());
        autoPing = new AutoPing(null, pingTarget, testWeblog);
        mgr.saveAutoPing(autoPing);
        String id = autoPing.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        autoPing = null;
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // lookup by ping target
        pingTarget = ptmgr.getPingTarget(pingTarget.getId());
        List<AutoPing> autoPings = mgr.getAutoPingsByTarget(pingTarget);
        assertNotNull(autoPings);
        assertEquals(1, autoPings.size());
        
        // lookup by weblog
        autoPings = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        autoPings = mgr.getAutoPingsByWebsite(testWeblog);
        assertNotNull(autoPing);
        assertEquals(1, autoPings.size());
        
        // delete autoPing
        autoPing = mgr.getAutoPing(autoPing.getId());
        mgr.removeAutoPing(autoPing);
        TestUtils.endSession(true);
        
        // teardown test ping target
        TestUtils.teardownPingTarget(pingTarget.getId());
        TestUtils.endSession(true);
    }

}
