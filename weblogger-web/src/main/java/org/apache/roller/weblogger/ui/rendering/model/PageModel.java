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

package org.apache.roller.weblogger.ui.rendering.model; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.ThemeTemplateWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesDayPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesLatestPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesMonthPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPermalinkPager;
import org.apache.roller.weblogger.ui.rendering.util.WeblogEntryCommentForm;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;


/**
 * Model which provides information needed to render a weblog page.
 */
public class PageModel implements Model {
    
    private static Log log = LogFactory.getLog(PageModel.class);
    
    private WeblogPageRequest pageRequest = null;
    private URLStrategy urlStrategy = null;
    private WeblogEntryCommentForm commentForm = null;
    private Map requestParameters = null;
    private Weblog weblog = null;
    private DeviceType deviceType = null;
    
    
    /**
     * 
     * Creates an un-initialized new instance, Weblogger calls init() to complete
     * construction.
     */
    public PageModel() {}
    
    
    /** 
     * Template context name to be used for model.
     */
    public String getModelName() {
        return "model";
    }
    
    
    /** 
     * Init page model based on request. 
     */
    public void init(Map initData) throws WebloggerException {
        
        // we expect the init data to contain a weblogRequest object
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if(weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        // PageModel only works on page requests, so cast weblogRequest
        // into a WeblogPageRequest and if it fails then throw exception
        if(weblogRequest instanceof WeblogPageRequest) {
            this.pageRequest = (WeblogPageRequest) weblogRequest;
        } else {
            throw new WebloggerException("weblogRequest is not a WeblogPageRequest."+
                    "  PageModel only supports page requests.");
        }
        
        // see if there is a comment form
        this.commentForm = (WeblogEntryCommentForm) initData.get("commentForm");
        
        // custom request parameters
        this.requestParameters = (Map)initData.get("requestParameters");
        
        // look for url strategy
        urlStrategy = (URLStrategy) initData.get("urlStrategy");
        if(urlStrategy == null) {
            urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();
        }
        
        // extract weblog object
        weblog = pageRequest.getWeblog();

        this.deviceType = weblogRequest.getDeviceType();
    }    
    
    
    /**
     * Get the weblog locale used to render this page, null if no locale.
     */
    public String getLocale() {
        return pageRequest.getLocale();
    }
    
    
    /**
     * Get weblog being displayed.
     */
    public WeblogWrapper getWeblog() {
        return WeblogWrapper.wrap(weblog, urlStrategy, getDeviceType().toString());
    }
    
    
    /**
     * Is this page considered a permalink?
     */
    public boolean isPermalink() {
        return (pageRequest.getWeblogAnchor() != null);
    }
    
    
    /**
     * Is this page showing search results?
     */
    public boolean isSearchResults() {
        // the search results model will extend this class and override this
        return false;
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public WeblogEntryWrapper getWeblogEntry() {
        if(pageRequest.getWeblogEntry() != null) {
            return WeblogEntryWrapper.wrap(pageRequest.getWeblogEntry(), urlStrategy);
        }
        return null;
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public ThemeTemplateWrapper getWeblogPage() {
        if(pageRequest.getWeblogPageName() != null) {
            return ThemeTemplateWrapper.wrap(pageRequest.getWeblogPage());
        } else {
            try {
                return ThemeTemplateWrapper.wrap(weblog.getTheme().getDefaultTemplate());
            } catch (WebloggerException ex) {
                log.error("Error getting default page", ex);
            }
        }
        return null;
    }
    
    
    /**
     * Get weblog category specified by request, or null if the category path
     * found in the request does not exist in the current weblog.
     */
    public WeblogCategoryWrapper getWeblogCategory() {
        if(pageRequest.getWeblogCategory() != null) {
            return WeblogCategoryWrapper.wrap(pageRequest.getWeblogCategory(), urlStrategy);
        }
        return null;
    }
    
    
    /**
     * Returns the list of tags specified in the request /tags/foo+bar
     */
    public List getTags() {
        return pageRequest.getTags();
    }
    
    
    /**
     * A map of entries representing this page. The collection is grouped by 
     * days of entries.  Each value is a list of entry objects keyed by the 
     * date they were published.
     */
    public WeblogEntriesPager getWeblogEntriesPager() {
        return getWeblogEntriesPager(null);
    }
    
    
    /**
     * A map of entries representing this page - with entries restricted by category.
     * The collection is grouped by days of entries.  
     * Each value is a list of entry objects keyed by the date they were published.
     * @param catArgument Category restriction (null or "nil" for no restriction)
     */
    public WeblogEntriesPager getWeblogEntriesPager(String catArgument) {
        return getWeblogEntriesPager(catArgument, null);
    }
    
    
    /**
     * A map of entries representing this page - with entries restricted by tag.
     * The collection is grouped by days of entries.  
     * Each value is a list of entry objects keyed by the date they were published.
     * @param tagArgument tag restriction (null or "nil" for no restriction)
     */
    public WeblogEntriesPager getWeblogEntriesPagerByTag(String tagArgument) {
        return getWeblogEntriesPager(null, tagArgument);
    }
    
    
    private WeblogEntriesPager getWeblogEntriesPager(String catArgument, String tagArgument) {
        
        // category specified by argument wins over request parameter
        String cat = pageRequest.getWeblogCategoryName();
        if (catArgument != null && !StringUtils.isEmpty(catArgument) && !"nil".equals(catArgument)) {
            cat = catArgument;
        }
        
        List tags = pageRequest.getTags();
        if (tagArgument != null && !StringUtils.isEmpty(tagArgument) && !"nil".equals(tagArgument)) {
            tags = new ArrayList();
            tags.add(tagArgument);
        }
        
        String dateString = pageRequest.getWeblogDate();
        
        // determine which mode to use
        if (pageRequest.getWeblogAnchor() != null) {
            return new WeblogEntriesPermalinkPager(
                    urlStrategy,
                    weblog,
                    pageRequest.getLocale(),
                    pageRequest.getWeblogPageName(),
                    pageRequest.getWeblogAnchor(),
                    pageRequest.getWeblogDate(),
                    cat,
                    tags,
                    pageRequest.getPageNum());
        } else if (dateString != null && dateString.length() == 8) {
            return new WeblogEntriesDayPager(
                    urlStrategy,
                    weblog,
                    pageRequest.getLocale(),
                    pageRequest.getWeblogPageName(),
                    pageRequest.getWeblogAnchor(),
                    pageRequest.getWeblogDate(),
                    cat,
                    tags,
                    pageRequest.getPageNum());
        } else if (dateString != null && dateString.length() == 6) {
            return new WeblogEntriesMonthPager(
                    urlStrategy,
                    weblog,
                    pageRequest.getLocale(),
                    pageRequest.getWeblogPageName(),
                    pageRequest.getWeblogAnchor(),
                    pageRequest.getWeblogDate(),
                    cat,
                    tags,
                    pageRequest.getPageNum());
          
        } else {
            return new WeblogEntriesLatestPager(
                    urlStrategy,
                    weblog,
                    pageRequest.getLocale(),
                    pageRequest.getWeblogPageName(),
                    pageRequest.getWeblogAnchor(),
                    pageRequest.getWeblogDate(),
                    cat,
                    tags,
                    pageRequest.getPageNum());
        }
    }
        
    
    /**
     * Get comment form to be displayed, may contain preview data.
     *
     * @return Comment form object
     */
    public WeblogEntryCommentForm getCommentForm() {
        
        if(commentForm == null) {
            commentForm = new WeblogEntryCommentForm();
        }
        return commentForm;
    }
    
    /**
     * Get request parameter by name.
     */
    public String getRequestParameter(String paramName) {
        if (requestParameters != null) {
            String[] values = (String[])requestParameters.get(paramName);
            if (values != null && values.length > 0) {
                return values[0];
            }
        }
        return null;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType type) {
        this.deviceType = type;
    }
}
