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

import java.util.Locale;
import java.util.Map;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.util.WeblogSearchRequest;
import org.apache.roller.weblogger.util.I18nMessages;

/**
 * Pager for navigating through search results.
 */
public class SearchResultsPager implements WeblogEntriesPager {
    
    // message utils for doing i18n messages
    I18nMessages messageUtils = null;
    
    private Map entries = null;
    
    private Weblog weblog = null;
    private String      locale = null;
    private String      query = null;
    private String      category = null;
    private int         page = 0;
    private boolean     moreResults = false;
    
    
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
        
        // get a message utils instance to handle i18n of messages
        Locale viewLocale = null;
        if(locale != null) {
            String[] langCountry = locale.split("_");
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
    
    
    public Map getEntries() {
        return entries;
    }
    
    
    public String getHomeLink() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(weblog, locale, false);
    }

    public String getHomeName() {
        return messageUtils.getString("searchPager.home");
    }

    
    public String getNextLink() {
        if(moreResults) {
            return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogSearchURL(weblog, locale, query, category, page + 1, false);
        }
        return null;
    }

    public String getNextName() {
        if (getNextLink() != null) {
            return messageUtils.getString("searchPager.next");
        }
        return null;
    }

    public String getPrevLink() {
        if(page > 0) {
            return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogSearchURL(weblog, locale, query, category, page - 1, false);
        }
        return null;
    }

    public String getPrevName() {
        if (getPrevLink() != null) {
            return messageUtils.getString("searchPager.prev");
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
