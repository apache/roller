/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;

/**
 * Entry point filter for all requests.  This filter ensures that the request encoding is set to UTF-8 before any other
 * processing forces request parsing using a default encoding.  It also syncs up the Struts and JSTL locales.  This
 * filter should normally be first and last in the chain.
 *
 * @author Anil Gangolli
 * @web.filter name="CharEncodingFilter"
 */

public class CharEncodingFilter implements Filter
{
    private FilterConfig mFilterConfig = null;
    private static Log mLogger =
        LogFactory.getFactory().getInstance(CharEncodingFilter.class);

    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        mFilterConfig = filterConfig;
    }

    /**
     * destroy
     */
    public void destroy()
    {
    }

    /**
     * Set the character encoding and sync up Struts and JSTL locales.  This filter should normally be first (and last)
     * in the chain.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException
    {
        if (mLogger.isDebugEnabled()) mLogger.debug("Processing CharEncodingFilter");
        try
        {
            req.setCharacterEncoding("UTF-8");
            if (mLogger.isDebugEnabled()) mLogger.debug("Set request character encoding to UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This should never happen since UTF-8 is a Java-specified required encoding.
            throw new ServletException("Can't set incoming encoding to UTF-8");
        }

        // Keep JSTL and Struts Locale's in sync
        // NOTE: The session here will get created if it is not present.  This code was taken from its
        // earlier incarnation in RequestFilter, which also caused the session to be created.
        HttpSession session = ((HttpServletRequest) req).getSession();
        if (mLogger.isDebugEnabled()) mLogger.debug("Synchronizing JSTL and Struts locales");
        Locale locale = (Locale) session.getAttribute(Globals.LOCALE_KEY);
        if (locale == null)
        {
            locale = req.getLocale();
        }
        if (req.getParameter("locale") != null)
        {
            locale = new Locale(req.getParameter("locale"));
        }
        session.setAttribute(Globals.LOCALE_KEY, locale);
        Config.set(session, Config.FMT_LOCALE, locale);

        chain.doFilter(req, res);
    }

}
