/*
 * DebugFilter.java
 *
 * Created on April 17, 2006, 10:30 AM
 */

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


/**
 * A simple debugging filter.
 *
 * This filter is NOT mapped by default and is here only for Roller developers
 * to use while they are working on the code and debugging things.
 *
 * @web.filter name="DebugFilter"
 */
public class DebugFilter implements Filter {
    
    private static Log log = LogFactory.getLog(DebugFilter.class);
    
    
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        log.info("ENTERING "+request.getRequestURL());
        
        chain.doFilter(request, response);
        
        log.info("EXITING "+request.getRequestURL());
    }
    
    
    public void destroy() {}
    
    
    public void init(FilterConfig filterConfig) {}
    
}
