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

package org.apache.roller.ui.rendering.filters;

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
import org.apache.roller.config.RollerConfig;
import org.apache.roller.ui.rendering.util.PlanetRequest;
import org.apache.roller.ui.core.util.CacheHttpServletResponseWrapper;
import org.apache.roller.ui.core.util.ResponseContent;
import org.apache.roller.ui.rendering.util.PlanetCache;


/**
 * A cache filter for Planet Roller page ... /planet.do
 *
 * @web.filter name="PlanetCacheFilter"
 */
public class PlanetCacheFilter implements Filter {
    
    private static Log log = LogFactory.getLog(PlanetCacheFilter.class);
    
    private PlanetCache planetCache = null;
    
    private boolean excludeOwnerPages = false;
    
    
    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        
        log.info("Initializing planet cache filter");
        
        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty("cache.excludeOwnerEditPages");
        
        this.planetCache = PlanetCache.getInstance();
    }
    
    
    /**
     * Process filter.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        log.debug("entering");
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        PlanetRequest planetRequest = null;
        try {
            planetRequest = new PlanetRequest(request);
        } catch(Exception e) {
            // some kind of error parsing the request
            log.debug("error creating planet request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String key = planetCache.generateKey(planetRequest);
        try {
            ResponseContent respContent = null;
            if(!this.excludeOwnerPages || !planetRequest.isLoggedIn()) {
                respContent = (ResponseContent) this.planetCache.get(key);
            }
            
            if(respContent == null) {
                
                CacheHttpServletResponseWrapper cacheResponse =
                        new CacheHttpServletResponseWrapper(response);
                
                chain.doFilter(request, cacheResponse);
                
                cacheResponse.flushBuffer();
                
                // only cache if we didn't get an exception
                if (request.getAttribute("DisplayException") == null) {
                    
                    // only cache if this is not a logged in user?
                    if(!this.excludeOwnerPages || !planetRequest.isLoggedIn()) {
                        ResponseContent rc = cacheResponse.getContent();
                        this.planetCache.put(key, rc);
                    } else {
                        log.debug("SKIPPED "+key);
                    }
                } else {
                    // it is expected that whoever caught this display exception
                    // is the one who reported it to the logs
                    log.debug("Display exception "+key);
                }
                
            } else {
                respContent.writeTo(response);
            }
            
        } catch(Exception ex) {
            
            if(ex.getMessage().indexOf("ClientAbort") != -1) {
                // ClientAbortException ... ignored
                log.debug(ex.getMessage());
                
            } else if(ex.getMessage().indexOf("SocketException") != -1) {
                // SocketException ... ignored
                log.debug(ex.getMessage());
                
            } else {
                log.error("Unexpected exception rendering page "+key, ex);
            }
            
            // gotta send something to the client
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        log.debug("exiting");
    }
    
    
    /**
     * Destroy method for this filter
     */
    public void destroy() {}
    
}
