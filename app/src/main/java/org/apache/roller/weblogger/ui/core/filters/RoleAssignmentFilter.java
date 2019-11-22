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

package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;


/**
 * Enable Roller's User Roles to work in CMA setup without a JDBC realm.
 * 
 * If you're using Container Manager Authentication (CMA) and you're not using
 * a JDBC realm that can add the User Roles defined by Roller, then you can
 * use this class to ensure that the request Role methods operate against the
 * User Roles as defined by Roller's User Manager.
 */
public class RoleAssignmentFilter implements Filter {
    
    private static Log log = LogFactory.getLog(RoleAssignmentFilter.class);

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        log.debug("Entered "+request.getRequestURI());        
        chain.doFilter(new RoleAssignmentRequestWrapper(request), res);
        log.debug("Exiting "+request.getRequestURI());
    }
    
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void destroy() {}
}


class RoleAssignmentRequestWrapper extends HttpServletRequestWrapper {
    private static Log log = LogFactory.getLog(RoleAssignmentRequestWrapper.class);

    public RoleAssignmentRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public boolean isUserInRole(String roleName) {
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        if (getUserPrincipal() != null) {
            try {
                User user = umgr.getUserByUserName(getUserPrincipal().getName(), Boolean.TRUE);
                return umgr.hasRole(roleName, user);
            } catch (WebloggerException ex) {
                log.error("ERROR checking user rile", ex);
            }
        }
        return false;
    }
}

