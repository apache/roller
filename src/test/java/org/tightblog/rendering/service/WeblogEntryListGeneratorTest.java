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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.tightblog.WebloggerTest;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.service.URLService;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntry.PubStatus;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.rendering.service.WeblogEntryListGenerator.WeblogEntryListData;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WeblogEntryListGeneratorTest {

    private static ResourceBundleMessageSource messages;
    private WeblogEntryListGenerator generator;
    private Weblog weblog;
    private WeblogEntryManager mockWEM;
    private URLService mockUrlService = mock(URLService.class);
    private Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
    private Instant threeDaysAgo = twoDaysAgo.minus(1, ChronoUnit.DAYS);
    private Instant oneDayAgo = twoDaysAgo.plus(1, ChronoUnit.DAYS);
    private LocalDate nowLD = LocalDate.from(twoDaysAgo.atZone(ZoneId.systemDefault()));
    private LocalDate yesterdayLD = LocalDate.from(threeDaysAgo.atZone(ZoneId.systemDefault()));

    @BeforeClass
    public static void initializeOnce() {
        messages = new ResourceBundleMessageSource();
        messages.setBasename("messages/messages");
    }

    @Before
    public void initialize() {
        weblog = new Weblog();
        weblog.setLocale(Locale.ENGLISH.getLanguage());
        mockWEM = mock(WeblogEntryManager.class);
        generator = new WeblogEntryListGenerator(mockWEM, mockUrlService, messages);
    }

    @Test
    public void getSearchPager() {
        WeblogSearchRequest wsr = new WeblogSearchRequest(weblog.getHandle(), null, null);
        wsr.setWeblog(weblog);
        wsr.setSearchPhrase("my query");
        wsr.setCategory("coins");
        wsr.setPageNum(10);
        Map<LocalDate, List<WeblogEntry>> entryMap = createSampleEntriesMap();

        when(mockUrlService.getWeblogSearchURL(wsr.getWeblog(), wsr.getSearchPhrase(), wsr.getCategory(),
                9)).thenReturn("nextUrl");
        when(mockUrlService.getWeblogSearchURL(wsr.getWeblog(), wsr.getSearchPhrase(), wsr.getCategory(),
                11)).thenReturn("prevUrl");

        WeblogEntryListData data = generator.getSearchPager(wsr.getWeblog(), wsr.getSearchPhrase(), wsr.getCategory(),
                wsr.getPageNum(), entryMap, true);
        Map<LocalDate, List<WeblogEntry>> results = data.getEntries();
        assertEquals(2, results.size());
        assertEquals(2, results.get(nowLD).size());
        assertEquals(2, results.get(yesterdayLD).size());
        assertEquals("day1story1", results.get(nowLD).get(0).getAnchor());
        assertEquals("day2story1", results.get(yesterdayLD).get(0).getAnchor());
        assertEquals("prevUrl", data.getPrevLink());
        assertNotNull(data.getPrevLabel());
        assertEquals("nextUrl", data.getNextLink());
        assertNotNull(data.getNextLabel());

        wsr.setPageNum(0);
        data = generator.getSearchPager(wsr.getWeblog(), wsr.getSearchPhrase(), wsr.getCategory(),
                wsr.getPageNum(), entryMap, false);
        results = data.getEntries();
        assertEquals(2, results.size());
        assertNull(data.getPrevLink());
        assertNull(data.getPrevLabel());
        assertNull(data.getNextLink());
        assertNull(data.getNextLabel());
    }

    @Test
    public void getPermalinkPager() {
        // Showing SCHEDULED entries allowed with canShowUnpublishedEntries = false
        WeblogEntry entryToShow = WebloggerTest.genWeblogEntry(weblog, "day1story1", twoDaysAgo);
        entryToShow.setStatus(PubStatus.SCHEDULED);
        WeblogEntry weNext = WebloggerTest.genWeblogEntry(weblog, "nextStory", oneDayAgo);
        weNext.setTitle("My Next Story");
        WeblogEntry wePrev = WebloggerTest.genWeblogEntry(weblog, "prevStory", threeDaysAgo);
        wePrev.setTitle("My Prev Story");
        when(mockWEM.getWeblogEntryByAnchor(weblog, "day1story1")).thenReturn(entryToShow);
        when(mockWEM.getNextPublishedEntry(entryToShow)).thenReturn(weNext);
        when(mockWEM.getPreviousPublishedEntry(entryToShow)).thenReturn(wePrev);
        when(mockUrlService.getWeblogEntryURL(weNext)).thenReturn("nextUrl");
        when(mockUrlService.getWeblogEntryURL(wePrev)).thenReturn("prevUrl");

        WeblogEntryListData data = generator.getPermalinkPager(weblog, "day1story1", true);
        assertEquals(1, data.getEntries().size());
        assertEquals("entry not added to map", entryToShow, data.getEntries().values().iterator().next().get(0));
        assertEquals("entry not in list", entryToShow, data.getEntriesAsList().get(0));
        assertEquals("entriesAsList() not constant w/multiple calls", entryToShow, data.getEntriesAsList().get(0));
        assertEquals("key not set correctly to entry publish time", entryToShow, data.getEntries().get(
                        WeblogEntryListGenerator.instantToWeblogLocalDate(entryToShow.getWeblog(), twoDaysAgo)).get(0));

        assertEquals("nextUrl", data.getNextLink());
        assertTrue(data.getNextLabel().contains("My Next Story"));
        assertNotNull("prevUrl", data.getPrevLink());
        assertTrue(data.getPrevLabel().contains("My Prev Story"));

        // Test entry still retrievable if no publish time
        entryToShow.setPubTime(null);
        data = generator.getPermalinkPager(weblog, "day1story1", true);
        assertEquals(1, data.getEntries().size());
        assertEquals("unpublished entry not added to map", entryToShow,
                data.getEntries().values().iterator().next().get(0));
        assertEquals("unpublished entry not in list", entryToShow, data.getEntriesAsList().get(0));

        // Test SCHEDULED entries not allowed with canShowUnpublishedEntries = false
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNull(data.getEntries());
        assertNull(data.getNextLabel());
        assertNull(data.getPrevLabel());

        // Showing DRAFT entries not allowed with canShowUnpublishedEntries = false
        entryToShow.setStatus(PubStatus.DRAFT);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNull(data.getEntries());
        assertNull(data.getNextLabel());
        assertNull(data.getPrevLabel());

        // Showing PUBLISHED entries allowed with canShowUnpublishedEntries = false
        entryToShow.setStatus(PubStatus.PUBLISHED);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNotNull(data.getEntries());
        assertNotNull(data.getNextLabel());
        assertNotNull(data.getPrevLabel());

        // Test next, prev link info empty if entries in the future
        Instant oneHourLater = Instant.now().plus(1, ChronoUnit.HOURS);
        weNext.setPubTime(oneHourLater);
        wePrev.setPubTime(oneHourLater);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNotNull(data.getEntries());
        assertNull(data.getNextLabel());
        assertNull(data.getNextLink());
        assertNull(data.getPrevLabel());
        assertNull(data.getPrevLink());

        // Test next link info empty if no next link
        when(mockWEM.getNextPublishedEntry(entryToShow)).thenReturn(null);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNotNull(data.getEntries());
        assertNull(data.getNextLabel());
        assertNull(data.getNextLink());

        // Test prev link info empty if no prev link
        when(mockWEM.getPreviousPublishedEntry(entryToShow)).thenReturn(null);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNotNull(data.getEntries());
        assertNull(data.getPrevLabel());
        assertNull(data.getPrevLink());

        // Showing data is empty if anchor cannot be found for weblog
        data = generator.getPermalinkPager(weblog, "anchorNotFound", false);
        assertNull(data.getEntries());
        assertTrue(StringUtils.isEmpty(data.getNextLabel()));
        assertTrue(StringUtils.isEmpty(data.getNextLink()));
        assertTrue(StringUtils.isEmpty(data.getPrevLabel()));
        assertTrue(StringUtils.isEmpty(data.getPrevLink()));
        assertNull(data.getEntriesAsList());
    }

    @Test
    public void getChronoPager() {
        String dateString = "20180110";
        String catName = "stamps";
        String tag = "airmail";
        int pageNum = 2;
        int maxEntries = 3;

        Map<LocalDate, List<WeblogEntry>> entryMap = createSampleEntriesMap();

        when(mockWEM.getDateToWeblogEntryMap(any())).thenReturn(entryMap);
        when(mockUrlService.getWeblogCollectionURL(weblog, catName, dateString,
                tag, pageNum - 1)).thenReturn("nextUrl");
        when(mockUrlService.getWeblogCollectionURL(weblog, catName, dateString,
                tag, pageNum + 1)).thenReturn("prevUrl");

        WeblogEntryListData data = generator.getChronoPager(weblog, dateString,
                catName, tag, pageNum, maxEntries, false);

        Map<LocalDate, List<WeblogEntry>> results = data.getEntries();
        assertEquals(2, results.size());
        assertEquals(2, results.get(nowLD).size());
        assertEquals(1, results.get(yesterdayLD).size());
        assertEquals("day1story1", results.get(nowLD).get(0).getAnchor());
        assertEquals("day1story2", results.get(nowLD).get(1).getAnchor());
        assertEquals("day2story1", results.get(yesterdayLD).get(0).getAnchor());
        assertEquals("prevUrl", data.getPrevLink());
        assertNotNull(data.getPrevLabel());
        assertEquals("nextUrl", data.getNextLink());
        assertNotNull(data.getNextLabel());

        // test wesc correctly populated
        ArgumentCaptor<WeblogEntrySearchCriteria> captor
                = ArgumentCaptor.forClass(WeblogEntrySearchCriteria.class);
        verify(mockWEM).getDateToWeblogEntryMap(captor.capture());
        WeblogEntrySearchCriteria wesc = captor.getValue();
        assertEquals(weblog, wesc.getWeblog());
        assertEquals(LocalDate.of(2018, 1, 10).atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant(), wesc.getStartDate());
        assertEquals(LocalDate.of(2018, 1, 11).atStartOfDay()
                .minusNanos(1)
                .atZone(ZoneId.systemDefault()).toInstant(), wesc.getEndDate());
        assertEquals("stamps", wesc.getCategoryName());
        assertEquals("airmail", wesc.getTag());
        assertEquals(pageNum * maxEntries, wesc.getOffset());
        assertEquals(WeblogEntry.PubStatus.PUBLISHED, wesc.getStatus());
        assertEquals(maxEntries + 1, wesc.getMaxResults());

        // test maxEntries honored
        data = generator.getChronoPager(weblog, dateString,
                catName, tag, pageNum, 1, false);
        assertEquals(1, data.getEntries().size());

        data = generator.getChronoPager(weblog, dateString,
                catName, tag, pageNum, 0, false);
        assertEquals(0, data.getEntries().size());

        // another wesc test:
        // if sitewide, no weblog
        // if no datestring, no start or end
        // page 0, so no next links
        // moreResults = false so no prev links
        Mockito.clearInvocations(mockWEM);
        dateString = null;
        pageNum = 0;
        maxEntries = 10;
        data = generator.getChronoPager(weblog, dateString, catName, tag, pageNum, maxEntries, true);
        verify(mockWEM).getDateToWeblogEntryMap(captor.capture());
        wesc = captor.getValue();
        assertNull(wesc.getWeblog());
        assertNull(wesc.getStartDate());
        assertNull(wesc.getEndDate());
        assertNull(data.getPrevLink());
        assertNull(data.getPrevLabel());
        assertNull(data.getNextLink());
        assertNull(data.getNextLabel());

        // check month format (YYYYMM) correctly processed in search criteria
        Mockito.clearInvocations(mockWEM);
        dateString = "201805";
        generator.getChronoPager(weblog, dateString, catName, tag, pageNum, maxEntries, true);
        verify(mockWEM).getDateToWeblogEntryMap(captor.capture());
        wesc = captor.getValue();
        assertEquals(LocalDate.of(2018, 5, 1).atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant(), wesc.getStartDate());
        assertEquals(LocalDate.of(2018, 6, 1).atStartOfDay()
                .minusNanos(1)
                .atZone(ZoneId.systemDefault()).toInstant(), wesc.getEndDate());

        // check invalid length of date format (YYYYMMD) ignored in search criteria
        Mockito.clearInvocations(mockWEM);
        dateString = "2018051";
        generator.getChronoPager(weblog, dateString, catName, tag, pageNum, maxEntries, true);
        verify(mockWEM).getDateToWeblogEntryMap(captor.capture());
        wesc = captor.getValue();
        assertNull(wesc.getStartDate());
        assertNull(wesc.getEndDate());
    }

    private Map<LocalDate, List<WeblogEntry>> createSampleEntriesMap() {
        WeblogEntry we1 = WebloggerTest.genWeblogEntry(weblog, "day1story1", twoDaysAgo);
        WeblogEntry we2 = WebloggerTest.genWeblogEntry(weblog, "day1story2", twoDaysAgo);
        WeblogEntry we3 = WebloggerTest.genWeblogEntry(weblog, "day2story1", threeDaysAgo);
        WeblogEntry we4 = WebloggerTest.genWeblogEntry(weblog, "day2story2", threeDaysAgo);
        List<WeblogEntry> listNow = new ArrayList<>();
        listNow.add(we1);
        listNow.add(we2);
        List<WeblogEntry> listYesterday = new ArrayList<>();
        listYesterday.add(we3);
        // won't be returned as maxEntries = 3
        listYesterday.add(we4);

        Map<LocalDate, List<WeblogEntry>> entryMap = new TreeMap<>(Collections.reverseOrder());
        entryMap.put(nowLD, listNow);
        entryMap.put(yesterdayLD, listYesterday);

        return entryMap;
    }
}
