/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.presentation.filters;

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
import org.apache.roller.business.referrers.IncomingReferrer;
import org.apache.roller.business.referrers.ReferrerQueueManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.presentation.RollerContext;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.InvalidRequestException;
import org.apache.roller.presentation.WeblogPageRequest;
import org.apache.roller.util.SpamChecker;


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
        StringBuffer reqsb = request.getRequestURL();
        if (request.getQueryString() != null) {
            reqsb.append("?");
            reqsb.append(request.getQueryString());
        }
        String requestUrl = reqsb.toString();
        
        // parse the incoming request and make sure it's a valid page request
        WebsiteData weblog = null;
        WeblogPageRequest pageRequest = null;
        try {
            pageRequest = new WeblogPageRequest(request);
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            weblog = userMgr.getWebsiteByHandle(pageRequest.getWeblogHandle());
            
            if(weblog == null) {
                throw new Exception("no weblog named "+pageRequest.getWeblogHandle());
            }
            
        } catch(Exception ex) {
            // bad url or couldn't obtain weblog
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
            
            RollerContext rctx = RollerContext.getRollerContext();
            
            // Base page URLs, with and without www.
            String basePageUrlWWW =
                    rctx.getAbsoluteContextUrl(request)+"/page/"+weblog.getHandle();
            String basePageUrl = basePageUrlWWW;
            if ( basePageUrlWWW.startsWith("http://www.") ) {
                // chop off the http://www.
                basePageUrl = "http://"+basePageUrlWWW.substring(11);
            }
            
            // ignore referrers coming from users own blog
            if (referrerUrl == null ||
                    (!referrerUrl.startsWith(basePageUrl) &&
                    !referrerUrl.startsWith(basePageUrlWWW))) {
                
                String selfSiteFragment = "/page/"+weblog.getHandle();

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
