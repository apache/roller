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
import org.tightblog.business.WebloggerStaticConfig;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.util.I18nMessages;
import org.tightblog.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Pager for daily, monthly, and standard reverse chronological paging, e.g.:
 * http://server/tightblog/myblog/date/20160120 (daily)
 * http://server/tightblog/myblog/date/20160120 (monthly)
 * http://server/tightblog/myblog/ (latest, pages via ?page=X query parameter)
 */
public class WeblogEntriesTimePager implements WeblogEntriesPager {

    private static Logger log = LoggerFactory.getLogger(WeblogEntriesTimePager.class);

    // url strategy for building urls
    private URLStrategy urlStrategy;

    // message utils for doing i18n messages
    private I18nMessages messageUtils;

    private Weblog weblog;
    private String dateString;
    private String catName;
    private String tag;
    private int offset;
    private int page;
    private int maxEntries;
    private Locale viewLocale;

    private WeblogEntryManager weblogEntryManager;

    private enum PagingInterval {
        // For day-by-day paging
        DAY("day", Utilities.FORMAT_8CHARS),
        // month-by-month paging
        MONTH("month", Utilities.FORMAT_6CHARS),
        // default "all" paging: reverse chronological, no time bounds
        LATEST("latest", null),
        // special pager for site weblog (behaves mostly like LATEST, but
        // searches all weblogs and has other options)
        SITE_LATEST("latest", null);

        // used to get text for labels
        private String messageIndex;

        public String getMessageIndex() {
            return messageIndex;
        }

        private String dateFormat;

        public String getDateFormat() {
            return dateFormat;
        }

        PagingInterval(String messageIndex, String dateFormat) {
            this.messageIndex = messageIndex;
            this.dateFormat = dateFormat;
        }
    }

    private PagingInterval interval;

    private DateTimeFormatter dateFormat;

    private LocalDate timePeriod;
    private LocalDate nextTimePeriod;
    private LocalDate prevTimePeriod;

    // collection for the pager
    private Map<LocalDate, List<WeblogEntry>> entries;

    // for site blog
    private List<WeblogEntry> items;

    // are there more pages?
    private boolean more;

    // PagingInterval.SITE_LATEST allows for initializing these fields
    private int siteMaxEntries = -1;
    private LocalDate fixedStartDate;
    private Weblog siteWeblog;

    // most recent update time of current set of entries
    private Instant lastUpdated;

    /**
     * This constructor is to create site-wide (frontpage) weblog pagers, which allow more
     * flexibility in specifying blogs, maxEntries of returns, etc.
     */
    public WeblogEntriesTimePager(
            WeblogEntryManager weblogEntryManager,
            URLStrategy strat,
            Weblog weblog,
            String catName,
            String tag,
            int page,
            int maxEntries,
            int sinceDays,
            Weblog siteWeblog) {

        // initialize site-specific search fields
        this.siteMaxEntries = maxEntries;
        this.siteWeblog = siteWeblog;
        this.viewLocale = siteWeblog.getLocaleInstance();

        if (sinceDays > 0) {
            fixedStartDate = LocalDate.now().minusDays(sinceDays);
        }

        setup(PagingInterval.SITE_LATEST, weblogEntryManager, strat, weblog, null,
                catName, tag, page);
    }

    public WeblogEntriesTimePager(
            WeblogEntryManager weblogEntryManager,
            URLStrategy strat,
            Weblog weblog,
            String dateString,
            String catName,
            String tag,
            int page) {

        PagingInterval pagingInterval = PagingInterval.LATEST;

        if (dateString != null) {
            int len = dateString.length();
            if (len == 8) {
                pagingInterval = PagingInterval.DAY;
            } else if (len == 6) {
                pagingInterval = PagingInterval.MONTH;
            }
        }

        this.viewLocale = weblog.getLocaleInstance();

        setup(pagingInterval, weblogEntryManager, strat, weblog, dateString,
                catName, tag, page);
    }

