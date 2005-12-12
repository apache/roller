/*
 * MainPageCacheFilter.java
 *
 * Created on November 7, 2005, 2:32 PM
 */
package org.roller.presentation.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
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
import org.roller.config.RollerConfig;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.CommentData;
import org.roller.pojos.FolderData;
import org.roller.pojos.RefererData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.LanguageUtil;
import org.roller.presentation.cache.Cache;
import org.roller.presentation.cache.CacheHandler;
import org.roller.presentation.cache.CacheManager;
import org.roller.presentation.util.CacheHttpServletResponseWrapper;
import org.roller.presentation.util.ResponseContent;


/**
 * A filter used for caching the fully rendered pages ... /main.do, /planet.do
 *
 * @web.filter name="MainPageCacheFilter"
 *
 * @author  Allen Gilliland
 */
public class MainPageCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = LogFactory.getLog(MainPageCacheFilter.class);
    
    private boolean excludeOwnerPages = false;
    private Cache mPageCache = null;
    
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
                respContent = (ResponseContent) this.mPageCache.get(key);
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
                        this.mPageCache.put(key, rc);
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
        this.mPageCache.clear();
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(WebsiteData website) {
        this.mPageCache.clear();
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
        this.mPageCache.clear();
        this.startTime = new Date();
        this.hits = 0;
        this.misses = 0;
        this.skips = 0;
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("cacheType", this.mPageCache.getClass().getName());
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
        
        String factory = RollerConfig.getProperty("cache.mainpage.factory");
        String size = RollerConfig.getProperty("cache.mainpage.size");
        String timeout = RollerConfig.getProperty("cache.mainpage.timeout");
        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty("cache.mainpage.excludeOwnerEditPages");
        
        int cacheSize = 20;
        try {
            cacheSize = Integer.parseInt(size);
        } catch (Exception e) {
            mLogger.warn("Invalid cache size ["+size+"], using default");
        }
        
        long cacheTimeout = 30 * 60;
        try {
            cacheTimeout = Long.parseLong(timeout);
        } catch (Exception e) {
            mLogger.warn("Invalid cache timeout ["+timeout+
                    "], using default");
        }
        
        
        Map props = new HashMap();
        props.put("timeout", ""+cacheTimeout);
        props.put("size", ""+cacheSize);
        
        if(factory != null && factory.trim().length() > 0)
            props.put("cache.factory", factory);
        
        mLogger.info(props);
        
        mPageCache = CacheManager.constructCache(this, props);
    }
    
}
