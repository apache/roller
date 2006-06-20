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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
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
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.WeblogFeedRequest;
import org.apache.roller.util.cache.CachedContent;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.roller.ui.rendering.RendererManager;
import org.apache.roller.ui.rendering.model.ModelLoader;
import org.apache.roller.util.cache.Cache;
import org.apache.roller.util.cache.CacheHandler;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.util.cache.LazyExpiringCacheEntry;


/**
 * Responsible for rendering weblog feeds.
 *
 * @web.servlet name="FeedServlet" load-on-startup="5"
 * @web.servlet-mapping url-pattern="/flavor/*"
 * @web.servlet-mapping url-pattern="/rss/*"
 * @web.servlet-mapping url-pattern="/atom/*"
 */ 
public class FeedServlet extends HttpServlet implements CacheHandler {
    
    private static Log log = LogFactory.getLog(FeedServlet.class);
    
    // a unique identifier for our cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.feed";
    
    private Cache contentCache = null;
    
    // for metrics
    private double hits = 0;
    private double misses = 0;
    private double purges = 0;
    private Date startTime = new Date();
    
    
    /**
     * Init method for this servlet
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        
        super.init(servletConfig);
        
        log.info("Initializing weblog feed servlet");
        
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
     * Handle GET requests for weblog feeds.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        // used for rendering
        HashMap model = new HashMap();
        
        RollerRequest rreq = null;
        
        WeblogFeedRequest feedRequest = null;
        try {
            feedRequest = new WeblogFeedRequest(request);
        } catch(Exception e) {
            // some kind of error parsing the request
            log.error("error creating weblog feed request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // first off lets parse the incoming request and validate it
        // TODO: this is old pre 3.0 stuff which should be removed when possible
        try {
            PageContext pageContext =
                    JspFactory.getDefaultFactory().getPageContext(
                    this, request,  response, "", true, 8192, true);
            
            rreq = RollerRequest.getRollerRequest(pageContext);
            
            // This is an ugly hack to fix the following bug:
            // ROL-547: "Site wide RSS feed is your own if you are logged in"
            String[] pathInfo = StringUtils.split(rreq.getPathInfo(),"/");
            if (pathInfo.length < 1) {
                // If weblog not specified in URL, set it to null
                rreq.setWebsite(null);
            }
            
        } catch (RollerException e) {
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            log.debug("ERROR initializing RollerRequest", e);
            return;
        }
        
        
        String pageId = null;
        WebsiteData weblog = rreq.getWebsite();
        if (request.getServletPath().endsWith("rss")) {
            
            if (weblog != null) {
                try {
                    // if useer has a custom rss template then use it
                    Template page = weblog.getPageByName("_rss");
                    if(page != null) {
                        pageId = page.getId();
                    }
                } catch (RollerException ex) {
                    // consider this a page not found
                }
            }
            
            if(pageId == null) {
                pageId = "templates/feeds/rss.vm";
            }
        } else if (request.getServletPath().endsWith("atom")) {
            
            if (weblog != null) {
                try {
                    // if user has a custom atom template then use it
                    Template page = weblog.getPageByName("_atom");
                    if(page != null) {
                        pageId = page.getId();
                    }
                } catch (RollerException ex) {
                    // consider this a page not found
                }
            }
            
            if(pageId == null) {
                pageId = "templates/feeds/atom.vm";
            }
        } else if (request.getParameter("flavor") != null) {
            
            // If request specifies a "flavor" then use that.
            String flavor = request.getParameter("flavor");
            pageId = "templates/feeds/" + flavor + ".vm";
        } else {
            
            // Fall through to default RSS page template.
            pageId = "templates/feeds/rss.vm";
        }
            
        
        // 304 if-modified-since checking
        long sinceDate = request.getDateHeader("If-Modified-Since");
        log.debug("since date = "+sinceDate);
        // TODO: need to have way to checking weblog last modified time
        if(weblog.getDateCreated().getTime() <= sinceDate) {
            log.debug("NOT MODIFIED "+request.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        
        // set last-modified date
        // TODO: figure out how to get weblog last modified time
        response.setDateHeader("Last-Modified", weblog.getDateCreated().getTime());
        
        // cached content checking
        String cacheKey = this.CACHE_ID+":"+this.generateKey(feedRequest);
        
        // we need the last expiration time for the given weblog
        long lastExpiration = 0;
        Date lastExpirationDate =
                (Date) CacheManager.getLastExpiredDate(feedRequest.getWeblogHandle());
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

        
        // looks like we need to render content
        try {
            // get update time before loading context
            // TODO: this should really be handled elsewhere
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            String catname = request.getParameter(RollerRequest.WEBLOGCATEGORYNAME_KEY);
            Date updateTime = wmgr.getWeblogLastPublishTime(rreq.getWebsite(), catname);
            request.setAttribute("updateTime", updateTime);
            
            // populate the model
            
            // TODO: remove this for Roller 3.0
            ModelLoader.loadOldModels(response, request, model);

            // Feeds get the weblog specific page model
            String modelsString = RollerConfig.getProperty("rendering.weblogPageModels");

            // Unless the weblog is the frontpage weblog w/aggregated feeds
            String frontPageHandle = 
                RollerConfig.getProperty("velocity.pagemodel.classname");
            boolean frontPageAggregated = 
                RollerConfig.getBooleanProperty("frontpage.weblog.aggregatedFeeds");
            if (weblog.getHandle().equals(frontPageHandle) && frontPageAggregated) {
                modelsString = RollerConfig.getProperty("rendering.weblogPageModels");
            }
            ModelLoader.loadConfiguredPageModels(modelsString, request, model);
            ModelLoader.loadUtilityObjects(model);

            // Feeds get weblog's additional custom models too
            if (weblog != null) {
                ModelLoader.loadAdditionalPageModels(weblog, request, model);
            }   
            
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
            renderer = RendererManager.getRenderer("velocity", pageId);
        } catch(Exception e) {
            // nobody wants to render my content :(
            log.error("Couldn't find renderer for page "+pageId, e);
            
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
            log.error("Error during rendering for page "+pageId, e);
            
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
        log.debug("PUT "+cacheKey);
        this.contentCache.put(cacheKey, new LazyExpiringCacheEntry(rendererOutput));
        
        log.debug("Exiting");
    }

    
    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     *
     * <context>[/handle]/<flavor>[/category]/<language>[/excerpts]
     *
     * examples ...
     *
     * main/rss/en
     * weblog/foo/rss/MyCategory/en
     * weblog/foo/atom/en/excerpts
     *
     */
    private String generateKey(WeblogFeedRequest feedRequest) {
        
        StringBuffer key = new StringBuffer();
        key.append(feedRequest.getContext());
        
        if(feedRequest.getContext().equals("weblog")) {
            key.append("/").append(feedRequest.getWeblogHandle().toLowerCase());
            key.append("/").append(feedRequest.getFlavor());
            
            if(feedRequest.getWeblogCategory() != null) {
                String cat = feedRequest.getWeblogCategory();
                if(cat.startsWith("/"))
                    cat = cat.substring(1).replaceAll("/","_");
                
                // categories may contain spaces, which is not desired
                key.append("/").append(org.apache.commons.lang.StringUtils.deleteWhitespace(cat));
            }
        } else {
            key.append("/").append(feedRequest.getFlavor());
        }
        
        // add language
        key.append("/").append(feedRequest.getLanguage());
        
        if(feedRequest.isExcerpts()) {
            key.append("/excerpts");
        }
        
        return key.toString();
    }
    
    
    /**
     * A weblog entry has changed.
     */
    public void invalidate(WeblogEntryData entry) {
        // ignored
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(WebsiteData website) {
        // ignored
    }
    
    
    /**
     * A bookmark has changed.
     */
    public void invalidate(BookmarkData bookmark) {
        // ignored
    }
    
    
    /**
     * A folder has changed.
     */
    public void invalidate(FolderData folder) {
        // ignored
    }
    
    
    /**
     * A comment has changed.
     */
    public void invalidate(CommentData comment) {
        // ignored
    }
    
    
    /**
     * A referer has changed.
     */
    public void invalidate(RefererData referer) {
        // ignored
    }
    
    
    /**
     * A user profile has changed.
     */
    public void invalidate(UserData user) {
        // ignored
    }
    
    
    /**
     * A category has changed.
     */
    public void invalidate(WeblogCategoryData category) {
        // ignored
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
    }
    
    
    /**
     * A weblog template has changed.
     */
    public void invalidate(WeblogTemplate template) {
        // ignored
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("cacheType", this.contentCache.getClass().getName());
        stats.put("startTime", this.startTime);
        stats.put("hits", new Double(this.hits));
        stats.put("misses", new Double(this.misses));
        stats.put("purges", new Double(this.purges));
        
        // calculate efficiency
        if((misses - purges) > 0) {
            double efficiency = hits / (misses + hits);
            stats.put("efficiency", new Double(efficiency * 100));
        }
        
        return stats;
    }
    
}
