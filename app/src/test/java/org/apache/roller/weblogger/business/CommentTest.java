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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Test Comment related business operations.
 *
 * That includes:
 */
public class CommentTest extends TestCase {
    
    public static Log log = LogFactory.getLog(CommentTest.class);
    
    User testUser = null;
    Weblog testWeblog = null;
    WeblogEntry testEntry = null;
    
    
    public CommentTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(CommentTest.class);
    }
    
    
    /**
     * All tests in this suite require a user, weblog, and an entry.
     */
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            testUser = TestUtils.setupUser("commentTestUser");
            testWeblog = TestUtils.setupWeblog("commentTestWeblog", testUser);
            testEntry = TestUtils.setupWeblogEntry("commentTestEntry", testWeblog.getDefaultCategory(), testWeblog, testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblogEntry(testEntry.getId());
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testCommentCRUD() throws Exception {
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setName("test");
        comment.setEmail("test");
        comment.setUrl("test");
        comment.setRemoteHost("foofoo");
        comment.setContent("this is a test comment");
        comment.setPostTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        comment.setWeblogEntry(TestUtils.getManagedWeblogEntry(testEntry));
        comment.setStatus(ApprovalStatus.APPROVED);
        
        // create a comment
        mgr.saveComment(comment);
        String id = comment.getId();
        TestUtils.endSession(true);
        
        // make sure comment was created
        comment = mgr.getComment(id);
        assertNotNull(comment);
        assertEquals("this is a test comment", comment.getContent());
        
        // update a comment
        comment.setContent("testtest");
        mgr.saveComment(comment);
        TestUtils.endSession(true);
        
        // make sure comment was updated
        comment = mgr.getComment(id);
        assertNotNull(comment);
        assertEquals("testtest", comment.getContent());
        
        // delete a comment
        mgr.removeComment(comment);
        TestUtils.endSession(true);
        
        // make sure comment was deleted
        comment = mgr.getComment(id);
        assertNull(comment);
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    public void testCommentLookups() throws Exception {
        
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        List comments;
        
        // we need some comments to play with
        testEntry = TestUtils.getManagedWeblogEntry(testEntry);
        WeblogEntryComment comment1 = TestUtils.setupComment("comment1", testEntry);
        WeblogEntryComment comment2 = TestUtils.setupComment("comment2", testEntry);
        WeblogEntryComment comment3 = TestUtils.setupComment("comment3", testEntry);
        TestUtils.endSession(true);
        
        // get all comments
        CommentSearchCriteria csc = new CommentSearchCriteria();
        comments = mgr.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // get all comments for entry
        testEntry = TestUtils.getManagedWeblogEntry(testEntry);
        csc.setEntry(testEntry);
        comments = mgr.getComments(csc);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // make some changes
        comment3 = mgr.getComment(comment3.getId());
        comment3.setStatus(ApprovalStatus.PENDING);
        mgr.saveComment(comment3);
        TestUtils.endSession(true);
        
        // get pending comments
        csc.setEntry(null);
        csc.setStatus(ApprovalStatus.PENDING);
        comments = mgr.getComments(csc);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        
        // get approved comments
        csc.setStatus(ApprovalStatus.APPROVED);
        comments = mgr.getComments(csc);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // get comments with offset
        csc.setStatus(null);
        csc.setOffset(1);
        comments = mgr.getComments(csc);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // remove test comments
        TestUtils.teardownComment(comment1.getId());
        TestUtils.teardownComment(comment2.getId());
        TestUtils.teardownComment(comment3.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test that when deleting parent objects of a comment that everything
     * down the chain is properly deleted as well.  i.e. deleting an entry
     * should delete all comments on that entry, and deleting a weblog should
     * delete all comments, etc.
     */
    public void testCommentParentDeletes() throws Exception {
        
        log.info("BEGIN");
        
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();        
            WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();        
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();

            // first make sure we can delete an entry with comments
            User user = TestUtils.setupUser("commentParentDeleteUser");
            Weblog weblog = TestUtils.setupWeblog("commentParentDelete", user);
            WeblogEntry entry = TestUtils.setupWeblogEntry("CommentParentDeletes1", 
                    weblog.getDefaultCategory(), weblog, user);
            TestUtils.endSession(true);

            entry = TestUtils.getManagedWeblogEntry(entry);
            TestUtils.setupComment("comment1", entry);
            TestUtils.setupComment("comment2", entry);
            TestUtils.setupComment("comment3", entry);
            TestUtils.endSession(true);

            // now deleting the entry should succeed and delete all comments
            Exception ex = null;
            try {
                emgr.removeWeblogEntry(TestUtils.getManagedWeblogEntry(entry));
                TestUtils.endSession(true);
            } catch (WebloggerException e) {
                ex = e;
            }
            assertNull(ex);

            // now make sure we can delete a weblog with comments
            weblog = TestUtils.getManagedWebsite(weblog);
            user = TestUtils.getManagedUser(user);
            entry = TestUtils.setupWeblogEntry("CommentParentDeletes2", 
                    weblog.getDefaultCategory(), weblog, user);
            TestUtils.endSession(true);

            entry = TestUtils.getManagedWeblogEntry(entry);
            TestUtils.setupComment("comment1", entry);
            TestUtils.setupComment("comment2", entry);
            TestUtils.setupComment("comment3", entry);
            TestUtils.endSession(true);

            // now deleting the website should succeed 
            ex = null;
            try {
                weblog = TestUtils.getManagedWebsite(weblog);
                wmgr.removeWeblog(weblog);
                TestUtils.endSession(true);
            } catch (WebloggerException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw); 
                e.printStackTrace(pw);
                log.info(sw.toString());
                ex = e;
            }
            assertNull(ex);

            // and delete test user as well
            umgr.removeUser(TestUtils.getManagedUser(user));
            
        } finally {
            TestUtils.endSession(true);
        }
        
        log.info("END");
    }
    
    
    /**
     * Apparently, HSQL has "issues" with LIKE expressions, 
     * so I'm commenting this out for now. 
     
    public void _testBulkCommentDelete() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        List comments = null;
        
        // we need some comments to play with
        WeblogEntryComment comment1 = TestUtils.setupComment("deletemeXXX", testEntry);
        WeblogEntryComment comment2 = TestUtils.setupComment("XXXdeleteme", testEntry);
        WeblogEntryComment comment3 = TestUtils.setupComment("deleteme", testEntry);
        WeblogEntryComment comment4 = TestUtils.setupComment("saveme", testEntry);
        WeblogEntryComment comment5 = TestUtils.setupComment("saveme", testEntry);
        WeblogEntryComment comment6 = TestUtils.setupComment("saveme", testEntry);
        TestUtils.endSession(true);
        
        // get all comments
        comments = null;
        comments = mgr.getComments(
            null, // website
            null, // entry
            null, // searchString
            null, // startDate
            null, // endDate
            null, // pending
            null, // approved
            null, // spam
            true, // reverseChrono
             0,   // offset
            -1);  // length
        assertNotNull(comments);
        assertEquals(6, comments.size());
        
        comments = mgr.getComments(
            null, // website
            null, // entry
            "deleteme", // searchString
            null, // startDate
            null, // endDate
            null, // pending
            null, // approved
            null, // spam
            true, // reverseChrono
             0,   // offset
            -1);  // length
        assertNotNull(comments);
        assertEquals(3, comments.size());
       
        int countDeleted = mgr.removeMatchingComments(
            null,         // website
            null,         // entry
            "deleteme",  // searchString
            null,         // startDate
            null,         // endDate
            null,         // pending
            null,         // approved
            null);        // spam        
        assertEquals(3, countDeleted);
        
        comments = mgr.getComments(
            null, // website
            null, // entry
            null, // searchString
            null, // startDate
            null, // endDate
            null, // pending
            null, // approved
            null, // spam
            true, // reverseChrono
             0,   // offset
            -1);  // length
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // remove test comments
        countDeleted = mgr.removeMatchingComments(
            null,         // website
            null,         // entry
            "saveme",    // searchString
            null,         // startDate
            null,         // endDate
            null,         // pending
            null,         // approved
            null);        // spam        
        assertEquals(3, countDeleted);
        TestUtils.endSession(true);
    }
    */
    
    /**
     * Test extra CRUD methods ... removeComments(ids), removeCommentsForEntry
     */
//    public void testAdvancedCommentCRUD() throws Exception {
//        
//        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
//        List comments = null;
//        
//        // we need some comments to play with
//        WeblogEntryComment comment1 = TestUtils.setupComment("comment1", testEntry);
//        WeblogEntryComment comment2 = TestUtils.setupComment("comment2", testEntry);
//        WeblogEntryComment comment3 = TestUtils.setupComment("comment3", testEntry);
//        WeblogEntryComment comment4 = TestUtils.setupComment("comment4", testEntry);
//        TestUtils.endSession(true);
//        
//        // remove a collection of comments
//        String[] delComments = new String[2];
//        delComments[0] = comment1.getId();
//        delComments[1] = comment2.getId();
//        mgr.removeComments(delComments);
//        TestUtils.endSession(true);
//        
//        // make sure comments were deleted
//        comments = null;
//        comments = mgr.getComments(null, null, null, null, null, null, null, null, false, 0, -1);
//        assertNotNull(comments);
//        assertEquals(2, comments.size());
//        
//        // remove all comments for entry
//        mgr.removeCommentsForEntry(testEntry.getId());
//        TestUtils.endSession(true);
//        
//        // make sure comments were deleted
//        comments = null;
//        comments = mgr.getComments(null, null, null, null, null, null, null, null, false, 0, -1);
//        assertNotNull(comments);
//        assertEquals(0, comments.size());
//    }
    
}
