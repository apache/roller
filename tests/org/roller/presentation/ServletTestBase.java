/*
 * Created on Mar 9, 2004
 */
package org.roller.presentation;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;

import org.roller.business.RollerTestBase;

/**
 * @author lance.lavandowska
 */
public class ServletTestBase extends RollerTestBase
{
    protected WebMockObjectFactory mockFactory;
    protected MockRollerContext rollerContext;
    
    public void setUp() throws Exception
    {
        // must do super.setup() before creating MockRollerContext
        super.setUp();
    
        mockFactory = new WebMockObjectFactory();
        
        // have to set ServletContext init-param
        MockServletContext mContext = mockFactory.getMockServletContext();
        String persistence = System.getProperty("rollerImpl");
        if (persistence == null || persistence.equals("hibernate") )
        {
            // Instantiate Hibernate version
            persistence = "org.roller.business.hibernate.RollerImpl";
        }
        else
        {
            // Instantiate Castor version
            persistence = "org.roller.business.castor.RollerImpl"; 
        }
        mContext.setInitParameter("org.roller.persistence", persistence);
    
        // create mock RollerContext
        rollerContext = new MockRollerContext();
        rollerContext.init(mContext);
    }
    
    public void tearDown() throws Exception
    {
        rollerContext = null;
        mockFactory = null;
        super.tearDown();
    }

    //-----------------------------------------------------------------------
    protected String authenticateUser()
    {
        String username = mWebsite.getUser().getUserName();

        // in order to pass authentication check
        MockHttpServletRequest mkRequest = mockFactory.getMockRequest();
        mkRequest.setUserPrincipal(new MockPrincipal(username));
        mkRequest.setUserInRole("editor", true);
        
        // for RollerRequest initialization, parseRequestParams()
        mkRequest.setRemoteUser(username);

        return username;
    }
}
