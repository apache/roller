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

package org.apache.roller.weblogger.ui.rendering.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.rendering.RequestMapper;

/**
 * Provides generalized request mapping capablilites.
 *
 * Incoming requests can be inspected by a series of RequestMappers and can
 * potentially be re-routed to different places within the application.
 *
 * @web.filter name="RequestMappingFilter"
 */
public class RequestMappingFilter implements Filter {
    
    private static Log log = LogFactory.getLog(RequestMappingFilter.class);
    
    // list of RequestMappers that want to inspect the request
    private final List requestMappers = new ArrayList();
    
    
    public void init(FilterConfig filterConfig) {
        
        // lookup set of request mappers we are going to use
        String rollerMappers = WebloggerConfig.getProperty("rendering.rollerRequestMappers");
        String userMappers = WebloggerConfig.getProperty("rendering.userRequestMappers");
        
        // instantiate user defined request mapper classes
        if(userMappers != null && userMappers.trim().length() > 0) {
            
            RequestMapper requestMapper = null;
            String[] uMappers = userMappers.split(",");
            for(int i=0; i < uMappers.length; i++) {
                try {
                    Class mapperClass = Class.forName(uMappers[i]);
                    requestMapper = (RequestMapper) mapperClass.newInstance();
                    requestMappers.add(requestMapper);
                } catch(ClassCastException cce) {
                    log.error("It appears that your mapper does not implement "+
                            "the RequestMapper interface", cce);
                } catch(Exception e) {
                    log.error("Unable to instantiate request mapper ["+uMappers[i]+"]", e);
                }
            }
        }
        
        // instantiate roller standard request mapper classes
        if(rollerMappers != null && rollerMappers.trim().length() > 0) {
            
            RequestMapper requestMapper = null;
            String[] rMappers = rollerMappers.split(",");
            for(int i=0; i < rMappers.length; i++) {
                try {
                    Class mapperClass = Class.forName(rMappers[i]);
                    requestMapper = (RequestMapper) mapperClass.newInstance();
                    requestMappers.add(requestMapper);
                } catch(ClassCastException cce) {
                    log.error("It appears that your mapper does not implement "+
                            "the RequestMapper interface", cce);
                } catch(Exception e) {
                    log.error("Unable to instantiate request mapper ["+rMappers[i]+"]", e);
                }
            }
        }
        
        if(requestMappers.size() < 1) {
            // hmm ... failed to load any request mappers?
            log.warn("Failed to load any request mappers.  "+
                    "Weblog urls probably won't function as you expect.");
        }
        
        log.info("Request mapping filter initialized, "+requestMappers.size()+
                " mappers configured.");
    }
    
    
    /**
     * Inspect incoming urls and see if they should be routed.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.debug("entering");
        
        // give each mapper a chance to handle the request
        RequestMapper mapper = null;
        Iterator mappersIT = this.requestMappers.iterator();
        while(mappersIT.hasNext()) {
            mapper = (RequestMapper) mappersIT.next();
            
            log.debug("trying mapper "+mapper.getClass().getName());
            
            boolean wasHandled = mapper.handleRequest(request, response);
            if(wasHandled) {
                // if mapper has handled the request then we are done
                log.debug("request handled by "+mapper.getClass().getName());
                log.debug("exiting");
                return;
            }
        }
        
        log.debug("request not mapped");
        
        // nobody handled the request, so let it continue as usual
        chain.doFilter(request, response);
        
        log.debug("exiting");
    }
    
    
    public void destroy() {}
    
}
