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
package org.tightblog.business;

import java.util.Iterator;
import java.util.List;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test HitCount related business operations.
 */
public class HitCountTest extends WebloggerTest {
    
    User testUser = null;
    Weblog testWeblog = null;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("hitCountTestUser");
        testWeblog = setupWeblog("hitCountTestWeblog", testUser);
        endSession(true);
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testHitCountCRUD() throws Exception {
        Weblog aWeblog = weblogManager.getWeblog(testWeblog.getId());
        int oldHits = aWeblog.getHitsToday();
        aWeblog.setHitsToday(oldHits + 10);
        weblogManager.saveWeblog(aWeblog);
        endSession(true);
        
        // make sure it was stored
        aWeblog = weblogManager.getWeblog(testWeblog.getId());
        assertNotNull(aWeblog);
        assertEquals(aWeblog, testWeblog);
        assertEquals(oldHits + 10, aWeblog.getHitsToday());

        // test lookup by weblog
        int hitCount = weblogManager.getHitCount(testWeblog);
        assertEquals(oldHits + 10, hitCount);
    }

    @Test
    public void testIncrementHitCount() throws Exception {
        Weblog aWeblog = getManagedWeblog(testWeblog);
        aWeblog.setHitsToday(10);
        weblogManager.saveWeblog(aWeblog);
        endSession(true);
        
        // make sure it was created
        aWeblog = getManagedWeblog(testWeblog);
        assertNotNull(aWeblog);
        assertEquals(10, aWeblog.getHitsToday());
        
        // increment
        for (int i = 0; i < 5; i++) {
            weblogManager.incrementHitCount(aWeblog);
        }
        weblogManager.updateHitCounters();

        // make sure it was incremented properly
        int hitCount = weblogManager.getHitCount(testWeblog);
        assertEquals(15, hitCount);
    }

    @Test
    public void testResetHitCounts() throws Exception {
        testUser = getManagedUser(testUser);
        Weblog blog1 = setupWeblog("hitCntTest1", testUser);
        Weblog blog2 = setupWeblog("hitCntTest2", testUser);
        Weblog blog3 = setupWeblog("hitCntTest3", testUser);
        
        blog1.setHitsToday(10);
        blog2.setHitsToday(20);
        blog3.setHitsToday(30);

        endSession(true);
        
        try {
            // make sure data was properly initialized
            int testCount;
            testCount = weblogManager.getHitCount(blog1);
            assertEquals(10, testCount);
            testCount = weblogManager.getHitCount(blog2);
            assertEquals(20, testCount);
            testCount = weblogManager.getHitCount(blog3);
            assertEquals(30, testCount);

            // reset all counts
            weblogManager.resetAllHitCounts();
            endSession(true);

            // make sure it reset all counts
            testCount = weblogManager.getHitCount(blog1);
            assertEquals(0, testCount);
            testCount = weblogManager.getHitCount(blog2);
            assertEquals(0, testCount);
            testCount = weblogManager.getHitCount(blog3);
            assertEquals(0, testCount);
        
        } finally {
            // cleanup
            teardownWeblog(blog1.getId());
            teardownWeblog(blog2.getId());
            teardownWeblog(blog3.getId());
        }
    }

    @Test
    public void testHotWeblogs() throws Exception {
        testUser = getManagedUser(testUser);
        Weblog blog1 = setupWeblog("hitCntHotTest1", testUser);
        Weblog blog2 = setupWeblog("hitCntHotTest2", testUser);
        Weblog blog3 = setupWeblog("hitCntHotTest3", testUser);
        
        blog1.setHitsToday(10);
        blog2.setHitsToday(20);
        blog3.setHitsToday(30);

        endSession(true);
        
        // make sure data was properly initialized
        int testCount;
        testCount = weblogManager.getHitCount(blog1);
        assertEquals(10, testCount);
        testCount = weblogManager.getHitCount(blog2);
        assertEquals(20, testCount);
        testCount = weblogManager.getHitCount(blog3);
        assertEquals(30, testCount);
        
        // get hot weblogs
        List<Weblog> hotBlogs = weblogManager.getHotWeblogs(1, 0, 5);
        assertNotNull(hotBlogs);
        assertEquals(3, hotBlogs.size());
        
        // also check ordering and values
        int hitCount;
        Iterator<Weblog> it = hotBlogs.iterator();
        for (int i=3; it.hasNext(); i--) {
            hitCount = it.next().getHitsToday();
            assertEquals(i*10, hitCount);
        }
        
        // cleanup
        teardownWeblog(blog1.getId());
        teardownWeblog(blog2.getId());
        teardownWeblog(blog3.getId());
    }

}
