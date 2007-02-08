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

package org.apache.roller.planet.ui.core.struts2;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class DebugFilter implements Filter {
    
    private static Log log = LogFactory.getLog(DebugFilter.class);
    
    
    public void init(FilterConfig filterConfig) {
        
    }
    
    
    /**
     * Inspect incoming urls and see if they should be routed.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.info("entering");
        
        // print out session details
        HttpSession session = request.getSession(false);
        if(session != null) {
            log.info("inbound session contains:");
            Enumeration foo = session.getAttributeNames();
            while(foo.hasMoreElements()) {
                String attr = (String) foo.nextElement();
                log.info(attr+" = "+session.getAttribute("attr"));
            }
        }
        
        // keep going
        chain.doFilter(request, response);
        
        // print out session details
        session = request.getSession(false);
        if(session != null) {
            log.info("outbound session contains:");
            Enumeration bar = session.getAttributeNames();
            while(bar.hasMoreElements()) {
                String attr = (String) bar.nextElement();
                log.info(attr+" = "+session.getAttribute("attr"));
            }
        }
        
        log.info("exiting");
    }
    
    
    public void destroy() {}
    
}
