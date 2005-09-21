/*
 * SchemeEnforcementFilter.java
 *
 * Created on September 16, 2005, 3:17 PM
 */

package org.roller.presentation.filters;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.roller.config.RollerConfig;


/**
 * The SchemeEnforcementFilter is provided for Roller sites that enable secure
 * logins and want to ensure that only login urls are used under https.
 *
 * @author  Allen Gilliland
 *
 * @web.filter name="SchemeEnforcementFilter"
 */
public class SchemeEnforcementFilter implements Filter {
    
    private static Log mLogger = 
            LogFactory.getLog(SchemeEnforcementFilter.class);
    
    private FilterConfig filterConfig = null;
    
    private boolean schemeEnforcementEnabled = false;
    private boolean secureLoginEnabled = false;
    private int httpPort = 80;
    private int httpsPort = 443;
    private String httpsHeaderName = null;
    private String httpsHeaderValue = null;
    
    private Set allowedUrls = new HashSet();
    
    
    /**
     * Process filter.
     *
     * We'll take the incoming request and first determine if this is a
     * secure request.  If the request is secure then we'll see if it matches
     * one of the allowed secure urls, if not then we will redirect back out
     * of https.
     */
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain)
            throws IOException, ServletException {
        
        if(this.schemeEnforcementEnabled && this.secureLoginEnabled) {
            
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse res = (HttpServletResponse) response;
            
            mLogger.debug("checking path = "+req.getServletPath());
            
            // first determine if request is secure
            boolean requestIsSecure = request.isSecure();
            if(!requestIsSecure && this.httpsHeaderName != null) {
                // try checking custom header
                String value = req.getHeader(this.httpsHeaderName);
                if(value != null && value.equals(this.httpsHeaderValue))
                    requestIsSecure = true;
            }
            
            // if request is secure then see if it's an allowable https url
            if(requestIsSecure && !allowedUrls.contains(req.getServletPath())) {
                String redirect = "http://"+req.getServerName();
                
                if(this.httpPort != 80)
                    redirect += ":"+this.httpPort;
                
                redirect += req.getRequestURI();
                
                if(req.getQueryString() != null)
                    redirect += "?"+req.getQueryString();
                
                mLogger.debug("Redirecting to "+redirect);
                res.sendRedirect(redirect);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
    
    
    public void destroy() {}
    
    
    /**
     * Filter init.
     *
     * We are just collecting init properties which we'll use for each request.
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        
        // determine if we are doing scheme enforcement
        this.schemeEnforcementEnabled = 
                RollerConfig.getBooleanProperty("schemeenforcement.enabled");
        this.secureLoginEnabled = 
                RollerConfig.getBooleanProperty("securelogin.enabled");
        
        if(this.schemeEnforcementEnabled && this.secureLoginEnabled) {
            // gather some more properties
            String http_port = 
                    RollerConfig.getProperty("securelogin.http.port");
            String https_port = 
                    RollerConfig.getProperty("securelogin.https.port");
            
            try {
                this.httpPort = Integer.parseInt(http_port);
                this.httpsPort = Integer.parseInt(https_port);
            } catch(NumberFormatException nfe) {
                // ignored ... guess we'll have to use the defaults
                mLogger.warn("error with secure login ports", nfe);
            }
            
            // also note if we are using a custom https header
            String header = 
                    RollerConfig.getProperty("securelogin.https.headername");
            String headerValue = 
                    RollerConfig.getProperty("securelogin.https.headervalue");
            
            if(header != null && headerValue != null) {
                this.httpsHeaderName = header;
                this.httpsHeaderValue = headerValue;
            }
            
            // finally, construct our list of allowable https urls
            String urls = 
                    RollerConfig.getProperty("schemeenforcement.https.urls");
            String[] urlsArray = urls.split(",");
            for(int i=0; i < urlsArray.length; i++)
                this.allowedUrls.add(urlsArray[i]);
            
            // some logging for the curious
            mLogger.info("Scheme enforcement = enabled");
            if(mLogger.isDebugEnabled()) {
                mLogger.debug("allowed urls are:");
                for(Iterator it = this.allowedUrls.iterator(); it.hasNext();)
                    mLogger.debug(it.next());
            }
        }
    }
    
}
