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
package org.apache.roller.weblogger.ui.rendering.comment;

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class CommentValidatorTest extends WebloggerTest {
    CommentValidationManager mgr = null;
    Weblog        weblog = null;
    User           user = null;
    WeblogEntry    entry = null;

    @Resource(name="commentValidatorList")
    private List<CommentValidator> commentValidators;

    public void setCommentValidators(List<CommentValidator> commentValidators) {
        this.commentValidators = commentValidators;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mgr = new CommentValidationManager(commentValidators);
        user = setupUser("johndoe");
        weblog = setupWeblog("doeblog", user);
        entry = setupWeblogEntry("anchor1", weblog, user);
        endSession(true);
    }
    
    @After
    public void tearDown() throws Exception {
        teardownWeblogEntry(entry.getId());
        teardownWeblog(weblog.getId());
        teardownUser(user.getUserName());
    }

    @Test
    public void testExcessSizeCommentValidator() {
        Map<String, List<String>> msgs = new HashMap<>();
        WeblogEntryComment comment = createEmptyComment();

        // string that exceeds default excess size threshold of 1000
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<101; i++) {
            sb.append("0123456789");
        }
        
        comment.setContent("short stuff"); 
        assertEquals(100, mgr.validateComment(comment, msgs));

        comment.setContent(sb.toString()); 
        assertTrue(mgr.validateComment(comment, msgs) != 100);
    }

    @Test
    public void testExcessLinksCommentValidator() {
        Map<String, List<String>> msgs = new HashMap<>();
        WeblogEntryComment comment = createEmptyComment();
        
        comment.setContent("<a href=\"http://example.com\">link1</a>"); 
        assertEquals(100, mgr.validateComment(comment, msgs));

        // String that exceeds default excess links threshold of 3
        comment.setContent(
            "<a href=\"http://example.com\">link1</a>" +
            "<a href=\"http://example.com\">link2</a>" +
            "<a href=\"http://example.com\">link3</a>" +
            "<a href=\"http://example.com\">link4</a>" +
            "<a href=\"http://example.com\">link5</a>"
        ); 
        assertTrue(mgr.validateComment(comment, msgs) != 100);
    }

    @Test
    public void testBlacklistCommentValidator() {
        Map<String, List<String>> msgs = new HashMap<>();
        WeblogEntryComment comment = createEmptyComment();
       
        comment.getWeblogEntry().getWeblog().setBlacklist("www.myblacklistedsite.com");

        comment.setContent("nice friendly stuff");
        assertEquals(100, mgr.validateComment(comment, msgs));

        comment.setContent("blah blah www.myblacklistedsite.com blah");
        assertTrue(mgr.validateComment(comment, msgs) != 100);
    }
    
    // To run this test uncomment the Akismet validator to commentValidatorList
    // in spring-beans.xml along with your Akismet API key
    @Ignore
    public void testAkismetCommentValidator() {
        Map<String, List<String>> msgs = new HashMap<>();
        WeblogEntryComment comment = createEmptyComment();
        comment.setContent("nice friendly stuff");

        assertEquals(100, mgr.validateComment(comment, msgs));

        comment.setName("viagra-test-123");
        assertTrue(mgr.validateComment(comment, msgs) != 100);
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
