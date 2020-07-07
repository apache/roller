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
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.firewall.RequestRejectedException;

/**
 * <p>The spring default firewall ({@link org.springframework.security.web.firewall.StrictHttpFirewall}) 
 * is throwing exceptions if it decides to block a request. For example double slashes (//) in the request are
 * interpreted as non-normalized URL and rejected by throwing RequestRejectedExceptions.
 * Those exceptions are caught by the server and cause 500 errors which isn't very nice behavior.</p>
 * 
 * <p>The most straightforward way to handle this seems to be a servlet filter.</p>
 * 
 * @see org.springframework.security.web.firewall.StrictHttpFirewall
 */
public class SpringFirewallExceptionFilter implements Filter {
    
    private final static Log log = LogFactory.getLog(SpringFirewallExceptionFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (RequestRejectedException ex) {
            
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            
            // url seems to be dangerous -> log & 404
            log.warn("request rejected: " + req.getRequestURL() + " cause: " + ex.getMessage());
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            
        }
    }

    @Override
    public void destroy() {}

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    

}
