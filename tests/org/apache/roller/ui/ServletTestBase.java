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
package org.apache.roller.ui;

import com.mockrunner.mock.web.ActionMockObjectFactory;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;
import com.mockrunner.struts.ActionTestModule;
import java.io.File;

import java.io.FileInputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.ui.core.filters.PersistenceSessionFilter;
import org.apache.roller.ui.core.filters.RequestFilter;


/**
 * Base class for Roller Servlet tests with setup method that creates required 
 * MockRunner mocks for Servlet context, request and JSP factory objects.
 */
public abstract class ServletTestBase extends TestCase {
    protected ServletTestModule  servletModule;
    private WebMockObjectFactory mockFactory;
    protected MockRollerContext  rollerContext;
    protected ActionTestModule   strutsModule; // need Struts for message resources
    
    public void setUp() throws Exception {
        getMockFactory().refresh();
        
        servletModule = new ServletTestModule(getMockFactory());
        strutsModule = new ActionTestModule(getStrutsMockFactory());
        
        MockHttpServletRequest request = getMockFactory().getMockRequest();
        request.setContextPath("/roller");

        RollerRuntimeConfig.setAbsoluteContextURL("http://localhost:8080/roller");
        
        JspFactory.setDefaultFactory(new MockJspFactory(getMockFactory()));
                
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setServletContextName("/roller"); 
        ctx.setRealPath("/", ".");    
        
        
        ctx.setInitParameter("contextConfigLocation", "WEB-INF/security.xml");
        ctx.setResourceAsStream("/WEB-INF/security.xml", 
            new FileInputStream("WEB-INF" + File.separator + "security.xml")); 
        
        ctx.setResourceAsStream("/WEB-INF/velocity.properties", 
            new FileInputStream("WEB-INF" + File.separator + "velocity.properties")); 
        
        rollerContext = new MockRollerContext();
        rollerContext.init(ctx);
    }
    
    /** Convenience method for placing username in role via mockRequest. */
    protected void authenticateUser(String username, String role) {
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setRemoteUser(username);
        mockRequest.setUserPrincipal(new MockPrincipal(username));
        mockRequest.setUserInRole(role, true);
    }
    
    /** Run request through Roller filters (PersistenceSessionFilter and RequestFilter) */
    protected void doFilters() {
        servletModule.createFilter(PersistenceSessionFilter.class);
        servletModule.createFilter(RequestFilter.class);
        servletModule.setDoChain(false);
        servletModule.doFilter();
        getMockFactory().addRequestWrapper(new HttpServletRequestWrapper(
                (HttpServletRequest)servletModule.getFilteredRequest()));
    }

    protected ActionMockObjectFactory getStrutsMockFactory() {
        return (ActionMockObjectFactory)getMockFactory();
    }
    
    protected WebMockObjectFactory getMockFactory() {
        if (mockFactory == null) {
            mockFactory = new ActionMockObjectFactory();
        }
        return mockFactory;
    }

    /** MockRunner doesn't have one of these, but Roller rendering process needs one. */
    public class MockJspFactory extends JspFactory {
        
        public WebMockObjectFactory factory;
        
        public MockJspFactory(WebMockObjectFactory factory) {
            this.factory = factory;
        }
        
        public PageContext getPageContext(
                Servlet arg0, 
                ServletRequest arg1, 
                ServletResponse arg2,
                String arg3, 
                boolean arg4, 
                int arg5, 
                boolean arg6) {
            return factory.getMockPageContext();
        }
        
        public void releasePageContext(PageContext arg0) {}
        
        public JspEngineInfo getEngineInfo() {return null;}
    }
}
