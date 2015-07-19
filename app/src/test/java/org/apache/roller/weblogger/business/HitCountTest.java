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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test HitCount related business operations.
 */
public class HitCountTest extends TestCase {
    
    public static Log log = LogFactory.getLog(HitCountTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    
    
    public HitCountTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(HitCountTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("hitCountTestUser");
            testWeblog = TestUtils.setupWeblog("hitCountTestWeblog", testUser);

            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
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
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    public void testHitCountCRUD() throws Exception {
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        Weblog aWeblog = mgr.getWeblog(testWeblog.getId());
        int oldHits = aWeblog.getHitsToday();
        aWeblog.setHitsToday(oldHits + 10);
        mgr.saveWeblog(aWeblog);
        TestUtils.endSession(true);
        
        // make sure it was stored
        aWeblog = mgr.getWeblog(testWeblog.getId());
        assertNotNull(aWeblog);
        assertEquals(aWeblog, testWeblog);
        assertEquals(oldHits + 10, aWeblog.getHitsToday());

        // test lookup by weblog
        int hitCount = mgr.getHitCount(testWeblog);
        assertEquals(oldHits + 10, hitCount);
    }

    public void testIncrementHitCount() throws Exception {
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        Weblog aWeblog = TestUtils.getManagedWebsite(testWeblog);
        aWeblog.setHitsToday(10);
        mgr.saveWeblog(aWeblog);
        TestUtils.endSession(true);
        
        // make sure it was created
        aWeblog = TestUtils.getManagedWebsite(testWeblog);
        assertNotNull(aWeblog);
        assertEquals(10, aWeblog.getHitsToday());
        
        // increment
        mgr.incrementHitCount(aWeblog, 25);
        TestUtils.endSession(true);
        
        // make sure it was incremented properly
        int hitCount = mgr.getHitCount(testWeblog);
        assertEquals(35, hitCount);
    }
    
    
    public void testResetHitCounts() throws Exception {
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        
        testUser = TestUtils.getManagedUser(testUser);
        Weblog blog1 = TestUtils.setupWeblog("hitCntTest1", testUser);
        Weblog blog2 = TestUtils.setupWeblog("hitCntTest2", testUser);
        Weblog blog3 = TestUtils.setupWeblog("hitCntTest3", testUser);
        
        blog1.setHitsToday(10);
        blog2.setHitsToday(20);
        blog3.setHitsToday(30);

        TestUtils.endSession(true);
        
        try {
            // make sure data was properly initialized
            int testCount;
            testCount = mgr.getHitCount(blog1);
            assertEquals(10, testCount);
            testCount = mgr.getHitCount(blog2);
            assertEquals(20, testCount);
            testCount = mgr.getHitCount(blog3);
            assertEquals(30, testCount);

            // reset count for one weblog
            blog1 = TestUtils.getManagedWebsite(blog1);
            mgr.resetHitCount(blog1);
            TestUtils.endSession(true);

            // make sure it reset only one weblog
            testCount = mgr.getHitCount(blog1);
            assertEquals(0, testCount);
            testCount = mgr.getHitCount(blog2);
            assertEquals(20, testCount);
            testCount = mgr.getHitCount(blog3);
            assertEquals(30, testCount);

            // reset all counts
            mgr.resetAllHitCounts();
            TestUtils.endSession(true);

            // make sure it reset all counts
            testCount = mgr.getHitCount(blog1);
            assertEquals(0, testCount);
            testCount = mgr.getHitCount(blog2);
            assertEquals(0, testCount);
            testCount = mgr.getHitCount(blog3);
            assertEquals(0, testCount);
        
        } finally {
            // cleanup
            TestUtils.teardownWeblog(blog1.getId());
            TestUtils.teardownWeblog(blog2.getId());
            TestUtils.teardownWeblog(blog3.getId());
        }
    }

    
    public void testHotWeblogs() throws Exception {
        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        
        testUser = TestUtils.getManagedUser(testUser);
        Weblog blog1 = TestUtils.setupWeblog("hitCntHotTest1", testUser);
        Weblog blog2 = TestUtils.setupWeblog("hitCntHotTest2", testUser);
        Weblog blog3 = TestUtils.setupWeblog("hitCntHotTest3", testUser);
        
        blog1.setHitsToday(10);
        blog2.setHitsToday(20);
        blog3.setHitsToday(30);

        TestUtils.endSession(true);
        
        // make sure data was properly initialized
        int testCount;
        testCount = mgr.getHitCount(blog1);
        assertEquals(10, testCount);
        testCount = mgr.getHitCount(blog2);
        assertEquals(20, testCount);
        testCount = mgr.getHitCount(blog3);
        assertEquals(30, testCount);
        
        // get hot weblogs
        LinkedHashMap<String, Integer> hotBlogs = mgr.getHotWeblogs(1, 0, 5);
        assertNotNull(hotBlogs);
        assertEquals(3, hotBlogs.size());
        
        // also check ordering and values
        int hitCount;
        Collection<Integer> list = hotBlogs.values();
        Iterator it = list.iterator();
        for (int i=3; it.hasNext(); i--) {
            hitCount = (Integer) it.next();
            assertEquals(i*10, hitCount);
        }
        
        // cleanup
        TestUtils.teardownWeblog(blog1.getId());
        TestUtils.teardownWeblog(blog2.getId());
        TestUtils.teardownWeblog(blog3.getId());
    }

}
