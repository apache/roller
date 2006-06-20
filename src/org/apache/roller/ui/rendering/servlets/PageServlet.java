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
import java.util.Enumeration;
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
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.InvalidRequestException;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.WeblogPageRequest;
import org.apache.roller.util.cache.CachedContent;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererManager;
import org.apache.roller.ui.rendering.model.ModelLoader;
import org.apache.roller.util.Utilities;
import org.apache.roller.util.cache.Cache;
import org.apache.roller.util.cache.CacheHandler;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.util.cache.LazyExpiringCacheEntry;


/**
 * Responsible for rendering weblog pages.
 *
 * @web.servlet name="PageServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/page/*"
 */
public class PageServlet extends HttpServlet implements CacheHandler {
    
    private static Log log = LogFactory.getLog(PageServlet.class);
    
    // a unique identifier for our cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.weblogpage";
    
    private boolean excludeOwnerPages = false;
    private Cache contentCache = null;
    
    // for metrics
    private double hits = 0;
    private double misses = 0;
    private double purges = 0;
    private double skips = 0;
    private Date startTime = new Date();
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing weblog page servlet");
        
        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty(this.CACHE_ID+".excludeOwnerEditPages");
        
        Map cacheProps = new HashMap();
        Enumeration allProps = RollerConfig.keys();
        String prop = null;
        while(allProps.hasMoreElements()) {
            prop = (String) allProps.nextElement();
            
            // we are only interested in props for this cache
            if(prop.startsWith(CACHE_ID+".")) {
                cacheProps.put(prop.substring(CACHE_ID.length()+1), 
                        RollerConfig.getProperty(prop));
            }
        }
        
        log.info(cacheProps);
        
