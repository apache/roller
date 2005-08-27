package org.roller.presentation.filters;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.util.RequestUtil;


/**
 * Entry point filter for Weblog page and Editor UI, this filter 
 * creates a RollerRequest object to parse pathinfo and request parameters.
 * 
 * @web.filter name="RequestFilter"
 * 
 * @author David M. Johnson, Matt Raible
 */
public class RequestFilter implements Filter
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
     * Request filter.
     */
    public void doFilter(
        ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException
    {
        // NOTE: Setting character encoding and JSTL/Struts locale sync has been moved to
        // CharEncodingFilter, which is mapped for all URIs in the context.
        HttpSession session = ((HttpServletRequest)req).getSession();
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        Roller roller = RollerContext.getRoller( request );
        RollerRequest rreq = null;
        try 
        {
            rreq = RollerRequest.getRollerRequest(
                request, mFilterConfig.getServletContext());

            // if user wants to be remembered, create a remember me cookie
            // TODO: Figure out a better place to put this - so it will
            // only be called when the user initially logs in
            String username = request.getRemoteUser();

            if (username != null)
            {
                if (session.getAttribute(RollerRequest.LOGIN_COOKIE) != null)
                {
                    session.removeAttribute(RollerRequest.LOGIN_COOKIE);

                    UserManager mgr = RollerFactory.getRoller().getUserManager();
                    String loginCookie = mgr.createLoginCookie(username);
                    RollerFactory.getRoller().commit();
                    RequestUtil.setCookie(response, RollerRequest.LOGIN_COOKIE,
                                         loginCookie, request.getContextPath());
                }
            }
            
          
        }
        catch (RollerException e)
        {
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        /*if (session != null)
        {
            // look for messages and errors in the request, and if they
            // exist, stuff them in the request - in Struts 1.2, you don't
            // need to do this
            if (session.getAttribute(Globals.MESSAGE_KEY) != null)
            {
                request.setAttribute(Globals.MESSAGE_KEY,
                        session.getAttribute(Globals.MESSAGE_KEY));
                session.removeAttribute(Globals.MESSAGE_KEY);
            }
            if (session.getAttribute(Globals.ERROR_KEY) != null)
            {
                request.setAttribute(Globals.ERROR_KEY,
                        session.getAttribute(Globals.ERROR_KEY));
                session.removeAttribute(Globals.ERROR_KEY);
            }
        }*/

        Date updateTime = null;
        try
        {
            updateTime = IfModifiedFilter.getLastPublishedDate(request);
        }
        catch (RollerException e1)
        {
            mLogger.debug("Getting lastUpdateTime", e1);
        }
        if (updateTime != null)
        {
            request.setAttribute("updateTime", updateTime);
        }

        chain.doFilter(req, res);
    }

    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        mFilterConfig = filterConfig;
    }
}

