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
package org.tightblog.business;

import java.util.List;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.PingTarget;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.util.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;


/**
 * Test Pings related business operations.
 */
public class PingsTest extends WebloggerTest {

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

        testUser = setupUser("wtTestUser");
        testWeblog = setupWeblog("wtTestWeblog", testUser);
        endSession(true);

        testCommonPing = new PingTarget();
        testCommonPing.setId(Utilities.generateUUID());
        testCommonPing.setName("testCommonPing");
        testCommonPing.setPingUrl("http://localhost/testCommonPing");
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);

        testCommonPing = null;
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testPingTargetCRUD() throws Exception {
        PingTarget ping;
        
        // create ping target
        pingTargetManager.savePingTarget(testCommonPing);
        String commonId = testCommonPing.getId();
        endSession(true);
        
        // make sure ping target was stored
        ping = pingTargetManager.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals(testCommonPing.getPingUrl(), ping.getPingUrl());
        
        // update ping target
        ping = pingTargetManager.getPingTarget(commonId);
        ping.setName("testtestCommon");
        pingTargetManager.savePingTarget(ping);
        endSession(true);
        
        // make sure ping target was updated
        ping = pingTargetManager.getPingTarget(commonId);
        assertNotNull(ping);
        assertEquals("testtestCommon", ping.getName());
        
        // delete ping target
        ping = pingTargetManager.getPingTarget(commonId);
        pingTargetManager.removePingTarget(ping);
        endSession(true);
        
        // make sure ping target was deleted
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
        List commonPings = pingTargetManager.getPingTargets();
        assertNotNull(commonPings);
        // correct answer is: 3 pings in config + 1 new one = 5
        assertEquals(4, commonPings.size());
        
        // delete common ping
        ping = pingTargetManager.getPingTarget(commonId);
        pingTargetManager.removePingTarget(ping);
        endSession(true);
    }
    
}
