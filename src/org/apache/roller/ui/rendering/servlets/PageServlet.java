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
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.util.cache.CachedContent;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererManager;
import org.apache.roller.ui.rendering.model.ModelLoader;
import org.apache.roller.ui.rendering.util.SiteWideCache;
import org.apache.roller.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.ui.rendering.util.WeblogPageCache;

 

/**
 * Provides access to weblog pages.
 *
 * @web.servlet name="PageServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/page/*"
 */
public class PageServlet extends HttpServlet {
    
    private static Log log = LogFactory.getLog(PageServlet.class);
    
    private boolean excludeOwnerPages = false;
    
    private WeblogPageCache weblogPageCache = null;
    private SiteWideCache siteWideCache = null;
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing PageServlet");
        
        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty("cache.excludeOwnerEditPages");
        
        // get a reference to the weblog page cache
        this.weblogPageCache = WeblogPageCache.getInstance();
        
        // get a reference to the site wide cache
        this.siteWideCache = SiteWideCache.getInstance();
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        WebsiteData weblog = null;
        boolean isSiteWide = false;
        
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
            
            weblog = pageRequest.getWeblog();
            if(weblog == null) {
                throw new RollerException("unable to lookup weblog: "+
                        pageRequest.getWeblogHandle());
            }
            
