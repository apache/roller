/*
 * WeblogPageTest.java
 *
 * Created on April 7, 2006, 2:57 PM
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
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.WebsiteData;


/**
 * Test Weblog Page related business operations.
 */
public class WeblogPageTest extends TestCase {
    
    public static Log log = LogFactory.getLog(WeblogPageTest.class);
    
    UserData testUser = null;
    WebsiteData testWeblog = null;
    WeblogTemplate testPage = null;
    
    
    public WeblogPageTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(WeblogPageTest.class);
    }
    
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        
        try {
            testUser = TestUtils.setupUser("wtTestUser");
            testWeblog = TestUtils.setupWeblog("wtTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
        
        testPage = new WeblogTemplate();
        testPage.setName("testTemplate");
        testPage.setDescription("Test Weblog Template");
        testPage.setLink("testTemp");
        testPage.setContents("a test weblog template.");
        testPage.setLastModified(new java.util.Date());
        testPage.setWebsite(testWeblog);
    }
    
    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
        
        testPage = null;
    }
    
    
    /**
     * Test basic persistence operations ... Create, Update, Delete
     */
    public void testTemplateCRUD() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        WeblogTemplate template = null;
        
        // create template
        mgr.savePage(testPage);
        TestUtils.endSession(true);
        
        // check that create was successful
        template = null;
        template = mgr.getPageByName(testWeblog, testPage.getName());
        assertNotNull(template);
        assertEquals(testPage.getContents(), template.getContents());
        
        // update template
        template.setName("testtesttest");
        mgr.savePage(template);
        TestUtils.endSession(true);
        
        // check that update was successful
        template = null;
        template = mgr.getPageByName(testWeblog, "testtesttest");
        assertNotNull(template);
        assertEquals(testPage.getContents(), template.getContents());
        
        // delete template
        mgr.removePage(template);
        TestUtils.endSession(true);
        
        // check that delete was successful
        template = null;
        template = mgr.getPageByName(testWeblog, testPage.getName());
        assertNull(template);
    }
    
    
    /**
     * Test lookup mechanisms ... id, name, link, weblog
     */
    public void testPermissionsLookups() throws Exception {
        
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        WeblogTemplate page = null;
        
        // create page
        mgr.savePage(testPage);
        String id = testPage.getId();
        TestUtils.endSession(true);
        
        // lookup by id
        page = mgr.getPage(id);
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup by name
        page = null;
        page = mgr.getPageByName(testWeblog, testPage.getName());
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup by link
        page = null;
        page = mgr.getPageByLink(testWeblog, testPage.getLink());
        assertNotNull(page);
        assertEquals(testPage.getContents(), page.getContents());
        
        // lookup all pages for weblog
        List pages = mgr.getPages(testWeblog);
        assertNotNull(pages);
        assertEquals(1, pages.size());
        
        // delete page
        mgr.removePage(page);
        TestUtils.endSession(true);
    }
    
}
