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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.rendering.util.WeblogFeedRequest;
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
 * @web.servlet-mapping url-pattern="/roller-ui/rendering/feed/*"
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
        
        log.info("Initializing FeedServlet");
        
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
        
        log.info("Feed cache = "+cacheProps);
        
        contentCache = CacheManager.constructCache(this, cacheProps);
    }
    
    
    /**
     * Handle GET requests for weblog feeds.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        log.debug("Entering");
        
        WebsiteData weblog = null;
        
        WeblogFeedRequest feedRequest = null;
        try {
            // parse the incoming request and extract the relevant data
            feedRequest = new WeblogFeedRequest(request);
            
            weblog = feedRequest.getWeblog();
            if(weblog == null) {
                throw new RollerException("unable to lookup weblog: "+
                        feedRequest.getWeblogHandle());
            }
        } catch(Exception e) {
            // invalid feed request format or weblog doesn't exist
            log.debug("error creating weblog feed request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
        
        // set last-modified date
        response.setDateHeader("Last-Modified", weblog.getLastModified().getTime());
        
        // cached content checking
        String cacheKey = this.CACHE_ID+":"+this.generateKey(feedRequest);
        LazyExpiringCacheEntry entry =
                (LazyExpiringCacheEntry) this.contentCache.get(cacheKey);
        if(entry != null) {
            CachedContent cachedContent = 
                    (CachedContent) entry.getValue(weblog.getLastModified().getTime());
            
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

        // set content type
        if("rss".equals(feedRequest.getFormat())) {
            response.setContentType("application/rss+xml; charset=utf-8");
        } else if("atom".equals(feedRequest.getFormat())) {
            response.setContentType("application/atom+xml; charset=utf-8");
        }
        
        // looks like we need to render content
        HashMap model = new HashMap();
        String pageId = null;
        try {
            // determine what template to render with
            if (RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                pageId = "templates/feeds/site-"+feedRequest.getType()+"-"+feedRequest.getFormat()+".vm";
            } else {
                pageId = "templates/feeds/weblog-"+feedRequest.getType()+"-"+feedRequest.getFormat()+".vm";
            }
            
            // populate the rendering model
            Map initData = new HashMap();
            initData.put("request", request);
            initData.put("feedRequest", feedRequest);
            
            // Feeds get the weblog specific page model
            ModelLoader.loadFeedModels(model, initData);
            
            // special handling for site wide feed
            if (RollerRuntimeConfig.isSiteWideWeblog(weblog.getHandle())) {
                ModelLoader.loadSiteModels(model, initData);
            }
            
            // utility helpers and plugin helpers
            ModelLoader.loadUtilityHelpers(model, initData);

            // Feeds get weblog's custom models too
            ModelLoader.loadCustomModels(weblog, model, initData);
            
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
        
        key.append("weblog");
        key.append("/").append(feedRequest.getWeblogHandle().toLowerCase());
        key.append("/").append(feedRequest.getType());
        key.append("/").append(feedRequest.getFormat());
        
        if(feedRequest.getWeblogCategoryName() != null) {
            String cat = feedRequest.getWeblogCategoryName();
            try {
                cat = URLEncoder.encode(cat, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // should never happen, utf-8 is always supported
            }
            
            key.append("/").append(cat);
        }
        
        // add locale
        key.append("/").append(feedRequest.getLocale());
        
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
