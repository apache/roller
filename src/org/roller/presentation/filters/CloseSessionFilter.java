package org.roller.presentation.filters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.presentation.RequestUtil;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

/**
 * Sole responsibility is to ensure that each request's Roller 
 * persistence session is released at end of the request.
 * 
 * @web.filter name="CloseSessionFilter"
 * 
 * @author David M. Johnson
 */
public class CloseSessionFilter implements Filter
{
    private FilterConfig mFilterConfig = null;
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
        ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        try 
        {
            chain.doFilter(request, response);
        }
        finally 
        {
            Roller roller = RollerContext.getRoller((HttpServletRequest)request);
            roller.release();
        }
    }

    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        mFilterConfig = filterConfig;
    }
}

