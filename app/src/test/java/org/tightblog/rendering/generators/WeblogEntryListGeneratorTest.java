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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.tightblog.WebloggerTest;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.rendering.generators.WeblogEntryListGenerator.WeblogEntryListData;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WeblogEntryListGeneratorTest {

    private WeblogEntryManager mockWEM = mock(WeblogEntryManager.class);
    private URLStrategy mockUrlStrategy = mock(URLStrategy.class);
    private WeblogEntryListGenerator generator;
    private Weblog weblog;

    @Before
    public void initialize() {
        generator = new WeblogEntryListGenerator();
        generator.setUrlStrategy(mockUrlStrategy);
        generator.setWeblogEntryManager(mockWEM);
        weblog = new Weblog();
        weblog.setLocale(Locale.ENGLISH.getLanguage());
    }

    @Test
    public void getPermalinkPager() throws Exception {
        // Showing SCHEDULED entries allowed with canShowDraftEntries = false
        Instant now = Instant.now();
        WeblogEntry we1 = WebloggerTest.genWeblogEntry("day1story1", now, weblog);
        we1.setStatus(PubStatus.SCHEDULED);
        WeblogEntry weNext = WebloggerTest.genWeblogEntry("nextStory", now, weblog);
        weNext.setTitle("My Next Story");
        weNext.setPubTime(now.minus(10, ChronoUnit.SECONDS));
        WeblogEntry wePrev = WebloggerTest.genWeblogEntry("prevStory", now, weblog);
        wePrev.setTitle("My Prev Story");
        when(mockWEM.getWeblogEntryByAnchor(weblog, "day1story1")).thenReturn(we1);
        when(mockWEM.getNextPublishedEntry(we1)).thenReturn(weNext);
        when(mockWEM.getPreviousPublishedEntry(we1)).thenReturn(wePrev);
        when(mockUrlStrategy.getWeblogEntryURL(weNext, true)).thenReturn("nextUrl");
        when(mockUrlStrategy.getWeblogEntryURL(wePrev, true)).thenReturn("prevUrl");

        WeblogEntryListData data = generator.getPermalinkPager(weblog, "day1story1", true);
        assertEquals(1, data.getEntries().size());
        assertEquals(we1, data.getEntries().values().stream().findFirst().get().get(0));

        assertEquals("nextUrl", data.getNextLink());
        assertTrue(data.getNextLabel().contains("My Next Story"));
        assertNotNull("prevUrl", data.getPrevLink());
        assertTrue(data.getPrevLabel().contains("My Prev Story"));

        // Showing SCHEDULED entries not allowed with canShowDraftEntries = false
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNull(data.getEntries());
        assertNull(data.getNextLabel());
        assertNull(data.getPrevLabel());

        // Showing DRAFT entries not allowed with canShowDraftEntries = false
        we1.setStatus(PubStatus.DRAFT);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNull(data.getEntries());
        assertNull(data.getNextLabel());
        assertNull(data.getPrevLabel());

        // Showing PUBLISHED entries allowed with canShowDraftEntries = false
        we1.setStatus(PubStatus.PUBLISHED);
        data = generator.getPermalinkPager(weblog, "day1story1", false);
        assertNotNull(data.getEntries());
        assertNotNull(data.getNextLabel());
        assertNotNull(data.getPrevLabel());
    }

    @Test
    public void getChronoPager() throws Exception {
        String dateString = "20180110";
        String catName = "stamps";
        String tag = "airmail";
        int pageNum = 2;
        int maxEntries = 3;
        boolean siteWideSearch = false;

        Instant now = Instant.now();
        Instant yesterday = now.minus(1, ChronoUnit.DAYS);
        LocalDate nowLD = LocalDate.from(now.atZone(ZoneId.systemDefault()));
        LocalDate yesterdayLD = LocalDate.from(yesterday.atZone(ZoneId.systemDefault()));

        WeblogEntry we1 = WebloggerTest.genWeblogEntry("day1story1", now, weblog);
        WeblogEntry we2 = WebloggerTest.genWeblogEntry("day1story2", now, weblog);
        WeblogEntry we3 = WebloggerTest.genWeblogEntry("day2story1", yesterday, weblog);
        WeblogEntry we4 = WebloggerTest.genWeblogEntry("day2story2", yesterday, weblog);
        List<WeblogEntry> listNow = new ArrayList<>();
        listNow.add(we1);
        listNow.add(we2);
        List<WeblogEntry> listYesterday = new ArrayList<>();
        listYesterday.add(we3);
        // won't be returned as maxEntries = 3
        listYesterday.add(we4);

        Map<LocalDate, List<WeblogEntry>> entryMap = new HashMap<>();
        entryMap.put(nowLD, listNow);
        entryMap.put(yesterdayLD, listYesterday);

        when(mockWEM.getDateToWeblogEntryMap(any())).thenReturn(entryMap);
        when(mockUrlStrategy.getWeblogCollectionURL(weblog, catName, dateString,
                tag, pageNum - 1, false)).thenReturn("nextUrl");
        when(mockUrlStrategy.getWeblogCollectionURL(weblog, catName, dateString,
                tag, pageNum + 1, false)).thenReturn("prevUrl");

        WeblogEntryListData data = generator.getChronoPager(weblog, dateString,
                catName, tag, pageNum, maxEntries, siteWideSearch);

        Map<LocalDate, List<WeblogEntry>> results = data.getEntries();
        assertEquals(2, results.size());
        assertEquals(2, results.get(nowLD).size());
        assertEquals(1, results.get(yesterdayLD).size());
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
        assertTrue(wesc.getTags().contains("airmail"));
        assertEquals(pageNum * maxEntries, wesc.getOffset());
        assertEquals(WeblogEntry.PubStatus.PUBLISHED, wesc.getStatus());
        assertEquals(maxEntries + 1, wesc.getMaxResults());

        // another wesc test:
        // if sitewide, no weblog
        // if no datestring, no start or end
        // page 0, so no next links
        // moreResults = false so no prev links
        Mockito.clearInvocations(mockWEM);
        siteWideSearch = true;
        dateString = null;
        pageNum = 0;
        maxEntries = 10;
        data = generator.getChronoPager(weblog, dateString,
                catName, tag, pageNum, maxEntries, siteWideSearch);
        verify(mockWEM).getDateToWeblogEntryMap(captor.capture());
        wesc = captor.getValue();
        assertNull(wesc.getWeblog());
        assertNull(wesc.getStartDate());
        assertNull(wesc.getEndDate());
        assertNull(data.getPrevLink());
        assertNull(data.getPrevLabel());
        assertNull(data.getNextLink());
        assertNull(data.getNextLabel());
    }
}