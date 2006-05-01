package org.apache.roller.presentation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.roller.presentation.filters.PersistenceSessionFilter;
import org.apache.roller.presentation.filters.RequestFilter;
import org.apache.roller.presentation.search.SearchServlet;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletConfig;


/**
 * @author Dave Johnson
 */
public class SearchServletTest extends VelocityServletTestBase
{    
    public void testSearch() throws Exception
    {
        servletModule.setServlet(
            servletModule.createServlet(SearchServlet.class)); 
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();

        mockRequest.setContextPath("/search");
        mockRequest.setupAddParameter("q","test");
 
        servletModule.createFilter(PersistenceSessionFilter.class);
        servletModule.createFilter(RequestFilter.class);
        servletModule.setDoChain(true);
        
        servletModule.doFilter();        
        getMockFactory().addRequestWrapper(new HttpServletRequestWrapper(
            (HttpServletRequest)servletModule.getFilteredRequest()));
        servletModule.doGet();
        assertNotNull(
            servletModule.getRequestAttribute("zzz_VelocityContext_zzz"));     
    }
    public static Test suite() 
    {
        return new TestSuite(SearchServletTest.class);
    }
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(SearchServletTest.class);
    }
}
