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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.util.URLUtilities;
import org.apache.roller.util.Utilities;


/**
 * Represents a request for a Roller weblog feed.
 * 
 * /roller-ui/rendering/feeds/*
 *
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogFeedRequest extends WeblogRequest {
    
    private static Log log = LogFactory.getLog(WeblogFeedRequest.class);
    
    private static final String FEED_SERVLET = "/roller-ui/rendering/feed";
    
    // lightweight attributes
    private String type = null;
    private String format = null;
    private String weblogCategoryName = null;
    private List   tags = new ArrayList();
    private boolean excerpts = false;
    
    // heavyweight attributes
    private WeblogCategoryData weblogCategory = null;
    
    
    public WeblogFeedRequest() {}
    
    
    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public WeblogFeedRequest(HttpServletRequest request) 
            throws InvalidRequestException {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        String servlet = request.getServletPath();
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        // parse the request object and figure out what we've got
        log.debug("parsing path "+pathInfo);
        
        // was this request bound for the feed servlet?
        if(servlet == null || !FEED_SERVLET.equals(servlet)) {
            throw new InvalidRequestException("not a weblog feed request, "+
                    request.getRequestURL());
        }
        
        
        /* 
         * parse the path info.
         * 
         * must look like this ...
         *
         * /<type>/<format>
         */
        if(pathInfo != null && pathInfo.trim().length() > 1) {
            
            String[] pathElements = pathInfo.split("/");
            if(pathElements.length == 2) {
                this.type = pathElements[0];
                this.format = pathElements[1];
            } else {
                throw new InvalidRequestException("invalid feed path info, "+
                        request.getRequestURL());
            }
            
        } else {
            throw new InvalidRequestException("invalid feed path info, "+
                    request.getRequestURL());
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
            this.weblogCategoryName =
                    URLUtilities.decode(request.getParameter("cat"));
            
            // all categories must start with a /
            if(!this.weblogCategoryName.startsWith("/")) {
                this.weblogCategoryName = "/"+this.weblogCategoryName;
            }
        }
        
        if(request.getParameter("tags") != null) {
          this.tags = Arrays.asList(StringUtils.split(URLUtilities.decode(request.getParameter("tags")),"+"));
          int maxSize = RollerConfig.getIntProperty("tags.queries.maxIntersectionSize", 3);                  
          if(this.tags.size() > maxSize)
              throw new InvalidRequestException("max number of tags allowed is " + maxSize + ", " + request.getRequestURL());
        }        
        
        if(request.getParameter("excerpts") != null) {
            this.excerpts = Boolean.valueOf(request.getParameter("excerpts")).booleanValue();
        }
        
        if((this.tags != null && this.tags.size() > 0) && this.weblogCategoryName != null) {
            throw new InvalidRequestException("please specify either category or tags but not both, " + request.getRequestURL());            
        }
        
        if(log.isDebugEnabled()) {
            log.debug("type = "+this.type);
            log.debug("format = "+this.format);
            log.debug("weblogCategory = "+this.weblogCategoryName);
            log.debug("tags = "+ Utilities.stringArrayToString((String[])this.tags.toArray(), ","));            
            log.debug("excerpts = "+this.excerpts);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

    public void setWeblogCategoryName(String weblogCategory) {
        this.weblogCategoryName = weblogCategory;
    }
    
    public List getTags() {
      return tags;
    }

    public void setTags(List tags) {
      this.tags = tags;
    }

    public boolean isExcerpts() {
        return excerpts;
    }

    public void setExcerpts(boolean excerpts) {
        this.excerpts = excerpts;
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
