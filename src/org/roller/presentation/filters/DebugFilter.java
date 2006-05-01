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
/*
 * DebugFilter.java
 *
 * Created on April 17, 2006, 10:30 AM
 */

package org.roller.presentation.filters;

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


/**
 * A simple debugging filter.
 *
 * This filter is NOT mapped by default and is here only for Roller developers
 * to use while they are working on the code and debugging things.
 *
 * @web.filter name="DebugFilter"
 */
public class DebugFilter implements Filter {
    
    private static Log log = LogFactory.getLog(DebugFilter.class);
    
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.info("ENTERING "+request.getRequestURL());
        
        chain.doFilter(request, response);
        
        log.info("EXITING "+request.getRequestURL());
    }
    
    
    public void destroy() {}
    
    
    public void init(FilterConfig filterConfig) {}
    
}
