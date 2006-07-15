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
import org.apache.roller.config.RollerRuntimeConfig;


/**
 * A special initialization filter which ensures that we have an opportunity
 * to extract a few pieces of information about the environment we are running
 * in when the first request is sent.
 *
 * @web.filter name="InitFilter"
 */
public class InitFilter implements Filter {
    
    private static Log log = LogFactory.getLog(InitFilter.class);
    
    private boolean initialized = false;
    
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        if(!initialized) {
            // first request, lets do our initialization
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            
            // determine absolute and relative url paths to the app
            String relPath = request.getContextPath();
            String absPath = this.getAbsoluteUrl(request);
            
            // set them in our config
            RollerRuntimeConfig.setAbsoluteContextURL(absPath);
            RollerRuntimeConfig.setRelativeContextURL(relPath);
            
            log.debug("relPath = "+relPath);
            log.debug("absPath = "+absPath);
            
            this.initialized = true;
        }
        
        chain.doFilter(req, res);
    }
    
    
    private String getAbsoluteUrl(HttpServletRequest request) {
        
        String url = null;
        
        String fullUrl = request.getRequestURL().toString();
        
        // first extract just the server portion of the url
        url = fullUrl.substring(0, fullUrl.indexOf(request.getRequestURI()));
        
        // then just add on the context path
        url += request.getContextPath();
        
        // make certain that we don't end with a /
        if(url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        
        return url;
    }
    
    
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    public void destroy() {}
    
}
