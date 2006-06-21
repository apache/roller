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

package org.apache.roller.ui.rendering.util;

import javax.servlet.http.HttpServletRequest;


/**
 * Represents a request to single weblog.
 *
 * This is a fairly generic parsed request which is only trying to figure out
 * the weblog handle that this request is destined for.
 */
public class WeblogRequest extends ParsedRequest {
    
    private String weblogHandle = null;
    
    
    public WeblogRequest(HttpServletRequest request) throws InvalidRequestException {
        
        // let our parent take care of their business first
        super(request);
        
        String pathInfo = request.getPathInfo();
        
        // we expect a path info of /<handle/*
        if(pathInfo != null && pathInfo.trim().length() > 1) {
            // strip off the leading slash
            pathInfo = pathInfo.substring(1);
            String[] pathElements = pathInfo.split("/");
            
            if(pathElements[0] != null && pathElements[0].trim().length() > 1) {
                this.weblogHandle = pathElements[0];
            } else {
                // no handle in path info
                throw new InvalidRequestException("not a weblog request, "+request.getRequestURL());
            }
            
        } else {
            // invalid request ... path info is empty
            throw new InvalidRequestException("not a weblog request, "+request.getRequestURL());
        }
    }
    
    
    public String getWeblogHandle() {
        return weblogHandle;
    }
    
}
