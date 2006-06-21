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

package org.apache.roller.ui.core;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents a request for a Roller weblog feed.
 * 
 * /roller-ui/rendering/feeds/*
 *
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogFeedRequest extends ParsedRequest {
    
    private static Log mLogger = LogFactory.getLog(WeblogFeedRequest.class);
    
    private static final String FEED_SERVLET = "/feeds";
    
    private String type = null;
    private String format = null;
    private String weblogHandle = null;
    private String weblogCategory = null;
    private boolean excerpts = false;
    
    
    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public WeblogFeedRequest(HttpServletRequest request) throws InvalidRequestException {
        
        super(request);
        
        // parse the request object and figure out what we've got
        mLogger.debug("parsing url "+request.getRequestURL());
        
        String servlet = request.getServletPath();
        String pathInfo = request.getPathInfo();
        
        // was this request bound for the feed servlet?
        if(servlet == null || !FEED_SERVLET.equals(servlet)) {
            throw new InvalidRequestException("not a weblog feed request, "+request.getRequestURL());
        }
        
        /* 
         * parse the path info.  must conform to the format below.
         *
         * /<weblog>/<type>/<format>
         *
         */
        if(pathInfo != null && pathInfo.trim().length() > 1) {
            // strip off the leading slash
            pathInfo = pathInfo.substring(1);
            String[] pathElements = pathInfo.split("/");
            
            if(pathElements.length == 3) {
                this.weblogHandle = pathElements[0];
                this.type = pathElements[1];
                this.format = pathElements[2];
            } else {
                throw new InvalidRequestException("invalid feed path info, "+request.getRequestURL());
            }
            
        } else {
            throw new InvalidRequestException("invalid feed path info, "+request.getRequestURL());
        }
        
        
        /* 
         * parse request parameters
         *
         * the only params we currently care about are:
         *   cat - specifies a weblog category
         *   excerpts - specifies the feed should only include excerpts
         *
         */
        if(request.getParameter("cat") != null) {
            try {
                this.weblogCategory = URLDecoder.decode(request.getParameter("cat"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                // should never happen, utf-8 is always supported by java
            }
        }
        
        if(request.getParameter("excerpts") != null) {
            this.excerpts = Boolean.valueOf(request.getParameter("excerpts")).booleanValue();
        }
        
    }
    

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
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
