package org.roller.presentation.pagecache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.pojos.UserData;
import org.roller.presentation.filters.IfModifiedFilter;
import org.roller.presentation.pagecache.rollercache.LRUCacheHandler2;


/////////////////////////////////////////////////////////////////////////////
/**
 * Roller's in memory page cache. See Javadoc for LRUCacheHandler for more 
 * information on configuring this cache. 
 * 
 * @web.filter name="PageCacheFilter"
 * 
 * @web.filter-init-param name="size" value="100"
 *     description="Number of pages to keep in cache"
 * 
 * @web.filter-init-param name="timeout" value="1800"
 *     description="Page Cache timeout interval in seconds"
 * 
 * @author Lance Lavandowska
 * @author David M Johnson
 */
public class PageCacheFilter implements Filter
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(PageCacheFilter.class);
        
    private FilterHandler mHandler = null;
    
    private static ArrayList mHandlers = new ArrayList();

    //-----------------------------------------------------------------------
    /**
     * Initialize the filter.
     * @param filerConfig The filter configuration
     */
    public void init(FilterConfig filterConfig)
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug(
                "Initializing as filterName: "+filterConfig.getFilterName());
        }        
        mHandler = new LRUCacheHandler2(filterConfig);
        mHandlers.add(mHandler);
    }

    //-----------------------------------------------------------------------
    /**
     * Filter clean-up
     */
    public void destroy()    
    {
        mHandler.destroy();
    }

    //-----------------------------------------------------------------------
    /**
     * Process the doFilter
     * @param request The servlet request
     * @param response The servlet response
     * @param chain The filet chain
     * @throws ServletException IOException
     */
    public void doFilter(ServletRequest request,
        ServletResponse response, FilterChain chain)
        throws ServletException, IOException
    {
        mHandler.doFilter(request, response, chain);
    }

    //-----------------------------------------------------------------------
    /** Flush cache for all handlers of this class */
    public static void flushCache(HttpServletRequest req)
    {
        Iterator iter = mHandlers.iterator();
        while (iter.hasNext())
        {
            FilterHandler handler = (FilterHandler)iter.next();
            handler.flushCache(req);            
        }
        IfModifiedFilter.purgeDateCache(null);
    }

    //-----------------------------------------------------------------------
    /** Remove from cache for all handlers of this class */
    public static void removeFromCache(HttpServletRequest req, UserData user)
    {
        Iterator iter = mHandlers.iterator();
        while (iter.hasNext())
        {
            FilterHandler handler = (FilterHandler)iter.next();
            handler.removeFromCache(req, user);
        }       
        IfModifiedFilter.purgeDateCache(user);
    }
}
