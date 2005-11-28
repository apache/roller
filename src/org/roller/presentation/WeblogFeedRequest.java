/*
 * WeblogFeedRequest.java
 *
 * Created on November 7, 2005, 1:59 PM
 */

package org.roller.presentation;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.pojos.WeblogTemplate;


/**
 * Represents a request for a Roller weblog feed.
 * 
 * any of /rss/*, /atom/*, /flavor/*, or /planetrss
 *
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 * 
 * @author Allen Gilliland
 */
public class WeblogFeedRequest extends ParsedRequest {
    
    private static Log mLogger = LogFactory.getLog(WeblogFeedRequest.class);
    
    private static Set feedServlets = new HashSet();
    
    private String context = null;
    private String flavor = null;
    private String weblogHandle = null;
    private String weblogCategory = null;
    private boolean excerpts = false;
    
    
    static {
        // initialize our servlet list
        feedServlets.add("rss");
        feedServlets.add("flavor");
        feedServlets.add("atom");
        feedServlets.add("planetrss");
    }
    
    
    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public WeblogFeedRequest(HttpServletRequest request) throws Exception {
        
        super(request);
        
        // parse the request object and figure out what we've got
        mLogger.debug("parsing url "+request.getRequestURL());
        
        String servlet = request.getServletPath();
        String pathInfo = request.getPathInfo();
        
        // what servlet is our destination?
        if(servlet != null) {
            // strip off the leading slash
            servlet = servlet.substring(1);
            
            if(feedServlets.contains(servlet)) {
                this.context = "weblog";
                this.flavor = servlet;
            } else {
                // not a request to a feed servlet
                throw new RollerException("not a weblog feed request, "+request.getRequestURL());
            }
        }
        
        // parse the path info
        if(pathInfo != null && pathInfo.trim().length() > 1) {
            // strip off the leading slash
            pathInfo = pathInfo.substring(1);
            String[] pathElements = pathInfo.split("/");
            
            if(pathElements[0].length() > 0) {
                this.weblogHandle = pathElements[0];
            }
            
        } else {
            
            // no path info means this was a non-weblog request
            // we handle a few exceptions for this which include
            //   /rss - main rss feed
            //   /planetrss - main planet rss feed
            //   /atom - main atom feed
            //   /flavor - main flavor feed
            if(servlet.equals("rss") || servlet.equals("atom") || 
                    servlet.equals("flavor")) {
                
                this.context = "main";
            } else if(servlet.equals("planetrss")) {
                
                this.context = "planet";
            }
        }
        
        /* 
         * parse request parameters
         *
         * the only params we currently care about are:
         *   flavor - defines the feed type
         *   catname - specifies a weblog category
         *   path - specifies a weblog category
         *   excerpts - specifies the feed should only include excerpts
         *
         */
        if(request.getParameter("flavor") != null) {
            this.flavor = request.getParameter("flavor");
        }
        
        if(request.getParameter("path") != null) {
            this.weblogCategory = request.getParameter("path");
        }
        
        if(request.getParameter("catname") != null) {
            this.weblogCategory = request.getParameter("catname");
        }
        
        if(request.getParameter("excerpts") != null) {
            this.excerpts = Boolean.valueOf(request.getParameter("excerpts")).booleanValue();
        }
        
        // one small final adjustment.
        // if our flavor is "flavor" then that means someone is just getting
        // the default flavor, which is rss, so let's set that
        if(this.flavor.equals("flavor")) {
            this.flavor = "rss";
        }
        
    }
    

    public String getContext() {
        return context;
    }

    public String getFlavor() {
        return flavor;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public String getWeblogCategory() {
        return weblogCategory;
    }

    public boolean isExcerpts() {
        return excerpts;
    }

}
