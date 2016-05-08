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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 * Test database implementation of PlanetManager for local feeds.
 */
public class PlanetManagerLocalTest extends WebloggerTest {

    User testUser = null;
    Weblog testWeblog = null;
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        testUser = setupUser("entryTestUser");
        testWeblog = setupWeblog("entryTestWeblog", testUser);
        endSession(true);

        testUser = getManagedUser(testUser);
        testWeblog = getManagedWeblog(testWeblog);

        WeblogEntry testEntry1 = new WeblogEntry();
        testEntry1.setId(WebloggerCommon.generateUUID());
        testEntry1.setTitle("entryTestEntry1");
        testEntry1.setText("blah blah entry1");
        testEntry1.setAnchor("testEntryAnchor1");
        testEntry1.setPubTime(new Timestamp(new Date().getTime()));
        testEntry1.setUpdateTime(new Timestamp(new Date().getTime()));
        testEntry1.setWeblog(testWeblog);
        testEntry1.setCreatorId(testUser.getId());
        testEntry1.setCategory(weblogManager.getWeblogCategoryByName(testWeblog, "General"));
        testEntry1.setStatus(PubStatus.PUBLISHED);
        weblogEntryManager.saveWeblogEntry(testEntry1);

        WeblogEntry testEntry2 = new WeblogEntry();
        testEntry2.setId(WebloggerCommon.generateUUID());
        testEntry2.setTitle("entryTestEntry2");
        testEntry2.setText("blah blah entry2");
        testEntry2.setAnchor("testEntryAnchor2");
        testEntry2.setPubTime(new Timestamp(new Date().getTime()));
        testEntry2.setUpdateTime(new Timestamp(new Date().getTime()));
        testEntry2.setWeblog(testWeblog);
        testEntry2.setCreatorId(testUser.getId());
        testEntry2.setCategory(weblogManager.getWeblogCategoryByName(testWeblog, "General"));
        testEntry2.setStatus(PubStatus.PUBLISHED);
        weblogEntryManager.saveWeblogEntry(testEntry2);

        WeblogEntry testEntry3 = new WeblogEntry();
        testEntry3.setId(WebloggerCommon.generateUUID());
        testEntry3.setTitle("entryTestEntry3");
        testEntry3.setText("blah blah entry3");
        testEntry3.setAnchor("testEntryAnchor3");
        testEntry3.setPubTime(new Timestamp(new Date().getTime()));
        testEntry3.setUpdateTime(new Timestamp(new Date().getTime()));
        testEntry3.setWeblog(testWeblog);
        testEntry3.setCreatorId(testUser.getId());
        testEntry3.setCategory(weblogManager.getWeblogCategoryByName(testWeblog, "General"));
        testEntry3.setStatus(PubStatus.PUBLISHED);
        weblogEntryManager.saveWeblogEntry(testEntry3);

        endSession(true);
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getUserName());
        endSession(true);
    }
    
    @Test
    public void testRefreshEntries() {
        try {      
            // run sync task to fill aggregator with websites created by super
            planetManager.syncAllBlogsPlanet();

            Planet planet = planetManager.getPlanet("all");
            assertEquals(1, planet.getSubscriptions().size());

            planetManager.updateSubscriptions();

            planet = planetManager.getPlanet("all");
            List agg = planetManager.getEntries(planet, 0, -1);
            assertEquals(3, agg.size());

            teardownPlanet(planet.getHandle());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}

