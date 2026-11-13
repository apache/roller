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

/* Created on March 8, 2023 */

package org.apache.roller.weblogger.business.search;

import java.util.List;
import java.util.Set;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;

public class SearchResultList {
    int limit;
    int offset;
    Set<String> categories;
    List<WeblogEntryWrapper> results;
    int totalHits;
    boolean more;

    /**
     * @param results    the entries on the requested page
     * @param categories the categories of those entries
     * @param limit      how many entries are on the page
     * @param offset     position of the page's first entry among all results
     * @param totalHits  how many entries matched the search, on every page
     * @param more       whether there are results after this page
     */
    public SearchResultList(
        List<WeblogEntryWrapper> results, Set<String> categories, int limit, int offset,
        int totalHits, boolean more) {
        this.results = results;
        this.categories = categories;
        this.limit = limit;
        this.offset = offset;
        this.totalHits = totalHits;
        this.more = more;
    }
    public int getLimit() {
        return limit;
    }
    public int getOffset() {
        return offset;
    }
    public List<WeblogEntryWrapper> getResults() {
        return results;
    }
    /** How many entries matched the search, counting every page. */
    public int getTotalHits() {
        return totalHits;
    }
    /** Whether there are results after this page. */
    public boolean hasMore() {
        return more;
    }
    public Set<String> getCategories() {
        return categories;
    }
}
