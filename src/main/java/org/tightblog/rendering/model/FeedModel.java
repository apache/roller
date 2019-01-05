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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.tightblog.config.DynamicProperties;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;

import java.time.Instant;

/**
 * Model which provides services needed to render a feed.
 */
@Component
@EnableConfigurationProperties(DynamicProperties.class)
public class FeedModel {

    private WeblogEntryListGenerator weblogEntryListGenerator;
    private WeblogEntryManager weblogEntryManager;
    private URLService urlService;
    private DynamicProperties dp;
    private int numEntriesPerPage;
    private String systemVersion;

    @Autowired
    FeedModel(WeblogEntryListGenerator weblogEntryListGenerator, WeblogEntryManager weblogEntryManager,
                    URLService urlService,
                    DynamicProperties dp, @Value("${weblogger.version:Unknown}") String systemVersion,
                    @Value("${site.feed.numEntries:20}") int numEntriesPerPage) {
        this.weblogEntryListGenerator = weblogEntryListGenerator;
        this.weblogEntryManager = weblogEntryManager;
        this.urlService = urlService;
        this.dp = dp;
        this.systemVersion = systemVersion;
        this.numEntriesPerPage = numEntriesPerPage;
    }

    /**
     * Gets most recent entries filtered by params provided in WeblogFeedRequest.
     */
    public WeblogEntryListData getWeblogEntriesPager(Weblog weblog, String categoryName, String tag,
                                                     int pageNum, boolean siteWide) {
        return weblogEntryListGenerator.getChronoPager(weblog, null, categoryName,
                tag, pageNum, numEntriesPerPage, siteWide);
    }

    public Instant getLastSitewideChange() {
        return dp.getLastSitewideChange();
    }

    /**
     * Transform blog text based on Edit Format and HTML policy
     */
    public String render(Weblog.EditFormat format, String str) {
        return weblogEntryManager.processBlogText(format, str);
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public URLService getURLService() {
        return urlService;
    }
}
