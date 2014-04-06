/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.pojos;

import java.util.Date;

public class CommentSearchCriteria {

    // TODO: Change status to an enum (either here or in WeblogEntryComment)

    // Weblog or null to get comments on all blogs
    private Weblog weblog;
    // Entry or null to include all comments
    private WeblogEntry entry;
    // Text appearing in comment, or null for all
    private String searchText;
    // Start date or null for no restriction
    private Date startDate;
    // End date or null for no restriction
    private Date endDate;
    // Comment status as defined in WeblogEntryComment, or null for any
    private String status;
    // True for results in reverse chrono order
    private boolean reverseChrono = false;
    // Offset into results for paging
    private int offset = 0;
    // Max comments to return (or -1 for no limit)
    private int maxResults = -1;

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public WeblogEntry getEntry() {
        return entry;
    }

    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isReverseChrono() {
        return reverseChrono;
    }

    public void setReverseChrono(boolean reverseChrono) {
        this.reverseChrono = reverseChrono;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

}
