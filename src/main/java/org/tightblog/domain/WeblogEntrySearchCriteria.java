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
package org.tightblog.domain;

import java.time.Instant;

public class WeblogEntrySearchCriteria {

    public enum SortOrder { ASCENDING, DESCENDING }

    public enum SortBy { PUBLICATION_TIME, UPDATE_TIME }

    // Weblog or null to get for all weblogs.
    private Weblog weblog;
    // User or null to get for all users.
    private User user;
    // Start date or null for no start date.
    private Instant startDate;
    // End date or null for no end date.
    private Instant endDate;
    // Category name or null for all categories.
    private String categoryName;
    // If provided, limit to entries having this tag
    private String tag;
    // Publication status of the weblog entry (DRAFT, PUBLISHED, etc.)
    private WeblogEntry.PubStatus status;
    // Text appearing in the text or summary, or null for all
    private String text;
    // Date field to sort by
    private SortBy sortBy = SortBy.PUBLICATION_TIME;
    // Order of sort
    private SortOrder sortOrder = SortOrder.DESCENDING;

    // Retrieve Permalink URLs for weblog entries?
    private boolean calculatePermalinks;

    // Offset into results for paging
    private int offset;

    private int maxResults = -1;

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public WeblogEntry.PubStatus getStatus() {
        return status;
    }

    public void setStatus(WeblogEntry.PubStatus status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public SortBy getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
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

    public boolean isCalculatePermalinks() {
        return calculatePermalinks;
    }

    public void setCalculatePermalinks(boolean calculatePermalinks) {
        this.calculatePermalinks = calculatePermalinks;
    }
}
