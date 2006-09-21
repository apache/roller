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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.Template;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.util.URLUtilities;
import org.apache.roller.util.Utilities;


/**
 * Represents a request for a Roller weblog page.
 *
 * any url from ... /roller-ui/rendering/page/*
 *
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogPageRequest extends WeblogRequest {
    
    private static Log log = LogFactory.getLog(WeblogPageRequest.class);
    
    private static final String PAGE_SERVLET = "/roller-ui/rendering/page";
    
    // lightweight attributes
    private String context = null;
    private String weblogAnchor = null;
    private String weblogPageName = null;
    private String weblogCategoryName = null;
    private String weblogDate = null;
    private List tags = new ArrayList();
    private int pageNum = 0;
    private Map customParams = new HashMap();
    
    // heavyweight attributes
    private WeblogEntryData weblogEntry = null;
    private Template weblogPage = null;
    private WeblogCategoryData weblogCategory = null;
    
    
    public WeblogPageRequest() {}
    
    
    /**
     * Construct the WeblogPageRequest by parsing the incoming url
     */
    public WeblogPageRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        String servlet = request.getServletPath();
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // parse the request object and figure out what we've got
        log.debug("parsing path "+pathInfo);
        
        // was this request bound for the right servlet?
        if(!isValidDestination(servlet)) {
            throw new InvalidRequestException("invalid destination for request, "+
                    request.getRequestURL());
        }
        
        
        /*
         * parse path info
         *
         * we expect one of the following forms of url ...
         *
         * /entry/<anchor> - permalink
         * /date/<YYYYMMDD> - date collection view
         * /category/<category> - category collection view
         * /tags/spring+framework - tags
         * /page/<pagelink> - custom page
         *
         * path info may be null, which indicates the weblog homepage
         */
        if(pathInfo != null && pathInfo.trim().length() > 0) {
            
            // all views use 2 path elements, except category
            String[] pathElements = pathInfo.split("/");
            if(pathElements.length > 1 && "category".equals(pathElements[0])) {
                
                // category may have multiple path elements, so re-split with max 2
                pathElements = pathInfo.split("/", 2);
                this.context = pathElements[0];
                this.weblogCategoryName = "/"+URLUtilities.decode(pathElements[1]);
                
                // all categories must start with a /
                if(!this.weblogCategoryName.startsWith("/")) {
                    this.weblogCategoryName = "/"+this.weblogCategoryName;
                }

            } else if(pathElements.length == 2) {
                
                this.context = pathElements[0];
                if("entry".equals(this.context)) {
                    this.weblogAnchor = URLUtilities.decode(pathElements[1]);
                    
                } else if("date".equals(this.context)) {
                    if(this.isValidDateString(pathElements[1])) {
                        this.weblogDate = pathElements[1];
                    } else {
                        throw new InvalidRequestException("invalid date, "+
                            request.getRequestURL());
                    }
                    
                } else if("page".equals(this.context)) {
                    this.weblogPageName = pathElements[1];

                } else if("tags".equals(this.context)) {
                  this.tags = Arrays.asList(StringUtils.split(pathElements[1],"+"));
                                      
                } else {
                    throw new InvalidRequestException("context "+this.context+
                            "not supported, "+request.getRequestURL());
                }
                
            } else {
                throw new InvalidRequestException("bad path info, "+
                        request.getRequestURL());
            }
            
        } else {
            // default view, weblog homepage
        }
        
        
        /*
         * parse request parameters
         *
         * the only params we currently allow are:
         *   date - specifies a weblog date string
         *   cat - specifies a weblog category
         *   anchor - specifies a weblog entry (old way)
         *   entry - specifies a weblog entry
         *
         * we only allow request params if the path info is null or on user
         * defined pages (for backwards compatability).  this way
         * we prevent mixing of path based and query param style urls.
         */
        if(pathInfo == null || this.weblogPageName != null) {
            
            // check for entry/anchor params which indicate permalink
            if(request.getParameter("entry") != null) {
                String anchor = request.getParameter("entry");
                if(StringUtils.isNotEmpty(anchor)) {
                    this.weblogAnchor = anchor;
                }
            } else if(request.getParameter("anchor") != null) {
                String anchor = request.getParameter("anchor");
                if(StringUtils.isNotEmpty(anchor)) {
                    this.weblogAnchor = anchor;
                }
            }
            
            // only check for other params if we didn't find an anchor above
            if(this.weblogAnchor == null) {
                if(request.getParameter("date") != null) {
                    String date = request.getParameter("date");
                    if(this.isValidDateString(date)) {
                        this.weblogDate = date;
                    } else {
                        throw new InvalidRequestException("invalid date, "+
                                request.getRequestURL());
                    }
                }
                
                if(request.getParameter("cat") != null) {
                    this.weblogCategoryName =
                            URLUtilities.decode(request.getParameter("cat"));
                    
                    // all categories must start with a /
                    if(!this.weblogCategoryName.startsWith("/")) {
                        this.weblogCategoryName = "/"+this.weblogCategoryName;
                    }
                }
                
                if(request.getParameter("tags") != null) {
                  this.tags = Arrays.asList(StringUtils.split(URLUtilities.decode(request.getParameter("tags")),"+"));
                }
            }
        }
        
        // page request param is supported in all views
        if(request.getParameter("page") != null) {
            String pageInt = request.getParameter("page");
            try {
                this.pageNum = Integer.parseInt(pageInt);
            } catch(NumberFormatException e) {
                // ignored, bad input
            }
        }
        
        // build customParams Map, we remove built-in params because we only
        // want this map to represent params defined by the template author
        customParams = new HashMap(request.getParameterMap());
        customParams.remove("entry");
        customParams.remove("anchor");
        customParams.remove("date");
        customParams.remove("cat");
        customParams.remove("page");
        customParams.remove("tags");
            
        if(log.isDebugEnabled()) {
            log.debug("context = "+this.context);
            log.debug("weblogAnchor = "+this.weblogAnchor);
            log.debug("weblogDate = "+this.weblogDate);
            log.debug("weblogCategory = "+this.weblogCategoryName);
            log.debug("tags = "+ Utilities.stringArrayToString((String[])this.tags.toArray(), ","));
            log.debug("weblogPage = "+this.weblogPageName);
            log.debug("pageNum = "+this.pageNum);
        }
    }
    
    
    boolean isValidDestination(String servlet) {
        return (servlet != null && PAGE_SERVLET.equals(servlet));
    }
    
    
    private boolean isValidDateString(String dateString) {
        // string must be all numeric and 6 or 8 characters
        return (dateString != null && StringUtils.isNumeric(dateString) &&
                (dateString.length() == 6 || dateString.length() == 8));
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public void setWeblogAnchor(String weblogAnchor) {
        this.weblogAnchor = weblogAnchor;
    }

    public String getWeblogPageName() {
        return weblogPageName;
    }

    public void setWeblogPageName(String weblogPage) {
        this.weblogPageName = weblogPage;
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

    public void setWeblogCategoryName(String weblogCategory) {
        this.weblogCategoryName = weblogCategory;
    }

    public String getWeblogDate() {
        return weblogDate;
    }

    public void setWeblogDate(String weblogDate) {
        this.weblogDate = weblogDate;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Map getCustomParams() {
        return customParams;
    }

    public void setCustomParams(Map customParams) {
        this.customParams = customParams;
    }
    
    public List getTags() {
      return tags;
    }
    
    public void setTags(List tags) {
      this.tags = tags;
    }
    
    public WeblogEntryData getWeblogEntry() {
        
        if(weblogEntry == null && weblogAnchor != null) {
            try {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                weblogEntry = wmgr.getWeblogEntryByAnchor(getWeblog(), weblogAnchor);
            } catch (RollerException ex) {
                log.error("Error getting weblog entry "+weblogAnchor, ex);
            }
        }
        
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntryData weblogEntry) {
        this.weblogEntry = weblogEntry;
    }

    public Template getWeblogPage() {
        
        if(weblogPage == null && weblogPageName != null) {
            try {
                UserManager umgr = RollerFactory.getRoller().getUserManager();
                weblogPage = getWeblog().getPageByLink(weblogPageName);                
            } catch (RollerException ex) {
                log.error("Error getting weblog page "+weblogPageName, ex);
            }
        }
        
        return weblogPage;
    }

    public void setWeblogPage(WeblogTemplate weblogPage) {
        this.weblogPage = weblogPage;
    }

    public WeblogCategoryData getWeblogCategory() {
        
        if(weblogCategory == null && weblogCategoryName != null) {
            try {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                weblogCategory = wmgr.getWeblogCategoryByPath(getWeblog(), weblogCategoryName);
            } catch (RollerException ex) {
                log.error("Error getting weblog category "+weblogCategoryName, ex);
            }
        }
        
        return weblogCategory;
    }

    public void setWeblogCategory(WeblogCategoryData weblogCategory) {
        this.weblogCategory = weblogCategory;
    }
    
}
