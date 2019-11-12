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
package org.tightblog.rendering.requests;

import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.model.FeedModel;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

/**
 * Represents a request for a TightBlog weblog feed.
 */
public final class WeblogFeedRequest extends WeblogRequest {

    private String categoryName;
    private String tag;
    private FeedModel feedModel;

    public WeblogFeedRequest(FeedModel feedModel) {
        this.feedModel = feedModel;
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

    public WeblogEntryListGenerator.WeblogEntryListData getWeblogEntriesPager() {
        return feedModel.getWeblogEntryListGenerator().getChronoPager(weblog,
                null, categoryName, tag, pageNum, feedModel.getNumEntriesPerPage(),
                isSiteWide());
    }

    public String getTransformedText(WeblogEntry entry) {
        return feedModel.render(entry.getEditFormat(), entry.getText());
    }

    public String getTransformedSummary(WeblogEntry entry) {
        return feedModel.render(entry.getEditFormat(), entry.getSummary());
    }

    /**
     * Supplies the "updated" element of the Atom feed, either last updated date of the
     * blog or (for the site weblog) the entire site.
     */
    public String getLastUpdated() {
        return formatIsoOffsetDateTime(isSiteWide() ?
                feedModel.getLastSitewideChange() : weblog.getLastModified());
    }

    public String formatIsoOffsetDateTime(Temporal dt) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(weblog.getZoneId()).format(dt);
    }

    public String getSystemVersion() {
        return feedModel.getSystemVersion();
    }

    public String getAtomFeedURL() {
        if (tag != null) {
            return feedModel.getURLService().getAtomFeedURLForTag(weblog, tag);
        } else if (categoryName != null) {
            return feedModel.getURLService().getAtomFeedURLForCategory(weblog, categoryName);
        }
        return feedModel.getURLService().getAtomFeedURL(weblog);
    }

    public String getAlternateURL() {
        return feedModel.getURLService().getWeblogURL(weblog);
    }

    public String getWeblogEntryURL(WeblogEntry entry) {
        return feedModel.getURLService().getWeblogEntryURL(entry);
    }
}
