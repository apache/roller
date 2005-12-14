/*
 * RssCacheFilter.java
 *
 * Created on November 5, 2005, 6:32 PM
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.util.Date;
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
    
    private Cache mFeedCache = null;
    
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
        
        String key = "feedCache:"+this.generateKey(feedRequest);
        
        try {
            ResponseContent respContent = (ResponseContent) this.mFeedCache.get(key);
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
                    
                    this.mFeedCache.put(key, rc);
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
            key.append("/").append(feedRequest.getWeblogHandle());
            key.append("/").append(feedRequest.getFlavor());
            
            if(feedRequest.getWeblogCategory() != null) {
                String cat = feedRequest.getWeblogCategory();
                if(cat.startsWith("/"))
                    cat = cat.substring(1).replaceAll("/","_");
                
                key.append("/").append(cat);
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
        
        // we need to remove the following cached items if they exist
        //   - the main feed
        //   - the planet feed
        //   - all weblog feeds
        
        Set removeSet = new HashSet();
        
        // TODO: it would be nice to be able to do this without iterating 
        //       over the entire cache key set
        String key = null;
        
        synchronized(mFeedCache) {
            Iterator allKeys = this.mFeedCache.keySet().iterator();
            while(allKeys.hasNext()) {
                key = (String) allKeys.next();
                
                if(key.startsWith("feedCache:main")) {
                    removeSet.add(key);
                } else if(key.startsWith("feedCache:planet")) {
                    removeSet.add(key);
                } else if(key.startsWith("feedCache:weblog/"+website.getHandle())) {
                    removeSet.add(key);
                }
            }
        }
        
        this.mFeedCache.remove(removeSet);
        this.purges += removeSet.size();
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
        this.mFeedCache.clear();
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
        stats.put("cacheType", this.mFeedCache.getClass().getName());
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
        
        String factory = RollerConfig.getProperty("cache.feed.factory");
        String size = RollerConfig.getProperty("cache.feed.size");
        String timeout = RollerConfig.getProperty("cache.feed.timeout");
        
        int cacheSize = 100;
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
        
        mFeedCache = CacheManager.constructCache(this, props);
    }
    
}
