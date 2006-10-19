/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.business;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.TestUtils;
import org.apache.roller.planet.model.PlanetManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.planet.tasks.RefreshEntriesTask;
import org.apache.roller.planet.tasks.SyncWebsitesTask;



/**
 * Test database implementation of PlanetManager for local feeds.
 * @author Dave Johnson
 */
public class PlanetManagerLocalTest extends TestCase {
    public static Log log = LogFactory.getLog(PlanetManagerLocalTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    
    public static void main(String[] args) {
        TestRunner.run(PlanetManagerLocalTest.class);
    }
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        try {
            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            
            WeblogEntryData testEntry1 = new WeblogEntryData();
            testEntry1.setTitle("entryTestEntry1");
            testEntry1.setLink("testEntryLink1");
            testEntry1.setText("blah blah entry1");
            testEntry1.setAnchor("testEntryAnchor1");
            testEntry1.setPubTime(new Timestamp(new Date().getTime()));
            testEntry1.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry1.setWebsite(testWeblog);
            testEntry1.setCreator(testUser);
            testEntry1.setCategory(testWeblog.getDefaultCategory());
            RollerFactory.getRoller().getWeblogManager().saveWeblogEntry(testEntry1);

            WeblogEntryData testEntry2 = new WeblogEntryData();
            testEntry2.setTitle("entryTestEntry2");
            testEntry2.setLink("testEntryLink2");
            testEntry2.setText("blah blah entry2");
            testEntry2.setAnchor("testEntryAnchor2");
            testEntry2.setPubTime(new Timestamp(new Date().getTime()));
            testEntry2.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry2.setWebsite(testWeblog);
            testEntry2.setCreator(testUser);
            testEntry2.setCategory(testWeblog.getDefaultCategory());
            RollerFactory.getRoller().getWeblogManager().saveWeblogEntry(testEntry1);

            WeblogEntryData testEntry3 = new WeblogEntryData();
            testEntry3.setTitle("entryTestEntry3");
            testEntry3.setLink("testEntryLink3");
            testEntry3.setText("blah blah entry3");
            testEntry3.setAnchor("testEntryAnchor3");
            testEntry3.setPubTime(new Timestamp(new Date().getTime()));
            testEntry3.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry3.setWebsite(testWeblog);
            testEntry3.setCreator(testUser);
            testEntry3.setCategory(testWeblog.getDefaultCategory());           
            RollerFactory.getRoller().getWeblogManager().saveWeblogEntry(testEntry1);

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
    
    public void testRefreshEntries() {
        try {      
            PlanetManager planet = RollerFactory.getRoller().getPlanetManager();
            
            // run sync task to fill aggregator with websites created by super
            SyncWebsitesTask syncTask = new SyncWebsitesTask();
            syncTask.init();
            syncTask.run();           
            
            RefreshEntriesTask refreshTask = new RefreshEntriesTask();
            refreshTask.init();
            refreshTask.run();
            
            List agg = planet.getAggregation(null, null, 0, -1);
            assertEquals(3, agg.size());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
    
    public static Test suite() {
        return new TestSuite(PlanetManagerLocalTest.class);
    }
    
    
}

