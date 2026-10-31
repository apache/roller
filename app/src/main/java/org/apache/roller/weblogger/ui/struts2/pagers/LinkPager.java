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
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * Paging through query results with links: first, previous (newer), next (older)
 * and last page. Page numbers are zero-based in links and one-based for display.
 */
public class LinkPager<T> {

    // the collection for the pager
    private final List<T> items;

    // base url for the pager
    private final String baseUrl;

    // what page we are on
    private final int pageNum;

    // rows matching the query across all pages
    private final long totalCount;

    private final int pageSize;


    public LinkPager(String url, int page, List<T> items, long totalCount, int pageSize) {
        this.baseUrl = url;
        this.pageNum = page;
        this.items = items;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
    }


    public String getFirstLink() {
        return pageNum > 0 ? pageLink(0) : null;
    }

    public String getPrevLink() {
        return pageNum > 0 ? pageLink(pageNum - 1) : null;
    }

    public String getNextLink() {
        return isMoreItems() ? pageLink(pageNum + 1) : null;
    }

    public String getLastLink() {
        return isMoreItems() ? pageLink(getPageCount() - 1) : null;
    }


    private String pageLink(int page) {
        Map<String, String> params = new HashMap<>();
        params.put("bean.page", ""+page);
        String qString = URLUtilities.getQueryString(params);

        if(baseUrl.indexOf('?') != -1) {
            // if base url already has params them just append our query string
            return baseUrl + "&" + qString.substring(1);
        } else {
            return baseUrl + qString;
        }
    }


    public List<T> getItems() {
        return items;
    }

    public boolean isMoreItems() {
        return (long) (pageNum + 1) * pageSize < totalCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    /** One-based page number, for display. */
    public int getPageNumber() {
        return pageNum + 1;
    }

    /** Number of pages; at least 1, so an empty result reads "Page 1 of 1". */
    public int getPageCount() {
        return (int) Math.max(1, (totalCount + pageSize - 1) / pageSize);
    }

}
