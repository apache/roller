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
package org.apache.roller.weblogger.ui.rendering.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.rendering.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.rendering.pagers.Pager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogsPager;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page model that provides access to site-wide users, weblogs and entries.
 */
public class SiteModel implements Model {

    private static Logger log = LoggerFactory.getLogger(SiteModel.class);

    private WeblogPageRequest pageRequest = null;
    private String pageLink = null;
    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Override
    public String getModelName() {
        return "site";
    }

    /**
     * Init page model, requires a WeblogPageRequest object.
     */
    @Override
    public void init(Map initData) {
        this.pageRequest = (WeblogPageRequest) initData.get("parsedRequest");

        if (pageRequest == null) {
            throw new IllegalStateException("Missing WeblogPageRequest object");
        }

        Template weblogPage = pageRequest.getWeblogTemplate();
        pageLink = (weblogPage != null) ? weblogPage.getRelativePath() : null;
    }

    /**
     * Get pager of WeblogEntry objects across all weblogs, in reverse chrono order by pubTime.
     *
     * @param sinceDays Limit to past X days in past (or -1 for no limit)
     * @param length    Max number of results to return
     */
    public Pager getWeblogEntriesPager(int sinceDays, int length) {
        return getWeblogEntriesPager(null, null, sinceDays, length);
    }

    /**
     * @param queryWeblog Restrict to this weblog
     * @param cat         Restrict to this category
     * @param sinceDays   Limit to past X days in past (or -1 for no limit)
     * @param length      Max number of results to return
     */
    public Pager getWeblogEntriesPager(Weblog queryWeblog, String cat, int sinceDays, int length) {
        return new WeblogEntriesTimePager(
                weblogEntryManager,
                urlStrategy,
                queryWeblog,
                cat,
                pageRequest.getTag(),
                pageRequest.getPageNum(),
                length,
                sinceDays,
                pageRequest.getWeblog());
    }

    /*
     * Get pager of most recent Comment objects across all weblogs,
     * in reverse chrono order by postTime.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public Pager getCommentsPager(int sinceDays, int length) {

        String pagerUrl = urlStrategy.getWeblogPageURL(pageRequest.getWeblog(), null,
                pageLink, null, null, null, null, 0, false);

        return new CommentsPager(
                weblogEntryManager,
                urlStrategy,
                pagerUrl,
                null,
                null,
                sinceDays,
                pageRequest.getPageNum(),
                length);
    }

    /**
     * Get pager of weblogs whose handles begin with specified letter
     */
    public Pager getWeblogsByLetterPager(String letter, int length) {

        String pagerUrl = urlStrategy.getWeblogPageURL(pageRequest.getWeblog(), null,
                pageLink, null, null, null, null, 0, false);

        if (letter != null && StringUtils.isEmpty(letter)) {
            letter = null;
        }

        return new WeblogsPager(weblogManager,
                urlStrategy,
                pagerUrl,
                letter,
                pageRequest.getPageNum(),
                length);
    }

    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map getWeblogHandleLetterMap() {
        Map results = new HashMap();
        try {
            results = weblogManager.getWeblogHandleLetterMap();
        } catch (Exception e) {
            log.error("ERROR: fetching weblog handle letter map", e);
        }
        return results;
    }

    /**
     * Get Website object by handle
     */
    public Weblog getWeblog(String handle) {
        Weblog weblog = null;
        try {
            weblog = weblogManager.getWeblogByHandle(handle);
        } catch (Exception e) {
            log.error("ERROR: fetching weblog", e);
        }
        return weblog;
    }

    /**
     * Get list of WebsiteDisplay objects, ordered by number of hits.
     *
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param length    Max number of results to return
     * @return list of Weblog objects with just name, handle, and hitsToday fields populated
     */
    public List<Weblog> getHotWeblogs(int sinceDays, int length) {

        List<Weblog> results = new ArrayList<>();
        try {
            List<Weblog> weblogs = weblogManager.getHotWeblogs(sinceDays, 0, length);

            for (Weblog weblog : weblogs) {
                Weblog copy = new Weblog();
                copy.setName(weblog.getName());
                copy.setHandle(weblog.getHandle());
                copy.setHitsToday(weblog.getHitsToday());
                results.add(copy);
            }
        } catch (Exception e) {
            log.error("ERROR: fetching hot weblog list", e);
        }

        return results;
    }

}
