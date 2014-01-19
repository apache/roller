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

package org.apache.roller.weblogger.ui.rendering.util;

import junit.framework.TestCase;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.plugins.comments.CommentValidationManager;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.TestUtils;

/**
 *
 * @author David M. Johnson
 */
public class CommentValidatorTest extends TestCase {
    CommentValidationManager mgr = null;
    Weblog        weblog = null;
    User           user = null;
    WeblogEntry    entry = null;
    
    /** Creates a new instance of CommentValidatorTest */
    public CommentValidatorTest() {
    } 

    protected void setUp() throws Exception {
        // setup weblogger
        TestUtils.setupWeblogger();
        
        mgr = new CommentValidationManager();
        
        user = TestUtils.setupUser("johndoe");
        //TestUtils.endSession(true);

        weblog = TestUtils.setupWeblog("doeblog", user);
        //TestUtils.endSession(true)
        
        entry = TestUtils.setupWeblogEntry("anchor1", weblog.getDefaultCategory(), weblog, user);

        TestUtils.endSession(true);
    }
    
    protected void tearDown() throws Exception {
        TestUtils.teardownWeblogEntry(entry.getId());
        //TestUtils.teardownWeblogCategory(weblog.getDefaultCategory().getId());
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getUserName());
    }
    
    public void testExcessSizeCommentValidator() {
        RollerMessages msgs = new RollerMessages();
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
    
    public void testExcessLinksCommentValidator() {
        RollerMessages msgs = new RollerMessages();
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
    
    public void testBlacklistCommentValidator() {
        RollerMessages msgs = new RollerMessages();
        WeblogEntryComment comment = createEmptyComment();
       
        comment.setContent("nice friendly stuff"); 
        assertEquals(100, mgr.validateComment(comment, msgs));

        comment.setContent("blah blah www.myblacklistedsite.com blah");
        assertTrue(mgr.validateComment(comment, msgs) != 100);
    }
    
// To run this test add the Akismet validator to comment.validator.classnames
// and put your Akismet key in comment.validator.akismet.apikey
//
//     public void testAkismetCommentValidator() {
//        RollerMessages msgs = new RollerMessages();
//        WeblogEntryComment comment = createEmptyComment();       
//        comment.setContent("nice friendly stuff"); 
//        
//        assertEquals(100, mgr.validateComment(comment, msgs));
//
//        comment.setName("viagra-test-123");
//        assertTrue(mgr.validateComment(comment, msgs) != 100);
//    }
    
    private WeblogEntryComment createEmptyComment() {
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setUrl("http://example.com");
        comment.setName("Mortimer Snerd");
        comment.setEmail("mortimer@snerd.com");
        comment.setWeblogEntry(entry);
        return comment;
    }
}
