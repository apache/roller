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
package org.tightblog.rendering.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Template;
import org.tightblog.rendering.generators.WeblogListGenerator;
import org.tightblog.rendering.pagers.WeblogEntriesPager;
import org.tightblog.rendering.pagers.WeblogEntriesTimePager;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SiteModel implements Model {

    private static Logger log = LoggerFactory.getLogger(SiteModel.class);

    private WeblogPageRequest pageRequest;

    @Autowired
    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Override
    public String getModelName() {
        return "site";
    }

    @Autowired
    private WeblogListGenerator weblogListGenerator;

    /**
     * Init page model, requires a WeblogPageRequest object.
     */
    @Override
    public void init(Map<String, Object> initData) {
        this.pageRequest = (WeblogPageRequest) initData.get("parsedRequest");

        if (pageRequest == null) {
            throw new IllegalStateException("Missing WeblogPageRequest object");
        }
    }

    /**
     * Get pager of WeblogEntry objects across all weblogs, in reverse chrono order by pubTime.
     *
     * @param sinceDays Limit to past X days in past (or -1 for no limit)
     * @param length    Max number of results to return
     */
    public WeblogEntriesPager getWeblogEntriesPager(int sinceDays, int length) {
        return new WeblogEntriesTimePager(
                weblogEntryManager,
                urlStrategy,
                null,
                null,
                null,
                pageRequest.getPageNum(),
                length,
                sinceDays,
                pageRequest.getWeblog());
    }

    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map<Character, Integer> getWeblogHandleLetterMap() {
        return weblogManager.getWeblogHandleLetterMap();
    }

    /**
     * Get pager of weblogs whose handles begin with specified letter or all weblogs
     * if letter is null.
     * @param length maximum number of weblogs to return
     */
    public WeblogListGenerator.WeblogListData getWeblogListData(Character letter, int length) {
        Template weblogPage = pageRequest.getTemplate();
        String pageLink = (weblogPage != null) ? weblogPage.getRelativePath() : null;
        String baseUrl = urlStrategy.getCustomPageURL(pageRequest.getWeblog(), pageLink, null, false);

        return weblogListGenerator.getWeblogsByLetter(baseUrl, letter, pageRequest.getPageNum(), length);
    }

    /**
     * Get list of WebsiteDisplay objects, ordered by number of hits.
     *
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param length    Max number of results to return
     * @return list of Weblog objects with just name, handle, and hitsToday fields populated
     */
    public List<WeblogListGenerator.WeblogData> getHotWeblogs(int sinceDays, int length) {
        return weblogListGenerator.getHotWeblogs(sinceDays, length);
    }
}
