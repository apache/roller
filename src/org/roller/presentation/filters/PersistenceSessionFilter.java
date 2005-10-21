package org.roller.presentation.filters;

import java.io.IOException;

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
import org.roller.model.Roller;
import org.roller.presentation.RollerContext;

/**
 * Sole responsibility is to ensure that each request's Roller 
 * persistence session is released at end of the request.
 * @web.filter name="PersistenceSessionFilter"
 * @author David M. Johnson
 */
public class PersistenceSessionFilter implements Filter
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(RequestFilter.class);

    /**
     * destroy
     */
    public void destroy()
    {
    }

    /**
     * Release Roller persistence session at end of request processing.
     */
    public void doFilter(
        ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Entered PersistenceSessionFilter");
        }
        Roller roller = RollerContext.getRoller((HttpServletRequest)request);
        try 
        {
            chain.doFilter(request, response);
        }
        finally 
        {
            roller.release();
        }
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Exiting PersistenceSessionFilter");
        }
    }

    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }
}

