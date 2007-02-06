package org.apache.roller.business;

import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.roller.TestUtils;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/**
 *
 */
public class WeblogStatsTest extends TestCase {
    
    private UserData user1, user2;
    
    private WebsiteData website1;
        private WeblogEntryData entry11;
            private CommentData comment11;
            private CommentData comment12;
        private WeblogEntryData entry12;
            private CommentData comment13;

    private WebsiteData website2;
        private WeblogEntryData entry21;
            private CommentData comment21; 
    
    public WeblogStatsTest() {
    }  
    protected void setUp() throws Exception {
        // create weblog with three entries and two comments per entry
        user1 = TestUtils.setupUser("a_commentCountTestUser");
        user2 = TestUtils.setupUser("b_commentCountTestUser");
        
        website1 = TestUtils.setupWeblog("a_testWebsite1", user1);
        entry11 = TestUtils.setupWeblogEntry(
                "anchor11", website1.getDefaultCategory(), website1, user1);
        comment11 = TestUtils.setupComment("Comment11", entry11);
        comment12 = TestUtils.setupComment("Comment12", entry11);
        entry12 = TestUtils.setupWeblogEntry(
                "anchor12", website1.getDefaultCategory(), website1, user1);
        comment13 = TestUtils.setupComment("Comment13", entry12);
        
        website2 = TestUtils.setupWeblog("b_testWebsite2", user1);
        entry21 = TestUtils.setupWeblogEntry(
                "anchor21", website2.getDefaultCategory(), website2, user1);
        comment21 = TestUtils.setupComment("Comment21", entry21);
        TestUtils.endSession(true);
    }
    public void testGetMostCommentedWeblogs() throws Exception {        
        UserManager mgr = RollerFactory.getRoller().getUserManager();      
        List list = mgr.getMostCommentedWebsites(null, null, 0, -1);  
        
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
    public void testGetMostCommentedWeblogEntries() throws Exception {
        
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();      
        List list = mgr.getMostCommentedWeblogEntries(null, null, null, 0, -1);
        
        assertNotNull(list);
        assertEquals(3, list.size());
        
        StatCount s1 = (StatCount)list.get(0);
        assertEquals(2L, s1.getCount()); 
        assertEquals(entry11.getId(), s1.getSubjectId());
        assertEquals(entry11.getAnchor(), s1.getSubjectNameShort());
        assertEquals(entry11.getWebsite().getHandle(), s1.getWeblogHandle());
               
        StatCount s2 = (StatCount)list.get(1);
        assertEquals(1L, s2.getCount());   
    }
    public void testGetUserNameLetterMap() throws Exception {        
        UserManager mgr = RollerFactory.getRoller().getUserManager();      
        Map map = mgr.getUserNameLetterMap();    
        assertNotNull(map.get("a"));
        assertNotNull(map.get("b"));
        assertNull(map.get("c"));
    }
    public void testGetWeblogLetterMap() throws Exception {        
        UserManager mgr = RollerFactory.getRoller().getUserManager();      
        Map map = mgr.getWeblogHandleLetterMap();    
        assertNotNull(map.get("a"));
        assertNotNull(map.get("b"));
        assertNull(map.get("c"));
    }
    protected void tearDown() throws Exception {
        
        // TODO: ATLAS figure out why comments must be torn down first
        TestUtils.teardownComment(comment11.getId());
        TestUtils.teardownComment(comment12.getId());
        TestUtils.teardownComment(comment13.getId());
        TestUtils.teardownWeblog(website1.getId());
        
        TestUtils.teardownComment(comment21.getId());
        TestUtils.teardownWeblog(website2.getId());  
        
        TestUtils.teardownUser(user1.getId());        
        TestUtils.teardownUser(user2.getId());        
        
        TestUtils.endSession(true);
    }
}
