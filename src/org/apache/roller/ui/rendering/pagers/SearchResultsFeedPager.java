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

package org.apache.roller.ui.rendering.pagers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.util.URLUtilities;


/**
 * Pager for navigating through search results feeds.
 */
public class SearchResultsFeedPager extends AbstractPager {
    
    private List entries = null;
    
    private WebsiteData weblog = null;
    private boolean     moreResults = false;
    
    private WeblogFeedRequest feedRequest = null;
    
    private String url = null;
    
    private static ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");
            
    public SearchResultsFeedPager(String baseUrl, int pageNum,
            WeblogFeedRequest feedRequest, List entries, boolean more) {
        
        super(baseUrl, pageNum);
        
        this.url = baseUrl;
        
        this.feedRequest = feedRequest;
        
        // store search results
        this.entries = entries;
        
        // data from search request
        this.weblog = feedRequest.getWeblog();
        
        // does this pager have more results?
        this.moreResults = more;
    }
    
    public List getItems() {
        return this.entries;
    }
    
    public boolean hasMoreItems() {
        return this.moreResults;
    }
    
    public String getHomeLink() {
        return URLUtilities.getWeblogURL(weblog, weblog.getLocale(), false);
    }

    public String getHomeName() {
        return bundle.getString("searchPager.home");
    }  
    
    protected String createURL(String url, Map params) {
        String category = feedRequest.getWeblogCategoryName();
        if(category != null && category.trim().length() > 0) {
            params.put("cat", URLUtilities.encode(category));
        }
        String term = feedRequest.getTerm();
        if(term != null && term.trim().length() > 0) {
            params.put("q", URLUtilities.encode(term.trim()));
        }        
        return super.createURL(url, params);
    }
    
    public String getUrl() {
        return createURL(url, new HashMap());
    }
}
