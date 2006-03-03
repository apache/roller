/*
 * IfModifiedFeedCacheFilter.java
 *
 * Created on November 9, 2005, 2:47 PM
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.text.SimpleDateFormat;
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
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
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


/**
 * A filter used for caching last modified dates.
 *
 * This may be applied to /rss/*, /atom/*, /flavor/*
 * 
 * @web.filter name="IfModifiedFeedCacheFilter"
 *
 * @author Allen Gilliland
 */
public class IfModifiedFeedCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = 
            LogFactory.getLog(IfModifiedFeedCacheFilter.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.ifmodified.feed";
    
    // the cache of last updated times
    private Cache mCache = null;
    
    // the last time we expired our main feeds
    private Date mainLastExpiredDate = new Date();
    
    SimpleDateFormat dateFormatter =
            new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
    
    
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
            mLogger.error("error creating feed request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String key = this.CACHE_ID+":"+this.generateKey(feedRequest);
        
        Date updateTime = null;
        try {
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
                updateTime = (Date) entry.getValue(lastExpiration);
                
                if(updateTime == null)
                    mLogger.debug("HIT-INVALID "+key);
            }
            
            if (updateTime == null) {
                mLogger.debug("MISS "+key);
                
                if(feedRequest.getWeblogHandle() != null) {
                    Roller roller = RollerFactory.getRoller();
                    UserManager umgr = roller.getUserManager();
                    WeblogManager wmgr = roller.getWeblogManager();
                    
                    updateTime = wmgr.getWeblogLastPublishTime(
                            umgr.getWebsiteByHandle(feedRequest.getWeblogHandle()),
                            feedRequest.getWeblogCategory());
                    
                    this.mCache.put(key, new LazyExpiringCacheEntry(updateTime));
                    
                } else {
                    this.mCache.put(key, new LazyExpiringCacheEntry(new Date()));
                }
                
            } else {
                mLogger.debug("HIT "+key);
            }
            
            // Check the incoming if-modified-since header
            Date sinceDate =
                    new Date(request.getDateHeader("If-Modified-Since"));
            
            if (updateTime != null) {
                // convert date (JDK 1.5 workaround)
                String date = dateFormatter.format(updateTime);
                updateTime = new Date(date);
                if (updateTime.compareTo(sinceDate) <= 0) {
                    mLogger.debug("NOT_MODIFIED "+key);
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }
            
        } catch (RollerException e) {
            // Thrown by getLastPublishedDate if there is a db-type error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (IllegalArgumentException e) {
            // Thrown by getDateHeader if not in valid format. This can be
            // safely ignored, the only consequence is that the NOT MODIFIED
            // response is not set.
        }
        
        // Set outgoing last modified header
        if (updateTime != null) {
            response.setDateHeader("Last-Modified", updateTime.getTime());
        }
        
        chain.doFilter(request, response);
        
        mLogger.debug("exiting");
    }
    
    
    /**
     * Generate a cache key from a parsed weblog feed request.
     * This generates a key of the form ...
     *
     * <context>/[handle]/<flavor>/[category]/<language>/[excerpts]
     *
     * examples ...
     *
     * main/rss/en
     * planet/rss/en
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
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        
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
        
        mLogger.info("Initializing if-modified feed cache");
        
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
