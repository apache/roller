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
package org.tightblog.business;

import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.tightblog.WebloggerTest;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WeblogStatsTestIT extends WebloggerTest {
    private User user1;
    private User user2;
    private Weblog website1;
    private WeblogEntryComment comment11;
    private WeblogEntryComment comment12;
    private WeblogEntryComment comment13;
    private Weblog website2;
    private WeblogEntryComment comment21;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // create weblog with three entries and two comments per entry
        user1 = setupUser("aCommentCountTestUser");
        user2 = setupUser("bCommentCountTestUser");

        website1 = setupWeblog("a-test-website1", user1);
        WeblogEntry entry11 = setupWeblogEntry(
                "anchor11", website1, user1);
        comment11 = setupComment("Comment11", entry11);
        comment12 = setupComment("Comment12", entry11);
        WeblogEntry entry12 = setupWeblogEntry(
                "anchor12", website1, user1);
        comment13 = setupComment("Comment13", entry12);

        website2 = setupWeblog("b-test-website2", user1);
        WeblogEntry entry21 = setupWeblogEntry(
                "anchor21", website2, user1);
        comment21 = setupComment("Comment21", entry21);

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

        teardownUser(user1.getId());
        teardownUser(user2.getId());
    }

    @Test
    public void testGetWeblogLetterMap() throws Exception {
        Map<Character, Integer> map = weblogManager.getWeblogHandleLetterMap();
        assertNotNull(map.get('A'));
        assertNotNull(map.get('B'));
        assertNotNull(map.get('C'));
    }

}
