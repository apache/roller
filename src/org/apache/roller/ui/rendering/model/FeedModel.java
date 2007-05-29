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

package org.apache.roller.ui.rendering.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.rendering.pagers.CommentsPager;
import org.apache.roller.ui.rendering.pagers.Pager;
import org.apache.roller.ui.rendering.pagers.WeblogEntriesListPager;
import org.apache.roller.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.ui.rendering.util.WeblogRequest;
import org.apache.roller.util.URLUtilities;


/**
 * Model which provides information needed to render a feed.
 */
public class FeedModel implements Model {
    
    private static Log log = LogFactory.getLog(FeedModel.class); 
    
    private static int DEFAULT_ENTRIES = RollerRuntimeConfig.getIntProperty("site.newsfeeds.defaultEntries");
    
    private WeblogFeedRequest feedRequest = null;
    private Weblog weblog = null;
    
    
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a weblogRequest object
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(weblogRequest == null) {
            throw new RollerException("expected weblogRequest from init data");
        }
        
        // PageModel only works on page requests, so cast weblogRequest
        // into a WeblogPageRequest and if it fails then throw exception
        if(weblogRequest instanceof WeblogFeedRequest) {
            this.feedRequest = (WeblogFeedRequest) weblogRequest;
        } else {
            throw new RollerException("weblogRequest is not a WeblogFeedRequest."+
                    "  FeedModel only supports feed requests.");
        }
        
        // extract weblog object
        weblog = feedRequest.getWeblog();
    }
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "model";
    }
    
    
    /**
     * Get the weblog locale used to render this page, null if no locale.
     */
    public String getLocale() {
        return feedRequest.getLocale();
    }
    
    
    /**
     * Get weblog being displayed.
     */
    public WebsiteDataWrapper getWeblog() {
        return WebsiteDataWrapper.wrap(weblog);
    }
    
    
    /**
     * Get category path or name specified by request.
     */
    public boolean getExcerpts() {
        return feedRequest.isExcerpts();
    }
    
    
    /**
     * Get category path or name specified by request.
     */
    public String getCategoryPath() {
        return feedRequest.getWeblogCategoryName();
    }
    
    /**
     * Gets most recent entries limited by: weblog and category specified in 
     * request plus the weblog.entryDisplayCount.
     */
    public Pager getWeblogEntriesPager() {
        return new FeedEntriesPager(feedRequest);        
    }
    
    
    /**
     * Gets most recent comments limited by: weblog specified in request and 
     * the weblog.entryDisplayCount.
     */
    public Pager getCommentsPager() {
        return new FeedCommentsPager(feedRequest);
    }    
        
    /**
     * Returns the list of tags specified in the request /?tags=foo+bar
     * @return
     */
    public List getTags() {
        return feedRequest.getTags();
    }    

    public static class FeedEntriesPager extends WeblogEntriesListPager {
        
        private WeblogFeedRequest feedRequest;
        
        public FeedEntriesPager(WeblogFeedRequest feedRequest) {
            super(URLUtilities.getWeblogFeedURL(feedRequest.getWeblog(), 
                    feedRequest.getLocale(), feedRequest.getType(),
                    feedRequest.getFormat(), null, null, null, false, true), 
                    feedRequest.getWeblog(), null, feedRequest.getWeblogCategoryName(), feedRequest.getTags(),
                    feedRequest.getLocale(), -1, feedRequest.getPage(), DEFAULT_ENTRIES);
            this.feedRequest = feedRequest;
        }
        
        protected String createURL(String url, Map params) {
            List tags = feedRequest.getTags();
            if(tags != null && tags.size() > 0) {
                params.put("tags", URLUtilities.getEncodedTagsString(tags));
            }
            String category = feedRequest.getWeblogCategoryName();
            if(category != null && category.trim().length() > 0) {
                params.put("cat", URLUtilities.encode(category));
            }  
            if(feedRequest.isExcerpts()) {
                params.put("excerpts", "true");
            }            
            return super.createURL(url, params);
        }
        
        public String getUrl() {
            return createURL(super.getUrl(), new HashMap());
        }
    }
    
    public static class FeedCommentsPager extends CommentsPager {
        
        private WeblogFeedRequest feedRequest;
        
        public FeedCommentsPager(WeblogFeedRequest feedRequest) {            
            super(URLUtilities.getWeblogFeedURL(feedRequest.getWeblog(), 
                    feedRequest.getLocale(), feedRequest.getType(),
                    feedRequest.getFormat(), null, null,
                    null, false, true), feedRequest.getLocale(), -1, feedRequest.getPage(), DEFAULT_ENTRIES);
            this.feedRequest = feedRequest;
        }
        
        protected String createURL(String url, Map params) {
            List tags = feedRequest.getTags();
            if(tags != null && tags.size() > 0) {
                params.put("tags", URLUtilities.getEncodedTagsString(tags));
            }
            String category = feedRequest.getWeblogCategoryName();
            if(category != null && category.trim().length() > 0) {
                params.put("cat", URLUtilities.encode(category));
            }  
            if(feedRequest.isExcerpts()) {
                params.put("excerpts", "true");
            }   
            return super.createURL(url, params);
        }
        
        public String getUrl() {
            return createURL(super.getUrl(), new HashMap());
        }
    }      
}
