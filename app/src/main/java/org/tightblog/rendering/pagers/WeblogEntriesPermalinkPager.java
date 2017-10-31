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
package org.tightblog.rendering.pagers;

import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.util.I18nMessages;
import org.tightblog.util.Utilities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Pager for viewing one entry at a time, e.g.:
 * http://server/tightblog/myblog/entry/my-blog-article
 */
public class WeblogEntriesPermalinkPager implements WeblogEntriesPager {

    // message utils for doing i18n messages
    private I18nMessages messageUtils = null;

    // url strategy for building urls
    private URLStrategy urlStrategy = null;

    protected WeblogEntryManager weblogEntryManager;

    private Weblog weblog = null;
    private String pageLink = null;
    private String entryAnchor = null;
    private Boolean canShowDraftEntries = false;

    private WeblogEntry currEntry = null;
    private WeblogEntry nextEntry = null;
    private WeblogEntry prevEntry = null;

    // collection for the pager
    private Map<LocalDate, List<WeblogEntry>> entries = null;

    public WeblogEntriesPermalinkPager(
            WeblogEntryManager weblogEntryManager,
            URLStrategy strat,
            Weblog weblog,
            String pageLink,
            String entryAnchor,
            Boolean canShowDraftEntries) {

        this.urlStrategy = strat;
        this.weblogEntryManager = weblogEntryManager;
        this.weblog = weblog;
        this.pageLink = pageLink;
        this.entryAnchor = entryAnchor;
        this.canShowDraftEntries = canShowDraftEntries;

        // get a message utils instance to handle i18n of messages
        Locale viewLocale = weblog.getLocaleInstance();
        this.messageUtils = I18nMessages.getMessages(viewLocale);

        getEntries();
    }

    @Override
    public List<WeblogEntry> getItems() {
        return entries.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    @Override
    public Map<LocalDate, List<WeblogEntry>> getEntries() {
        if (entries == null) {
            currEntry = weblogEntryManager.getWeblogEntryByAnchor(weblog, entryAnchor);
            if (!canShowDraftEntries) {
                if (currEntry != null && currEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                    entries = new TreeMap<>();
                    entries.put(currEntry.getPubTime()
                                    .atZone(currEntry.getWeblog().getZoneId())
                                    .toLocalDate(),
                            Collections.singletonList(currEntry));
                }
            } else {
                // for weblog entry previews, here we allow unpublished entries to be shown
                if (currEntry != null) {

                    // clone the entry since we don't want to work with the real pojo
                    WeblogEntry tmpEntry = new WeblogEntry();
                    tmpEntry.setData(currEntry);

                    // for display, set the pubtime to the current time if it is not set
                    if (tmpEntry.getPubTime() == null) {
                        tmpEntry.setPubTime(Instant.now());
                    }

                    // store the entry in the collection
                    entries = new TreeMap<>();
                    entries.put(tmpEntry.getPubTime()
                            .atZone(currEntry.getWeblog().getZoneId())
                            .toLocalDate(), Collections.singletonList(tmpEntry));
                }
            }
        }
        return entries;
    }

    @Override
    public String getHomeLink() {
        return createURL(weblog, pageLink, null);
    }

    @Override
    public String getHomeLabel() {
        return messageUtils.getString("weblogEntriesPager.single.home");
    }

    @Override
    public String getNextLink() {
        if (getNextEntry() != null) {
            return createURL(weblog, pageLink, nextEntry);
        }
        return null;
    }

    @Override
    public String getNextLabel() {
        if (getNextEntry() != null) {
            String title = Utilities.truncateText(getNextEntry().getTitle(), 15, 20, "...");
            return messageUtils.getString("weblogEntriesPager.single.next", new Object[]{title});
        }
        return null;
    }

    @Override
    public String getPrevLink() {
        if (getPrevEntry() != null) {
            return createURL(weblog, pageLink, prevEntry);
        }
        return null;
    }

    @Override
    public String getPrevLabel() {
        if (getPrevEntry() != null) {
            String title = Utilities.truncateText(getPrevEntry().getTitle(), 15, 20, "...");
            return messageUtils.getString("weblogEntriesPager.single.prev", new Object[]{title});
        }
        return null;
    }

    private WeblogEntry getNextEntry() {
        if (nextEntry == null && currEntry.getPubTime() != null) {
            nextEntry = weblogEntryManager.getNextEntry(currEntry, null);
            // make sure that entry is published and not to future
            if (nextEntry != null && nextEntry.getPubTime().isAfter(Instant.now()) &&
                    nextEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                nextEntry = null;
            }
        }
        return nextEntry;
    }

    private WeblogEntry getPrevEntry() {
        if (prevEntry == null && currEntry.getPubTime() != null) {
            prevEntry = weblogEntryManager.getPreviousEntry(currEntry, null);
            // make sure that entry is published and not to future
            if (prevEntry != null && prevEntry.getPubTime().isAfter(Instant.now()) &&
                    prevEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                prevEntry = null;
            }
        }
        return prevEntry;
    }

    /**
     * Create URL that encodes pager state using most appropriate form of URL.
     */
    private String createURL(Weblog weblog, String pageLink, WeblogEntry entry) {

        if (pageLink != null) {
            return urlStrategy.getCustomPageURL(weblog, pageLink, false);
        } else if (entry != null) {
            return urlStrategy.getWeblogEntryURL(entry, true);
        }
        // home page URL
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, null, 0, false);
    }
}
