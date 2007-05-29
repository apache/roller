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

package org.apache.roller.business;

import java.util.Iterator;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.HitCountData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.Weblog;


/**
 * Test HitCount related business operations.
 */
public class HitCountTest extends TestCase {
    
    public static Log log = LogFactory.getLog(HitCountTest.class);
    
    UserData testUser = null;
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
            TestUtils.teardownUser(testUser.getId());
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
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        HitCountData testCount = new HitCountData();
        testCount.setWeblog(testWeblog);
        testCount.setDailyHits(10);
        
        // create
        mgr.saveHitCount(testCount);
        String id = testCount.getId();
        TestUtils.endSession(true);
        
        // make sure it was created
        HitCountData hitCount = null;
        hitCount = mgr.getHitCount(id);
        assertNotNull(hitCount);
        assertEquals(testCount, hitCount);
        assertEquals(10, hitCount.getDailyHits());
        
        // update
        hitCount.setDailyHits(25);
        mgr.saveHitCount(hitCount);
        TestUtils.endSession(true);
        
        // make sure it was updated
        hitCount = null;
        hitCount = mgr.getHitCount(id);
        assertNotNull(hitCount);
        assertEquals(testCount, hitCount);
        assertEquals(25, hitCount.getDailyHits());
        
        // delete
        mgr.removeHitCount(hitCount);
        TestUtils.endSession(true);
        
