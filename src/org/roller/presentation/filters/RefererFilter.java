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
import org.roller.model.RefererManager;
import org.roller.model.RollerFactory;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.config.RollerConfig;

/**
 * Keep track of referers.
 *
 * @author David M. Johnson
 * @web.filter name="RefererFilter"
 */
public class RefererFilter implements Filter {
    private FilterConfig mFilterConfig = null;
    private static Log mLogger = LogFactory.getFactory().getInstance(RefererFilter.class);
    private static Pattern robotPattern = null;
    private static final String ROBOT_PATTERN_PROP_NAME = "referrer.robotCheck.userAgentPattern";

    /**
     * destroy
     */
    public void destroy() {
    }

    /**
     * doFilter
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        boolean isRefSpammer = false;
        boolean isRobot = false;

        try {
            if (robotPattern != null) {
                // If the pattern is present, we check for whether the User-Agent matches,
                // and set isRobot if so.  Currently, all referral processing, including
                // spam check, is skipped for robots identified in this way.
                String userAgent = request.getHeader("User-Agent");
                isRobot = (userAgent != null && userAgent.length() > 0 && robotPattern.matcher(userAgent).matches());
            }

            if (!isRobot) {
                RollerRequest rreq = RollerRequest.getRollerRequest(request);
                RollerContext rctx = RollerContext.getRollerContext(mFilterConfig.getServletContext());

                if (rreq != null && rreq.getWebsite() != null) {
                    String handle = rreq.getWebsite().getHandle();

                    // Base page URLs, with and without www.
                    String basePageUrlWWW = rctx.getAbsoluteContextUrl(request) + "/page/" + handle;
                    String basePageUrl = basePageUrlWWW;
                    if (basePageUrlWWW.startsWith("http://www.")) {
                        // chop off the http://www.
                        basePageUrl = "http://" + basePageUrlWWW.substring(11);
                    }

                    // Base comment URLs, with and without www.
                    String baseCommentsUrlWWW = rctx.getAbsoluteContextUrl(request) + "/comments/" + handle;
                    String baseCommentsUrl = baseCommentsUrlWWW;
                    if (baseCommentsUrlWWW.startsWith("http://www.")) {
                        // chop off the http://www.
                        baseCommentsUrl = "http://" + baseCommentsUrlWWW.substring(11);
                    }

                    // Don't process hits from same user's blogs as referers by
                    // ignoring Don't process referer from pages that start with base URLs.
                    String referer = request.getHeader("Referer");
                    if (referer == null || (!referer.startsWith(basePageUrl) && !referer.startsWith(basePageUrlWWW) && !referer.startsWith(baseCommentsUrl) && !referer.startsWith(baseCommentsUrlWWW)))
                    {
                        RefererManager refMgr = RollerFactory.getRoller().getRefererManager();
                        isRefSpammer = refMgr.processRequest(rreq);
                    } else {
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug("Ignoring referer=" + referer);
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.error("Processing referer", e);
        }

        if (isRefSpammer) {
            HttpServletResponse response = (HttpServletResponse) res;
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
}
