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

package org.apache.roller.weblogger.ui.rendering.util;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * Represents a request for a weblog preview.
 */
public class WeblogSearchRequest extends WeblogRequest {
    
    private static Log log = LogFactory.getLog(WeblogSearchRequest.class);
    
    private static final String SEARCH_SERVLET = "/roller-ui/rendering/search";
    
    // lightweight attributes
    private String query = null;
    private int pageNum = 0;
    private String weblogCategoryName = null;
    
    // heavyweight attributes
    private WeblogCategory weblogCategory = null;
    
    
    public WeblogSearchRequest() {}
    
    
    public WeblogSearchRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        String servlet = request.getServletPath();
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // was this request bound for the search servlet?
        if(servlet == null || !SEARCH_SERVLET.equals(servlet)) {
            throw new InvalidRequestException("not a weblog search request, "+
                    request.getRequestURL());
        }
        
        if(pathInfo != null) {
            throw new InvalidRequestException("invalid path info, "+
                    request.getRequestURL());
        }
        
        
        /*
         * parse request parameters
         *
         * the only params we currently care about are:
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
                    URLUtilities.decode(request.getParameter("cat"));
            
            // all categories must start with a /
            if(!this.weblogCategoryName.startsWith("/")) {
                this.weblogCategoryName = "/"+this.weblogCategoryName;
            }
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

    public void setWeblogCategoryName(String weblogCategory) {
        this.weblogCategoryName = weblogCategory;
    }

    public WeblogCategory getWeblogCategory() {
        
        if(weblogCategory == null && weblogCategoryName != null) {
            try {
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                weblogCategory = wmgr.getWeblogCategoryByPath(getWeblog(), weblogCategoryName);
            } catch (WebloggerException ex) {
                log.error("Error getting weblog category "+weblogCategoryName, ex);
            }
        }
        
        return weblogCategory;
    }

    public void setWeblogCategory(WeblogCategory weblogCategory) {
        this.weblogCategory = weblogCategory;
    }
    
}
