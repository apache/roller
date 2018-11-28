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

package org.apache.roller.weblogger.ui.rendering.velocity.deprecated;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogTemplate;


/**
 * Represents a request for an *old* Roller weblog feed.
 * 
 * any of /rss/*, /atom/*, /flavor/*
 *
 * While these urls are no longer used we do provide redirect support for them
 * for users who have upgraded from earlier versions.  We keep this class to
 * help with parsing these urls since they are fairly complex.
 */
public class OldFeedRequest {
    
    private static Log mLogger = LogFactory.getLog(OldFeedRequest.class);
    
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
    }
    
    
    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public OldFeedRequest(HttpServletRequest request) throws Exception {
        
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
                throw new Exception("not a weblog feed request, "+request.getRequestURL());
            }
        } else {
            throw new Exception("not a weblog feed request, "+request.getRequestURL());
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
            //   /atom - main atom feed
            //   /flavor - main flavor feed
            
            this.context = "main";
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
