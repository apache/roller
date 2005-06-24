/*
 * Created on Oct 27, 2003
 */
package org.roller.presentation.bookmarks;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.FolderData;
import org.roller.pojos.UserData;
import org.roller.presentation.StrutsActionTestBase;
import org.roller.presentation.bookmarks.actions.BookmarksAction;
import org.roller.presentation.bookmarks.formbeans.BookmarksForm;

import com.mockrunner.mock.web.MockActionMapping;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;

/**
 * Test BookmarkAction (proof-of-concept for Mockrunner Struts testing)
 * @author Dave Johnson
 */
public class BookmarksActionTest extends StrutsActionTestBase
{
    public void testSelectFolder() 
    {       
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setServletContextName("/roller");        
        MockHttpServletRequest request = getMockFactory().getMockRequest();
        request.setContextPath("/roller");

        UserManager umgr = null;
        UserData user = null; 
        try
        {
            umgr = getRoller().getUserManager();
            user = (UserData)umgr.getUsers(mWebsite, null).get(0);       
        }
        catch (RollerException e)
        {
            e.printStackTrace();
            fail();
        }
        authenticateUser(user.getUserName(), "editor");
        doFilters();

        // Setup form bean
        BookmarksForm form = (BookmarksForm)
            strutsModule.createActionForm(BookmarksForm.class);

        // Setup mapping and request parameters
        MockActionMapping mapping = strutsModule.getMockActionMapping();
        mapping.setupForwards(new String[] {"access-denied","BookmarksForm"});
        mapping.setParameter("method");        
        strutsModule.addRequestParameter("method","selectFolder"); 
        
        strutsModule.actionPerform(BookmarksAction.class, form);        
        
        // Test for success
        strutsModule.verifyNoActionMessages();
        strutsModule.verifyForward("BookmarksForm");
        
        // Verify objects we put in context for JSP page
        verifyPageContext();
    }
    
    protected void verifyPageContext() 
    {
        HttpServletRequest req = (HttpServletRequest)
        servletModule.getFilteredRequest();
        assertTrue(req.getAttribute("folder") instanceof FolderData);
        assertTrue(req.getAttribute("folders") instanceof List);
        assertTrue(req.getAttribute("bookmarks") instanceof Set);
    }

    public static Test suite() 
    {
        return new TestSuite(BookmarksActionTest.class);
    }
}
