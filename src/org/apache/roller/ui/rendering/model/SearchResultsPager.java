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

import java.util.Map;
import java.util.ResourceBundle;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.util.URLUtilities;


/**
 * Pager for navigating through search results.
 */
public class SearchResultsPager implements WeblogEntriesPager {
    
    private Map entries = null;
    
    private WebsiteData weblog = null;
    private String      locale = null;
    private String      query = null;
    private String      category = null;
    private int         page = 0;
    private boolean     moreResults = false;
    
    private static ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");
    
    
    public SearchResultsPager() {}
    
    public SearchResultsPager(WeblogSearchRequest searchRequest, Map entries, boolean more) {
        
        // store search results
        this.entries = entries;
        
        // data from search request
        this.weblog = searchRequest.getWeblog();
        this.query = searchRequest.getQuery();
        this.category = searchRequest.getWeblogCategoryName();
        this.locale = searchRequest.getLocale();
        this.page = searchRequest.getPageNum();
        
        // does this pager have more results?
        this.moreResults = more;
    }
    
    
    public Map getEntries() {
        return entries;
    }
    
    
    public String getHomeLink() {
        // TODO 3.0: url construction logic
        return weblog.getURL();
    }

    public String getHomeName() {
        return bundle.getString("searchPager.home");
    }

    
    public String getNextLink() {
        if(moreResults) {
            return URLUtilities.getWeblogSearchURL(weblog, locale, query, category, page + 1, false);
        }
        return null;
    }

    public String getNextName() {
        if (getNextLink() != null) {
            return bundle.getString("searchPager.next");
        }
        return null;
    }

    public String getPrevLink() {
        if(page > 0) {
            return URLUtilities.getWeblogSearchURL(weblog, locale, query, category, page - 1, false);
        }
        return null;
    }

    public String getPrevName() {
        if (getPrevLink() != null) {
            return bundle.getString("searchPager.prev");
        }
        return null;
    }

    
    public String getNextCollectionLink() {
        return null;
    }

    public String getNextCollectionName() {
        return null;
    }

    public String getPrevCollectionLink() {
        return null;
    }

    public String getPrevCollectionName() {
        return null;
    }
    
}
