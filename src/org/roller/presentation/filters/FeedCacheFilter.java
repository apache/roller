/*
 * RssCacheFilter.java
 *
 * Created on November 5, 2005, 6:32 PM
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
import org.roller.presentation.WeblogFeedRequest;
import org.roller.presentation.cache.Cache;
import org.roller.presentation.cache.CacheHandler;
import org.roller.presentation.cache.CacheManager;
import org.roller.presentation.cache.LazyExpiringCacheEntry;
import org.roller.presentation.util.CacheHttpServletResponseWrapper;
import org.roller.presentation.util.ResponseContent;


/**
 * A filter used for caching fully rendered xml feeds.
 *
 * This filter should only be applied to /rss/*, /atom/*, /flavor/*
 *
 * @web.filter name="FeedCacheFilter"
 *
 * @author  Allen Gilliland
 */
public class FeedCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = LogFactory.getLog(FeedCacheFilter.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.feed";
    
    // our cache of rendered feeds
    private Cache mCache = null;
    
    // the last time the main feeds were expired
    private Date mainLastExpiredDate = new Date();
    
    // for metrics
    private double hits = 0;
    private double misses = 0;
    private double purges = 0;
    private Date startTime = new Date();
    
    
    /**
     * Process filter.
     */
    public void doFilter(ServletRequest req,
                        ServletResponse res,
                        FilterChain chain)
            throws IOException, ServletException {
        
        mLogger.debug("entering");
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        WeblogFeedRequest feedRequest = null;
        try {
            feedRequest = new WeblogFeedRequest(request);
        } catch(Exception e) {
            // some kind of error parsing the request
            mLogger.error("error creating weblog feed request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String key = this.CACHE_ID+":"+this.generateKey(feedRequest);
        
        try {
            ResponseContent respContent = null;
            long lastExpiration = 0;
            
            // first, we need to determine the last time the specified feed was expired.
            // if this is a weblog specific feed then ask the CacheManager for
            // the last expired time of the weblog.  otherwise this is a main feed and we
            // keep that last expired time ourselves
            if(feedRequest.getWeblogHandle() != null) {
                Date lastExpirationDate =
                        (Date) CacheManager.getLastExpiredDate(feedRequest.getWeblogHandle());
                if(lastExpirationDate != null)
                    lastExpiration = lastExpirationDate.getTime();
            } else {
                lastExpiration = this.mainLastExpiredDate.getTime();
            }
            
            LazyExpiringCacheEntry entry =
                    (LazyExpiringCacheEntry) this.mCache.get(key);
            if(entry != null) {
                respContent = (ResponseContent) entry.getValue(lastExpiration);
                
                if(respContent == null)
                    mLogger.debug("HIT-INVALID "+key);
            }
                
            if (respContent == null) {
                
                mLogger.debug("MISS "+key);
                this.misses++;
                
                CacheHttpServletResponseWrapper cacheResponse =
                        new CacheHttpServletResponseWrapper(response);
                
                chain.doFilter(request, cacheResponse);
                
                cacheResponse.flushBuffer();
                
                // only cache if there wasn't an exception
                if (request.getAttribute("DisplayException") == null) {
                    ResponseContent rc = cacheResponse.getContent();
                    
                    this.mCache.put(key, new LazyExpiringCacheEntry(rc));
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
                mLogger.error("Unexpected exception rendering feed "+key, ex);
            }
            
            // gotta send something to the client
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        mLogger.debug("exiting");
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
        this.invalidate(entry.getWebsite());
    }
    
    
    /**
     * A weblog has changed.
     */
    public synchronized void invalidate(WebsiteData website) {
        
        mLogger.debug("invalidating website = "+website.getHandle());
        
        // update our main feed last expiration date
        synchronized(this) {
            this.mainLastExpiredDate = new Date();
        }
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
        this.invalidate(category.getWebsite());
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
        stats.put("cacheType", this.mCache.getClass().getName());
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
    
    
    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }
    
    
    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        
        mLogger.info("Initializing feed cache");
        
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
        
        mLogger.info(cacheProps);
        
        mCache = CacheManager.constructCache(this, cacheProps);
    }
    
}
