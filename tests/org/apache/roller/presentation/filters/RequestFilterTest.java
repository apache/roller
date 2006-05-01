package org.apache.roller.presentation.filters;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.VelocityServletTestBase;
import org.apache.roller.presentation.velocity.PageServlet;

import com.mockrunner.mock.web.MockHttpServletRequest;

/** 
 * @author Dave Johnson
 */
public class RequestFilterTest extends VelocityServletTestBase {
    public void setUp() throws Exception
    {
        super.setUp();       
    }
    public RequestFilterTest() {
    }
    public void testRequestFilter() throws Exception {        
        
        servletModule.setServlet(
           servletModule.createServlet(PageServlet.class));       

        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/roller/page");
        mockRequest.setPathInfo("/testuser/20050101");
        mockRequest.setRequestURL("http://localost:8080");

        servletModule.createFilter(PersistenceSessionFilter.class);
        servletModule.createFilter(RequestFilter.class);
        servletModule.setDoChain(true);

        servletModule.doFilter();   
        
        HttpServletRequest req = (HttpServletRequest)
            servletModule.getFilteredRequest();
        RollerRequest rreq = RollerRequest.getRollerRequest(req);
        assertNotNull(rreq);    
    }
    public static Test suite() 
    {
        return new TestSuite(RequestFilterTest.class);
    }
}