    private void setup(PagingInterval pagingInterval,
                       WeblogEntryManager wem,
                       URLStrategy strat,
                       Weblog blog,
                       String dateStr,
                       String categoryName,
                       String tagName,
                       int pageNum) {

        this.interval = pagingInterval;
        this.urlStrategy = strat;
        this.weblogEntryManager = wem;
        this.weblog = blog;
        this.dateString = dateStr;
        this.catName = categoryName;
        this.messageUtils = I18nMessages.getMessages(viewLocale);
        this.tag = tagName;

        if (pageNum > 0) {
            this.page = pageNum;
        }

        if (pagingInterval == PagingInterval.SITE_LATEST) {
            maxEntries = siteMaxEntries;
        } else {
            // make sure offset, maxEntries, and page are valid
            int maxLength = WebloggerStaticConfig.getIntProperty("site.pages.maxEntries", 30);
            maxEntries = blog.getEntriesPerPage();
            if (maxEntries > maxLength) {
                maxEntries = maxLength;
            }
        }
        this.offset = maxEntries * page;

        LocalDateTime startTime = fixedStartDate == null ? null : fixedStartDate.atStartOfDay();
        LocalDateTime endTime = null;

        if (pagingInterval == PagingInterval.DAY || pagingInterval == PagingInterval.MONTH) {
            dateFormat = DateTimeFormatter.ofPattern(
                    messageUtils.getString("weblogEntriesPager." + pagingInterval.getMessageIndex() + ".dateFormat"),
                    blog.getLocaleInstance());

            timePeriod = Utilities.parseURLDate(dateStr);

            if (pagingInterval == PagingInterval.DAY) {
                startTime = timePeriod.atStartOfDay();
                endTime = timePeriod.atStartOfDay().plusDays(1).minusNanos(1);

                // determine if we should have next and prev month links, and if so, the months for them to point to
                WeblogEntry temp = wem.findNearestWeblogEntry(blog, categoryName, startTime.minusNanos(1), false);
                prevTimePeriod = weblogEntryPublishDateToLocalDate(temp);

                temp = wem.findNearestWeblogEntry(blog, categoryName, endTime.plusNanos(1), true);
                nextTimePeriod = weblogEntryPublishDateToLocalDate(temp);
            } else {
                startTime = timePeriod.withDayOfMonth(1).atStartOfDay();
                endTime = startTime.plusMonths(1).minusNanos(1);

                // determine if we should have next and prev month links, and if so, the months for them to point to
                WeblogEntry temp = wem.findNearestWeblogEntry(blog, categoryName, startTime.minusNanos(1), false);
                prevTimePeriod = firstDayOfMonthOfWeblogEntry(temp);

                temp = wem.findNearestWeblogEntry(blog, categoryName, endTime.plusNanos(1), true);
                nextTimePeriod = firstDayOfMonthOfWeblogEntry(temp);
            }
        }

        loadEntries(startTime, endTime);
    }

