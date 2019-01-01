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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;
import org.tightblog.rendering.requests.WeblogFeedRequest;
import org.tightblog.repository.WebloggerPropertiesRepository;

import java.time.Instant;
import java.util.Map;

/**
 * Model which provides information needed to render a feed.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FeedModel implements Model {

    private WeblogFeedRequest feedRequest;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;
    private WeblogEntryListGenerator weblogEntryListGenerator;
    private WeblogEntryManager weblogEntryManager;
    private int numEntriesPerPage;

    @Autowired
    FeedModel(WebloggerPropertiesRepository webloggerPropertiesRepository,
                     WeblogEntryListGenerator weblogEntryListGenerator, WeblogEntryManager weblogEntryManager,
                     @Value("${site.feed.numEntries:20}") int numEntriesPerPage) {
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.weblogEntryListGenerator = weblogEntryListGenerator;
        this.weblogEntryManager = weblogEntryManager;
        this.numEntriesPerPage = numEntriesPerPage;
    }

    /**
     * Template context name to be used for model
     */
    @Override
    public String getModelName() {
        return "model";
    }

    @Override
    public void init(Map<String, Object> initData) {
        this.feedRequest = (WeblogFeedRequest) initData.get("parsedRequest");

        if (feedRequest == null) {
            throw new IllegalStateException("Missing WeblogFeedRequest object");
        }
    }

    /**
     * Get weblog being displayed.
     */
    public Weblog getWeblog() {
        return feedRequest.getWeblog();
    }

    /**
     * Get category path or name specified by request.
     */
    public String getCategoryName() {
        return feedRequest.getWeblogCategoryName();
    }

    /**
     * Gets most recent entries limited by: weblog and category specified in
     * request plus the weblog.entryDisplayCount.
     */
    public WeblogEntryListData getWeblogEntriesPager() {
        return weblogEntryListGenerator.getChronoPager(
                feedRequest.getWeblog(),
                null,
                feedRequest.getWeblogCategoryName(), feedRequest.getTag(),
                feedRequest.getPageNum(),
                numEntriesPerPage,
                feedRequest.isSiteWide());
    }

    public boolean isSiteWideFeed() {
        return feedRequest.isSiteWide();
    }

    /**
     * Returns the last updated date of either the weblog or the entire site, the latter if and
     * only if the weblog is a site weblog.  Useful for supplying the "updated" element of the
     * Atom feed.
     *
     * @return last update date for the feed
     */
    @SuppressWarnings("unused")
    public Instant getLastUpdated() {
        return isSiteWideFeed() ? webloggerPropertiesRepository.findOrNull().getLastWeblogChange()
                : feedRequest.getWeblog().getLastModified();
    }

    /**
     * Returns the tag specified in the request /?tag=foo
     */
    public String getTag() {
        return feedRequest.getTag();
    }

    public String getTransformedText(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getText());
    }

    public String getTransformedSummary(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getSummary());
    }

    /**
     * Transform string based on Edit Format and HTML policy
     */
    private String render(Weblog.EditFormat format, String str) {
        return weblogEntryManager.processBlogText(format, str);
    }
}
