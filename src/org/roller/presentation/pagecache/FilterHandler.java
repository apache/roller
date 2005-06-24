package org.roller.presentation.pagecache;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.roller.pojos.WebsiteData;

public interface FilterHandler
{
    /**
     * Clean up anything necessary before destruction.
     */
    public void destroy();

    /**
     * Exactly as Filter.doFilter().
     */
    public void doFilter(ServletRequest request,
        ServletResponse response, FilterChain chain)
        throws ServletException, IOException;

    /**
     * Clear the entire cache.
     */
    public void flushCache(HttpServletRequest req);

    /**
     * Remove the entries for this User
     * from the cache.
     */
    public void removeFromCache(HttpServletRequest req, WebsiteData website);
}
