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
import org.apache.roller.util.IPBanList;


/**
 * Simple IP restriction filter.  Denied clients get a 404 reponse.
 *
 * @web.filter name="IPBanFilter"
 */
public class IPBanFilter implements Filter {
    
    private static Log log = LogFactory.getLog(IPBanFilter.class);
    
    
    public void init(FilterConfig filterConfig) throws ServletException {
        
        log.info("INIT IPBanFilter");
    }
    
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        // check if client is allowed
        if(IPBanList.getInstance().isBanned(request.getRemoteAddr())) {
            log.debug("BANNED "+request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            chain.doFilter(request, response);
        }
    }
    
    
    public void destroy() {}
    
}