        contentCache = CacheManager.constructCache(this, cacheProps);
    }
    
    
    /**
     * Handle GET requests for weblog pages.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        // used for rendering
        HashMap model = new HashMap();
        
        WebsiteData website = null;
        
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
        } catch (Exception e) {
            // some kind of error parsing the request
            log.error("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // first off lets parse the incoming request and validate it
        // TODO: this is old logic from pre 3.0 that we'll remove when possible
        RollerRequest rreq = null;
        try {
            PageContext pageContext = 
                    JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
            
            rreq = RollerRequest.getRollerRequest(pageContext);
            
            // make sure the website is valid
            website = rreq.getWebsite();
            if(website == null)
                throw new InvalidRequestException("invalid weblog");
            
        } catch (Exception e) {
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.debug("ERROR initializing RollerRequest", e);
            return;
        }
        
        // determine what to render
        Template page = null;
        
        // If this is a popup request, then deal with it specially
        if (request.getParameter("popup") != null) {
            try {
                // Does user have a popupcomments page?
                page = website.getPageByName("_popupcomments");
            } catch(Exception e ) {
                // ignored ... considered page not found
            }
            
            // User doesn't have one so return the default
            if(page == null) {
                page = new WeblogTemplate("templates/weblog/popupcomments.vm", website,
                        "Comments", "Comments", "dummy_link",
                        "dummy_template", new Date());
            }
            
            rreq.setPage(page);
            
        } else if (rreq.getPage() != null) {
            // If request specified the page, then go with that
            page = rreq.getPage();
            
        } else {
            // If page not available from request, then use website's default
            try {
                page = website.getDefaultPage();
                rreq.setPage(page);
            } catch(Exception e) {
                log.error(e);
            }
        }
        
        // Still no page?  Then that is a 404
        if (page == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        log.debug("page found, dealing with it");
        
        // 304 if-modified-since checking
        long sinceDate = request.getDateHeader("If-Modified-Since");
        log.debug("since date = "+sinceDate);
        if(website.getLastModified().getTime() <= sinceDate) {
            log.debug("NOT MODIFIED "+request.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // set the content type
        String pageLink = page.getLink();
        String mimeType = RollerContext.getServletContext().getMimeType(pageLink);
        if(mimeType != null) {
            // we found a match ... set the content type
            response.setContentType(mimeType+"; charset=utf-8");
        } else {
            response.setContentType("text/html; charset=utf-8");
        }
        
        // set last-modified date
        response.setDateHeader("Last-Modified", website.getLastModified().getTime());
        
        // cached content checking
        String cacheKey = this.CACHE_ID+":"+this.generateKey(pageRequest);
        if(!this.excludeOwnerPages || !pageRequest.isLoggedIn()) {
            // we need the last expiration time for the given weblog
            long lastExpiration = 0;
            Date lastExpirationDate =
                    (Date) CacheManager.getLastExpiredDate(pageRequest.getWeblogHandle());
            if(lastExpirationDate != null)
                lastExpiration = lastExpirationDate.getTime();
            
            LazyExpiringCacheEntry entry =
                    (LazyExpiringCacheEntry) this.contentCache.get(cacheKey);
            if(entry != null) {
                CachedContent cachedContent = (CachedContent) entry.getValue(lastExpiration);
                
                if(cachedContent != null) {
                    log.debug("HIT "+cacheKey);
                    this.hits++;
                    
                    response.setContentLength(cachedContent.getContent().length);
                    response.getOutputStream().write(cachedContent.getContent());
                    return;

                } else {
                    log.debug("HIT-EXPIRED "+cacheKey);
                }
                
            } else {
                log.debug("MISS "+cacheKey);
                this.misses++;
            }
        }

        
        // looks like we need to render content
        try {
            // populate the model
            ModelLoader.loadPageModels(rreq.getWebsite(), rreq.getPageContext(), model);
            
        } catch (RollerException ex) {
            log.error("ERROR loading model for page", ex);
            
            if(!response.isCommitted()) response.reset();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        
        
        // lookup Renderer we are going to use
        Renderer renderer = null;
        try {
            log.debug("Looking up renderer");
            renderer = RendererManager.getRenderer("velocityWeblogPage", page.getId());
        } catch(Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for page "+page.getId(), e);
            
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
        if (!this.excludeOwnerPages || !pageRequest.isLoggedIn()) {
            log.debug("PUT "+cacheKey);
            this.contentCache.put(cacheKey, new LazyExpiringCacheEntry(rendererOutput));
        } else {
            log.debug("SKIPPED "+cacheKey);
            this.skips++;
        }
        
        log.debug("Exiting");
    }
        
    
    /**
     * Generate a cache key from a parsed weblog page request.
     * This generates a key of the form ...
     *
     * weblog/<handle>/page/<type>[/anchor]/<weblogPage>/<language>[/user]
     *   or
     * weblog/<handle>/page/<type>[/date][/category]/<weblogPage>/<language>[/user]
     *
     *
     * examples ...
     *
     * weblog/foo/page/main/Weblog/en
     * weblog/foo/page/permalink/entry_anchor/Weblog/en
     * weblog/foo/page/archive/20051110/Weblog/en
     * weblog/foo/page/archive/MyCategory/Weblog/en/user=myname
     *
     */
    private String generateKey(WeblogPageRequest pageRequest) {
        
        StringBuffer key = new StringBuffer();
        key.append("weblog/");
        key.append(pageRequest.getWeblogHandle().toLowerCase());
        key.append("/page/");
        key.append(pageRequest.getPageType());
        
        if(pageRequest.getWeblogAnchor() != null) {
            // convert to base64 because there can be spaces in anchors :/
            key.append("/").append(Utilities.toBase64(pageRequest.getWeblogAnchor().getBytes()));
        } else {
            
            if(pageRequest.getWeblogDate() != null) {
                key.append("/").append(pageRequest.getWeblogDate());
            }
            
            if(pageRequest.getWeblogCategory() != null) {
                String cat = pageRequest.getWeblogCategory();
                if(cat.startsWith("/")) {
                    cat = cat.substring(1);
                }

                // categories may contain spaces, which is not desired
                key.append("/").append(org.apache.commons.lang.StringUtils.deleteWhitespace(cat));
            }
        }
        
        // add page name
        key.append("/").append(pageRequest.getWeblogPage());
        
        // add language
        key.append("/").append(pageRequest.getLanguage());
        
        // add login state
        if(pageRequest.getAuthenticUser() != null) {
            key.append("/user=").append(pageRequest.getAuthenticUser());
        }
        
        return key.toString();
    }
    
    
    /**
     * A weblog entry has changed.
     */
    public void invalidate(WeblogEntryData entry) {
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(WebsiteData website) {
    }
    
    
    /**
     * A bookmark has changed.
     */
    public void invalidate(BookmarkData bookmark) {
    }
    
    
    /**
     * A folder has changed.
     */
    public void invalidate(FolderData folder) {
    }
    
    
    /**
     * A comment has changed.
     */
    public void invalidate(CommentData comment) {
    }
    
    
    /**
     * A referer has changed.
     */
    public void invalidate(RefererData referer) {
    }
    
    
    /**
     * A user profile has changed.
     */
    public void invalidate(UserData user) {
    }
    
    
    /**
     * A category has changed.
     */
    public void invalidate(WeblogCategoryData category) {
    }
    
    
    /**
     * A weblog template has changed.
     */
    public void invalidate(WeblogTemplate template) {
    }
    
    
    /**
     * Clear the entire cache.
     */
    public void clear() {
        log.info("Clearing cache");
        this.contentCache.clear();
        this.startTime = new Date();
        this.hits = 0;
        this.misses = 0;
        this.purges = 0;
        this.skips = 0;
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("cacheType", this.contentCache.getClass().getName());
        stats.put("startTime", this.startTime);
        stats.put("hits", new Double(this.hits));
        stats.put("misses", new Double(this.misses));
        stats.put("purges", new Double(this.purges));
        stats.put("skips", new Double(this.skips));
        
        // calculate efficiency
        if((misses - purges) > 0) {
            double efficiency = hits / (misses + hits);
            stats.put("efficiency", new Double(efficiency * 100));
        }
        
        return stats;
    }
    
}
