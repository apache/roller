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
/*
 * MainPageCacheFilter.java
 *
 * Created on November 7, 2005, 2:32 PM
 */
package org.apache.roller.ui.rendering.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
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
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.LanguageUtil;
import org.apache.roller.util.cache.Cache;
import org.apache.roller.util.cache.CacheHandler;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.ui.core.util.CacheHttpServletResponseWrapper;
import org.apache.roller.ui.core.util.ResponseContent;


/**
 * A filter used for caching the fully rendered pages ... /main.do, /planet.do
 *
 * @web.filter name="MainPageCacheFilter"
 *
 * @author  Allen Gilliland
 */
public class MainPageCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = LogFactory.getLog(MainPageCacheFilter.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.mainpage";
    
    private boolean excludeOwnerPages = false;
    private Cache mCache = null;
    
    // for metrics
    private double hits = 0;
    private double misses = 0;
    private double skips = 0;
    private Date startTime = new Date();
    
    
    /**
     * Process filter.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        mLogger.debug("entering");
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String key = null;
        Principal prince = null;
        
        String servlet = request.getServletPath();
        if(servlet.equals("/main.do")) {
            key = "main/page";
        } else {
            // not a main page request
            mLogger.warn("not a main page "+servlet);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        try {
            // determine language
            Locale locale = LanguageUtil.getViewLocale(request);
            key += "/" + locale.getLanguage();
            
            // login status
            prince = request.getUserPrincipal();
            if(prince != null) {
                key += "/user=" + prince.getName();
            }
            
        } catch(Exception e) {
            // problem getting language?
            mLogger.error("problem parsing request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        
        try {
            ResponseContent respContent = null;
            if(!this.excludeOwnerPages || prince == null) {
                respContent = (ResponseContent) this.mCache.get(key);
            }
            
            if(respContent == null) {

                mLogger.debug("MISS "+key);
                this.misses++;
                
                CacheHttpServletResponseWrapper cacheResponse =
                        new CacheHttpServletResponseWrapper(response);
                
                chain.doFilter(request, cacheResponse);
                
                cacheResponse.flushBuffer();
                
                // only cache if we didn't get an exception
                if (request.getAttribute("DisplayException") == null) {
                    ResponseContent rc = cacheResponse.getContent();
                    
                    // only cache if this is not a logged in user?
                    if(!this.excludeOwnerPages || prince == null) {
                        this.mCache.put(key, rc);
                    } else {
                        mLogger.debug("SKIPPED "+key);
                        this.skips++;
                    }
                } else {
                    // it is expected that whoever caught this display exception
                    // is the one who reported it to the logs
                    mLogger.debug("Display exception "+key);
                }
                
            } else {
                
                mLogger.debug("HIT "+key);
                this.hits++;
                
                respContent.writeTo(response);
            }
            
        } catch(Exception ex) {
            
            if(ex.getMessage().indexOf("ClientAbort") != -1) {
                // ClientAbortException ... ignored
                mLogger.debug(ex.getMessage());
                
            } else if(ex.getMessage().indexOf("SocketException") != -1) {
                // SocketException ... ignored
                mLogger.debug(ex.getMessage());
                
            } else {
                mLogger.error("Unexpected exception rendering page "+key, ex);
            }
            
            // gotta send something to the client
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        mLogger.debug("exiting");
    }
    
    
    /**
     * A weblog entry has changed.
     */
    public void invalidate(WeblogEntryData entry) {
        this.mCache.clear();
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(WebsiteData website) {
        this.mCache.clear();
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
     * A weblog template has changed.
     */
    public void invalidate(WeblogTemplate template) {
        // ignored
    }
    
    
    /**
     * Clear the entire cache.
     */
    public void clear() {
        mLogger.info("Clearing cache");
        this.mCache.clear();
        this.startTime = new Date();
        this.hits = 0;
        this.misses = 0;
        this.skips = 0;
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("cacheType", this.mCache.getClass().getName());
        stats.put("startTime", this.startTime);
        stats.put("hits", new Double(this.hits));
        stats.put("misses", new Double(this.misses));
        stats.put("skips", new Double(this.skips));
        
        // calculate efficiency
        if(misses > 0) {
            double efficiency = hits / (misses + hits);
            stats.put("efficiency", new Double(efficiency * 100));
        }
        
        return stats;
    }
    
    
    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }
    
    
    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        
        mLogger.info("Initializing main page cache");

        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty("cache.mainpage.excludeOwnerEditPages");
        
        Map cacheProps = new HashMap();
        cacheProps.put("id", CACHE_ID);
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
        
        mLogger.info(cacheProps);
        
        mCache = CacheManager.constructCache(this, cacheProps);
    }
    
}
