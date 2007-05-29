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

package org.apache.roller.business;

import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.Weblog;


/**
 * Test Pings related business operations.
 */
public class PingsTest extends TestCase {
    
    public static Log log = LogFactory.getLog(PingsTest.class);
    
    UserData testUser = null;
    Weblog testWeblog = null;
    PingTargetData testCommonPing = null;
    PingTargetData testCustomPing = null;
    
    
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
        
        try {
            testUser = TestUtils.setupUser("wtTestUser");
            testWeblog = TestUtils.setupWeblog("wtTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        testCommonPing = new PingTargetData();
        testCommonPing.setName("testCommonPing");
        testCommonPing.setPingUrl("http://localhost/testCommonPing");
        
        testCustomPing = new PingTargetData();
        testCustomPing.setName("testCommonPing");
        testCustomPing.setPingUrl("http://localhost/testCommonPing");
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        testCommonPing = null;
        testCustomPing = null;
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testPingTargetCRUD() throws Exception {
        
        PingTargetManager mgr = RollerFactory.getRoller().getPingTargetManager();
        PingTargetData ping = null;
        
        // create common ping
        mgr.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        TestUtils.endSession(true);
        
        // make sure common ping was stored
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getPingUrl(), ping.getPingUrl());
        
        // create custom ping
        testCustomPing.setWebsite(TestUtils.getManagedWebsite(testWeblog));
        mgr.savePingTarget(testCustomPing);
        String customId = testCustomPing.getId();
        TestUtils.endSession(true);
        
        // make sure custom ping was stored
        ping = null;
        ping = mgr.getPingTarget(customId);
        assertNotNull(ping);
        assertEquals(testCustomPing.getPingUrl(), ping.getPingUrl());
        
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
        
        // update custom ping
        ping = null;
        ping = mgr.getPingTarget(customId);
        ping.setName("testtestCustom");
        mgr.savePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure custom ping was updated
        ping = null;
        ping = mgr.getPingTarget(customId);
        assertNotNull(ping);
        assertEquals("testtestCustom", ping.getName());
        
        // delete common ping
        ping = null;
        ping = mgr.getPingTarget(commonId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure common ping was deleted
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNull(ping);
        
        // delete custom ping
        ping = null;
        ping = mgr.getPingTarget(customId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
        
        // make sure custom ping was deleted
        ping = null;
        ping = mgr.getPingTarget(customId);
        assertNull(ping);
    }
    
    
    /**
     * Test lookup mechanisms ... id, all common, all custom for weblog
     */
    public void testPingTargetLookups() throws Exception {
        
        PingTargetManager mgr = RollerFactory.getRoller().getPingTargetManager();
        PingTargetData ping = null;
        
        // create common ping
        mgr.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        TestUtils.endSession(true);
        
        // create custom ping
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testCustomPing.setWebsite(testWeblog);
        mgr.savePingTarget(testCustomPing);
        String customId = testCustomPing.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        ping = null;
        ping = mgr.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getName(), ping.getName());
        
        // lookup all common pings
        List commonPings = mgr.getCommonPingTargets();
        assertNotNull(commonPings);
        assertEquals(1, commonPings.size());
        
        // lookup all custom pings for weblog
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        List customPings = mgr.getCustomPingTargets(testWeblog);
        assertNotNull(customPings);
        assertEquals(1, customPings.size());
        
        // delete common ping
        ping = null;
        ping = mgr.getPingTarget(commonId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
        
        // delete custom ping
        ping = null;
        ping = mgr.getPingTarget(customId);
        mgr.removePingTarget(ping);
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testAutoPingCRUD() throws Exception {
        
        AutoPingManager mgr = RollerFactory.getRoller().getAutopingManager();
        AutoPingData autoPing = null;
        
        // create ping target to use for tests
        PingTargetData pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        PingTargetData pingTarget2 = TestUtils.setupPingTarget("blahPing", "http://blah/null");
        TestUtils.endSession(true);
        
        // create autoPing
        autoPing = new AutoPingData(null, pingTarget, testWeblog);
        mgr.saveAutoPing(autoPing);
        String id = autoPing.getId();
        TestUtils.endSession(true);
        
        // make sure autoPing was stored
        autoPing = null;
        autoPing = mgr.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // update autoPing
        autoPing.setPingTarget(pingTarget2);
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
    public void testPingTargetRemovals() throws Exception {
        
        AutoPingManager mgr = RollerFactory.getRoller().getAutopingManager();
        PingTargetManager ptmgr = RollerFactory.getRoller().getPingTargetManager();
        AutoPingData testAutoPing = null;
        
        // create ping target to use for tests
        PingTargetData pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        PingTargetData pingTarget2 = TestUtils.setupPingTarget("blahPing", "http://blah/null");
        PingTargetData pingTarget3 = TestUtils.setupPingTarget("gahPing", "http://gah/null");
        
        try {
        
            // create auto pings for test
            testWeblog = TestUtils.getManagedWebsite(testWeblog);
            AutoPingData autoPing = TestUtils.setupAutoPing(pingTarget, testWeblog);
            AutoPingData autoPing2 = TestUtils.setupAutoPing(pingTarget2, testWeblog);
            AutoPingData autoPing3 = TestUtils.setupAutoPing(pingTarget3, testWeblog);
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
            List autoPings = new ArrayList();
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
    public void testAutoPingLookups() throws Exception {
        
        AutoPingManager mgr = RollerFactory.getRoller().getAutopingManager();
        PingTargetManager ptmgr = RollerFactory.getRoller().getPingTargetManager();
        AutoPingData autoPing = null;
        
        // create autoPing target to use for tests
        PingTargetData pingTarget = TestUtils.setupPingTarget("fooPing", "http://foo/null");
        TestUtils.endSession(true);
        
        // create autoPing
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        pingTarget = ptmgr.getPingTarget(pingTarget.getId());
        autoPing = new AutoPingData(null, pingTarget, testWeblog);
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
        List autoPings = mgr.getAutoPingsByTarget(pingTarget);
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
    
    
    public void testApplicableAutoPings() throws Exception {
        
    }
    
    
    /**
     * Test that we can properly remove a ping target when it has
     * associated elements like auto pings and ping queue entries.
     */
    public void testRemoveLoadedPingTarget() throws Exception {
        // TODO: implement this test
    }
    
}
