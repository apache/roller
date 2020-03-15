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

package org.apache.roller.weblogger.business;

import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class WeblogStatsTest  {
    
    private User user1, user2;
    
    private Weblog website1;
        private WeblogEntry entry11;
            private WeblogEntryComment comment11;
            private WeblogEntryComment comment12;
        private WeblogEntry entry12;
            private WeblogEntryComment comment13;

    private Weblog website2;
        private WeblogEntry entry21;
            private WeblogEntryComment comment21; 

    @BeforeEach
    public void setUp() throws Exception {
        // create weblog with three entries and two comments per entry
        user1 = TestUtils.setupUser("a_commentCountTestUser");
        user2 = TestUtils.setupUser("b_commentCountTestUser");
        
        website1 = TestUtils.setupWeblog("a_testWebsite1", user1);
        entry11 = TestUtils.setupWeblogEntry(
                "anchor11", website1, user1);
        comment11 = TestUtils.setupComment("Comment11", entry11);
        comment12 = TestUtils.setupComment("Comment12", entry11);
        entry12 = TestUtils.setupWeblogEntry(
                "anchor12", website1, user1);
        comment13 = TestUtils.setupComment("Comment13", entry12);
        
        website2 = TestUtils.setupWeblog("b_testWebsite2", user1);
        entry21 = TestUtils.setupWeblogEntry(
                "anchor21", website2, user1);
        comment21 = TestUtils.setupComment("Comment21", entry21);
        TestUtils.endSession(true);

        Thread.sleep(RollerConstants.SEC_IN_MS);
    }

    @Test
    public void testGetMostCommentedWeblogs() throws Exception {        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();     
        List list = mgr.getMostCommentedWeblogs(null, null, 0, -1);  
        
        assertNotNull(list);
        assertEquals(2, list.size());
        
        StatCount s1 = (StatCount)list.get(0);
        assertEquals(website1.getId(), s1.getSubjectId());
        assertEquals(3L, s1.getCount());   
        assertEquals(website1.getHandle(), s1.getSubjectNameShort());
        assertEquals(website1.getHandle(), s1.getWeblogHandle());
        
        StatCount s2 = (StatCount)list.get(1);
        assertEquals(website2.getId(), s2.getSubjectId());
        assertEquals(1L, s2.getCount());   
    }

    @Test
    public void testGetMostCommentedWeblogEntries() throws Exception {
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();      
        List list = mgr.getMostCommentedWeblogEntries(null, null, null, 0, -1);
        
        assertNotNull(list);
        assertEquals(3, list.size());
        
        StatCount s1 = (StatCount)list.get(0);
        assertEquals(2L, s1.getCount()); 
        assertEquals(entry11.getAnchor(), s1.getSubjectNameShort());
        assertEquals(entry11.getWebsite().getHandle(), s1.getWeblogHandle());
               
        StatCount s2 = (StatCount)list.get(1);
        assertEquals(1L, s2.getCount());   
    }

    @Test
    public void testGetUserNameLetterMap() throws Exception {        
        UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();      
        Map map = mgr.getUserNameLetterMap();    
        assertNotNull(map.get("A"));
        assertNotNull(map.get("B"));
        assertNotNull(map.get("C"));
    }

    @Test
    public void testGetWeblogLetterMap() throws Exception {        
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        Map map = mgr.getWeblogHandleLetterMap();    
        assertNotNull(map.get("A"));
        assertNotNull(map.get("B"));
        assertNotNull(map.get("C"));
    }

    @AfterEach
    public void tearDown() throws Exception {
        
        // TODO: ATLAS figure out why comments must be torn down first
        TestUtils.teardownComment(comment11.getId());
        TestUtils.teardownComment(comment12.getId());
        TestUtils.teardownComment(comment13.getId());
        TestUtils.teardownWeblog(website1.getId());
        
        TestUtils.teardownComment(comment21.getId());
        TestUtils.teardownWeblog(website2.getId());  
        
        TestUtils.teardownUser(user1.getUserName());        
        TestUtils.teardownUser(user2.getUserName());        
        
        TestUtils.endSession(true);
    }
}