    private LocalDate weblogEntryPublishDateToLocalDate(WeblogEntry entry) {
        return (entry == null) ? null : entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalDate firstDayOfMonthOfWeblogEntry(WeblogEntry entry) {
        return (entry == null) ? null : weblogEntryPublishDateToLocalDate(entry).withDayOfMonth(1);
    }

    @Override
    public List<WeblogEntry> getItems() {
        if (items == null) {
            items = entries.values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
        return items;
    }

    @Override
    public Map<LocalDate, List<WeblogEntry>> getEntries() {
        return entries;
    }

    private void loadEntries(LocalDateTime startTime, LocalDateTime endTime) {

        if (entries == null) {
            entries = new TreeMap<>(Collections.reverseOrder());
            try {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                // With WESC, if any values null, equivalent to not setting the criterion.
                wesc.setWeblog(weblog);
                if (startTime != null) {
                    wesc.setStartDate(startTime.atZone(ZoneId.systemDefault()).toInstant());
                }
                if (endTime != null) {
                    wesc.setEndDate(endTime.atZone(ZoneId.systemDefault()).toInstant());
                }
                wesc.setCategoryName(catName);
                if (tag != null) {
                    wesc.setTags(Collections.singleton(tag));
                }
                wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
                wesc.setOffset(offset);
                wesc.setMaxResults(maxEntries + 1);
                Map<LocalDate, List<WeblogEntry>> mmap = weblogEntryManager.getDateToWeblogEntryMap(wesc);

                // need to wrap pojos
                int count = 0;
                for (Map.Entry<LocalDate, List<WeblogEntry>> entry : mmap.entrySet()) {
                    // now we need to go through each entry in a timePeriod
                    List<WeblogEntry> entrySubset = new ArrayList<>();
                    List<WeblogEntry> dayEntries = entry.getValue();
                    for (int i = 0; i < dayEntries.size(); i++) {
                        if (count++ < maxEntries) {
                            entrySubset.add(i, dayEntries.get(i));
                        } else {
                            more = true;
                        }
                    }

                    // done with that timePeriod, put it in the map
                    if (entrySubset.size() > 0) {
                        entries.put(entry.getKey(), entrySubset);
                    }
                }
            } catch (Exception e) {
                log.error("ERROR: getting entry month map", e);
            }
        }
    }

    @Override
    public String getHomeLink() {
        return createURL(0, 0, null);
    }

    @Override
    public String getHomeLabel() {
        return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".home");
    }

    @Override
    public String getNextLink() {
        if (more) {
            return createURL(page, 1, dateString);
        } else {
            return getNextCollectionLink();
        }
    }

    @Override
    public String getNextLabel() {
        if (more) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".next",
                    dateFormat != null ? new Object[]{dateFormat.format(timePeriod)} : null);
        } else {
            return getNextCollectionLabel();
        }
    }

    @Override
    public String getPrevLink() {
        if (page > 0) {
            return createURL(page, -1, dateString);
        } else {
            return getPrevCollectionLink();
        }
    }

    @Override
    public String getPrevLabel() {
        if (page > 0) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".prev",
                    dateFormat != null ? new Object[]{dateFormat.format(timePeriod)} : null);
        } else {
            return getPrevCollectionLabel();
        }
    }

    private String getNextCollectionLink() {
        if (nextTimePeriod != null) {
            String next = nextTimePeriod.format(DateTimeFormatter.ofPattern(interval.getDateFormat(),
                    weblog.getLocaleInstance()));
            return createURL(0, 0, next);
        }
        return null;
    }

    private String getNextCollectionLabel() {
        if (nextTimePeriod != null) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".nextCollection",
                    dateFormat != null ? new Object[]{dateFormat.format(nextTimePeriod)} : null);
        }
        return null;
    }

    private String getPrevCollectionLink() {
        if (prevTimePeriod != null) {
            String prev = prevTimePeriod.format(DateTimeFormatter.ofPattern(interval.getDateFormat(),
                    weblog.getLocaleInstance()));
            return createURL(0, 0, prev);
        }
        return null;
    }

    private String getPrevCollectionLabel() {
        if (prevTimePeriod != null) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".prevCollection",
                    dateFormat != null ? new Object[]{dateFormat.format(prevTimePeriod)} : null);
        }
        return null;
    }

    /**
     * Create URL that encodes pager state using most appropriate forms of URL.
     *
     * @param pageAdd To be added to page number, or 0 for no page number
     */
    private String createURL(
            int pageIndex,
            int pageAdd,
            String dateStr) {

        int pageNum = pageIndex + pageAdd;
        return urlStrategy.getWeblogCollectionURL((interval == PagingInterval.SITE_LATEST) ? siteWeblog : weblog,
                catName, dateStr, tag, pageNum, false);
    }

    /**
     * Get last updated time from items in pager
     */
    @SuppressWarnings("unused")
    public Instant getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by pubtime, so first might not be last updated
            List<WeblogEntry> weblogEntries = getItems();
            if (weblogEntries != null && weblogEntries.size() > 0) {
                Instant newest = weblogEntries.get(0).getUpdateTime();
                for (WeblogEntry e : weblogEntries) {
                    if (e.getUpdateTime().isAfter(newest)) {
                        newest = e.getUpdateTime();
                    }
                }
                lastUpdated = newest;
            } else {
                // no articles, so choose today's date
                lastUpdated = Instant.now();
            }
        }
        return lastUpdated;
    }

}
