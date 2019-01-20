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
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.generators.WeblogListGenerator;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;
import org.tightblog.rendering.requests.WeblogPageRequest;

import java.util.List;
import java.util.Map;

/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
public class SiteModel {

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private WeblogListGenerator weblogListGenerator;

    public void setWeblogListGenerator(WeblogListGenerator weblogListGenerator) {
        this.weblogListGenerator = weblogListGenerator;
    }

    @Autowired
    private WeblogEntryListGenerator weblogEntryListGenerator;

    public void setWeblogEntryListGenerator(WeblogEntryListGenerator weblogEntryListGenerator) {
        this.weblogEntryListGenerator = weblogEntryListGenerator;
    }

    @Autowired
    private URLService urlService;

    public void setUrlService(URLService urlService) {
        this.urlService = urlService;
    }

    @Autowired
    private WeblogPageRequest pageRequest;

    @Autowired
    public SiteModel(WeblogPageRequest pageRequest) {
        this.pageRequest = pageRequest;
    }

    /**
     * Get pager of WeblogEntry objects across all weblogs, in reverse chrono order by pubTime.
     * @param length Max number of results to return
     */
    public WeblogEntryListData getWeblogEntriesPager(int length) {
        return weblogEntryListGenerator.getChronoPager(
                pageRequest.getWeblog(),
                null,
                null,
                null,
                pageRequest.getPageNum(),
                Math.min(100, length),
                true);
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
        String baseUrl = urlService.getCustomPageURL(pageRequest.getWeblog(), pageLink, null);

        return weblogListGenerator.getWeblogsByLetter(baseUrl, letter, pageRequest.getPageNum(), length);
    }

    /**
     * Get list of WebsiteDisplay objects, ordered by number of hits.
     *
     * @param length Max number of results to return
     * @return list of WeblogListGenerator.WeblogData objects in descending order of hit counts
     */
    public List<WeblogListGenerator.WeblogData> getHotWeblogs(int length) {
        return weblogListGenerator.getHotWeblogs(length);
    }

    public String getWeblogHome(Weblog blog) {
        return urlService.getWeblogURL(blog);
    }
}
