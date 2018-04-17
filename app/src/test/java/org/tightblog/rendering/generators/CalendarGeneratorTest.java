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
package org.tightblog.rendering.generators;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.CalendarData;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
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
    private URLStrategy mockUrlStrategy;
    private Map<LocalDate, List<WeblogEntry>> dateToWeblogEntryMap;

    @Before
    public void initialize() {
        calendarGenerator = new CalendarGenerator();
        mockUrlStrategy = mock(URLStrategy.class);
        WeblogEntryManager mockWeblogEntryManager = mock(WeblogEntryManager.class);
        calendarGenerator.setUrlStrategy(mockUrlStrategy);
        calendarGenerator.setWeblogEntryManager(mockWeblogEntryManager);
        dateToWeblogEntryMap = new HashMap<>();
        when(mockWeblogEntryManager.getDateToWeblogEntryMap(any())).thenReturn(dateToWeblogEntryMap);
    }

    private void initializeDateToWeblogEntryMap() {
        WeblogEntry weblogEntry1a = new WeblogEntry();
        weblogEntry1a.setTitle("A short title");
        when(mockUrlStrategy.getWeblogEntryURL(weblogEntry1a, true)).thenReturn("my url 1a");
        WeblogEntry weblogEntry1b = new WeblogEntry();
        weblogEntry1b.setTitle("A very long title to ensure it is longer than 43 characters");
        when(mockUrlStrategy.getWeblogEntryURL(weblogEntry1b, true)).thenReturn("my url 1b");
        WeblogEntry weblogEntry2 = new WeblogEntry();
        weblogEntry2.setTitle("Blog entry on different day");
        when(mockUrlStrategy.getWeblogEntryURL(weblogEntry2, true)).thenReturn("my url 2");

        LocalDate localDate1 = LocalDate.of(1858, 10, 14);
        LocalDate localDate2 = LocalDate.of(1858, 11, 21);

        dateToWeblogEntryMap.put(localDate1, Arrays.asList(weblogEntry1a, weblogEntry1b));
        dateToWeblogEntryMap.put(localDate2, Collections.singletonList(weblogEntry2));
    }

    @Test
    public void testGetCalendarData() {
        initializeDateToWeblogEntryMap();
        Weblog weblog = new Weblog();
        weblog.setLocale("EN_US");
        WeblogPageRequest wpr = new WeblogPageRequest();
        wpr.setWeblogDate("18581014");
        wpr.setWeblog(weblog);
        wpr.setWeblogCategoryName("stamps");

        when(mockUrlStrategy.getWeblogCollectionURL(weblog, "stamps", "18581014",
                null, -1, false)).thenReturn("WeblogCollectionURL");

        CalendarData data = calendarGenerator.getCalendarData(wpr, false);
        assertEquals("October 1858", data.getCalendarTitle());
        assertArrayEquals(new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}, data.getDayOfWeekNames());
        assertEquals(6, data.getWeeks().length);
        // October 1st 1858 was on a Friday, 14th on a Thursday, 31st on a Sunday
        assertEquals("1", data.getWeeks()[0].getDays()[5].getDayNum());
        assertEquals("14", data.getWeeks()[2].getDays()[4].getDayNum());
        assertFalse(data.getWeeks()[2].getDays()[4].isToday());
        assertEquals("WeblogCollectionURL", data.getWeeks()[2].getDays()[4].getLink());
        assertNull(data.getWeeks()[2].getDays()[4].getEntries());
        assertEquals("31", data.getWeeks()[5].getDays()[0].getDayNum());
        assertNull(data.getWeeks()[5].getDays()[1].getDayNum());
        assertNull(data.getWeeks()[5].getDays()[0].getEntries());

        data = calendarGenerator.getCalendarData(wpr, true);
        assertEquals(2, data.getWeeks()[2].getDays()[4].getEntries().size());
        assertEquals("A short title", data.getWeeks()[2].getDays()[4].getEntries().get(0).getTitle());
        assertEquals("my url 1a", data.getWeeks()[2].getDays()[4].getEntries().get(0).getLink());
        assertEquals("A very long title to ensure it is longer...",
                data.getWeeks()[2].getDays()[4].getEntries().get(1).getTitle());
        assertEquals("my url 1b", data.getWeeks()[2].getDays()[4].getEntries().get(1).getLink());
    }

    @Test
    public void testTodayMarkedInCalendar() {
        initializeDateToWeblogEntryMap();
        Weblog weblog = new Weblog();
        weblog.setLocale("EN_US");
        WeblogPageRequest wpr = new WeblogPageRequest();
        wpr.setWeblog(weblog);
        wpr.setWeblogCategoryName("stamps");

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
        for (CalendarData.Week week : data.getWeeks()) {
            for (CalendarData.Day day : week.getDays()) {
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
        WeblogPageRequest wpr = new WeblogPageRequest();
        wpr.setWeblog(weblog);
        wpr.setWeblogCategoryName("stamps");

        String test1 = calendarGenerator.computeMonthUrl(wpr, null);
        assertNull(test1);

        LocalDate localDate = LocalDate.of(1858, 10, 14);
        when(mockUrlStrategy.getWeblogCollectionURL(weblog, "stamps", "185810",
                null, -1, false)).thenReturn("getWeblogCollectionURL");
        test1 = calendarGenerator.computeMonthUrl(wpr, localDate);
        assertEquals("getWeblogCollectionURL", test1);

        wpr.setCustomPageName("my custom page");
        when(mockUrlStrategy.getCustomPageURL(weblog, "my custom page", "185810",
                false)).thenReturn("getCustomPageURL");
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
