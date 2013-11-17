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

package org.apache.roller.weblogger.ui.struts2.pagers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * Paging through a collection of entries.
 */
public class EntriesPager {
    
    // the collection for the pager
    private final List<WeblogEntry> items;
    
    // base url for the pager
    private final String baseUrl;
    
    // what page we are on
    private final int pageNum;
    
    // are there more items?
    private final boolean moreItems;
    
    
    public EntriesPager(String url, int page, List<WeblogEntry> entries, boolean hasMore) {
        this.baseUrl = url;
        this.pageNum = page;
        this.items = entries;
        this.moreItems = hasMore;
    }
    
    
    public String getNextLink() {
        if(isMoreItems()) {
            int nextPage = pageNum + 1;
            Map<String, String> params = new HashMap<String, String>();
            params.put("bean.page", ""+nextPage);
            return createURL(baseUrl, params);
        }
        return null;
    }
    
    
    public String getPrevLink() {
        if (pageNum > 0) {
            int prevPage = pageNum - 1;
            Map<String, String> params = new HashMap();
            params.put("bean.page", ""+prevPage);
            return createURL(baseUrl, params);
        }
        return null;
    }
    
    
    private String createURL(String base, Map<String, String> params) {
        String qString = URLUtilities.getQueryString(params);
        
        if(base.indexOf('?') != -1) {
            // if base url already has params them just append our query string
            return base + "&" + qString.substring(1);
        } else {
            return base + qString;
        }
    }
    
    
    public List<WeblogEntry> getItems() {
        return items;
    }

    public boolean isMoreItems() {
        return moreItems;
    }
    
}
