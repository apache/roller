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
package org.tightblog.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import org.tightblog.WebloggerTest;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.User;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.RollbackException;

import static org.junit.Assert.*;


/**
 * Test Comment related business operations.
 */
public class WeblogEntryManagerCommentIT extends WebloggerTest {
    private User testUser;
    private Weblog testWeblog;
    private WeblogEntry testEntry;

    /**
     * All tests in this suite require a user, weblog, and an entry.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        try {
            testUser = setupUser("commentTestUser");
            testWeblog = setupWeblog("commenttestweblog", testUser);
            testEntry = setupWeblogEntry("commentTestEntry", testWeblog, testUser);
        } catch (Exception ex) {
            throw new Exception("Test setup failed", ex);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            weblogEntryManager.removeWeblogEntry(testEntry);
            weblogManager.removeWeblog(testWeblog);
            userManager.removeUser(testUser);
        } catch (Exception ex) {
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testCommentCRUD() {

        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setName("test");
        comment.setEmail("test");
        comment.setUrl("test");
        comment.setRemoteHost("foofoo");
        comment.setContent("this is a test comment");
        comment.setPostTime(Instant.now());
        comment.setWeblogEntry(testEntry);
        comment.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        
        // create a comment
        weblogEntryManager.saveComment(comment, true);
        String id = comment.getId();

        // make sure comment was created
        comment = weblogEntryCommentDao.findByIdOrNull(id);
        assertNotNull(comment);
        assertEquals("this is a test comment", comment.getContent());
        
        // update a comment
        comment.setContent("testtest");
        weblogEntryManager.saveComment(comment, true);

        // make sure comment was updated
        comment = weblogEntryCommentDao.findByIdOrNull(id);
        assertNotNull(comment);
        assertEquals("testtest", comment.getContent());
        
        // delete a comment
        weblogEntryManager.removeComment(comment);

        // make sure comment was deleted
        comment = weblogEntryCommentDao.findByIdOrNull(id);
        assertNull(comment);
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    @Test
    public void testCommentLookups() {
        List comments;
        
        // we need some comments to play with
        WeblogEntryComment comment1 = setupComment("comment1", testEntry);
        WeblogEntryComment comment2 = setupComment("comment2", testEntry);
        WeblogEntryComment comment3 = setupComment("comment3", testEntry);

        // get all comments
        CommentSearchCriteria csc = new CommentSearchCriteria();
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // get all comments for entry
        csc.setEntry(testEntry);
        comments = weblogEntryCommentDao.findByWeblogEntry(testEntry);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // make some changes
        comment3 = weblogEntryCommentDao.findByIdOrNull(comment3.getId());
        comment3.setStatus(WeblogEntryComment.ApprovalStatus.PENDING);
        weblogEntryManager.saveComment(comment3, false);

        // get pending comments
        csc.setEntry(null);
        csc.setStatus(WeblogEntryComment.ApprovalStatus.PENDING);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        
        // get approved comments
        comments = weblogEntryCommentDao.findByWeblogEntryAndStatusApproved(testEntry);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // get comments with offset
        csc.setStatus(null);
        csc.setOffset(1);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        weblogEntryManager.removeComment(comment1);
        weblogEntryManager.removeComment(comment2);
        weblogEntryManager.removeComment(comment3);
    }

    /**
     * Test that when deleting parent objects of a comment that everything
     * down the chain is properly deleted as well.  i.e. deleting an entry
     * should delete all comments on that entry, and deleting a weblog should
     * delete all comments, etc.
     */
    @Test
    public void testCommentParentDeletes() {

            // first make sure we can delete an entry with comments
            User user = setupUser("commentParentDeleteUser");
            Weblog weblog = setupWeblog("commentparentdelete", user);
            WeblogEntry entry = setupWeblogEntry("CommentParentDeletes1", weblog, user);

            setupComment("comment1", entry);
            setupComment("comment2", entry);
            setupComment("comment3", entry);

            // now deleting the entry should succeed and delete all comments
            RollbackException ex = null;
            try {
                weblogEntryManager.removeWeblogEntry(entry);
            } catch (RollbackException e) {
                ex = e;
            }
            assertNull(ex);

            // now make sure we can delete a weblog with comments
            weblog = weblogDao.findByIdOrNull(weblog.getId());
            entry = setupWeblogEntry("CommentParentDeletes2", weblog, user);

            setupComment("comment1", entry);
            setupComment("comment2", entry);
            setupComment("comment3", entry);

            // now deleting the weblog should succeed
            ex = null;
            try {
                weblogManager.removeWeblog(weblog);
            } catch (RollbackException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                ex = e;
            }
            assertNull(ex);

            // and delete test user as well
            userManager.removeUser(user);
    }

}
