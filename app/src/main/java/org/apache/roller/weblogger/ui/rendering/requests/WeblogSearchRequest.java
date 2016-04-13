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
import org.apache.roller.weblogger.util.Utilities;

/**
 * Represents a request for a weblog search.
 */
public class WeblogSearchRequest extends WeblogRequest {

    private String query = null;
    private int pageNum = 0;
    private String weblogCategoryName = null;
    
    public WeblogSearchRequest(HttpServletRequest request) {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();

        if(pathInfo != null) {
            throw new IllegalArgumentException("Invalid path info: " + request.getRequestURL());
        }
        
        /*
         * parse request parameters
         *
         * the params we care about are:
         *   q - specifies the search query
         *   pageNum - specifies what pageNum # to display
         *   cat - limit results to a certain weblogCategoryName
         */
        if(request.getParameter("q") != null &&
                request.getParameter("q").trim().length() > 0) {
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
        
        if(request.getParameter("cat") != null &&
                request.getParameter("cat").trim().length() > 0) {
            this.weblogCategoryName =
                    Utilities.decode(request.getParameter("cat"));
        }
    }

    public String getQuery() {
        return query;
    }

    public int getPageNum() {
        return pageNum;
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

}
