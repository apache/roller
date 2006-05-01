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
package org.roller.presentation.filters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.RollerFactory;
import org.roller.presentation.PlanetRequest;
import org.roller.presentation.cache.ExpiringCacheEntry;


/**
 * Handles if-modified-since checking for planet resources.
 *
 * @web.filter name="IfPlanetModifiedFilter"
 *
 * @author David M Johnson
 */
public class IfPlanetModifiedFilter implements Filter {
    
    private static Log mLogger = LogFactory.getLog(IfPlanetModifiedFilter.class);
    
    private long timeout = 15 * 60 * 1000;
    private ExpiringCacheEntry lastUpdateTime = null;
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
    
    
    /**
     * Filter processing.
     *
     * We check the incoming request for an "if-modified-since" header and
     * repond with a 304 NOT MODIFIED when appropriate.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        PlanetRequest planetRequest = null;
        try {
            planetRequest = new PlanetRequest(request);
        } catch(Exception e) {
            mLogger.error("error creating planet request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        Date updateTime = null;
        try {
            // first try our cached version
            if(this.lastUpdateTime != null) {
                updateTime = (Date) this.lastUpdateTime.getValue();
            }
            
            // we need to get a fresh value
            if(updateTime == null) {
                
                updateTime = RollerFactory.getRoller().getPlanetManager().getLastUpdated();
                if (updateTime == null) {
                    updateTime = new Date();
                    mLogger.warn("Can't get lastUpdate time, using current time instead");
                }
                
                this.lastUpdateTime = new ExpiringCacheEntry(updateTime, this.timeout);
            }
            
            // RSS context loader needs updateTime, so stash it
            request.setAttribute("updateTime", updateTime);
            
            // Check the incoming if-modified-since header
            Date sinceDate =
                    new Date(request.getDateHeader("If-Modified-Since"));
            
            if (updateTime != null) {
                // convert date (JDK 1.5 workaround)
                synchronized (dateFormatter) {
                    String date = dateFormatter.format(updateTime);
                    updateTime = new Date(date);
                }
                if (updateTime.compareTo(sinceDate) <= 0) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }
            
        } catch(RollerException re) {
            // problem talking to db?
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("DisplayException", re);
            return;
        } catch(IllegalArgumentException e) {
            // Thrown by getDateHeader if not in valid format. This can be
            // safely ignored, the only consequence is that the NOT MODIFIED
            // response is not set.
        }
        
        // Set outgoing last modified header
        if (updateTime != null) {
            response.setDateHeader("Last-Modified", updateTime.getTime());
        }
        
        chain.doFilter(request, response);
    }
    
    
    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        
        mLogger.info("Initializing if-modified planet filter");
        
        // lookup our timeout value
        String timeoutString = RollerConfig.getProperty("cache.planet.timeout");
        try {
            long timeoutSecs = Long.parseLong(timeoutString);
            this.timeout = timeoutSecs * 1000;
        } catch(Exception e) {
            // ignored ... illegal value
        }
    }
    
    
    public void destroy() {}
    
}
