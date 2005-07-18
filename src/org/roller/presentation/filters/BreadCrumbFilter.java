package org.roller.presentation.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.RollerConfig;
import org.roller.presentation.RollerSession;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

/**
 * Intercepts requests and places URL
 * into breadcrumb stack.
 * 
 * @web.filter name="BreadCrumbFilter"
 * *web.filter-mapping url-pattern="/*.do"
 * @web.filter-init-param name="maxStackSize" value="3"
 *
 * @author <a href="mailto:lance@brainopolis.com">Lance Lavandowska</a>
 *
**/
public final class BreadCrumbFilter implements Filter
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(BreadCrumbFilter.class);
    
    private int mMaxStackSize = 10;

    
    public void doFilter(
        ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession ses = request.getSession(false);
        ArrayStack stack = null;
        if (ses != null)
        {
            stack = (ArrayStack)ses.getAttribute(RollerSession.BREADCRUMB);
        }
        if (stack == null)
        {
            stack = new ArrayStack();
        }

        // This gives you a chance to look at your breadcrumb trail
        if (request.getQueryString() != null 
            && request.getQueryString().equals("BreadCrumb"))
        {
            response.setContentType("text/html; charset=UTF-8");
            ServletOutputStream out = response.getOutputStream();

            for (int i=0; i<stack.size(); i++)
            {
                out.println(stack.peek(i).toString() +"<br>");
            }
            out.flush();
            out.close();
        }
        else
        {
            chain.doFilter(req, resp);
        }

        StringBuffer url = request.getRequestURL();
        // now that we've successfully returned,
        // add url to stack if it isn't a Velocity page
        String servletPath = request.getServletPath();
        if (servletPath.indexOf("page") == -1 &&
            servletPath.indexOf("comments") == -1)
        {    
            if (request.getQueryString() != null)
            {
                url.append("?").append( request.getQueryString() );
            }
            if (stack.size() == mMaxStackSize)
            {
                stack.remove(mMaxStackSize-1);
            }
            stack.push(url.toString());
        }
        if (ses != null)
        {    
        	ses.setAttribute(RollerSession.BREADCRUMB, stack);
        }
    }

    /**
     * Initialize controller values of filter.
    **/
    public void init(FilterConfig filterConfig)
    {
        mLogger.debug("Initializing Breadcrumb Filter");

        String stackSize = RollerConfig.getProperty("breadcrumbs.stacksize");
        if (!StringUtils.isEmpty(stackSize))
        {
            int mSS = Utilities.stringToInt(stackSize);
            if (mSS != 0)
            {
                mMaxStackSize = mSS;
                mLogger.info("set breadcrumb stack size to "+mSS);
            }
        }
    }

    /** destroy any instance values other than filterConfig **/
    public void destroy()
    {
        mMaxStackSize = 10;
    }
}
