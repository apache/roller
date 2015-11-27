/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test Pings related business operations.
 */
public class PingsTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PingsTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    PingTarget testCommonPing = null;

    
    public PingsTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(PingsTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
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
        testCommonPing.setId(WebloggerCommon.generateUUID());
        testCommonPing.setName("testCommonPing");
        testCommonPing.setPingUrl("http://localhost/testCommonPing");
    }
    
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
    public void testPingTargetCRUD() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        PingTarget ping;
        
        // create common ping
        mgr.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        TestUtils.endSession(true);
        
        // make sure common ping was stored
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getPingUrl(), ping.getPingUrl());
        
        // update common ping
        ping = mgr.getPingTarget(commonId);
        ping.setName("testtestCommon");
        mgr.savePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure common ping was updated
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals("testtestCommon", ping.getName());
        
        // delete common ping
        ping = mgr.getPingTarget(commonId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure common ping was deleted
        ping = mgr.getPingTarget(commonId);
        assertNull(ping);
    }
    
    
    /**
     * Test lookup mechanisms ... id, all common for weblog
     */
    public void testPingTargetLookups() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        PingTarget ping;
        
        // create common ping
        mgr.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getName(), ping.getName());
        
        // lookup all common pings
        List commonPings = mgr.getCommonPingTargets();
        assertNotNull(commonPings);
        // correct answer is: 3 pings in config + 1 new one = 5
        assertEquals(4, commonPings.size());
        
        // delete common ping
        ping = mgr.getPingTarget(commonId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testAutoPingCRUD() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        AutoPing autoPing;
        
        // create ping target to use for tests
        PingTarget pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        PingTarget pingTarget2 = TestUtils.setupPingTarget("blahPing", "http://blah/null");
        TestUtils.endSession(true);
        
        // create autoPing
        autoPing = new AutoPing(pingTarget, testWeblog);
        mgr.saveAutoPing(autoPing);
        String id = autoPing.getId();
        TestUtils.endSession(true);
        
        // make sure autoPing was stored
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // update autoPing
        autoPing.setPingTarget(mgr.getPingTarget(pingTarget2.getId()));
        mgr.saveAutoPing(autoPing);
        TestUtils.endSession(true);
        
        // make sure autoPing was updated
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget2, autoPing.getPingTarget());
        
        // delete autoPing
        mgr.removeAutoPing(autoPing);
        TestUtils.endSession(true);
        
        // make sure common autoPing was deleted
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
    public void testPingTargetRemovals() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        AutoPing testAutoPing;
        
        // create ping target to use for tests
        PingTarget pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        PingTarget pingTarget2 = TestUtils.setupPingTarget("blahPing", "http://blah/null");
        PingTarget pingTarget3 = TestUtils.setupPingTarget("gahPing", "http://gah/null");
        
        try {
        
            // create auto pings for test
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            AutoPing autoPing = TestUtils.setupAutoPing(pingTarget, testWeblog);
            TestUtils.setupAutoPing(pingTarget2, testWeblog);
            TestUtils.setupAutoPing(pingTarget3, testWeblog);
            TestUtils.endSession(true);

            // remove by weblog/target
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            pingTarget = mgr.getPingTarget(pingTarget.getId());
            mgr.removeAutoPing(pingTarget, testWeblog);
            TestUtils.endSession(true);

            // make sure remove succeeded
            testAutoPing = mgr.getAutoPing(autoPing.getId());
            assertNull(testAutoPing);

            // need to create more test pings
            TestUtils.setupAutoPing(pingTarget, testWeblog);
            TestUtils.setupAutoPing(pingTarget2, testWeblog);
            TestUtils.setupAutoPing(pingTarget3, testWeblog);
            TestUtils.endSession(true);

            // remove all
            mgr.removeAllAutoPings();
            TestUtils.endSession(true);

            // make sure remove succeeded
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            List autoPings = mgr.getAutoPingsByWeblog(testWeblog);
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
    public void testAutoPingLookups() throws Exception {
        
        PingTargetManager mgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        AutoPing autoPing;
        
        // create autoPing target to use for tests
        PingTarget pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        TestUtils.endSession(true);
        
        // create autoPing
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        pingTarget = mgr.getPingTarget(pingTarget.getId());
        autoPing = new AutoPing(pingTarget, testWeblog);
        mgr.saveAutoPing(autoPing);
        String id = autoPing.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // lookup by ping target
        pingTarget = mgr.getPingTarget(pingTarget.getId());
        List autoPings = mgr.getAutoPingsByTarget(pingTarget);
        assertNotNull(autoPings);
        assertEquals(1, autoPings.size());
        
        // lookup by weblog
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        autoPings = mgr.getAutoPingsByWeblog(testWeblog);
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
    
    /**
     * Test that we can properly remove a ping target when it has
     * auto pings.
     */
    public void testRemoveLoadedPingTarget() throws Exception {
        // TODO: implement this test
    }
    
}
