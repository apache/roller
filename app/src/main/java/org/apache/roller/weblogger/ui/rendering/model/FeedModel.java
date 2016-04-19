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
package org.apache.roller.weblogger.ui.rendering.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogFeedRequest;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Model which provides information needed to render a feed.
 */
public class FeedModel implements Model {

    private WeblogFeedRequest feedRequest = null;

    private URLStrategy urlStrategy = null;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    protected WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    /** Template context name to be used for model */
    @Override
    public String getModelName() {
        return "model";
    }

    @Override
    public void init(Map initData) throws WebloggerException {
        this.feedRequest = (WeblogFeedRequest) initData.get("parsedRequest");

        if (feedRequest == null) {
            throw new WebloggerException("Missing WeblogFeedRequest object");
        }
    }

    /**
     * Get weblog being displayed.
     */
    public Weblog getWeblog() {
        return feedRequest.getWeblog();
    }
    
    /**
     * Get category path or name specified by request.
     */
    public String getCategoryName() {
        return feedRequest.getWeblogCategoryName();
    }
    
    /**
     * Gets most recent entries limited by: weblog and category specified in 
     * request plus the weblog.entryDisplayCount.
     */
    public Pager getWeblogEntriesPager() {
        return new FeedEntriesPager(feedRequest);        
    }
    
    
    public boolean isSiteWideFeed() {
        return feedRequest.isSiteWideFeed();
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
     */
    public List getTags() {
        return feedRequest.getTags();
    }    

    public class FeedEntriesPager extends WeblogEntriesTimePager {
        
        private WeblogFeedRequest feedRequest;
        
        public FeedEntriesPager(WeblogFeedRequest feedRequest) {
            super(weblogEntryManager, propertiesManager, urlStrategy,
                    feedRequest.isSiteWideFeed() ? null : feedRequest.getWeblog(),
                    feedRequest.isSiteWideFeed() ? null : feedRequest.getWeblogCategoryName(),
                    feedRequest.getTags(), feedRequest.getPage(),
                    propertiesManager.getIntProperty("site.newsfeeds.maxEntries"),
                    -1, feedRequest.getWeblog());
            this.feedRequest = feedRequest;
        }

        @Override
        public String getHomeLink() {
            return urlStrategy.getWeblogFeedURL(feedRequest.getWeblog(), feedRequest.getType(),
                    feedRequest.getFormat(), null, null);
        }

    }
    
    public class FeedCommentsPager extends CommentsPager {
        
        private WeblogFeedRequest feedRequest;
        
        public FeedCommentsPager(WeblogFeedRequest feedRequest) {            
            super(weblogEntryManager, urlStrategy, urlStrategy.getWeblogFeedURL(feedRequest.getWeblog(),
                    feedRequest.getType(), feedRequest.getFormat(), null, null),
                    feedRequest.isSiteWideFeed() ? null : feedRequest.getWeblog(), -1, feedRequest.getPage(),
                    propertiesManager.getIntProperty("site.newsfeeds.maxEntries"));

            this.feedRequest = feedRequest;
        }
        
        protected String createURL(String url, Map<String, String> params) {
            List tags = feedRequest.getTags();
            if(tags != null && tags.size() > 0) {
                params.put("tags", Utilities.getEncodedTagsString(tags));
            }
            String category = feedRequest.getWeblogCategoryName();
            if(category != null && category.trim().length() > 0) {
                params.put("cat", Utilities.encode(category));
            }  
            return super.createURL(url, params);
        }
        
        public String getUrl() {
            return createURL(super.getUrl(), new HashMap<>());
        }
    }      

}
