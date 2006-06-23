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
 * Represents a request for a weblog preview.
 */
public class WeblogSearchRequest extends WeblogRequest {
    
    private String query = null;
    private int pageNum = 0;
    private String weblogCategory = null;
    
    
    public WeblogSearchRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // our parent will determine the weblog handle for us
        super(request);
        
        /*
         * parse request parameters
         *
         * the only params we currently care about are:
         *   q - specifies the search query
         *   pageNum - specifies what pageNum # to display
         *   cat - limit results to a certain weblogCategory
         */
        if(request.getParameter("q") != null) {
            this.query = request.getParameter("q");
        }
        
        if(request.getParameter("page") != null) {
            String pageInt = request.getParameter("page");
            try {
                this.pageNum = Integer.parseInt(pageInt);
            } catch(NumberFormatException e) {
                // ignored, bad input
            }
        }
        
        if(request.getParameter("cat") != null) {
            this.weblogCategory = request.getParameter("cat");
        }
    }

    
    public String getQuery() {
        return query;
    }

    public int getPageNum() {
        return pageNum;
    }

    public String getWeblogCategory() {
        return weblogCategory;
    }
    
}
