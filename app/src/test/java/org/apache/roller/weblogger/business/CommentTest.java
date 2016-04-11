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
package org.apache.roller.weblogger.business;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test Comment related business operations.
 */
public class CommentTest extends WebloggerTest {
    public static Log log = LogFactory.getLog(CommentTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    WeblogEntry testEntry = null;

    /**
     * All tests in this suite require a user, weblog, and an entry.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        try {
            testUser = setupUser("commentTestUser");
            testWeblog = setupWeblog("commentTestWeblog", testUser);
            testEntry = setupWeblogEntry("commentTestEntry", testWeblog, testUser);
            endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            teardownWeblogEntry(testEntry.getId());
            teardownWeblog(testWeblog.getId());
            teardownUser(testUser.getUserName());
            endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    @Test
    public void testCommentCRUD() throws Exception {

        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setName("test");
        comment.setEmail("test");
        comment.setUrl("test");
        comment.setRemoteHost("foofoo");
        comment.setContent("this is a test comment");
        comment.setPostTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        comment.setWeblogEntry(getManagedWeblogEntry(testEntry));
        comment.setStatus(ApprovalStatus.APPROVED);
        
        // create a comment
        weblogEntryManager.saveComment(comment, true);
        String id = comment.getId();
        endSession(true);
        
        // make sure comment was created
        comment = weblogEntryManager.getComment(id);
        assertNotNull(comment);
        assertEquals("this is a test comment", comment.getContent());
        
        // update a comment
        comment.setContent("testtest");
        weblogEntryManager.saveComment(comment, true);
        endSession(true);
        
        // make sure comment was updated
        comment = weblogEntryManager.getComment(id);
        assertNotNull(comment);
        assertEquals("testtest", comment.getContent());
        
        // delete a comment
        weblogEntryManager.removeComment(comment);
        endSession(true);
        
        // make sure comment was deleted
        comment = weblogEntryManager.getComment(id);
        assertNull(comment);
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    @Test
    public void testCommentLookups() throws Exception {
        List comments;
        
        // we need some comments to play with
        testEntry = getManagedWeblogEntry(testEntry);
        WeblogEntryComment comment1 = setupComment("comment1", testEntry);
        WeblogEntryComment comment2 = setupComment("comment2", testEntry);
        WeblogEntryComment comment3 = setupComment("comment3", testEntry);
        endSession(true);
        
        // get all comments
        CommentSearchCriteria csc = new CommentSearchCriteria();
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // get all comments for entry
        testEntry = getManagedWeblogEntry(testEntry);
        csc.setEntry(testEntry);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // make some changes
        comment3 = weblogEntryManager.getComment(comment3.getId());
        comment3.setStatus(ApprovalStatus.PENDING);
        weblogEntryManager.saveComment(comment3, false);
        endSession(true);
        
        // get pending comments
        csc.setEntry(null);
        csc.setStatus(ApprovalStatus.PENDING);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        
        // get approved comments
        csc.setStatus(ApprovalStatus.APPROVED);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // get comments with offset
        csc.setStatus(null);
        csc.setOffset(1);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // remove test comments
        teardownComment(comment1.getId());
        teardownComment(comment2.getId());
        teardownComment(comment3.getId());
        endSession(true);
    }
    
    
    /**
     * Test that when deleting parent objects of a comment that everything
     * down the chain is properly deleted as well.  i.e. deleting an entry
     * should delete all comments on that entry, and deleting a weblog should
     * delete all comments, etc.
     */
    @Test
    public void testCommentParentDeletes() throws Exception {
        
        log.info("BEGIN");
        
        try {
            // first make sure we can delete an entry with comments
            User user = setupUser("commentParentDeleteUser");
            Weblog weblog = setupWeblog("commentParentDelete", user);
            WeblogEntry entry = setupWeblogEntry("CommentParentDeletes1", weblog, user);
            endSession(true);

            entry = getManagedWeblogEntry(entry);
            setupComment("comment1", entry);
            setupComment("comment2", entry);
            setupComment("comment3", entry);
            endSession(true);

            // now deleting the entry should succeed and delete all comments
            Exception ex = null;
            try {
                weblogEntryManager.removeWeblogEntry(getManagedWeblogEntry(entry));
                endSession(true);
            } catch (WebloggerException e) {
                ex = e;
            }
            assertNull(ex);

            // now make sure we can delete a weblog with comments
            weblog = getManagedWeblog(weblog);
            user = getManagedUser(user);
            entry = setupWeblogEntry("CommentParentDeletes2", weblog, user);
            endSession(true);

            entry = getManagedWeblogEntry(entry);
            setupComment("comment1", entry);
            setupComment("comment2", entry);
            setupComment("comment3", entry);
            endSession(true);

            // now deleting the website should succeed 
            ex = null;
            try {
                weblog = getManagedWeblog(weblog);
                weblogManager.removeWeblog(weblog);
                endSession(true);
            } catch (WebloggerException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw); 
                e.printStackTrace(pw);
                log.info(sw.toString());
                ex = e;
            }
            assertNull(ex);

            // and delete test user as well
            userManager.removeUser(getManagedUser(user));
            
        } finally {
            endSession(true);
        }
        
        log.info("END");
    }

    @Test
    public void testBulkCommentDelete() throws Exception {
        List comments;
        
        // we need some comments to play with
        setupComment("deletemeXXX", testEntry);
        setupComment("XXXdeleteme", testEntry);
        setupComment("deleteme", testEntry);
        setupComment("saveme", testEntry);
        setupComment("saveme", testEntry);
        setupComment("saveme", testEntry);
        endSession(true);
        
        // get all comments
        CommentSearchCriteria csc = new CommentSearchCriteria();
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(6, comments.size());

        csc = new CommentSearchCriteria();
        csc.setSearchText("deleteme");
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());
       
        int countDeleted = weblogEntryManager.removeMatchingComments(
                null,         // website
                null,         // entry
                "deleteme",   // searchString
                null,         // startDate
                null,         // endDate
                null);        // approved
        assertEquals(3, countDeleted);
        strategy.flush();

        csc = new CommentSearchCriteria();
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());

        // remove remaining comments for entry
        countDeleted = weblogEntryManager.removeMatchingComments(
            null,         // website
            testEntry,    // entry
            null,         // searchString
            null,         // startDate
            null,         // endDate
            null);        // approved
        assertEquals(3, countDeleted);
        endSession(true);

        csc = new CommentSearchCriteria();
        csc.setEntry(testEntry);
        comments = weblogEntryManager.getComments(csc);
        assertNotNull(comments);
        assertEquals(0, comments.size());
    }
}
