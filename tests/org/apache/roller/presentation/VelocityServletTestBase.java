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
package org.apache.roller.presentation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.apache.roller.RollerTestBase;
import org.apache.roller.presentation.filters.PersistenceSessionFilter;
import org.apache.roller.presentation.filters.RequestFilter;

import com.mockrunner.mock.web.ActionMockObjectFactory;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletConfig;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;
import com.mockrunner.struts.ActionTestModule;
import com.mockrunner.struts.MapMessageResources;


/** 
 * Base for VelocityServlet testing. 
 * @author Dave Johnson
 */
public abstract class VelocityServletTestBase extends RollerTestBase
{ 
    protected ServletTestModule servletModule;
    private WebMockObjectFactory mockFactory;
    protected MockRollerContext rollerContext;
    protected ActionTestModule strutsModule; // need Struts for message resources
    
    public void setUp() throws Exception
    {
        super.setUp();     
        getMockFactory().refresh();
        servletModule = new ServletTestModule(getMockFactory());
        strutsModule = new ActionTestModule(getStrutsMockFactory()); 
        
        MockServletContext app = getMockFactory().getMockServletContext();
        app.addResourcePath("/WEB-INF/toolbox.xml","/WEB-INF/toolbox.xml");
        app.setResourceAsStream("/WEB-INF/toolbox.xml",
                new FileInputStream("./WEB-INF/toolbox.xml"));
        
        MockServletConfig config = getMockFactory().getMockServletConfig();
        config.setInitParameter(
                "org.apache.velocity.properties","WEB-INF/velocity.properties");       

        MapMessageResources resources = new MapMessageResources();
        resources.putMessages("WEB-INF/classes/ApplicationResources.properties");
        strutsModule.setResources(resources);
        
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setRealPath("/", "");
        rollerContext = new MockRollerContext();
        rollerContext.init(ctx);
        
        JspFactory.setDefaultFactory(new MockJspFactory(getMockFactory())); 
    }
    protected void authenticateUser(String username, String role)
    {
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setRemoteUser(username);
        mockRequest.setUserPrincipal(new MockPrincipal(username));
        mockRequest.setUserInRole(role, true);
    }
    protected void doFilters()
    {
        servletModule.createFilter(PersistenceSessionFilter.class);
        servletModule.createFilter(RequestFilter.class);
        servletModule.setDoChain(false);
        servletModule.doFilter();        
        getMockFactory().addRequestWrapper(new HttpServletRequestWrapper(
            (HttpServletRequest)servletModule.getFilteredRequest()));
    }
    /** MockRunner doesn't have one of these */
    public class MockJspFactory extends JspFactory
    {
        public WebMockObjectFactory factory;
        public MockJspFactory(WebMockObjectFactory factory)
        {
            this.factory = factory;
        }
        public PageContext getPageContext(
            Servlet arg0, ServletRequest arg1, ServletResponse arg2, 
            String arg3, boolean arg4, int arg5, boolean arg6)
        {
            return factory.getMockPageContext();
        }
        public void releasePageContext(PageContext arg0) {}
        public JspEngineInfo getEngineInfo() {return null;}
    }
    protected ActionMockObjectFactory getStrutsMockFactory()
    {
        return (ActionMockObjectFactory)getMockFactory();
    }
    protected WebMockObjectFactory getMockFactory()
    {
        if (mockFactory == null) 
        {
            mockFactory = new ActionMockObjectFactory();
        }
        return mockFactory;
    }
}
