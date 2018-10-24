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
public class HitCountTestIT extends WebloggerTest {
    
    User testUser;
    Weblog testWeblog;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("hitCountTestUser");
        testWeblog = setupWeblog("hitcounttestweblog", testUser);
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
    }

    /**
     * Test basic persistence operations ... Create, Update, Delete.
     */
    @Test
    public void testHitCountCRUD() throws Exception {
        Weblog aWeblog = weblogRepository.findById(testWeblog.getId()).orElse(null);
        int oldHits = aWeblog.getHitsToday();
        aWeblog.setHitsToday(oldHits + 10);
        weblogManager.saveWeblog(aWeblog);

        // make sure it was stored
        aWeblog = weblogRepository.findById(testWeblog.getId()).orElse(null);
        assertNotNull(aWeblog);
        assertEquals(aWeblog, testWeblog);
        assertEquals(oldHits + 10, aWeblog.getHitsToday());
    }

    @Test
    public void testIncrementHitCount() throws Exception {
        Weblog aWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        aWeblog.setHitsToday(10);
        weblogManager.saveWeblog(aWeblog);

        // make sure it was created
        aWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        assertNotNull(aWeblog);
        assertEquals(10, aWeblog.getHitsToday());
        
        // increment
        for (int i = 0; i < 5; i++) {
            weblogManager.incrementHitCount(aWeblog);
        }
        weblogManager.updateHitCounters();

        // make sure it was incremented properly
        aWeblog = weblogRepository.findByIdOrNull(testWeblog.getId());
        assertEquals(15, aWeblog.getHitsToday());
    }

    @Test
    public void testResetHitCounts() throws Exception {
        Weblog blog1 = setupWeblog("hit-cnt-test1", testUser);
        Weblog blog2 = setupWeblog("hit-cnt-test2", testUser);

        blog1.setHitsToday(10);
        blog2.setHitsToday(20);

        weblogManager.saveWeblog(blog1);
        weblogManager.saveWeblog(blog2);

        try {
            // make sure data was properly initialized
            assertEquals(10, blog1.getHitsToday());
            assertEquals(20, blog2.getHitsToday());

            // reset all counts
            weblogRepository.updateDailyHitCountZero();

            blog1 = weblogRepository.findByIdOrNull(blog1.getId());
            blog2 = weblogRepository.findByIdOrNull(blog2.getId());

            // make sure it reset all counts
            assertEquals(0, blog1.getHitsToday());
            assertEquals(0, blog2.getHitsToday());

        } finally {
            // cleanup
            teardownWeblog(blog1.getId());
            teardownWeblog(blog2.getId());
        }
    }

}
