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
package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.planet.tasks.RefreshRollerPlanetTask;
import org.apache.roller.weblogger.planet.tasks.SyncWebsitesTask;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Test database implementation of PlanetManager for local feeds.
 * @author Dave Johnson
 */
public class PlanetManagerLocalTest  {
    public static Log log = LogFactory.getLog(PlanetManagerLocalTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @BeforeEach
    public void setUp() throws Exception {
        
        try {
            TestUtils.setupWeblogger();

            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            TestUtils.endSession(true);

            testUser = TestUtils.getManagedUser(testUser);
            testWeblog = TestUtils.getManagedWebsite(testWeblog);

            WeblogEntry testEntry1 = new WeblogEntry();
            testEntry1.setTitle("entryTestEntry1");
            testEntry1.setLink("testEntryLink1");
            testEntry1.setText("blah blah entry1");
            testEntry1.setAnchor("testEntryAnchor1");
            testEntry1.setPubTime(new Timestamp(new Date().getTime()));
            testEntry1.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry1.setWebsite(testWeblog);
            testEntry1.setCreatorUserName(testUser.getUserName());
            testEntry1.setCategory(testWeblog.getWeblogCategory("General"));
            testEntry1.setStatus(PubStatus.PUBLISHED);
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry1);

            WeblogEntry testEntry2 = new WeblogEntry();
            testEntry2.setTitle("entryTestEntry2");
            testEntry2.setLink("testEntryLink2");
            testEntry2.setText("blah blah entry2");
            testEntry2.setAnchor("testEntryAnchor2");
            testEntry2.setPubTime(new Timestamp(new Date().getTime()));
            testEntry2.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry2.setWebsite(testWeblog);
            testEntry2.setCreatorUserName(testUser.getUserName());
            testEntry2.setCategory(testWeblog.getWeblogCategory("General"));
            testEntry2.setStatus(PubStatus.PUBLISHED);
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry2);

            WeblogEntry testEntry3 = new WeblogEntry();
            testEntry3.setTitle("entryTestEntry3");
            testEntry3.setLink("testEntryLink3");
            testEntry3.setText("blah blah entry3");
            testEntry3.setAnchor("testEntryAnchor3");
            testEntry3.setPubTime(new Timestamp(new Date().getTime()));
            testEntry3.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry3.setWebsite(testWeblog);
            testEntry3.setCreatorUserName(testUser.getUserName());
            testEntry3.setCategory(testWeblog.getWeblogCategory("General"));
            testEntry3.setStatus(PubStatus.PUBLISHED);
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry3);

            TestUtils.endSession(true);
            
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @AfterEach
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

    @Test
    public void testRefreshEntries() {
        try {      
            PlanetManager planet = WebloggerFactory.getWeblogger().getPlanetManager();
            
            // run sync task to fill aggregator with websites created by super
            SyncWebsitesTask syncTask = new SyncWebsitesTask();
            syncTask.init();
            syncTask.runTask();
            
            Planet planetObject = planet.getWebloggerById("zzz_default_planet_zzz");
            assertNotNull(planetObject);
            PlanetGroup group = planet.getGroup(planetObject, "all");
            assertEquals(1, group.getSubscriptions().size());

            RefreshRollerPlanetTask refreshTask = new RefreshRollerPlanetTask();
            refreshTask.runTask();
            
            planetObject = planet.getWeblogger("default");
            group = planet.getGroup(planetObject, "all");
            List agg = planet.getEntries(group, 0, -1);
            assertEquals(3, agg.size());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}