        // make sure it was deleted
        hitCount = null;
        hitCount = mgr.getHitCount(id);
        assertNull(hitCount);
    }
    
    
    public void testHitCountLookups() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        HitCountData testCount = new HitCountData();
        testCount.setWeblog(testWeblog);
        testCount.setDailyHits(10);
        
        // create
        mgr.saveHitCount(testCount);
        String id = testCount.getId();
        TestUtils.endSession(true);
        
        // test lookup by id
        HitCountData hitCount = null;
        hitCount = mgr.getHitCount(id);
        assertNotNull(hitCount);
        assertEquals(testCount, hitCount);
        assertEquals(10, hitCount.getDailyHits());
        
        // test lookup by weblog
        hitCount = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        hitCount = mgr.getHitCountByWeblog(testWeblog);
        assertNotNull(hitCount);
        assertEquals(testCount, hitCount);
        assertEquals(10, hitCount.getDailyHits());
        
        // delete
        mgr.removeHitCount(hitCount);
        TestUtils.endSession(true);
        
        // make sure it was deleted
        hitCount = null;
        hitCount = mgr.getHitCount(id);
        assertNull(hitCount);
    }
    
    
    public void testIncrementHitCount() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        HitCountData testCount = new HitCountData();
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        testCount.setWeblog(testWeblog);
        testCount.setDailyHits(10);
        
        // create
        mgr.saveHitCount(testCount);
        String id = testCount.getId();
        TestUtils.endSession(true);
        
        // make sure it was created
        HitCountData hitCount = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        hitCount = mgr.getHitCountByWeblog(testWeblog);
        assertNotNull(hitCount);
        assertEquals(10, hitCount.getDailyHits());
        
        // increment
        mgr.incrementHitCount(testWeblog, 25);
        TestUtils.endSession(true);
        
        // make sure it was incremented properly
        hitCount = null;
        testWeblog = TestUtils.getManagedWebsite(testWeblog);
        hitCount = mgr.getHitCountByWeblog(testWeblog);
        assertNotNull(hitCount);
        assertEquals(35, hitCount.getDailyHits());
        
        // delete
        mgr.removeHitCount(hitCount);
        TestUtils.endSession(true);
        
        // make sure it was deleted
        hitCount = null;
        hitCount = mgr.getHitCount(id);
        assertNull(hitCount);
    }
    
    
    public void testResetHitCounts() throws Exception {
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        testUser = TestUtils.getManagedUser(testUser);
        Weblog blog1 = TestUtils.setupWeblog("hitCntTest1", testUser);
        Weblog blog2 = TestUtils.setupWeblog("hitCntTest2", testUser);
        Weblog blog3 = TestUtils.setupWeblog("hitCntTest3", testUser);
        
        HitCountData cnt1 = TestUtils.setupHitCount(blog1, 10);
        HitCountData cnt2 = TestUtils.setupHitCount(blog2, 20);
        HitCountData cnt3 = TestUtils.setupHitCount(blog3, 30);
        
        TestUtils.endSession(true);
        
        try {
            // make sure data was properly initialized
            HitCountData testCount = null;
            testCount = mgr.getHitCount(cnt1.getId());
            assertEquals(10, testCount.getDailyHits());
            testCount = mgr.getHitCount(cnt2.getId());
            assertEquals(20, testCount.getDailyHits());
            testCount = mgr.getHitCount(cnt3.getId());
            assertEquals(30, testCount.getDailyHits());

            // reset count for one weblog
            blog1 = TestUtils.getManagedWebsite(blog1);
            mgr.resetHitCount(blog1);
            TestUtils.endSession(true);

            // make sure it reset only one weblog
            testCount = mgr.getHitCount(cnt1.getId());
            assertEquals(0, testCount.getDailyHits());
            testCount = mgr.getHitCount(cnt2.getId());
            assertEquals(20, testCount.getDailyHits());
            testCount = mgr.getHitCount(cnt3.getId());
            assertEquals(30, testCount.getDailyHits());

            // reset all counts
            mgr.resetAllHitCounts();
            TestUtils.endSession(true);

            // make sure it reset all counts
            testCount = mgr.getHitCount(cnt1.getId());
            assertEquals(0, testCount.getDailyHits());
            testCount = mgr.getHitCount(cnt2.getId());
            assertEquals(0, testCount.getDailyHits());
            testCount = mgr.getHitCount(cnt3.getId());
            assertEquals(0, testCount.getDailyHits());
        
        } finally {
            // cleanup
            TestUtils.teardownHitCount(cnt1.getId());
            TestUtils.teardownHitCount(cnt2.getId());
            TestUtils.teardownHitCount(cnt3.getId());
            TestUtils.teardownWeblog(blog1.getId());
            TestUtils.teardownWeblog(blog2.getId());
            TestUtils.teardownWeblog(blog3.getId());
        }
    }

    
    public void testHotWeblogs() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        testUser = TestUtils.getManagedUser(testUser);
        Weblog blog1 = TestUtils.setupWeblog("hitCntHotTest1", testUser);
        Weblog blog2 = TestUtils.setupWeblog("hitCntHotTest2", testUser);
        Weblog blog3 = TestUtils.setupWeblog("hitCntHotTest3", testUser);
        
        HitCountData cnt1 = TestUtils.setupHitCount(blog1, 10);
        HitCountData cnt2 = TestUtils.setupHitCount(blog2, 20);
        HitCountData cnt3 = TestUtils.setupHitCount(blog3, 30);
        
        TestUtils.endSession(true);
        
        // make sure data was properly initialized
        HitCountData testCount = null;
        testCount = mgr.getHitCount(cnt1.getId());
        assertEquals(10, testCount.getDailyHits());
        testCount = mgr.getHitCount(cnt2.getId());
        assertEquals(20, testCount.getDailyHits());
        testCount = mgr.getHitCount(cnt3.getId());
        assertEquals(30, testCount.getDailyHits());
        
        // get hot weblogs
        List hotBlogs = mgr.getHotWeblogs(1, 0, 5);
        assertNotNull(hotBlogs);
        assertEquals(3, hotBlogs.size());
        
        // also check ordering and values
        HitCountData hitCount = null;
        Iterator it = hotBlogs.iterator();
        for(int i=3; it.hasNext(); i--) {
            hitCount = (HitCountData) it.next();
            
            assertEquals(i*10, hitCount.getDailyHits());
        }
        
        // cleanup
        TestUtils.teardownHitCount(cnt1.getId());
        TestUtils.teardownHitCount(cnt2.getId());
        TestUtils.teardownHitCount(cnt3.getId());
        TestUtils.teardownWeblog(blog1.getId());
        TestUtils.teardownWeblog(blog2.getId());
        TestUtils.teardownWeblog(blog3.getId());
    }

}
