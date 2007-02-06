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
/*
 * Created on Oct 27, 2003
 */
package org.apache.roller.ui.authoring.struts.actions;



import com.mockrunner.mock.web.MockActionMapping;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.RollerException;
import org.apache.roller.TestUtils;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.StrutsActionTestBase;
import org.apache.roller.ui.authoring.struts.formbeans.BookmarksForm;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;

/**
 * Test BookmarkAction (proof-of-concept for Mockrunner Struts testing)
 * @author Dave Johnson
 */
public class BookmarksActionTest extends StrutsActionTestBase {
    private WebsiteData testWeblog = null;
    private UserData testUser = null;
    public static Log log = LogFactory.getLog(BookmarksActionTest.class);
    
    /**
     * All tests in this suite require a user and a weblog.
     */
    public void setUp() throws Exception {
        super.setUp();
        try {
            testUser = TestUtils.setupUser("bkmrkTestUser");
            testWeblog = TestUtils.setupWeblog("bkmrkTestWeblog", testUser);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getId());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }
    
    public void testSelectFolder() {
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setServletContextName("/roller");
        MockHttpServletRequest request = getMockFactory().getMockRequest();
        request.setContextPath("/roller");
        
        UserManager umgr = null;
        UserData user = null;
        try {
            umgr = RollerFactory.getRoller().getUserManager();
            user = (UserData)umgr.getUsers(testWeblog, null, null, null, 0, Integer.MAX_VALUE).get(0);
            doFilters();
            authenticateUser(user.getUserName(), "editor");
        } catch (RollerException e) {
            e.printStackTrace();
            fail();
        }
        
        // Setup form bean
        BookmarksForm form = (BookmarksForm)
        strutsModule.createActionForm(BookmarksForm.class);
        
        // Setup mapping and request parameters
        MockActionMapping mapping = strutsModule.getMockActionMapping();
        mapping.setupForwards(new String[] {"access-denied","BookmarksForm"});
        mapping.setParameter("method");
        strutsModule.addRequestParameter("weblog",testWeblog.getHandle());
        strutsModule.addRequestParameter("method","selectFolder");
        
        try {
            RollerRequest rreq = new RollerRequest(strutsModule.getMockPageContext());
            rreq.setWebsite(testWeblog);
            strutsModule.setRequestAttribute(RollerRequest.ROLLER_REQUEST, rreq);
            strutsModule.actionPerform(BookmarksAction.class, form);
        } catch (Throwable e) {
            e.printStackTrace();
            fail();
        }
        // Test for success
        strutsModule.verifyNoActionMessages();
        strutsModule.verifyForward("BookmarksForm");
        
        // Verify objects we put in context for JSP page
        verifyPageContext();
    }
    
    protected void verifyPageContext() {
        HttpServletRequest req = (HttpServletRequest)
        servletModule.getFilteredRequest();
        assertTrue(req.getAttribute("folder") instanceof FolderData);
        assertTrue(req.getAttribute("model") instanceof BasePageModel);
    }
    
    public static Test suite() {
        return new TestSuite(BookmarksActionTest.class);
    }
    
}
