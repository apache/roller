/*
   Copyright 2018 the original author or authors.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.util.Utilities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class WeblogEntryListGenerator {

    private WeblogEntryManager weblogEntryManager;
    private URLService urlService;
    private MessageSource messages;

    @Autowired
    WeblogEntryListGenerator(WeblogEntryManager weblogEntryManager, URLService urlService,
                                    MessageSource messages) {
        this.weblogEntryManager = weblogEntryManager;
        this.urlService = urlService;
        this.messages = messages;
    }

    public WeblogEntryListData getSearchPager(Weblog weblog, String searchPhrase, String category, int page,
                                              Map<LocalDate, List<WeblogEntry>> entries, boolean moreResults) {

        WeblogEntryListData data = new WeblogEntryListData();

        // store search results
        data.entries = entries;

        if (page > 0) {
            data.nextLink = urlService.getWeblogSearchURL(weblog, searchPhrase, category, page - 1);
            data.nextLabel = messages.getMessage("weblogEntriesPager.newer", null, weblog.getLocaleInstance());
        }

        if (moreResults) {
            data.prevLink = urlService.getWeblogSearchURL(weblog, searchPhrase, category, page + 1);
            data.prevLabel = messages.getMessage("weblogEntriesPager.prior", null, weblog.getLocaleInstance());
        }

        return data;
    }

    static LocalDate instantToWeblogLocalDate(Weblog weblog, Instant instant) {
        return instant.atZone(weblog.getZoneId()).toLocalDate();
    }

    public WeblogEntryListData getPermalinkPager(Weblog weblog, String entryAnchor, Boolean canShowUnpublishedEntries) {
        WeblogEntryListData data = new WeblogEntryListData();

        WeblogEntry currEntry = weblogEntryManager.getWeblogEntryByAnchor(weblog, entryAnchor);
        if (currEntry != null) {
            if (canShowUnpublishedEntries || WeblogEntry.PubStatus.PUBLISHED.equals(currEntry.getStatus())) {
                data.entries = new HashMap<>();
                data.entries.put(instantToWeblogLocalDate(currEntry.getWeblog(),
                        currEntry.getPubTime() == null ? Instant.now() : currEntry.getPubTime()),
                        Collections.singletonList(currEntry));

                // make sure that entry is published and not to future
                WeblogEntry nextEntry = weblogEntryManager.getNextPublishedEntry(currEntry);
                if (nextEntry != null && nextEntry.getPubTime().isBefore(Instant.now())) {
                    data.nextLink = urlService.getWeblogEntryURL(nextEntry);
                    data.nextLabel = messages.getMessage("weblogEntriesPager.single.next", new Object[]{nextEntry.getTitle()},
                            weblog.getLocaleInstance());
                }

                // make sure that entry is published and not to future
                WeblogEntry prevEntry = weblogEntryManager.getPreviousPublishedEntry(currEntry);
                if (prevEntry != null && prevEntry.getPubTime().isBefore(Instant.now())) {
                    data.prevLink = urlService.getWeblogEntryURL(prevEntry);
                    data.prevLabel = messages.getMessage("weblogEntriesPager.single.prev", new Object[]{prevEntry.getTitle()},
                            weblog.getLocaleInstance());
                }
            }
        }

        return data;
    }

    public WeblogEntryListData getChronoPager(Weblog weblog, String dateString, String catName, String tag,
                                              int pageNum, int maxEntries, boolean siteWideSearch) {

        WeblogEntryListData data = new WeblogEntryListData();
        // first page is 0, higher page number means more in the past.
        int page = Math.max(pageNum, 0);

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        // month (YYYYMM) or day (YYYYMMDD) searches have specific start and end times
        if (dateString != null) {
            int len = dateString.length();
            if (len == 6 || len == 8) {
                LocalDate timePeriod = Utilities.parseURLDate(dateString);

                if (len == 6) {
                    startTime = timePeriod.withDayOfMonth(1).atStartOfDay();
                    endTime = startTime.plusMonths(1).minusNanos(1);
                } else {
                    startTime = timePeriod.atStartOfDay();
                    endTime = timePeriod.atStartOfDay().plusDays(1).minusNanos(1);
                }
            }
        }

        data.entries = new TreeMap<>(Collections.reverseOrder());

        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        if (!siteWideSearch) {
            wesc.setWeblog(weblog);
        }
        if (startTime != null) {
            wesc.setStartDate(startTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        if (endTime != null) {
            wesc.setEndDate(endTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        wesc.setCategoryName(catName);
        wesc.setTag(tag);
        wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        wesc.setOffset(maxEntries * page);
        wesc.setMaxResults(maxEntries + 1);
        Map<LocalDate, List<WeblogEntry>> mmap = weblogEntryManager.getDateToWeblogEntryMap(wesc);

        boolean moreResults = false;
        int count = 0;
        for (Map.Entry<LocalDate, List<WeblogEntry>> entry : mmap.entrySet()) {
            // now we need to go through each entry in a timePeriod
            List<WeblogEntry> entrySubset = new ArrayList<>();
            List<WeblogEntry> dayEntries = entry.getValue();
            for (int i = 0; i < dayEntries.size(); i++) {
                if (count++ < maxEntries) {
                    entrySubset.add(i, dayEntries.get(i));
                } else {
                    moreResults = true;
                }
            }

            // done with that timePeriod, put it in the map
            if (entrySubset.size() > 0) {
                data.entries.put(entry.getKey(), entrySubset);
            }
        }

        if (page > 0) {
            data.nextLink = urlService.getWeblogCollectionURL(weblog, catName, dateString, tag,
                    page - 1);
            data.nextLabel = messages.getMessage("weblogEntriesPager.newer", null, weblog.getLocaleInstance());
        }

        if (moreResults) {
            data.prevLink = urlService.getWeblogCollectionURL(weblog, catName, dateString, tag,
                    page + 1);
            data.prevLabel = messages.getMessage("weblogEntriesPager.prior", null, weblog.getLocaleInstance());
        }

        return data;
    }

    public static class WeblogEntryListData {
        private String nextLink;
        private String prevLink;
        private String nextLabel;
        private String prevLabel;

        private Map<LocalDate, List<WeblogEntry>> entries;
        private List<WeblogEntry> items;

        public String getNextLink() {
            return nextLink;
        }

        public String getPrevLink() {
            return prevLink;
        }

        public String getNextLabel() {
            return nextLabel;
        }

        public String getPrevLabel() {
            return prevLabel;
        }

        public Map<LocalDate, List<WeblogEntry>> getEntries() {
            return entries;
        }

        public List<WeblogEntry> getEntriesAsList() {
            if (items == null && entries != null) {
                items = entries.values().stream().flatMap(List::stream).collect(Collectors.toList());
            }
            return items;
        }
    }
}
