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
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Redirects clients to install page when app is not bootstrapped and install
 * type is "auto", otherwise does nothing.
 */
public class BootstrapFilter implements Filter {
    private ServletContext context = null;
    private static Log log = LogFactory.getLog(BootstrapFilter.class);
    
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.debug("Entered "+request.getRequestURI());
        
        if ("auto".equals(WebloggerConfig.getProperty("installation.type"))
                && !WebloggerFactory.isBootstrapped() 
                && !isInstallUrl(request.getRequestURI())) {
                    
            log.debug("Forwarding to install page");
            
            // we doing an install, so forward to installer
            RequestDispatcher rd = context.getRequestDispatcher(
                "/roller-ui/install/install.rol");
            rd.forward(req, res);
            
        } else {
            chain.doFilter(request, response);
        }
        
        log.debug("Exiting "+request.getRequestURI());
    }
    
    
    private boolean isInstallUrl(String uri) {
        return (uri != null && (
                   uri.endsWith("bootstrap.rol")
                || uri.endsWith("create.rol") 
                || uri.endsWith("upgrade.rol") 
                || uri.endsWith(".js") 
                || uri.endsWith(".css")));
    }
    
    
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }
    
    public void destroy() {}    
}
