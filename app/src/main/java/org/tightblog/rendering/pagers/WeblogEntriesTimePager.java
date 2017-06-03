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

import org.apache.commons.lang3.StringUtils;
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
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private URLStrategy urlStrategy = null;

    // message utils for doing i18n messages
    private I18nMessages messageUtils = null;

    private Weblog weblog = null;
    private String dateString = null;
    private String catName = null;
    private String tag = null;
    private int offset = 0;
    private int page = 0;
    private int maxEntries = 0;
    private Locale viewLocale = null;

    private WeblogEntryManager weblogEntryManager;

    public enum PagingInterval {
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
    private Map<LocalDate, List<WeblogEntry>> entries = null;

    // for site blog
    private List<WeblogEntry> items = null;

    // are there more pages?
    private boolean more = false;

    // PagingInterval.SITE_LATEST allows for initializing these fields
    private int siteMaxEntries = -1;
    private LocalDate fixedStartDate = null;
    private Weblog siteWeblog = null;

    // most recent update time of current set of entries
    private Instant lastUpdated = null;

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
            PagingInterval interval,
            WeblogEntryManager weblogEntryManager,
            URLStrategy strat,
            Weblog weblog,
            String dateString,
            String catName,
            String tag,
            int page) {

        this.viewLocale = weblog.getLocaleInstance();

        setup(interval, weblogEntryManager, strat, weblog, dateString,
                catName, tag, page);
    }

    private void setup(PagingInterval interval,
                       WeblogEntryManager weblogEntryManager,
                       URLStrategy strat,
                       Weblog weblog,
                       String dateString,
                       String catName,
                       String tag,
                       int pageNum) {

        this.interval = interval;
        this.urlStrategy = strat;
        this.weblogEntryManager = weblogEntryManager;
        this.weblog = weblog;
        this.dateString = dateString;
        this.catName = catName;
        this.messageUtils = I18nMessages.getMessages(viewLocale);
        this.tag = tag;

        if (pageNum > 0) {
            this.page = pageNum;
        }

        if (interval == PagingInterval.SITE_LATEST) {
            maxEntries = siteMaxEntries;
        } else {
            // make sure offset, maxEntries, and page are valid
            int maxLength = WebloggerStaticConfig.getIntProperty("site.pages.maxEntries", 30);
            maxEntries = weblog.getEntriesPerPage();
            if (maxEntries > maxLength) {
                maxEntries = maxLength;
            }
        }
        this.offset = maxEntries * page;

        LocalDateTime startTime = fixedStartDate == null ? null : fixedStartDate.atStartOfDay();
        LocalDateTime endTime = null;

        if (interval == PagingInterval.DAY || interval == PagingInterval.MONTH) {
            ZoneId zoneId = weblog.getZoneId();

            dateFormat = DateTimeFormatter.ofPattern(
                    messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".dateFormat"),
                    weblog.getLocaleInstance());

            timePeriod = parseDate(dateString);

            ZonedDateTime weblogInitialDate = weblog.getDateCreated().atZone(weblog.getZoneId()).minusDays(1);

            if (interval == PagingInterval.DAY) {
                startTime = timePeriod.atStartOfDay();
                endTime = timePeriod.atStartOfDay().plusDays(1).minusNanos(1);

                nextTimePeriod = timePeriod.plusDays(1);
                ZonedDateTime next = ZonedDateTime.of(nextTimePeriod.atStartOfDay(), zoneId);

                if (next.isAfter(getToday())) {
                    nextTimePeriod = null;
                }

                // don't allow for paging into days before the blog's create date
                prevTimePeriod = timePeriod.minusDays(1);
                ZonedDateTime prev = ZonedDateTime.of(prevTimePeriod.atStartOfDay(), zoneId);
                if (prev.isBefore(weblogInitialDate)) {
                    prevTimePeriod = null;
                }
            } else {
                startTime = timePeriod.withDayOfMonth(1).atStartOfDay();
                endTime = startTime.plusMonths(1).minusNanos(1);

                nextTimePeriod = timePeriod.plusMonths(1);

                // don't allow for paging into months in the future
                if (YearMonth.from(nextTimePeriod).isAfter(YearMonth.from(getToday()))) {
                    nextTimePeriod = null;
                }

                // don't allow for paging into months before the blog's create date
                prevTimePeriod = timePeriod.minusMonths(1);
                if (YearMonth.from(prevTimePeriod).isBefore(YearMonth.from(weblogInitialDate).minusMonths(1))) {
                    prevTimePeriod = null;
                }
            }
        }

        loadEntries(startTime, endTime);
    }

    public List<WeblogEntry> getItems() {
        if (items == null) {
            items = entries.values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
        return items;
    }

    public Map<LocalDate, List<WeblogEntry>> getEntries() {
        return entries;
    }

    public Map<LocalDate, List<WeblogEntry>> loadEntries(LocalDateTime startTime, LocalDateTime endTime) {

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
                Map<LocalDate, List<WeblogEntry>> mmap = weblogEntryManager.getWeblogEntryObjectMap(wesc);

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
        return entries;
    }

    public String getHomeLink() {
        return createURL(0, 0, null);
    }

    public String getHomeName() {
        return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".home");
    }

    public String getNextLink() {
        if (more) {
            return createURL(page, 1, dateString);
        }
        return null;
    }

    public String getNextName() {
        if (getNextLink() != null) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".next",
                    dateFormat != null ? new Object[]{dateFormat.format(timePeriod)} : null);
        }
        return null;
    }

    public String getPrevLink() {
        if (page > 0) {
            return createURL(page, -1, dateString);
        }
        return null;
    }

    public String getPrevName() {
        if (getPrevLink() != null) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".prev",
                    dateFormat != null ? new Object[]{dateFormat.format(timePeriod)} : null);
        }
        return null;
    }

    public String getNextCollectionLink() {
        if (nextTimePeriod != null) {
            String next = nextTimePeriod.format(DateTimeFormatter.ofPattern(interval.getDateFormat(), weblog.getLocaleInstance()));
            return createURL(0, 0, next);
        }
        return null;
    }

    public String getNextCollectionName() {
        if (nextTimePeriod != null) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".nextCollection",
                    dateFormat != null ? new Object[]{dateFormat.format(nextTimePeriod)} : null);
        }
        return null;
    }

    public String getPrevCollectionLink() {
        if (prevTimePeriod != null) {
            String prev = prevTimePeriod.format(DateTimeFormatter.ofPattern(interval.getDateFormat(), weblog.getLocaleInstance()));
            return createURL(0, 0, prev);
        }
        return null;
    }

    public String getPrevCollectionName() {
        if (prevTimePeriod != null) {
            return messageUtils.getString("weblogEntriesPager." + interval.getMessageIndex() + ".prevCollection",
                    dateFormat != null ? new Object[]{dateFormat.format(prevTimePeriod)} : null);
        }
        return null;
    }

    /**
     * Return today based on current blog's timezone/locale.
     */
    protected ZonedDateTime getToday() {
        return ZonedDateTime.now(weblog.getZoneId());
    }

    /**
     * Parse data as either 6-char or 8-char format.
     */
    protected LocalDate parseDate(String dateString) {
        DateTimeFormatter dtf;
        LocalDate ldt;
        if (dateString != null && StringUtils.isNumeric(dateString) && (dateString.length() == 6 || dateString.length() == 8)) {
            if (dateString.length() == 8) {
                dtf = DateTimeFormatter.ofPattern(Utilities.FORMAT_8CHARS, weblog.getLocaleInstance());
                ldt = LocalDate.parse(dateString, dtf);
            } else {
                dtf = DateTimeFormatter.ofPattern(Utilities.FORMAT_6CHARS, weblog.getLocaleInstance());
                YearMonth tmp = YearMonth.parse(dateString, dtf);
                ldt = tmp.atDay(1);
            }
        } else {
            return null;
        }

        // make sure the requested date is not in the future
        ZonedDateTime today = getToday();
        ZonedDateTime requested = ZonedDateTime.of(ldt.atStartOfDay(), weblog.getZoneId());

        if (requested.isAfter(today)) {
            requested = today;
        }
        return requested.toLocalDate();
    }

    /**
     * Create URL that encodes pager state using most appropriate forms of URL.
     *
     * @param pageAdd To be added to page number, or 0 for no page number
     */
    protected String createURL(
            int page,
            int pageAdd,
            String dateString) {

        int pageNum = page + pageAdd;
        return urlStrategy.getWeblogCollectionURL((interval == PagingInterval.SITE_LATEST) ? siteWeblog : weblog,
                catName, dateString, tag, pageNum, false);
    }

    /**
     * Get last updated time from items in pager
     */
    public Instant getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by pubtime, so first might not be last updated
            List<WeblogEntry> items = getItems();
            if (items != null && items.size() > 0) {
                Instant newest = items.get(0).getUpdateTime();
                for (WeblogEntry e : items) {
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
