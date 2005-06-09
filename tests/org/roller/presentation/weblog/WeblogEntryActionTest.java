package org.roller.presentation.weblog;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.presentation.StrutsActionTestBase;
import org.roller.presentation.weblog.actions.WeblogEntryFormAction;
import org.roller.presentation.weblog.formbeans.WeblogEntryFormEx;

import com.mockrunner.mock.web.MockActionMapping;
import com.mockrunner.mock.web.MockHttpServletRequest;

/**
 * @author dave
 */
public class WeblogEntryActionTest extends StrutsActionTestBase
{
    public void testCreateWeblogEntry() 
    {
        authenticateUser(mWebsite.getUser().getUserName(), "editor");
        
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/dummy");
        
        doFilters();

        // Setup mapping and request parameters
        MockActionMapping mapping = strutsModule.getMockActionMapping();
        mapping.setupForwards(new String[] {
            "access-denied","weblogEdit.page","weblogEntryRemove.page"});
        mapping.setParameter("method");        
        strutsModule.addRequestParameter("method","create"); 
        
        // Setup form bean
        WeblogEntryFormEx form = (WeblogEntryFormEx)
            strutsModule.createActionForm(WeblogEntryFormEx.class);
        form.setTitle("test_title");
        form.setText("Test blog text");

        strutsModule.actionPerform(WeblogEntryFormAction.class, form);        
        
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
