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

import java.time.Instant;

import org.apache.commons.lang3.time.DateUtils;
import org.tightblog.WebloggerTest;
import org.tightblog.business.search.tasks.IndexEntryTask;
import org.tightblog.business.search.tasks.SearchTask;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;


/**
 * Test Search Manager business layer operations.
 */
public class IndexManagerTest extends WebloggerTest {

    private User testUser;
    private Weblog testWeblog;

    @Resource
    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("entryTestUser");
        testWeblog = setupWeblog("entryTestWeblog", testUser);
        endSession(true);
        Assert.assertEquals(1, weblogManager.getWeblogCount());
    }

    @After
    public void tearDown() throws Exception {
        teardownWeblog(testWeblog.getId());
        teardownUser(testUser.getId());
        endSession(true);
    }

    @Test
    public void testSearch() throws Exception {
        WeblogEntry wd1 = new WeblogEntry();
        wd1.setTitle("The Tholian Web");
        wd1.setText(
         "When the Enterprise attempts to ascertain the fate of the  "
        + "U.S.S. Defiant which vanished 3 weeks ago, the warp engines  "
        + "begin to lose power, and Spock reports strange sensor readings.");
        wd1.setAnchor("dummy1");
        wd1.setCreator(testUser);
        wd1.setStatus(PubStatus.PUBLISHED);
        wd1.setUpdateTime(Instant.now());
        wd1.setPubTime(Instant.now());
        wd1.setWeblog(getManagedWeblog(testWeblog));

        WeblogCategory cat = weblogManager.getWeblogCategoryByName(testWeblog, "General");
        wd1.setCategory(cat);

        weblogEntryManager.saveWeblogEntry(wd1);
        endSession(true);
        wd1 = getManagedWeblogEntry(wd1);

        indexManager.executeIndexOperationNow(
                new IndexEntryTask(weblogEntryManager, indexManager, wd1, false));

        WeblogEntry wd2 = new WeblogEntry();
        wd2.setTitle("A Piece of the Action");
        wd2.setText(
          "The crew of the Enterprise attempts to make contact with "
          + "the inhabitants of planet Sigma Iotia II, and Uhura puts Kirk "
          + "in communication with Boss Oxmyx.");
        wd2.setAnchor("dummy2");
        wd2.setStatus(PubStatus.PUBLISHED);
        wd2.setCreator(testUser);
        wd2.setUpdateTime(Instant.now());
        wd2.setPubTime(Instant.now());
        wd2.setWeblog(getManagedWeblog(testWeblog));

        cat = weblogManager.getWeblogCategoryByName(testWeblog, "General");
        wd2.setCategory(cat);

        weblogEntryManager.saveWeblogEntry(wd2);
        endSession(true);
        wd2 = getManagedWeblogEntry(wd2);

        indexManager.executeIndexOperationNow(
            new IndexEntryTask(weblogEntryManager, indexManager, wd2, false));

        Thread.sleep(DateUtils.MILLIS_PER_SECOND);

        SearchTask search = new SearchTask(indexManager);
        search.setTerm("Enterprise");
        indexManager.executeIndexOperationNow(search);
        assertEquals(2, search.getResultsCount());

        SearchTask search2 = new SearchTask(indexManager);
        search2.setTerm("Tholian");
        indexManager.executeIndexOperationNow(search2);
        assertEquals(1, search2.getResultsCount());

        // Clean up
        IndexEntryTask t1 = new IndexEntryTask(weblogEntryManager, indexManager, wd1, true);
        indexManager.executeIndexOperationNow(t1);
        IndexEntryTask t2 = new IndexEntryTask(weblogEntryManager, indexManager, wd2, true);
        indexManager.executeIndexOperationNow(t2);

        SearchTask search3 = new SearchTask(indexManager);
        search3.setTerm("Enterprise");
        indexManager.executeIndexOperationNow(search3);
        assertEquals(0, search3.getResultsCount());
    }    
}
