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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.roller.RollerException;
import org.apache.roller.RollerTestBase;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;


import com.mockrunner.mock.web.ActionMockObjectFactory;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;
import com.mockrunner.struts.ActionTestModule;
import com.mockrunner.struts.MapMessageResources;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.core.filters.PersistenceSessionFilter;
import org.apache.roller.ui.core.filters.RequestFilter;

/** 
 * Base for Struts Action testing. 
 * @author Dave Johnson
 */
public class StrutsActionTestBase extends RollerTestBase
{     
    private ActionMockObjectFactory mockFactory;
    protected MockRollerContext rollerContext;
    protected ActionTestModule strutsModule;
    protected ServletTestModule servletModule;

    public void setUp() throws Exception
    {
        super.setUp();       
        getMockFactory().refresh();
        strutsModule = new ActionTestModule(getStrutsMockFactory()); 
        servletModule = new ServletTestModule(getStrutsMockFactory());
        
        MapMessageResources resources = new MapMessageResources();
        resources.putMessages("WEB-INF/classes/ApplicationResources.properties");
        strutsModule.setResources(resources);
        
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setRealPath("/", "");
        rollerContext = new MockRollerContext();
        rollerContext.init(ctx);
    }
    protected void authenticateUser(String username, String role) 
        throws RollerException
    {
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setRemoteUser(username);
        mockRequest.setUserPrincipal(new MockPrincipal(username));
        mockRequest.setUserInRole(role, true);
        
        HttpSession session = mockRequest.getSession(true);        
        UserManager umgr = getRoller().getUserManager();
        UserData user = umgr.getUserByUsername(username);

        RollerSession rollerSession = new RollerSession();
        rollerSession.setAuthenticatedUser(user);
        session.setAttribute(RollerSession.ROLLER_SESSION, rollerSession);
    }
    
    protected void doFilters()
    {
        servletModule.createFilter(PersistenceSessionFilter.class);
        servletModule.createFilter(RequestFilter.class);
        servletModule.setDoChain(true);
        servletModule.doFilter();        
        getMockFactory().addRequestWrapper(new HttpServletRequestWrapper(
            (HttpServletRequest)servletModule.getFilteredRequest()));
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
