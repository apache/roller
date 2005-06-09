package org.roller.presentation.filters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.roller.model.Roller;
import org.roller.presentation.RollerRequest;

/**
 * Entry point filter for Newsfeed Servlets, this filter 
 * Handles If-Modified-Since header using per-user and per-category
 * last weblog pub time. Returns 304 if requested weblog has not been
 * modified since. Also, sets Last-Modified on outgoing response.
 *
 * @web.filter name="IfPlanetModifiedFilter"
 * @web.filter-mapping url-pattern="/planetrss/*"
 * 
 * @author David M Johnson
 */
public class IfPlanetModifiedFilter implements Filter
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(IfPlanetModifiedFilter.class);

    SimpleDateFormat dateFormatter = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");

    public IfPlanetModifiedFilter()
    {
        super();
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
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
                 synchronized (dateFormatter)
                 {
                     String date = dateFormatter.format(updateTime);
                     updateTime = new Date(date);
                 }
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
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        Roller roller = rreq.getRoller();
        Date lastUpdated = roller.getPlanetManager().getLastUpdated();
        if (lastUpdated == null)
        {
            lastUpdated = new Date();
            mLogger.warn("Can't get lastUpdate time, using current time instead");
        }
        return lastUpdated;
	}

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
    }
}
