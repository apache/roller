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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.domain;

import java.time.Instant;

public class CommentSearchCriteria {

    // Weblog or null to get comments on all blogs
    private Weblog weblog;
    // Entry or null to include all comments
    private WeblogEntry entry;
    // Optional weblog category name to filter by
    private String categoryName;
    // Text appearing in comment, or null for all
    private String searchText;
    // Start date or null for no restriction
    private Instant startDate;
    // End date or null for no restriction
    private Instant endDate;
    // Comment status as defined in WeblogEntryComment, or null for any
    private WeblogEntryComment.ApprovalStatus status;
    // True for results in latest-first order
    private boolean reverseChrono = true;
    // Offset into results for paging
    private int offset;
    // Max comments to return (or -1 for no limit)
    private int maxResults = -1;

    public static CommentSearchCriteria builder(WeblogEntry entry, boolean approvedOnly, boolean reverseChrono) {
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setEntry(entry);
        csc.setReverseChrono(reverseChrono);
        if (approvedOnly) {
            csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        }
        return csc;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public WeblogEntryComment.ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(WeblogEntryComment.ApprovalStatus status) {
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
