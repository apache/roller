/*
 * CommentTest.java
 *
 * Created on April 12, 2006, 3:12 PM
 */

package org.roller.business;

import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.TestUtils;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;


/**
 * Test Comment related business operations.
 *
 * That includes:
 */
public class CommentTest extends TestCase {
    
    public static Log log = LogFactory.getLog(CommentTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    WeblogEntryData testEntry = null;
    
    
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
            TestUtils.teardownUser(testUser.getId());
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
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        
        CommentData comment = new CommentData();
        comment.setName("test");
        comment.setEmail("test");
        comment.setUrl("test");
        comment.setRemoteHost("foofoo");
        comment.setContent("this is a test comment");
        comment.setPostTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        comment.setWeblogEntry(testEntry);
        comment.setPending(Boolean.FALSE);
        comment.setApproved(Boolean.TRUE);
        
        // create a comment
        mgr.saveComment(comment);
        String id = comment.getId();
        TestUtils.endSession(true);
        
        // make sure comment was created
        comment = null;
        comment = mgr.getComment(id);
        assertNotNull(comment);
        assertEquals("this is a test comment", comment.getContent());
        
        // update a comment
        comment.setContent("testtest");
        mgr.saveComment(comment);
        TestUtils.endSession(true);
        
        // make sure comment was updated
        comment = null;
        comment = mgr.getComment(id);
        assertNotNull(comment);
        assertEquals("testtest", comment.getContent());
        
        // delete a comment
        mgr.removeComment(comment);
        TestUtils.endSession(true);
        
        // make sure comment was deleted
        comment = null;
        comment = mgr.getComment(id);
        assertNull(comment);
    }
    
    
    /**
     * Test lookup mechanisms ... 
     */
    public void testCommentLookups() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        List comments = null;
        
        // we need some comments to play with
        CommentData comment1 = TestUtils.setupComment("comment1", testEntry);
        CommentData comment2 = TestUtils.setupComment("comment2", testEntry);
        CommentData comment3 = TestUtils.setupComment("comment3", testEntry);
        TestUtils.endSession(true);
        
        // get all comments
        comments = null;
        comments = mgr.getComments(null, null, null, null, null, null, null, null, false, 0, -1);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // get all comments for entry
        comments = null;
        comments = mgr.getComments(null, testEntry, null, null, null, null, null, null, false, 0, -1);
        assertNotNull(comments);
        assertEquals(3, comments.size());
        
        // make some changes
        comment3.setPending(Boolean.TRUE);
        comment3.setApproved(Boolean.FALSE);
        mgr.saveComment(comment3);
        
        // get pending comments
        comments = null;
        comments = mgr.getComments(null, null, null, null, null, Boolean.TRUE, null, null, false, 0, -1);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        
        // get approved comments
        comments = null;
        comments = mgr.getComments(null, null, null, null, null, null, Boolean.TRUE, null, false, 0, -1);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // get comments with offset
        comments = null;
        comments = mgr.getComments(null, null, null, null, null, null, null, null, false, 1, -1);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        
        // remove test comments
        TestUtils.teardownComment(comment1.getId());
        TestUtils.teardownComment(comment2.getId());
        TestUtils.teardownComment(comment3.getId());
        TestUtils.endSession(true);
    }
    
    
    /**
     * Test extra CRUD methods ... removeComments(ids), removeCommentsForEntry
     */
//    public void testAdvancedCommentCRUD() throws Exception {
//        
//        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
//        List comments = null;
//        
//        // we need some comments to play with
//        CommentData comment1 = TestUtils.setupComment("comment1", testEntry);
//        CommentData comment2 = TestUtils.setupComment("comment2", testEntry);
//        CommentData comment3 = TestUtils.setupComment("comment3", testEntry);
//        CommentData comment4 = TestUtils.setupComment("comment4", testEntry);
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
