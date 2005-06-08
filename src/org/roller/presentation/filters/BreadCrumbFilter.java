package org.roller.presentation.filters;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.NumberUtils;
import org.roller.presentation.RollerSession;
import org.roller.util.StringUtils;

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
    private FilterConfig mFilterConfig = null;
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
            response.setContentType("text/html");
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
        this.mFilterConfig = filterConfig;

        // debug
        //System.out.println("BreadCrumbFilter is loaded");

        if (!StringUtils.isEmpty(filterConfig.getInitParameter("maxStackSize")))
        {
            String temp = filterConfig.getInitParameter("maxStackSize");
            int mSS = NumberUtils.stringToInt(temp);
            if (mSS != 0)
            {
                mMaxStackSize = mSS;
            }
        }
    }

    /** destroy any instance values other than filterConfig **/
    public void destroy()
    {
        mMaxStackSize = 10;
    }
}
