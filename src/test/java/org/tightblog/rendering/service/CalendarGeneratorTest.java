/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.CalendarData;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.Utilities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CalendarGeneratorTest {

    private CalendarGenerator calendarGenerator;
    private URLService mockUrlService;
    private Map<LocalDate, List<WeblogEntry>> dateToWeblogEntryMap;

    @Before
    public void initialize() {
        Locale.setDefault(Locale.US);
        mockUrlService = mock(URLService.class);
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
        WeblogEntryManager mockWeblogEntryManager = mock(WeblogEntryManager.class);
        calendarGenerator = new CalendarGenerator(mockWeblogEntryManager, mockUrlService, messages);
        dateToWeblogEntryMap = new HashMap<>();
        when(mockWeblogEntryManager.getDateToWeblogEntryMap(any())).thenReturn(dateToWeblogEntryMap);
    }

    private void initializeDateToWeblogEntryMap() {
        WeblogEntry weblogEntry1a = new WeblogEntry();
        weblogEntry1a.setTitle("A short title");
        when(mockUrlService.getWeblogEntryURL(weblogEntry1a)).thenReturn("my url 1a");
        WeblogEntry weblogEntry1b = new WeblogEntry();
        weblogEntry1b.setTitle("A very long title to ensure it is longer than 43 characters");
        when(mockUrlService.getWeblogEntryURL(weblogEntry1b)).thenReturn("my url 1b");
        WeblogEntry weblogEntry2 = new WeblogEntry();
        weblogEntry2.setTitle("Blog entry on different day");
        when(mockUrlService.getWeblogEntryURL(weblogEntry2)).thenReturn("my url 2");

        LocalDate localDate1 = LocalDate.of(1858, 10, 14);
        LocalDate localDate2 = LocalDate.of(1858, 11, 21);

        dateToWeblogEntryMap.put(localDate1, Arrays.asList(weblogEntry1a, weblogEntry1b));
        dateToWeblogEntryMap.put(localDate2, Collections.singletonList(weblogEntry2));
    }

    @Test
    public void testGetCalendarData() {
        initializeDateToWeblogEntryMap();
        Weblog weblog = new Weblog();
        weblog.setLocale("en-US");
        weblog.setHandle("testblog");
        WeblogPageRequest wpr = new WeblogPageRequest(weblog.getHandle(), null, mock(PageModel.class));
        wpr.setWeblogDate("18581014");
        wpr.setWeblog(weblog);
        wpr.setCategory("stamps");

        when(mockUrlService.getWeblogCollectionURL(weblog, "stamps", "18581014",
                null, -1)).thenReturn("WeblogCollectionURL");

        CalendarData data = calendarGenerator.getCalendarData(wpr, false);
        assertEquals("October 1858", data.getCalendarTitle());
        assertArrayEquals(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}, data.getDayOfWeekNames());
        // October 1st 1858 was on a Friday, 14th on a Thursday, 31st on a Sunday
        assertEquals("1", data.getWeek(0).getDay(5).getDayNum());
        assertEquals("14", data.getWeek(2).getDay(4).getDayNum());
        assertFalse(data.getWeek(2).getDay(4).isToday());
        assertEquals("WeblogCollectionURL", data.getWeek(2).getDay(4).getLink());
        assertNull(data.getWeek(2).getDay(4).getEntries());
        assertEquals("31", data.getWeek(5).getDay(0).getDayNum());
        assertNull(data.getWeek(5).getDay(1).getDayNum());
        assertNull(data.getWeek(5).getDay(0).getEntries());

        data = calendarGenerator.getCalendarData(wpr, true);
        assertEquals(2, data.getWeek(2).getDay(4).getEntries().size());
        assertEquals("A short title", data.getWeek(2).getDay(4).getEntries().get(0).getTitle());
        assertEquals("my url 1a", data.getWeek(2).getDay(4).getEntries().get(0).getLink());
        assertEquals("A very long title to ensure it is longer...",
                data.getWeek(2).getDay(4).getEntries().get(1).getTitle());
        assertEquals("my url 1b", data.getWeek(2).getDay(4).getEntries().get(1).getLink());
    }

    @Test
    public void testTodayMarkedInCalendar() {
        initializeDateToWeblogEntryMap();
        Weblog weblog = new Weblog();
        weblog.setLocale("EN_US");
        weblog.setHandle("testblog");
        WeblogPageRequest wpr = new WeblogPageRequest(weblog.getHandle(), null, mock(PageModel.class));
        wpr.setWeblog(weblog);
        wpr.setCategory("stamps");

        LocalDate todaysDatePre;
        LocalDate todaysDatePost;
        CalendarData data;

        // ensure test isn't running when day changes
        do {
            todaysDatePre = LocalDate.now(wpr.getWeblog().getZoneId());
            wpr.setWeblogDate(todaysDatePre.format(Utilities.YMD_FORMATTER));
            data = calendarGenerator.getCalendarData(wpr, false);
            todaysDatePost = LocalDate.now(wpr.getWeblog().getZoneId());
        } while (!todaysDatePre.equals(todaysDatePost));

        boolean found = false;
        for (int weekNum = 0; weekNum < 6; weekNum++) {
            CalendarData.Week week = data.getWeek(weekNum);

            for (int dayNum = 0; dayNum < 7; dayNum++) {
                CalendarData.Day day = week.getDay(dayNum);
                if (day.getDayNum() != null && Integer.valueOf(day.getDayNum()).equals(todaysDatePre.getDayOfMonth())) {
                    found = true;
                    assertTrue(day.isToday());
                } else {
                    assertFalse(day.isToday());
                }
            }
        }
        assertTrue(found);
    }

    @Test
    public void testComputeMonthUrl() {
        Weblog weblog = new Weblog();
        weblog.setHandle("testblog");
        WeblogPageRequest wpr = new WeblogPageRequest(weblog.getHandle(), null, mock(PageModel.class));
        wpr.setWeblog(weblog);
        wpr.setCategory("stamps");

        String test1 = calendarGenerator.computeMonthUrl(wpr, null);
        assertNull(test1);

        LocalDate localDate = LocalDate.of(1858, 10, 14);
        when(mockUrlService.getWeblogCollectionURL(weblog, "stamps", "185810",
                null, -1)).thenReturn("getWeblogCollectionURL");
        test1 = calendarGenerator.computeMonthUrl(wpr, localDate);
        assertEquals("getWeblogCollectionURL", test1);

        wpr.setCustomPageName("my custom page");
        when(mockUrlService.getCustomPageURL(weblog, "my custom page", "185810"))
                .thenReturn("getCustomPageURL");
        test1 = calendarGenerator.computeMonthUrl(wpr, localDate);
        assertEquals("getCustomPageURL", test1);

    }

    @Test
    public void testGetCalendarEntries() {
        initializeDateToWeblogEntryMap();
        LocalDate localDate1 = LocalDate.of(1858, 10, 14);
        LocalDate localDate2 = LocalDate.of(1858, 11, 21);
        LocalDate localDate3 = LocalDate.of(1858, 10, 24);

        List<CalendarData.BlogEntry> test1 = calendarGenerator.getCalendarEntries(localDate1, dateToWeblogEntryMap);
        assertEquals(2, test1.size());
        assertEquals("my url 1a", test1.get(0).getLink());
        assertEquals("my url 1b", test1.get(1).getLink());
        assertEquals("A short title", test1.get(0).getTitle());
        assertEquals("A very long title to ensure it is longer than 40 characters".substring(0, 40) + "...",
                test1.get(1).getTitle());

        List<CalendarData.BlogEntry> test2 = calendarGenerator.getCalendarEntries(localDate2, dateToWeblogEntryMap);
        assertEquals(1, test2.size());
        assertEquals("my url 2", test2.get(0).getLink());
        assertEquals("Blog entry on different day", test2.get(0).getTitle());

        List<CalendarData.BlogEntry> test3 = calendarGenerator.getCalendarEntries(localDate3, dateToWeblogEntryMap);
        assertEquals(0, test3.size());
    }

    @Test
    public void testBuildDayNames() {
        String[] dayNames = CalendarGenerator.buildDayNames(Locale.US);
        String[] dayNamesActual = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        assertArrayEquals(dayNamesActual, dayNames);

        dayNames = CalendarGenerator.buildDayNames(Locale.FRANCE);
        dayNamesActual = new String[]{"lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim."};
        assertArrayEquals(dayNamesActual, dayNames);
    }

    @Test
    public void testFirstDayOfMonthOfWeblogEntry() {
        assertNull(CalendarGenerator.firstDayOfMonthOfWeblogEntry(null));

        WeblogEntry entry = new WeblogEntry();
        Instant pubTime = LocalDate.of(1858, 10, 14).atStartOfDay().toInstant(ZoneOffset.UTC);
        entry.setPubTime(pubTime);

        LocalDate date = CalendarGenerator.firstDayOfMonthOfWeblogEntry(entry);
        assertEquals(LocalDate.of(1858, 10, 1), date);
    }
}
