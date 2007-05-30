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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Represents an *old* request for a Roller weblog comments permalink.
 * 
 * any url from ... /comments/*
 * 
 * While these urls are no longer used we do provide redirect support for them
 * for users who have upgraded from earlier versions.  We keep this class to
 * help with parsing these urls since they are fairly complex.
 */
public class OldCommentsRequest {
    
    private static Log log = LogFactory.getLog(OldCommentsRequest.class);
    
    // various page types
    public static final String MAIN = "main";
    public static final String PERMALINK = "permalink";
    public static final String ARCHIVE = "archive";
    
    private String context = null;
    private String pageType = null;
    private String weblogHandle = null;
    private String weblogAnchor = null;
    private String weblogPage = null;
    private String weblogCategory = null;
    private String weblogDate = null;
    
    
    /**
     * Construct the WeblogPageRequest by parsing the incoming url
     */
    public OldCommentsRequest(HttpServletRequest request) throws Exception {
        
        // parse the request object and figure out what we've got
        log.debug("parsing url "+request.getRequestURL());
        
        String servlet = request.getServletPath();
        String pathInfo = request.getPathInfo();
        
        // make sure this request was destined for the comments servlet
        if(servlet != null) {
            // strip off the leading slash
            servlet = servlet.substring(1);
            
            if("comments".equals(servlet)) {
                this.context = "weblog";
            } else {
                // not a request to the page servlet
                throw new Exception("not a weblog page request, "+request.getRequestURL());
            }
        } else {
            throw new Exception("not a weblog page request, "+request.getRequestURL());
        }
        
        
        /*
         * parse path info
         *
         * we expect one of the following forms of urls ...
         *
         * [handle] - get default page for user for today's date
         * [handle]/[date] - get default page for user for specified date
         * [handle]/[pagelink] - get specified page for today's date
         * [handle]/[pagelink]/[date] - get specified page for specified date
         * [handle]/[pagelink]/[anchor] - get specified page & entry (by anchor)
         * [handle]/[pagelink]/[date]/[anchor] - get specified page & entry (by anchor)
         */
        if(pathInfo != null && pathInfo.trim().length() > 1) {
            // strip off the leading slash
            pathInfo = pathInfo.substring(1);
            String[] pathElements = pathInfo.split("/");
            
            if ( pathElements.length == 1 ) {
                
                // /handle
                this.weblogHandle = pathElements[0];
                this.weblogPage = "Weblog";
                this.pageType = MAIN;
                
            } else if ( pathElements.length == 2 ) {
                
                // /handle/date or /handle/page
                this.weblogHandle = pathElements[0];
                this.weblogPage = "Weblog";
                
                if(this.isValidDateString(pathElements[1])) {
                    this.weblogDate = pathElements[1];
                    this.pageType = ARCHIVE;
                } else {
                    this.weblogPage = pathElements[1];
                    this.pageType = MAIN;
                }
                
            } else if ( pathElements.length == 3 ) {
                
                // /handle/page/date or /handle/page/anchor
                this.weblogHandle = pathElements[0];
                this.weblogPage = pathElements[1];
                
                if(this.isValidDateString(pathElements[2])) {
                    this.weblogDate = pathElements[2];
                    this.pageType = ARCHIVE;
                } else {
                    this.weblogAnchor = pathElements[2];
                    this.pageType = PERMALINK;
                }
                
            } else if ( pathElements.length == 4 ) {
                
                // /handle/page/date/anchor
                this.weblogHandle = pathElements[0];
                this.weblogPage = pathElements[1];
                this.weblogDate = pathElements[2];
                this.weblogAnchor = pathElements[3];
                this.pageType = PERMALINK;
            }
            
        } else {
            // invalid request ... path info is empty
            throw new Exception("not a weblog page request, "+request.getRequestURL());
        }
        
        
        /*
         * parse request parameters
         *
         * the only params we currently care about are:
         *   anchor - specifies a weblog entry
         *   entry - specifies a weblog entry
         *   catname - specifies a weblog category
         */
        if(request.getParameter("anchor") != null) {
            this.weblogAnchor = request.getParameter("anchor");
            this.pageType = PERMALINK;
        }
        
        if(request.getParameter("entry") != null) {
            this.weblogAnchor = request.getParameter("entry");
            this.pageType = PERMALINK;
        }
        
        if(request.getParameter("catname") != null) {
            String cat = request.getParameter("catname");
            
            this.weblogCategory = cat;
            this.pageType = ARCHIVE;
        }

        // comments only supported permalinks, so if anchor is null then error
        if(this.weblogAnchor == null) {
            throw new Exception("invalid comments request, no anchor");
        }
    }
    
    
    private boolean isValidDateString(String dateString) {
        return (dateString != null && dateString.length() > 3 && StringUtils.isNumeric(dateString));
    }

    public String getContext() {
        return context;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public String getWeblogPage() {
        return weblogPage;
    }

    public String getWeblogCategory() {
        return weblogCategory;
    }

    public String getWeblogDate() {
        return weblogDate;
    }

    public String getPageType() {
        return pageType;
    }
    
}
