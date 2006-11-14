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
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;


/**
 * Sole responsibility is to ensure that each request's Roller
 * persistence session is released at end of the request.
 *
 * @web.filter name="PersistenceSessionFilter"
 */
public class PersistenceSessionFilter implements Filter {
    
    private static Log log = LogFactory.getLog(PersistenceSessionFilter.class);
    
    
    /**
     * Release Roller persistence session at end of request processing.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.debug("Entered "+request.getRequestURI());
        
        try {
            chain.doFilter(request, response);
        } finally {
            log.debug("Releasing Roller Session");
            RollerFactory.getRoller().release();
        }
        
        log.debug("Exiting "+request.getRequestURI());
    }
    
    
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    public void destroy() {}
    
}

