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
package org.apache.roller.ui.core.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.util.RequestUtil;


/**
 * Entry point filter for Weblog page and Editor UI, this filter
 * creates a RollerRequest object to parse pathinfo and request parameters.
 *
 * @web.filter name="RequestFilter"
 *
 * @author David M. Johnson, Matt Raible
 */
public class RequestFilter implements Filter {
    private FilterConfig mFilterConfig = null;
    private static Log mLogger =
        LogFactory.getFactory().getInstance(RequestFilter.class);
    
    public void doFilter(
            ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        // NOTE: Setting character encoding and JSTL/Struts locale sync has been
        // moved to CharEncodingFilter, which is mapped for all URIs in the context.
        
        HttpSession session = ((HttpServletRequest)req).getSession();
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        Roller roller = RollerFactory.getRoller();
        RollerRequest rreq = null;
        try {
            rreq = RollerRequest.getRollerRequest(
                       request, mFilterConfig.getServletContext());
        } catch (Throwable e) {            
            // NOTE: this is not a page-not-found problem
            request.setAttribute("DisplayException", e);            
            mLogger.error(e);
            return;
        }
        chain.doFilter(req, res);
    }
    
    public void init(FilterConfig filterConfig) throws ServletException {
        mFilterConfig = filterConfig;
    }
    
    public void destroy() {
    }
}

