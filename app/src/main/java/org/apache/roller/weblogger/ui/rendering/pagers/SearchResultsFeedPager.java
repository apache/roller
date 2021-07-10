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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.ui.rendering.util.WeblogFeedRequest;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * Pager for navigating through search results feeds.
 */
public class SearchResultsFeedPager extends AbstractPager<WeblogEntryWrapper> {
    
    // message utils for doing i18n messages
    I18nMessages messageUtils = null;
    
    private final List<WeblogEntryWrapper> entries;
    
    private final Weblog weblog;
    private final boolean moreResults;
    
    private final WeblogFeedRequest feedRequest;
    
    private final String url;
    
            
    public SearchResultsFeedPager(URLStrategy strat, String baseUrl, int pageNum,
            WeblogFeedRequest feedRequest, List<WeblogEntryWrapper> entries, boolean more) {
        
        super(strat, baseUrl, pageNum);
        
        this.url = baseUrl;
        
        this.feedRequest = feedRequest;
        
        // store search results
        this.entries = entries;
        
        // data from search request
        this.weblog = feedRequest.getWeblog();
        
        // does this pager have more results?
        this.moreResults = more;
        
        // get a message utils instance to handle i18n of messages
        Locale viewLocale = null;
        if(feedRequest.getLocale() != null) {
            String[] langCountry = feedRequest.getLocale().split("_");
            if(langCountry.length == 1) {
                viewLocale = new Locale(langCountry[0]);
            } else if(langCountry.length == 2) {
                viewLocale = new Locale(langCountry[0], langCountry[1]);
            }
        } else {
            viewLocale = weblog.getLocaleInstance();
        }
        this.messageUtils = I18nMessages.getMessages(viewLocale);
    }
    
    @Override
    public List<WeblogEntryWrapper> getItems() {
        return this.entries;
    }
    
    @Override
    public boolean hasMoreItems() {
        return this.moreResults;
    }
    
    @Override
    public String getHomeLink() {
        return urlStrategy.getWeblogURL(weblog, weblog.getLocale(), false);
    }

    @Override
    public String getHomeName() {
        return messageUtils.getString("searchPager.home");
    }  
    
    @Override
    protected String createURL(String url, Map<String, String> params) {
        String category = feedRequest.getWeblogCategoryName();
        if(category != null && !category.isBlank()) {
            params.put("cat", URLUtilities.encode(category));
        }
        String term = feedRequest.getTerm();
        if(term != null && !term.isBlank()) {
            params.put("q", URLUtilities.encode(term.trim()));
        }     
        List<String> tags = feedRequest.getTags();
        if(tags != null && !tags.isEmpty()) {
            params.put("tags", URLUtilities.getEncodedTagsString(tags));
        }
        if(feedRequest.isExcerpts()) {
            params.put("excerpts", "true");
        }        
        return super.createURL(url, params);
    }
    
    @Override
    public String getUrl() {
        return createURL(url, new HashMap<>());
    }
}
