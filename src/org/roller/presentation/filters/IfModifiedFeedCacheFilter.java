/*
 * IfModifiedFeedCacheFilter.java
 *
 * Created on November 9, 2005, 2:47 PM
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.text.SimpleDateFormat;
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


/**
 * A filter used for caching last modified dates.
 *
 * This may be applied to /rss/*, /atom/*, /flavor/*, and /planetrss
 * 
 * @web.filter name="IfModifiedFeedCacheFilter"
 *
 * @author Allen Gilliland
 */
public class IfModifiedFeedCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = 
            LogFactory.getLog(IfModifiedFeedCacheFilter.class);
    
    private Cache mCache = null;
    
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
        
        String key = "ifmod:"+this.generateKey(feedRequest);
        
        Date updateTime = null;
        try {
            updateTime = (Date) this.mCache.get(key);
            
            if (updateTime == null) {
                mLogger.debug("MISS "+key);
                
                if(feedRequest.getWeblogHandle() != null) {
                    Roller roller = RollerFactory.getRoller();
                    UserManager umgr = roller.getUserManager();
                    WeblogManager wmgr = roller.getWeblogManager();
                    
                    updateTime = wmgr.getWeblogLastPublishTime(
                            umgr.getWebsiteByHandle(feedRequest.getWeblogHandle()),
                            feedRequest.getWeblogCategory());
                    
                    this.mCache.put(key, updateTime);
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
    public void invalidate(WebsiteData website) {
        
        mLogger.debug("invalidating website = "+website.getHandle());
        
        // we need to remove the following cached items if they exist
        //   - the main feed
        //   - the planet feed
        //   - all weblog feeds
        
        Set removeSet = new HashSet();
        
        // TODO: it would be nice to be able to do this without iterating 
        //       over the entire cache key set
        String key = null;
        Iterator allKeys = this.mCache.keySet().iterator();
        while(allKeys.hasNext()) {
            key = (String) allKeys.next();
            
            if(key.startsWith("ifmod:main")) {
                removeSet.add(key);
            } else if(key.startsWith("ifmod:planet")) {
                removeSet.add(key);
            } else if(key.startsWith("ifmod:weblog/"+website.getHandle())) {
                removeSet.add(key);
            }
        }
        
        this.mCache.remove(removeSet);
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
        
        mLogger.info("Initializing if-modified cache");
        
        String factory = RollerConfig.getProperty("cache.feed.ifmodified.factory");
        String size = RollerConfig.getProperty("cache.feed.ifmodified.size");
        String timeout = RollerConfig.getProperty("cache.feed.ifmodified.timeout");
        
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
            mLogger.warn("Invalid cache timeout ["+timeout+"], using default");
        }
        
        
        Map props = new HashMap();
        props.put("timeout", ""+cacheTimeout);
        props.put("size", ""+cacheSize);
        
        if(factory != null && factory.trim().length() > 0)
            props.put("cache.factory", factory);
        
        mLogger.info(props);
        
        mCache = CacheManager.constructCache(this, props);
    }
    
}
