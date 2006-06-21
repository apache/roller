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

package org.apache.roller.ui.rendering.servlets;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.InvalidRequestException;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererManager;
import org.apache.roller.util.cache.CachedContent;


/**
 * Generates simple rsd feed for a given weblog.
 *
 * This servlet supports 304 If-Modified-Since checking, but does not do any
 * level of content caching.
 *
 * @web.servlet name="RSDServlet" load-on-startup="7"
 * @web.servlet-mapping url-pattern="/rsd/*"
 */
public class RSDServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(RSDServlet.class);
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing RSDServlet");
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        HashMap model = new HashMap();
        WebsiteData weblog = null;
        WeblogRequest weblogRequest = null;
        
        // first off lets parse the incoming request and validate it
        try {
            weblogRequest = new WeblogRequest(request);
            
            // now make sure the specified weblog really exists
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            weblog = userMgr.getWebsiteByHandle(weblogRequest.getWeblogHandle(), Boolean.TRUE);
            
        } catch(InvalidRequestException ire) {
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.error("Bad Request: "+ire.getMessage());
            return;
            
        } catch(RollerException re) {
            // error looking up the weblog, we assume it doesn't exist
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.warn("Unable to lookup weblog ["+
                    weblogRequest.getWeblogHandle()+"] "+re.getMessage());
            return;
        }
        
        
        // 304 if-modified-since checking
        long sinceDate = request.getDateHeader("If-Modified-Since");
        log.debug("since date = "+sinceDate);
        if(weblog.getLastModified().getTime() <= sinceDate) {
            log.debug("NOT MODIFIED "+request.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // set the content type
        response.setContentType("application/rsd+xml; charset=utf-8");
        
        // set last-modified date
        response.setDateHeader("Last-Modified", weblog.getLastModified().getTime());
        
        
        // populate the model
        model.put("website", weblog);
        
        RollerContext rollerContext = new RollerContext();
        model.put("absBaseURL", rollerContext.getAbsoluteContextUrl(request));

        
        // lookup Renderer we are going to use
        Renderer renderer = null;
        try {
            log.debug("Looking up renderer");
            renderer = RendererManager.getRenderer("velocity", "templates/weblog/rsd.vm");
        } catch(Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for rsd template", e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // render content
        CachedContent rendererOutput = new CachedContent(4096);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());
            
            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch(Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for rsd template", e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // post rendering process
        
        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());
        
        log.debug("Exiting");
    }
    
}
