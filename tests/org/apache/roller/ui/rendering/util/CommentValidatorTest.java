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

package org.apache.roller.ui.rendering.util;

import junit.framework.TestCase;
import org.apache.roller.TestUtils;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.RollerMessages;

/**
 *
 * @author David M. Johnson
 */
public class CommentValidatorTest extends TestCase {
    CommentValidationManager mgr = null;
    WebsiteData        weblog = null;
    UserData           user = null;
    WeblogEntryData    entry = null;
    
    /** Creates a new instance of CommentValidatorTest */
    public CommentValidatorTest() {
    } 

    protected void setUp() throws Exception {
        mgr = new CommentValidationManager();
        
        user = TestUtils.setupUser("johndoe");
        TestUtils.endSession(true);

        weblog = TestUtils.setupWeblog("doeblog", user);
        TestUtils.endSession(true);
        
        entry = TestUtils.setupWeblogEntry("anchor1", weblog.getDefaultCategory(), weblog, user);
    }
    
    protected void tearDown() throws Exception {
        TestUtils.teardownWeblogEntry(entry.getId());
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getId());
    }
    
    public void testInitialization() {
        // by default, we provide 3 comment validators
        assertEquals(3, mgr.getValidatorCount());
    }
    
    public void testExcessSizeCommentValidator() {
        RollerMessages msgs = new RollerMessages();
        CommentData comment = createEmptyComment();

        // string that exceeds default excess size threshold of 1000
        StringBuffer sb = new StringBuffer();
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
        CommentData comment = createEmptyComment();
        
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
        CommentData comment = createEmptyComment();
       
        comment.setContent("nice friendly stuff"); 
        assertEquals(100, mgr.validateComment(comment, msgs));

        comment.setContent("blah blah 01-suonerie.com blah"); 
        assertTrue(mgr.validateComment(comment, msgs) != 100);
    }  
    
    private CommentData createEmptyComment() {
        CommentData comment = new CommentData();
        comment.setUrl("http://example.com");
        comment.setName("Mortimer Snerd");
        comment.setEmail("mortimer@snerd.com");
        comment.setWeblogEntry(entry);
        return comment;
    }
}
