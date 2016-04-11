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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.requests;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents a request for a weblog resource file.
 *
 * /tb-ui/rendering/media-resources/*
 */
public class WeblogMediaResourceRequest extends WeblogRequest {

    private static Log log = LogFactory.getLog(WeblogMediaResourceRequest.class);
        
    private String resourceId = null;
    private boolean thumbnail = false;
    
    /**
     * Construct the WeblogResourceRequest by parsing the incoming url
     */
    public WeblogMediaResourceRequest(HttpServletRequest request) {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // parse the request object and figure out what we've got
        log.debug("parsing path "+pathInfo);
                
        // any id is okay...
        if (pathInfo != null && pathInfo.trim().length() > 1) {
            
            this.resourceId = pathInfo;
            if (pathInfo.startsWith("/")) {
                this.resourceId = pathInfo.substring(1);
            }
        
        } else {
            throw new IllegalArgumentException("Invalid resource path info: " + request.getRequestURL());
        }

        if (request.getParameter("t") != null && "true".equals(request.getParameter("t"))) {
            thumbnail = true;
        }
        
        if(log.isDebugEnabled()) {
            log.debug("resourceId = "+this.resourceId);
        }
    }
    
    public String getResourceId() {
        return resourceId;
    }

    public boolean isThumbnail() {
        return thumbnail;
    }

}
