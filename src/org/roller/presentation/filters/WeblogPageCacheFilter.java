/*
 * WeblogPageCacheFilter.java
 *
 * Created on October 27, 2005, 12:19 PM
 */
package org.roller.presentation.filters;

import java.io.IOException;
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
import org.roller.presentation.util.CacheHttpServletResponseWrapper;
import org.roller.presentation.util.ResponseContent;
import org.roller.util.Utilities;


/**
 * A filter used for caching fully rendered weblog pages ... /page/*
 *
 * @web.filter name="WeblogPageCacheFilter"
 *
 * @author  Allen Gilliland
 */
public class WeblogPageCacheFilter implements Filter, CacheHandler {
    
    private static Log mLogger = LogFactory.getLog(WeblogPageCacheFilter.class);
    
    // a unique identifier for this cache, this is used as the prefix for
    // roller config properties that apply to this cache
    private static final String CACHE_ID = "cache.weblogpage";
    
    private boolean excludeOwnerPages = false;
    private Cache mCache = null;
    
    // for metrics
    private double hits = 0;
    private double misses = 0;
    private double purges = 0;
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
        
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
        } catch(Exception e) {
            // some kind of error parsing the request
            mLogger.error("error creating page request", e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        String key = this.CACHE_ID+":"+this.generateKey(pageRequest);
        
        try {
            ResponseContent respContent = null;
            if(!this.excludeOwnerPages || !pageRequest.isLoggedIn()) {
                // we need the last expiration time for the given weblog
                long lastExpiration = 0;
                Date lastExpirationDate = 
                        (Date) CacheManager.getLastExpiredDate(pageRequest.getWeblogHandle());
                if(lastExpirationDate != null)
                    lastExpiration = lastExpirationDate.getTime();
                
                LazyExpiringCacheEntry entry = 
                        (LazyExpiringCacheEntry) this.mCache.get(key);
                if(entry != null) {
                    respContent = (ResponseContent) entry.getValue(lastExpiration);
                    
                    if(respContent == null)
                        mLogger.debug("HIT-INVALID "+key);
                }
            }
            
            if (respContent == null) {
                
                mLogger.debug("MISS "+key);
                this.misses++;
                
                CacheHttpServletResponseWrapper cacheResponse =
                        new CacheHttpServletResponseWrapper(response);
                
                chain.doFilter(request, cacheResponse);
                
                cacheResponse.flushBuffer();
                
                // Store as the cache content the result of the response
                // if no exception was noted by content generator.
                if (request.getAttribute("DisplayException") == null) {
                    ResponseContent rc = cacheResponse.getContent();
                    
                    // only cache if this is not a logged in user?
                    if (!this.excludeOwnerPages || !pageRequest.isLoggedIn()) {
                        if (rc != null && rc.getSize() > 0) {
                            this.mCache.put(key, new LazyExpiringCacheEntry(rc));
                        } else {
                            mLogger.debug("Not caching zero length content for key: " + key);
                        }
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
        key.append(pageRequest.getWeblogHandle());
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
    public synchronized void invalidate(WeblogEntryData entry) {
        
        mLogger.debug("invalidating entry = "+entry.getAnchor());
        
        // we need to remove the following cached items if they exist
        //   - the weblog main page
        //   - the weblog entry permalink page
        //   - the weblog archive pages
        
        /*
        String handle = entry.getWebsite().getHandle();
        String keyBase = "pageCache:weblog/"+handle+"/page";
        
        Set removeSet = new HashSet();
        
        // TODO: it would be nice to be able to do this without iterating 
        //       over the entire cache key set
        String key = null;
        
        synchronized(mCache) {
            Iterator allKeys = this.mCache.keySet().iterator();
            while(allKeys.hasNext()) {
                key = (String) allKeys.next();
                if(key.startsWith(keyBase+"/main")) {
                    removeSet.add(key);
                } else if(key.startsWith(keyBase+"/archive")) {
                    // at some point it would be cool to actually calculate what
                    // archive pages to remove in specific, rather than all of 'em
                    removeSet.add(key);
                } else if(key.startsWith(keyBase+"/permalink/"+entry.getAnchor())) {
                    removeSet.add(key);
                }
            }
        }

        this.mCache.remove(removeSet);
        this.purges += removeSet.size();
        */
        
        this.invalidate(entry.getWebsite());
    }
    
    
    /**
     * A weblog has changed.
     */
    public synchronized void invalidate(WebsiteData website) {
        
        mLogger.debug("invalidating website = "+website.getHandle());
        
        /*
        // we need to remove the following cached items if they exist
        //   - all pages for this weblog
        
        Set removeSet = new HashSet();
        
        // TODO: it would be nice to be able to do this without iterating 
        //       over the entire cache key set
        String key = null;
        
        synchronized(mCache) {
            Iterator allKeys = this.mCache.keySet().iterator();
            while(allKeys.hasNext()) {
                key = (String) allKeys.next();
                
                if(key.startsWith("pageCache:weblog/"+website.getHandle())) {
                    removeSet.add(key);
                }
            }
        }
        
        this.mCache.remove(removeSet);
        this.purges += removeSet.size();
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
        // TODO: i don't think weblog pages currently reference user objects
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
        this.skips = 0;
    }
    
    
    public Map getStats() {
        
        Map stats = new HashMap();
        stats.put("cacheType", this.mCache.getClass().getName());
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
    
    
    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }
    
    
    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        
        mLogger.info("Initializing weblog page cache");

        this.excludeOwnerPages = 
                RollerConfig.getBooleanProperty("cache.weblogpage.excludeOwnerEditPages");
        
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
