package org.roller.presentation.filters;

import java.io.IOException;
import java.util.regex.Pattern;
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
import org.roller.business.referrers.IncomingReferrer;
import org.roller.business.referrers.ReferrerQueueManager;
import org.roller.model.RollerFactory;
import org.roller.presentation.RollerContext;
import org.roller.config.RollerConfig;
import org.roller.model.UserManager;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.WeblogPageRequest;
import org.roller.util.SpamChecker;


/**
 * Keep track of referers.
 *
 * @author David M. Johnson
 * @web.filter name="RefererFilter"
 */
public class RefererFilter implements Filter {
    
    private static Log mLogger = LogFactory.getLog(RefererFilter.class);
    private static final String ROBOT_PATTERN_PROP_NAME = "referrer.robotCheck.userAgentPattern";
    
    private static Pattern robotPattern = null;
    
    private FilterConfig mFilterConfig = null;
    private boolean processingEnabled = true;
    
    
    /**
     * doFilter
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        // if referrer processing is disabled then we are done
        if(!this.processingEnabled) {
            chain.doFilter(req, res);
            return;
        }
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        boolean ignoreReferrer = false;
        boolean isRefSpammer = false;
        boolean isRobot = false;
        String referrerUrl = request.getHeader("Referer");
        String requestUrl = request.getRequestURL().toString();
        
        // parse the incoming request and make sure it's a valid page request
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
        } catch(Exception e) {
            // illegal page request
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            mLogger.warn("Illegal page request: "+request.getRequestURL());
            return;
        }
        
        // determine if this request came from a robot
        if (robotPattern != null) {
            // If the pattern is present, we check for whether the User-Agent matches,
            // and set isRobot if so.  Currently, all referral processing, including
            // spam check, is skipped for robots identified in this way.
            String userAgent = request.getHeader("User-Agent");
            isRobot = (userAgent != null && userAgent.length() > 0 && robotPattern.matcher(userAgent).matches());
        }
        
        // validate the referrer
        if (pageRequest != null && pageRequest.getWeblogHandle() != null && !isRobot) {
            String handle = pageRequest.getWeblogHandle();
            
            RollerContext rctx =
                    RollerContext.getRollerContext(mFilterConfig.getServletContext());
            
            // Base page URLs, with and without www.
            String basePageUrlWWW =
                    rctx.getAbsoluteContextUrl(request)+"/page/"+handle;
            String basePageUrl = basePageUrlWWW;
            if ( basePageUrlWWW.startsWith("http://www.") ) {
                // chop off the http://www.
                basePageUrl = "http://"+basePageUrlWWW.substring(11);
            }
            
            // ignore referres coming from users own blog
            if (referrerUrl == null ||
                    (!referrerUrl.startsWith(basePageUrl) &&
                    !referrerUrl.startsWith(basePageUrlWWW))) {
                
                String selfSiteFragment = "/page/"+handle;
                WebsiteData weblog = null;
                
                // lookup the weblog now
                try {
                    UserManager userMgr = RollerFactory.getRoller().getUserManager();
                    weblog = userMgr.getWebsiteByHandle(handle);
                } catch(Exception e) {
                    // if we can't get the WebsiteData object we can't continue
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    mLogger.error("Error retrieving weblog: "+handle, e);
                    return;
                }
                
                // validate the referrer
                if ( referrerUrl != null ) {
                    // ignore a Referrer from the persons own blog
                    if (referrerUrl.indexOf(selfSiteFragment) != -1) {
                        referrerUrl = null;
                        ignoreReferrer = true;
                    } else {
                        // treat editor referral as direct
                        int lastSlash = requestUrl.indexOf("/", 8);
                        if (lastSlash == -1) lastSlash = requestUrl.length();
                        String requestSite = requestUrl.substring(0, lastSlash);
                        
                        if (referrerUrl.matches(requestSite + ".*\\.do.*")) {
                            referrerUrl = null;
                        } else {
                            // If referer URL is blacklisted, throw it out
                            isRefSpammer = SpamChecker.checkReferrer(weblog, referrerUrl);
                        }
                    }
                }
                
            } else {
                mLogger.debug("Ignoring referer = "+referrerUrl);
                ignoreReferrer = true;
            }
        }
        
        // pre-processing complete, let's finish the job
        if (isRefSpammer) {
            // spammers get a 403 Access Denied
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
            
        } else if(!isRobot && !ignoreReferrer) {
            // referrer is valid, lets record it
            try {
                IncomingReferrer referrer = new IncomingReferrer();
                referrer.setReferrerUrl(referrerUrl);
                referrer.setRequestUrl(requestUrl);
                referrer.setWeblogHandle(pageRequest.getWeblogHandle());
                referrer.setWeblogAnchor(pageRequest.getWeblogAnchor());
                referrer.setWeblogDateString(pageRequest.getWeblogDate());
                
                ReferrerQueueManager refQueue =
                        RollerFactory.getRoller().getReferrerQueueManager();
                refQueue.processReferrer(referrer);
            } catch(Exception e) {
                mLogger.error("Error processing referrer", e);
            }
        }
        
        // referrer processed, continue with request
        chain.doFilter(req, res);
    }
    
    
    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException {
        
        this.mFilterConfig = filterConfig;
        
        // see if built-in referrer processing is enabled
        this.processingEnabled = 
                RollerConfig.getBooleanProperty("referrers.processing.enabled");
        
        mLogger.info("Referrer processing enabled = "+this.processingEnabled);
        
        // check for possible robot pattern
        String robotPatternStr = RollerConfig.getProperty(ROBOT_PATTERN_PROP_NAME);
        if (robotPatternStr != null && robotPatternStr.length() >0) {
            // Parse the pattern, and store the compiled form.
            try {
                robotPattern = Pattern.compile(robotPatternStr);
            } catch (Exception e) {
                // Most likely a PatternSyntaxException; log and continue as if it is not set.
                mLogger.error("Error parsing "+ ROBOT_PATTERN_PROP_NAME + " value '" +
                        robotPatternStr + "'.  Robots will not be filtered. ", e);
            }
        }
    }
    
    
    /**
     * destroy
     */
    public void destroy() {}
    
}
