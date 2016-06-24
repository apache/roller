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
package org.apache.roller.weblogger.ui.rendering.pagers;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;

/**
 *  Pager for viewing one entry at a time, e.g.:
 *  http://server/tightblog/myblog/entry/my-blog-article
 */
public class WeblogEntriesPermalinkPager implements WeblogEntriesPager {
    
    // message utils for doing i18n messages
    I18nMessages messageUtils = null;

    // url strategy for building urls
    URLStrategy urlStrategy = null;

    protected WeblogEntryManager weblogEntryManager;

    Weblog weblog = null;
    String pageLink = null;
    String entryAnchor = null;
    Boolean canShowDraftEntries = false;

    WeblogEntry currEntry = null;
    WeblogEntry nextEntry = null;
    WeblogEntry prevEntry = null;
    
    // collection for the pager
    Map<LocalDate, List<WeblogEntry>> entries = null;

    public WeblogEntriesPermalinkPager(
            WeblogEntryManager weblogEntryManager,
            URLStrategy        strat,
            Weblog             weblog,
            String             pageLink,
            String             entryAnchor,
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

    public List<WeblogEntry> getItems() {
        return entries.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

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

    public String getHomeLink() {
        return createURL(weblog, pageLink, null);
    }

    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager.single.home");
    }

    public String getNextLink() {
        if (getNextEntry() != null) {
            return createURL(weblog, pageLink, nextEntry.getAnchor());
        }
        return null;
    }

    public String getNextName() {
        if (getNextEntry() != null) {
            String title = Utilities.truncateHTML(getNextEntry().getTitle(), 15, 20, "...");
            return messageUtils.getString("weblogEntriesPager.single.next", new Object[] {title});
        }
        return null;
    }

    public String getPrevLink() {
        if (getPrevEntry() != null) {
            return createURL(weblog, pageLink, prevEntry.getAnchor());
        }
        return null;
    }
    
    
    public String getPrevName() {
        if (getPrevEntry() != null) {
            String title = Utilities.truncateHTML(getPrevEntry().getTitle(), 15, 20, "...");
            return messageUtils.getString("weblogEntriesPager.single.prev", new Object[] {title});
        }
        return null;
    }


    public String getNextCollectionLink() {
        return null;
    }


    public String getNextCollectionName() {
        return null;
    }


    public String getPrevCollectionLink() {
        return null;
    }


    public String getPrevCollectionName() {
        return null;
    }

    private WeblogEntry getNextEntry() {
        if (nextEntry == null && currEntry.getPubTime() != null) {
            nextEntry = weblogEntryManager.getNextEntry(currEntry, null);
            // make sure that entry is published and not to future
            if (nextEntry != null && nextEntry.getPubTime().isAfter(Instant.now())
                    && nextEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                nextEntry = null;
            }
        }
        return nextEntry;
    }
    
    
    private WeblogEntry getPrevEntry() {
        if (prevEntry == null && currEntry.getPubTime() != null) {
            prevEntry = weblogEntryManager.getPreviousEntry(currEntry, null);
            // make sure that entry is published and not to future
            if (prevEntry != null && prevEntry.getPubTime().isAfter(Instant.now())
                    && prevEntry.getStatus().equals(PubStatus.PUBLISHED)) {
                prevEntry = null;
            }
        }
        return prevEntry;
    }

    /**
     * Create URL that encodes pager state using most appropriate form of URL.
     */
    protected String createURL(Weblog website, String pageLink, String entryAnchor) {

        if (pageLink != null) {
            return urlStrategy.getWeblogPageURL(website, null, pageLink, entryAnchor, null, null, null, 0, false);
        } else if (entryAnchor != null) {
            return urlStrategy.getWeblogEntryURL(website, entryAnchor, true);
        }
        // home page URL
        return urlStrategy.getWeblogCollectionURL(website, null, null, null, 0, false);
    }
}
