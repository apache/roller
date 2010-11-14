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

package org.apache.roller.weblogger.planet.business;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.fetcher.FeedFetcher;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test custom weblogger feed fetcher.
 */
public class WebloggerRomeFeedFetcherTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WebloggerRomeFeedFetcherTest.class);
    
    //User testUser = null;
    //Weblog testWeblog = null;
    String feed_url = "weblogger:webloggerFetcherTestWeblog";
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Override
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        TestUtils.setupPlanet();
        
        try {
            //testUser = TestUtils.setupUser("webloggerFetcherTestUser");
            //testWeblog = TestUtils.setupWeblog("webloggerFetcherTestWeblog", testUser);
            //TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    @Override
    public void tearDown() throws Exception {
        
        try {
            //TestUtils.teardownWeblog(testWeblog.getId());
            //TestUtils.teardownUser(testUser.getUserName());
            //TestUtils.endSession(true);
            
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    public void testFetchSubscription() throws Exception {
        try {
            FeedFetcher feedFetcher = PlanetFactory.getPlanet().getFeedFetcher();

            // first fetch non-conditionally so we know we should get a Sub
            Subscription sub = feedFetcher.fetchSubscription(feed_url);
            assertNotNull(sub);
            assertEquals(feed_url, sub.getFeedURL());
            assertNotNull(sub.getLastUpdated());

            // now do a conditional fetch and we should get back null
            Subscription updatedSub = feedFetcher.fetchSubscription(feed_url, sub.getLastUpdated());
            assertNull(updatedSub);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
}
