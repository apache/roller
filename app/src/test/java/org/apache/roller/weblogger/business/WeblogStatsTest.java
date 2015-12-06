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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
*/
package org.apache.roller.weblogger.business;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class WeblogStatsTest extends WebloggerTest {
    private User user1, user2;
    private Weblog website1;
    private WeblogEntry entry11;
    private WeblogEntryComment comment11;
    private WeblogEntryComment comment12;
    private WeblogEntryComment comment13;
    private Weblog website2;
    private WeblogEntryComment comment21;


    @Before
    public void setUp() throws Exception {
        super.setUp();
        // create weblog with three entries and two comments per entry
        user1 = setupUser("a_commentCountTestUser");
        user2 = setupUser("b_commentCountTestUser");

        website1 = setupWeblog("a_testWebsite1", user1);
        entry11 = setupWeblogEntry(
                "anchor11", website1, user1);
        comment11 = setupComment("Comment11", entry11);
        comment12 = setupComment("Comment12", entry11);
        WeblogEntry entry12 = setupWeblogEntry(
                "anchor12", website1, user1);
        comment13 = setupComment("Comment13", entry12);

        website2 = setupWeblog("b_testWebsite2", user1);
        WeblogEntry entry21 = setupWeblogEntry(
                "anchor21", website2, user1);
        comment21 = setupComment("Comment21", entry21);
        endSession(true);

        Thread.sleep(DateUtils.MILLIS_PER_SECOND);
    }

    @After
    public void tearDown() throws Exception {
        teardownComment(comment11.getId());
        teardownComment(comment12.getId());
        teardownComment(comment13.getId());
        teardownWeblog(website1.getId());

        teardownComment(comment21.getId());
        teardownWeblog(website2.getId());

        teardownUser(user1.getUserName());
        teardownUser(user2.getUserName());

        endSession(true);
    }

    @Test
    public void testGetMostCommentedWeblogs() throws Exception {
        List list = weblogManager.getMostCommentedWeblogs(null, null, 0, -1);

        assertNotNull(list);
        assertEquals(2, list.size());

        StatCount s1 = (StatCount) list.get(0);
        assertEquals(website1.getId(), s1.getSubjectId());
        assertEquals(3L, s1.getCount());
        assertEquals(website1.getHandle(), s1.getSubjectNameShort());
        assertEquals(website1.getHandle(), s1.getWeblogHandle());

        StatCount s2 = (StatCount) list.get(1);
        assertEquals(website2.getId(), s2.getSubjectId());
        assertEquals(1L, s2.getCount());
    }

    @Test
    public void testGetMostCommentedWeblogEntries() throws Exception {
        List list = weblogEntryManager.getMostCommentedWeblogEntries(null, null, null, 0, -1);

        assertNotNull(list);
        assertEquals(3, list.size());

        StatCount s1 = (StatCount) list.get(0);
        assertEquals(2L, s1.getCount());
        assertEquals(entry11.getAnchor(), s1.getSubjectNameShort());
        assertEquals(entry11.getWeblog().getHandle(), s1.getWeblogHandle());

        StatCount s2 = (StatCount) list.get(1);
        assertEquals(1L, s2.getCount());
    }

    @Test
    public void testGetUserNameLetterMap() throws Exception {
        Map map = userManager.getUserNameLetterMap();
        assertNotNull(map.get("A"));
        assertNotNull(map.get("B"));
        assertNotNull(map.get("C"));
    }

    @Test
    public void testGetWeblogLetterMap() throws Exception {
        Map map = weblogManager.getWeblogHandleLetterMap();
        assertNotNull(map.get("A"));
        assertNotNull(map.get("B"));
        assertNotNull(map.get("C"));
    }

}
