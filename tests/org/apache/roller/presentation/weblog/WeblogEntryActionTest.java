package org.apache.roller.presentation.weblog;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.roller.RollerException;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.StrutsActionTestBase;
import org.apache.roller.presentation.weblog.actions.WeblogEntryFormAction;
import org.apache.roller.presentation.weblog.formbeans.WeblogEntryFormEx;

import com.mockrunner.mock.web.MockActionMapping;
import com.mockrunner.mock.web.MockHttpServletRequest;

/**
 * @author dave
 */
public class WeblogEntryActionTest extends StrutsActionTestBase
{
    public void testCreateWeblogEntry() 
    {
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/dummy");        
        doFilters();
        
        UserManager umgr = null;
        UserData user = null; 
        try
        {
            umgr = getRoller().getUserManager();
            user = (UserData)umgr.getUsers(mWebsite, null).get(0);       
            authenticateUser(user.getUserName(), "editor");
        }
        catch (RollerException e)
        {
            e.printStackTrace();
            fail();
        }
        
        // Setup mapping and request parameters
        MockActionMapping mapping = strutsModule.getMockActionMapping();
        mapping.setupForwards(new String[] {
            "access-denied","weblogEdit.page","weblogEntryRemove.page"});
        mapping.setParameter("method");  
        strutsModule.addRequestParameter("weblog",mWebsite.getHandle()); 
        strutsModule.addRequestParameter("method","create"); 
        
        // Setup form bean
        WeblogEntryFormEx form = (WeblogEntryFormEx)
            strutsModule.createActionForm(WeblogEntryFormEx.class);
        form.setTitle("test_title");
        form.setText("Test blog text");

        try {
            RollerRequest rreq = new RollerRequest(strutsModule.getMockPageContext());
            rreq.setWebsite(mWebsite);
            strutsModule.setRequestAttribute(RollerRequest.ROLLER_REQUEST, rreq);
            strutsModule.actionPerform(WeblogEntryFormAction.class, form);        
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
        // Test for success
        strutsModule.verifyNoActionMessages();
        strutsModule.verifyForward("weblogEdit.page");
        
        // Verify objects we put in context for JSP page
        verifyPageContext();
    }
    
    protected void verifyPageContext() 
    {
        HttpServletRequest req = (HttpServletRequest)
            servletModule.getFilteredRequest();
        assertNotNull(req.getAttribute("model"));
    }

    public static Test suite() 
    {
        return new TestSuite(WeblogEntryActionTest.class);
    }

}
