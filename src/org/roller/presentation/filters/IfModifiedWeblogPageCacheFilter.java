/*
 * IfModifiedWeblogPageCacheFilter.java
 *
 * Created on November 9, 2005, 9:02 PM
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
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
import org.roller.presentation.WeblogPageRequest;
import org.roller.presentation.cache.Cache;
import org.roller.presentation.cache.CacheHandler;
import org.roller.presentation.cache.CacheManager;
import org.roller.presentation.cache.LazyExpiringCacheEntry;


/**
 * A filter used for caching last modified dates.
 * 
 * @web.filter name="IfModifiedWeblogPageCacheFilter"
 *
 * @author Allen Gilliland
 */
public class IfModifiedWeblogPageCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = 
            LogFactory.getLog(IfModifiedWeblogPageCacheFilter.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.ifmodified.page";
    
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
        
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
        } catch(Exception e) {
            mLogger.error("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String key = this.CACHE_ID+":"+this.generateKey(pageRequest);
        
        Date updateTime = null;
        try {
            // we need the last expiration time for the given weblog
            long lastExpiration = 0;
            Date lastExpirationDate =
                    (Date) CacheManager.getLastExpiredDate(pageRequest.getWeblogHandle());
            if(lastExpirationDate != null)
                lastExpiration = lastExpirationDate.getTime();
            
            LazyExpiringCacheEntry entry =
                    (LazyExpiringCacheEntry) this.mCache.get(key);
            if(entry != null) {
                updateTime = (Date) entry.getValue(lastExpiration);
                
                if(updateTime == null)
                    mLogger.debug("HIT-INVALID "+key);
            }
            
            if (updateTime == null) {
                mLogger.debug("MISS "+key);
                
                if(pageRequest.getWeblogHandle() != null) {
                    // just set updateTime to now
                    updateTime = new Date();
                    this.mCache.put(key, new LazyExpiringCacheEntry(updateTime));
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
     * Generate a cache key from a parsed weblog page request.
     * This generates a key of the form ...
     *
     * weblog/<handle>/page/<weblogPage>/[anchor]/<language>/[user]
     *   or
     * weblog/<handle>/page/<weblogPage>/[date]/[category]/<language>/[user]
     *
     *
     * examples ...
     *
     * weblog/foo/page/Weblog/en
     * weblog/foo/page/Weblog/entry_anchor/en
     * weblog/foo/page/Weblog/20051110/en
     * weblog/foo/page/Weblog/MyCategory/en
     *
     */
    private String generateKey(WeblogPageRequest pageRequest) {
        
        StringBuffer key = new StringBuffer();
        key.append("weblog/");
        key.append(pageRequest.getWeblogHandle());
        key.append("/page/");
        key.append(pageRequest.getWeblogPage());
        
        if(pageRequest.getWeblogAnchor() != null) {
            // TODO: do we need to convert to base64 here?
            // this.weblogAnchor = Utilities.toBase64(this.weblogAnchor.getBytes());
            key.append("/").append(pageRequest.getWeblogAnchor());
        } else {
            if(pageRequest.getWeblogDate() != null) {
                key.append("/").append(pageRequest.getWeblogDate());
            }
            
            if(pageRequest.getWeblogCategory() != null) {
                String cat = pageRequest.getWeblogCategory();
                if(cat.startsWith("/")) {
                    cat = cat.substring(1);
                }

                key.append("/").append(cat);
            }
        }
        
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
        
        mLogger.debug("invalidating entry = "+entry.getAnchor());
        
        // we need to remove the following cached items if they exist
        //   - the weblog main page
        //   - the weblog entry permalink page
        //   - the weblog entry category page
        //   - the weblog entry date archive pages
        
        /*
        Set removeSet = new HashSet();
        
        // TODO: it would be nice to be able to do this without iterating
        //       over the entire cache key set
        String key = null;
        Iterator allKeys = this.mCache.keySet().iterator();
        while(allKeys.hasNext()) {
            key = (String) allKeys.next();
            if(key.startsWith("ifmod:weblog/"+entry.getWebsite().getHandle())) {
                removeSet.add(key);
            }
        }
        
        this.mCache.remove(removeSet);
        */
    }
    
    
    /**
     * A weblog has changed.
     */
    public void invalidate(WebsiteData website) {
        
        mLogger.debug("invalidating website = "+website.getHandle());
        
        // we need to remove the following cached items if they exist
        //   - all pages for this weblog
        
        /*
        Set removeSet = new HashSet();
        
        // TODO: it would be nice to be able to do this without iterating
        //       over the entire cache key set
        String key = null;
        Iterator allKeys = this.mCache.keySet().iterator();
        while(allKeys.hasNext()) {
            key = (String) allKeys.next();
            
            if(key.startsWith("ifmod:weblog/"+website.getHandle())) {
                removeSet.add(key);
            }
        }
        
        this.mCache.remove(removeSet);
        */
    }
    
    
    /**
     * A bookmark has changed.
     */
    public void invalidate(BookmarkData bookmark) {
        this.invalidate(bookmark.getWebsite());
    }
    
    
    /**
     * A folder has changed.
     */
    public void invalidate(FolderData folder) {
        this.invalidate(folder.getWebsite());
    }
    
    
    /**
     * A comment has changed.
     */
    public void invalidate(CommentData comment) {
        this.invalidate(comment.getWeblogEntry());
    }
    
    
    /**
     * A referer has changed.
     */
    public void invalidate(RefererData referer) {
        // ignored
        // TODO: we probably should invalidate the entire website?
    }
    
    
    /**
     * A user profile has changed.
     */
    public void invalidate(UserData user) {
        // ignored
        // TODO: i don't think weblog pages currently have user info, but this may change
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
        this.invalidate(template.getWebsite());
    }
    
    
    public void clear() {
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
        
        mLogger.info("Initializing if-modified cache");
        
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
