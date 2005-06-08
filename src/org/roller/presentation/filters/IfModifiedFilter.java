/*
 * Created on Apr 19, 2003
 */
package org.roller.presentation.filters;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.util.LRUCache;
import org.roller.util.LRUCache2;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Entry point filter for Newsfeed Servlets, this filter 
 * Handles If-Modified-Since header using per-user and per-category
 * last weblog pub time. Returns 304 if requested weblog has not been
 * modified since. Also, sets Last-Modified on outgoing response.
 *
 * @web.filter name="IfModifiedFilter"
 *
 * @author David M Johnson
 */
public class IfModifiedFilter implements Filter
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(IfModifiedFilter.class);

    private ServletContext mContext;
    private FilterConfig mConfig;

    // TODO: make cache configurable
    private static LRUCache2 mDateCache = new LRUCache2(300, 20000);
    SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");

    public IfModifiedFilter()
    {
        super();
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // This method is not multithreaded, so we dont need to sync
        mContext = filterConfig.getServletContext();
    }

    /**
     * @see javax.servlet.Filter#doFilter(
     * javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    public void doFilter(
        ServletRequest req,
        ServletResponse res,
        FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        Roller roller = RollerContext.getRoller( request );

        Date updateTime = null;
        try
        {
            updateTime = getLastPublishedDate(request);

            // RSS context loader needs updateTime, so stash it
            request.setAttribute("updateTime", updateTime);

            // Check the incoming if-modified-since header
            Date sinceDate =
                new Date(request.getDateHeader("If-Modified-Since"));

            if (updateTime != null)
            {
                 // convert date (JDK 1.5 workaround)
                 String date = dateFormatter.format(updateTime);
                 updateTime = new Date(date);
                 if (updateTime.compareTo(sinceDate) <= 0)
                 {
                     response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                     return;
                 }
            }
            mLogger.debug("Not returning 304 for: "+request.getRequestURI());
        }
        catch (RollerException e)
        {
            // Thrown by getLastPublishedDate if there is a db-type error
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        catch (IllegalArgumentException e)
        {
            // Thrown by getDateHeader if not in valid format. This can be
            // safely ignored, the only consequence is that the NOT MODIFIED
            // response is not set.
        }

        // Set outgoing last modified header
        if (updateTime != null)
        {
            response.setDateHeader("Last-Modified", updateTime.getTime());
        }

        chain.doFilter(request, response);
    }

    public static Date getLastPublishedDate(HttpServletRequest request)
	    throws RollerException
	{
	    // Get user name without using heavy RollerRequest URL parser
	    String userName = null;
	    String pathInfo = request.getPathInfo();
	    pathInfo = pathInfo != null ? pathInfo : "";
	    String[] pathInfoArray = StringUtils.split(pathInfo, "/");
	    if (pathInfoArray.length == 1)
	    {
	        userName = pathInfoArray[0];
	    }
	    else if (pathInfoArray.length > 1) 
	    {
	        // request is for a specific date or anchor, can't return 304
	        return null;
	    }
	
	    // Get last pub time for specific weblog category requested
	    String catname =
	        request.getParameter(RollerRequest.WEBLOGCATEGORYNAME_KEY);
	
	    // update times are cached to reduce database queries per request
	    StringBuffer sb = new StringBuffer();
        sb.append("zzz_");
	    sb.append(userName);
        sb.append("_zzz_");
	    sb.append(catname);
	    String key = sb.toString();
	
	    Date updateTime = (Date)mDateCache.get(key);
	    if (updateTime == null)
	    {
	        mLogger.debug("Hitting database for update time: "+key);
	        Roller roller = RollerContext.getRoller(request);
	        roller.begin();
	        WeblogManager wmgr = roller.getWeblogManager();
	        updateTime = wmgr.getWeblogLastPublishTime(userName, catname);	
	        mDateCache.put(key, updateTime);
	    }
	    return updateTime;
	}
    
    public static void purgeDateCache(UserData user) 
    {
        String userName = (user != null) ? user.getUserName() : null;
        StringBuffer sb = new StringBuffer();
        sb.append("zzz_");
        sb.append(userName);
        sb.append("_zzz_");
        mDateCache.purge(new String[] {sb.toString()});
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
    }
}