            // is this the site-wide weblog?
            isSiteWide = RollerRuntimeConfig.isSiteWideWeblog(pageRequest.getWeblogHandle());
            
        } catch (Exception e) {
            // some kind of error parsing the request or looking up weblog
            log.debug("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // determine the lastModified date for this content
        long lastModified = 0;
        if(isSiteWide) {
            lastModified = siteWideCache.getLastModified().getTime();
        } else {
            lastModified = weblog.getLastModified().getTime();
        }
        
        // 304 if-modified-since checking
        long sinceDate = request.getDateHeader("If-Modified-Since");
        log.debug("since date = "+sinceDate);
        if(lastModified <= sinceDate) {
            log.debug("NOT MODIFIED "+request.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // set last-modified date
        response.setDateHeader("Last-Modified", lastModified);
        
        
        // set the content type
        String pageLink = pageRequest.getWeblogPageName();
        String mimeType = RollerContext.getServletContext().getMimeType(pageLink);
        if(mimeType != null) {
            // we found a match ... set the content type
            response.setContentType(mimeType+"; charset=utf-8");
        } else {
            response.setContentType("text/html; charset=utf-8");
        }
        
        
        // generate cache key
        String cacheKey = null;
        if(isSiteWide) {
            cacheKey = siteWideCache.generateKey(pageRequest);
        } else {
            cacheKey = weblogPageCache.generateKey(pageRequest);
        }
        
        // cached content checking
        if((!this.excludeOwnerPages || !pageRequest.isLoggedIn()) &&
                request.getAttribute("skipCache") == null) {
            
            CachedContent cachedContent = null;
            if(isSiteWide) {
                cachedContent = (CachedContent) siteWideCache.get(cacheKey);
            } else {
                cachedContent = (CachedContent) weblogPageCache.get(cacheKey, lastModified);
            }
            
            if(cachedContent != null) {
                log.debug("HIT "+cacheKey);
                
                response.setContentLength(cachedContent.getContent().length);
                response.getOutputStream().write(cachedContent.getContent());
                return;
                
            } else {
                log.debug("MISS "+cacheKey);
            }
        }

        
        // figure out what we are going to render
        Template page = null;
        
        // If this is a popup request, then deal with it specially
        if (request.getParameter("popup") != null) {
            try {
                // Does user have a popupcomments page?
                page = weblog.getPageByName("_popupcomments");
            } catch(Exception e ) {
                // ignored ... considered page not found
            }
            
            // User doesn't have one so return the default
            if(page == null) {
                page = new WeblogTemplate("templates/weblog/popupcomments.vm", weblog,
                        "Comments", "Comments", "dummy_link",
                        "dummy_template", new Date(), "velocity", true, null);
            }
            
        // If request specified the page, then go with that
        } else if (pageRequest.getWeblogPageName() != null) {
            page = pageRequest.getWeblogPage();
            
        // If page not available from request, then use weblog's default
        } else {
            try {
                page = weblog.getDefaultPage();
            } catch(Exception e) {
                log.error("Error getting weblogs default page", e);
            }
        }
        
        // Still no page?  Then that is a 404
        if (page == null) {
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        log.debug("page found, dealing with it");
        
        // validation
        boolean invalid = false;
        if(pageRequest.getWeblogPageName() != null && page.isHidden()) {
            invalid = true;
        }
        if(pageRequest.getLocale() != null) {
            
            // locale view only allowed if weblog has enabled it
            if(!pageRequest.getWeblog().isEnableMultiLang()) {
                invalid = true;
            }
            
        }
        if(pageRequest.getWeblogAnchor() != null) {
            
            // permalink specified.  entry must exist and locale must match
            if(pageRequest.getWeblogEntry() == null) {
                invalid = true;
            } else if (pageRequest.getLocale() != null && 
                    !pageRequest.getLocale().equals(pageRequest.getWeblogEntry().getLocale())) {
                invalid = true;
            }
            
        } else if(pageRequest.getWeblogCategoryName() != null) {
            
            // category specified.  category must exist.
            if(pageRequest.getWeblogCategory() == null) {
                invalid = true;
            }
        }
        
        if(invalid) {
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // looks like we need to render content
        HashMap model = new HashMap();
        try {
            PageContext pageContext = JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
            
            // special hack for menu tag
            request.setAttribute("pageRequest", pageRequest);
            
            // populate the rendering model
            Map initData = new HashMap();
            initData.put("request", request);
            initData.put("pageRequest", pageRequest);
            initData.put("weblogRequest", pageRequest);
            initData.put("pageContext", pageContext);
            
            // if this was a comment posting, check for comment form
            WeblogEntryCommentForm commentForm = 
                    (WeblogEntryCommentForm) request.getAttribute("commentForm");
            if(commentForm != null) {
                initData.put("commentForm", commentForm);
            }
            
            // Load models for pages
            String pageModels = RollerConfig.getProperty("rendering.pageModels");
            ModelLoader.loadModels(pageModels, model, initData, true);
            
            // Load special models for site-wide blog
            if(RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                String siteModels = RollerConfig.getProperty("rendering.siteModels");
                ModelLoader.loadModels(siteModels, model, initData, true);
            }

            // Load weblog custom models
            ModelLoader.loadCustomModels(weblog, model, initData);
            
            // ick, gotta load pre-3.0 model stuff as well :(
            ModelLoader.loadOldModels(model, request, response, pageContext, pageRequest);
            
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
            renderer = RendererManager.getRenderer(page);
        } catch(Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for page "+page.getId(), e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // render content.  use size of about 24K for a standard page
        CachedContent rendererOutput = new CachedContent(24567);
        try {
            log.debug("Doing rendering");
            renderer.render(model, rendererOutput.getCachedWriter());
            
            // flush rendered output and close
            rendererOutput.flush();
            rendererOutput.close();
        } catch(Exception e) {
            // bummer, error during rendering
            log.error("Error during rendering for page "+page.getId(), e);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        // post rendering process
        
        // flush rendered content to response
        log.debug("Flushing response output");
        response.setContentLength(rendererOutput.getContent().length);
        response.getOutputStream().write(rendererOutput.getContent());
        
        // cache rendered content.  only cache if user is not logged in?
        if((!this.excludeOwnerPages || !pageRequest.isLoggedIn()) &&
                request.getAttribute("skipCache") == null) {
            log.debug("PUT "+cacheKey);
            
            // put it in the right cache
            if(isSiteWide) {
                siteWideCache.put(cacheKey, rendererOutput);
            } else {
                weblogPageCache.put(cacheKey, rendererOutput);
            }
        } else {
            log.debug("SKIPPED "+cacheKey);
        }
        
        log.debug("Exiting");
    }
        
    
    /**
     * Handle POST requests.
     *
     * We have this here because the comment servlet actually forwards some of
     * its requests on to us to render some pages with cusom messaging.  We
     * may want to revisit this approach in the future and see if we can do
     * this in a different way, but for now this is the easy way.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // make sure caching is disabled
        request.setAttribute("skipCache", "true");
        
        // handle just like a GET request
        this.doGet(request, response);
    }
    
}
