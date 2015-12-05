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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;


/**
 * Test Pings related business operations.
 */
public class PingsTest extends WebloggerTest {
    public static Log log = LogFactory.getLog(PingsTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    PingTarget testCommonPing = null;

    @Resource
    PingTargetManager pingTargetManager;

    public void setPingTargetManager(PingTargetManager pingTargetManager) {
        this.pingTargetManager = pingTargetManager;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        try {
            testUser = setupUser("wtTestUser");
            testWeblog = setupWeblog("wtTestWeblog", testUser);
            endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        testCommonPing = new PingTarget();
        testCommonPing.setId(WebloggerCommon.generateUUID());
        testCommonPing.setName("testCommonPing");
        testCommonPing.setPingUrl("http://localhost/testCommonPing");
    }

    @After
    public void tearDown() throws Exception {
        try {
            teardownWeblog(testWeblog.getId());
            teardownUser(testUser.getUserName());
            endSession(true);
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
        PingTarget ping;
        
        // create common ping
        pingTargetManager.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        endSession(true);
        
        // make sure common ping was stored
        ping = pingTargetManager.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getPingUrl(), ping.getPingUrl());
        
        // update common ping
        ping = pingTargetManager.getPingTarget(commonId);
        ping.setName("testtestCommon");
        pingTargetManager.savePingTarget(ping);
        endSession(true);
        
        // make sure common ping was updated
        ping = pingTargetManager.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals("testtestCommon", ping.getName());
        
        // delete common ping
        ping = pingTargetManager.getPingTarget(commonId);
        pingTargetManager.removePingTarget(ping);
        endSession(true);
        
        // make sure common ping was deleted
        ping = pingTargetManager.getPingTarget(commonId);
        assertNull(ping);
    }
    
    
    /**
     * Test lookup mechanisms ... id, all common for weblog
     */
    @Test
    public void testPingTargetLookups() throws Exception {
        PingTarget ping;
        
        // create common ping
        pingTargetManager.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        endSession(true);
        
        // lookup by id
        ping = pingTargetManager.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getName(), ping.getName());
        
        // lookup all common pings
        List commonPings = pingTargetManager.getCommonPingTargets();
        assertNotNull(commonPings);
        // correct answer is: 3 pings in config + 1 new one = 5
        assertEquals(4, commonPings.size());
        
        // delete common ping
        ping = pingTargetManager.getPingTarget(commonId);
        pingTargetManager.removePingTarget(ping);
        endSession(true);
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testAutoPingCRUD() throws Exception {
        AutoPing autoPing;
        
        // create ping target to use for tests
        PingTarget pingTarget = setupPingTarget("fooPing", "http://foo/null");
        PingTarget pingTarget2 = setupPingTarget("blahPing", "http://blah/null");
        endSession(true);
        
        // create autoPing
        autoPing = new AutoPing(pingTarget, testWeblog);
        pingTargetManager.saveAutoPing(autoPing);
        String id = autoPing.getId();
        endSession(true);
        
        // make sure autoPing was stored
        autoPing = pingTargetManager.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // update autoPing
        autoPing.setPingTarget(pingTargetManager.getPingTarget(pingTarget2.getId()));
        pingTargetManager.saveAutoPing(autoPing);
        endSession(true);
        
        // make sure autoPing was updated
        autoPing = pingTargetManager.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget2, autoPing.getPingTarget());
        
        // delete autoPing
        pingTargetManager.removeAutoPing(autoPing);
        endSession(true);
        
        // make sure common autoPing was deleted
        autoPing = pingTargetManager.getAutoPing(id);
        assertNull(autoPing);
        
        // teardown test ping target
        teardownPingTarget(pingTarget.getId());
        teardownPingTarget(pingTarget2.getId());
        endSession(true);
    }
    
    
    /**
     * Test special ping target removal methods ... by weblog/target, collection, all
     */
    @Test
    public void testPingTargetRemovals() throws Exception {
        AutoPing testAutoPing;
        
        // create ping target to use for tests
        PingTarget pingTarget = setupPingTarget("fooPing", "http://foo/null");
        PingTarget pingTarget2 = setupPingTarget("blahPing", "http://blah/null");
        PingTarget pingTarget3 = setupPingTarget("gahPing", "http://gah/null");
        
        try {
        
            // create auto pings for test
            testWeblog = getManagedWeblog(testWeblog);
            AutoPing autoPing = setupAutoPing(pingTarget, testWeblog);
            setupAutoPing(pingTarget2, testWeblog);
            setupAutoPing(pingTarget3, testWeblog);
            endSession(true);

            // remove by weblog/target
            testWeblog = getManagedWeblog(testWeblog);
            pingTarget = pingTargetManager.getPingTarget(pingTarget.getId());
            pingTargetManager.removeAutoPing(pingTarget, testWeblog);
            endSession(true);

            // make sure remove succeeded
            testAutoPing = pingTargetManager.getAutoPing(autoPing.getId());
            assertNull(testAutoPing);

            // need to create more test pings
            setupAutoPing(pingTarget, testWeblog);
            setupAutoPing(pingTarget2, testWeblog);
            setupAutoPing(pingTarget3, testWeblog);
            endSession(true);

            // remove all
            pingTargetManager.removeAllAutoPings();
            endSession(true);

            // make sure remove succeeded
            testWeblog = getManagedWeblog(testWeblog);
            List autoPings = pingTargetManager.getAutoPingsByWeblog(testWeblog);
            assertNotNull(autoPings);
            assertEquals(0, autoPings.size());
        
        } finally {
            // teardown test ping target
            teardownPingTarget(pingTarget.getId());
            teardownPingTarget(pingTarget2.getId());
            endSession(true);
        }
    }
    
    
    /**
     * Test lookup mechanisms ... id, ping target, weblog
     */
    @Test
    public void testAutoPingLookups() throws Exception {
        AutoPing autoPing;
        
        // create autoPing target to use for tests
        PingTarget pingTarget = setupPingTarget("fooPing", "http://foo/null");
        endSession(true);
        
        // create autoPing
        testWeblog = getManagedWeblog(testWeblog);
        pingTarget = pingTargetManager.getPingTarget(pingTarget.getId());
        autoPing = new AutoPing(pingTarget, testWeblog);
        pingTargetManager.saveAutoPing(autoPing);
        String id = autoPing.getId();
        endSession(true);
        
        // lookup by id
        autoPing = pingTargetManager.getAutoPing(id);
        assertNotNull(autoPing);
        assertEquals(pingTarget, autoPing.getPingTarget());
        
        // lookup by ping target
        pingTarget = pingTargetManager.getPingTarget(pingTarget.getId());
        List autoPings = pingTargetManager.getAutoPingsByTarget(pingTarget);
        assertNotNull(autoPings);
        assertEquals(1, autoPings.size());
        
        // lookup by weblog
        testWeblog = getManagedWeblog(testWeblog);
        autoPings = pingTargetManager.getAutoPingsByWeblog(testWeblog);
        assertNotNull(autoPing);
        assertEquals(1, autoPings.size());
        
        // delete autoPing
        autoPing = pingTargetManager.getAutoPing(autoPing.getId());
        pingTargetManager.removeAutoPing(autoPing);
        endSession(true);
        
        // teardown test ping target
        teardownPingTarget(pingTarget.getId());
        endSession(true);
    }

    private PingTarget setupPingTarget(String name, String url) throws Exception {
        PingTarget testPing = new PingTarget(name, url, false);
        pingTargetManager.savePingTarget(testPing);
        strategy.flush();

        PingTarget ping = pingTargetManager.getPingTarget(testPing.getId());
        if (ping == null) {
            throw new WebloggerException("error setting up ping target");
        }
        return ping;
    }

    private void teardownPingTarget(String id) throws Exception {
        PingTarget ping = pingTargetManager.getPingTarget(id);
        pingTargetManager.removePingTarget(ping);
        strategy.flush();
    }

    private AutoPing setupAutoPing(PingTarget ping, Weblog weblog) throws Exception {
        AutoPing autoPing = new AutoPing(ping, getManagedWeblog(weblog));
        pingTargetManager.saveAutoPing(autoPing);
        strategy.flush();

        autoPing = pingTargetManager.getAutoPing(autoPing.getId());
        if (autoPing == null) {
            throw new WebloggerException("error setting up auto ping");
        }
        return autoPing;
    }
}
