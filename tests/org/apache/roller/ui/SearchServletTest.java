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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.mockrunner.mock.web.MockHttpServletRequest;
import org.apache.roller.ui.core.filters.PersistenceSessionFilter;
import org.apache.roller.ui.core.filters.RequestFilter;
import org.apache.roller.ui.rendering.search.SearchServlet;
import org.apache.roller.ui.rendering.velocity.VelocityServletTestBase;


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
