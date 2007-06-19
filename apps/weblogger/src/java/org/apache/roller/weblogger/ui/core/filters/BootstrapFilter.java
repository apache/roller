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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.business.utils.DatabaseCreator;
import org.apache.roller.weblogger.business.utils.DatabaseUpgrader;


/**
 * Checks database setup, forwards to appropriate error or setup page.
 */
public class BootstrapFilter implements Filter {
    private ServletContext context = null;
    private static Log log = LogFactory.getLog(BootstrapFilter.class);
    
    
    /**
     * Release Roller persistence session at end of request processing.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.debug("Entered "+request.getRequestURI());
        
        if ("auto".equals(RollerConfig.getProperty("installation.type"))) {            
            // an auto-install is in progress, do some checks and if necessary
            // redirect to error, db create or db upgrade page

            // only do this if Roller is configured for auto-install and 
            // only if request is NOT for install page or a style/script file            
            String requestURI = request.getRequestURI();
            if (     requestURI != null 
                 &&  requestURI.indexOf("/roller-ui/install") < 0 
                 && !requestURI.endsWith(".js")
                 && !requestURI.endsWith(".css")) {
                
                if (!RollerFactory.isBootstrapped()) {
                    // we doing an install, so forward to installer
                    RequestDispatcher rd = context.getRequestDispatcher(
                            "/roller-ui/install/install.rol");
                    rd.forward(req, res);
                    return;
                }
            }
        }
        chain.doFilter(request, response);                    
        log.debug("Exiting "+request.getRequestURI());
    }
    
    
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }
    
    public void destroy() {}
    
}

