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
package org.tightblog.rendering.comment;

import org.tightblog.WebloggerTest;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CommentValidatorTest extends WebloggerTest {
    private Weblog weblog = null;
    private User testUser = null;
    private WeblogEntry entry = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        testUser = setupUser("johndoe");
        weblog = setupWeblog("doeblog", testUser);
        entry = setupWeblogEntry("anchor1", weblog, testUser);
        endSession(true);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblogEntry(entry.getId());
        teardownWeblog(weblog.getId());
        teardownUser(testUser.getId());
    }

    @Test
    public void testBlacklistCommentValidator() {
        BlacklistCommentValidator validator = new BlacklistCommentValidator();
        validator.setWeblogManager(weblogManager);
        Map<String, List<String>> msgs = new HashMap<>();

        WeblogEntryComment comment = createEmptyComment();
        comment.getWeblogEntry().getWeblog().setBlacklist("www.myblacklistedsite.com");

        comment.setContent("nice friendly stuff");
        assertEquals(100, validator.validate(comment, msgs));

        comment.setContent("blah blah www.myblacklistedsite.com blah");
        assertTrue(validator.validate(comment, msgs) != 100);
    }
    
    // To run this test provide your Akismet API key below.
    public void testAkismetCommentValidator() {
        AkismetCommentValidator validator = new AkismetCommentValidator(urlStrategy, "api code here");

        Map<String, List<String>> msgs = new HashMap<>();
        WeblogEntryComment comment = createEmptyComment();
        comment.setContent("nice friendly stuff");

        assertEquals(100, validator.validate(comment, msgs));

            // per Akismet docs, name hardcoded to always fail
        comment.setName("viagra-test-123");
        assertTrue(validator.validate(comment, msgs) != 100);
    }

    private WeblogEntryComment createEmptyComment() {
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setUrl("http://example.com");
        comment.setName("Mortimer Snerd");
        comment.setEmail("mortimer@snerd.com");
        comment.setWeblogEntry(entry);
        return comment;
    }
}
