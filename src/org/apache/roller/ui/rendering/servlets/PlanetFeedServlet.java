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
import java.util.Date;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.StaticTemplate;
import org.apache.roller.pojos.Template;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererManager;
import org.apache.roller.ui.rendering.util.PlanetCache;
import org.apache.roller.ui.rendering.util.PlanetRequest;
import org.apache.roller.ui.rendering.velocity.deprecated.OldUtilities;
import org.apache.roller.util.cache.CachedContent;


/**
 * Planet Roller (i.e. NOT for Planet Tool) RSS feed.
 *
 * @web.servlet name="PlanetFeedServlet" load-on-startup="7"
 * @web.servlet-mapping url-pattern="/planetrss/*"
 */
public class PlanetFeedServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(PlanetFeedServlet.class);
    
    private PlanetCache planetCache = null;
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing PlanetRssServlet");
        
        this.planetCache = PlanetCache.getInstance();
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        PlanetManager planet = null;
        try {
            planet = RollerFactory.getRoller().getPlanetManager();
        } catch (RollerException ex) {
            // error getting planet manager :(
            log.error("Unable to get planet manager", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        PlanetRequest planetRequest = null;
        try {
            planetRequest = new PlanetRequest(request);
        } catch (Exception e) {
            // some kind of error parsing the request
            log.debug("error creating planet request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // figure planet last modified date
        Date lastModified = planetCache.getLastModified();
        
        // 304 if-modified-since checking
        long sinceDate = request.getDateHeader("If-Modified-Since");
        log.debug("since date = "+sinceDate);
        if(lastModified.getTime() <= sinceDate) {
            log.debug("NOT MODIFIED "+request.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // set content type
        // set content type
        String accepts = request.getHeader("Accept");
        String userAgent = request.getHeader("User-Agent");
        if (accepts != null && userAgent != null 
            && accepts.indexOf("*/*") != -1 && userAgent.startsWith("Mozilla")) {
            // client is a browser and now that we offer styled feeds we want 
            // browsers to load the page rather than popping up the download 
            // dialog, so we provide a content-type that browsers will display
            response.setContentType("text/xml");
        } else {
            response.setContentType("application/rss+xml; charset=utf-8");
        }
        
        // set last-modified date
        // small hack here, we add 1 second (1000 ms) to the last mod time to
        // account for some lost precision when converting request date headers
        response.setDateHeader("Last-Modified", lastModified.getTime()+1000);
        
        // cached content checking
        String cacheKey = PlanetCache.CACHE_ID+":"+this.generateKey(planetRequest);
        CachedContent entry = (CachedContent) planetCache.get(cacheKey);
        if(entry != null) {
            response.setContentLength(entry.getContent().length);
            response.getOutputStream().write(entry.getContent());
            return;
            
        }
        
        
        // looks like we need to render content
        HashMap model = new HashMap();
        try {
            // populate the rendering model
            if (request.getParameter("group") != null) {
                model.put("group", planet.getGroup(request.getParameter("group")));
            }
            model.put("planet", planet);
            model.put("date", new Date());
            model.put("utilities", new OldUtilities());
            
            int entryCount =
                    RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
            int maxEntries =
                    RollerRuntimeConfig.getIntProperty("site.newsfeeds.maxEntries");
            String sCount = request.getParameter("count");
            if (sCount!=null) {
                try {
                    entryCount = Integer.parseInt(sCount);
                } catch (NumberFormatException e) {
                    log.warn("Improperly formatted count parameter");
                }
                if ( entryCount > maxEntries ) entryCount = maxEntries;
                if ( entryCount < 0 ) entryCount = 0;
            }
            model.put("entryCount", new Integer(entryCount));
            
        } catch (RollerException ex) {
            log.error("Error loading model objects for page", ex);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        
        // lookup Renderer we are going to use
        Renderer renderer = null;
        try {
            log.debug("Looking up renderer");
            Template template = new StaticTemplate("templates/planet/planetrss.vm", null, "velocity");
            renderer = RendererManager.getRenderer(template);
        } catch(Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for planet rss", e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // render content.  use default size of about 24K for a standard page
        CachedContent rendererOutput = new CachedContent(24567);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());
            
            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch(Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for planet rss", e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // post rendering process
        
        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());
        
        // cache rendered content.
        this.planetCache.put(cacheKey, rendererOutput);
        
        log.debug("Exiting");
    }

    
    /**
     * Generate a cache key from a parsed planet request.
     * This generates a key of the form ...
     *
     * <context>/<type>/<language>[/user]
     *   or
     * <context>/<type>[/flavor]/<language>[/excerpts]
     *
     *
     * examples ...
     *
     * planet/page/en
     * planet/feed/rss/en/excerpts
     *
     */
    private String generateKey(PlanetRequest planetRequest) {
        
        StringBuffer key = new StringBuffer();
        key.append(planetRequest.getContext());
        key.append("/");
        key.append(planetRequest.getType());
        
        if(planetRequest.getFlavor() != null) {
            key.append("/").append(planetRequest.getFlavor());
        }
        
        // add language
        key.append("/").append(planetRequest.getLanguage());
        
        if(planetRequest.getFlavor() != null) {
            // add excerpts
            if(planetRequest.isExcerpts()) {
                key.append("/excerpts");
            }
        } else {
            // add login state
            if(planetRequest.getAuthenticUser() != null) {
                key.append("/user=").append(planetRequest.getAuthenticUser());
            }
        }
        
        return key.toString();
    }
    
}
