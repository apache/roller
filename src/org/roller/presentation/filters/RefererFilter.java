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
import org.roller.model.RefererManager;
import org.roller.model.RollerFactory;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;

/**
 * Keep track of referers.
 *
 * @web.filter name="RefererFilter"
 *
 * @author David M. Johnson
 */
public class RefererFilter implements Filter {
    private FilterConfig   mFilterConfig = null;
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RefererFilter.class);
    
    /**
     * destroy
     */
    public void destroy() {
    }
    
    /**
     * doFilter
     */
    public void doFilter(
            ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        boolean isRefSpammer = false;
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerContext rctx = RollerContext.getRollerContext(
                    mFilterConfig.getServletContext());
            
            if (rreq!=null && rreq.getWebsite() != null) {
                String handle = rreq.getWebsite().getHandle();
                
                // Base page URLs, with and without www.
                String basePageUrlWWW =
                        rctx.getAbsoluteContextUrl(request)+"/page/"+handle;
                String basePageUrl = basePageUrlWWW;
                if ( basePageUrlWWW.startsWith("http://www.") ) {
                    // chop off the http://www.
                    basePageUrl = "http://"+basePageUrlWWW.substring(11);
                }
                
                // Base comment URLs, with and without www.
                String baseCommentsUrlWWW =
                        rctx.getAbsoluteContextUrl(request)+"/comments/"+handle;
                String baseCommentsUrl = baseCommentsUrlWWW;
                if ( baseCommentsUrlWWW.startsWith("http://www.") ) {
                    // chop off the http://www.
                    baseCommentsUrl= "http://"+baseCommentsUrlWWW.substring(11);
                }
                
                // Don't process hits from same user's blogs as referers by
                // ignoring Don't process referer from pages that start with base URLs.
                String referer = request.getHeader("Referer");
                if (  referer==null ||
                        (
                        !referer.startsWith( basePageUrl )
                        && !referer.startsWith( basePageUrlWWW )
                        && !referer.startsWith( baseCommentsUrl )
                        && !referer.startsWith( baseCommentsUrlWWW )
                        )
                        ) {
                    RefererManager refMgr =
                            RollerFactory.getRoller().getRefererManager();
                    isRefSpammer = refMgr.processRequest(rreq);
                } else {
                    if (mLogger.isDebugEnabled()) {
                        mLogger.debug("Ignoring referer="+referer);
                    }
                }
            }
        } catch (Exception e) {
            mLogger.error("Processing referer",e);
        }
        
        if (isRefSpammer) {
            HttpServletResponse response = (HttpServletResponse)res;
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            chain.doFilter(req, res);
        }
    }
    
    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        mFilterConfig = filterConfig;
    }
}
