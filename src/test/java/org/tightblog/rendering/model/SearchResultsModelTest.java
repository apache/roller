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
package org.tightblog.rendering.model;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.tightblog.service.ThemeManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.indexer.FieldConstants;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.service.indexer.SearchTask;
import org.tightblog.domain.SharedTheme;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntry.PubStatus;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.repository.WeblogEntryRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SearchResultsModelTest {

    private SearchTask mockSearchTask;
    private IndexSearcher mockIndexSearcher;
    private WeblogEntryRepository mockWeblogEntryRepository;
    private LuceneIndexer mockLuceneIndexer;
    private WeblogEntryListGenerator mockGenerator;
    private SearchResultsModel searchResultsModel;
    private WeblogPageRequest pageRequest;
    private Weblog weblog;

    @Captor
    ArgumentCaptor<Map<LocalDate, List<WeblogEntry>>> dateEntryMapCaptor;

    @Before
    public void initialize() {
        mockGenerator = mock(WeblogEntryListGenerator.class);
        mockSearchTask = mock(SearchTask.class);
        mockIndexSearcher = mock(IndexSearcher.class);
        when(mockSearchTask.getSearcher()).thenReturn(mockIndexSearcher);
        ThemeManager mockThemeManager = mock(ThemeManager.class);
        mockLuceneIndexer = mock(LuceneIndexer.class);
        SharedTheme sharedTheme = new SharedTheme();
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);
        WeblogEntryManager mockWeblogEntryManager = mock(WeblogEntryManager.class);
        mockWeblogEntryRepository = mock(WeblogEntryRepository.class);
        weblog = new Weblog();
        weblog.setHandle("testblog");
        weblog.setLocale("EN_US");
        pageRequest = new WeblogPageRequest();
        pageRequest.setWeblog(weblog);
        pageRequest.setWeblogHandle(weblog.getHandle());
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", pageRequest);
        searchResultsModel = new SearchResultsModel();
        searchResultsModel.setWeblogEntryManager(mockWeblogEntryManager);
        searchResultsModel.setWeblogEntryRepository(mockWeblogEntryRepository);
        searchResultsModel.setWeblogEntryListGenerator(mockGenerator);
        searchResultsModel.setThemeManager(mockThemeManager);
        searchResultsModel.setLuceneIndexer(mockLuceneIndexer);
        searchResultsModel.init(initData);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEmptyPagerIfNoQuery() {
        pageRequest.setQuery(null);
        searchResultsModel.getWeblogEntriesPager();
        verify(mockGenerator).getSearchPager(any(), dateEntryMapCaptor.capture(), eq(false));
        Map<LocalDate, List<WeblogEntry>> results = dateEntryMapCaptor.getValue();
        assertEquals(0, results.size());
    }

    @Test
    public void testSearchTaskPopulatedCorrectly() {
        pageRequest.setQuery("stamps");
        pageRequest.setCategory("collectibles");
        searchResultsModel.getWeblogEntriesPager();
        ArgumentCaptor<SearchTask> searchTaskCaptor = ArgumentCaptor.forClass(SearchTask.class);
        verify(mockLuceneIndexer).executeIndexOperationNow(searchTaskCaptor.capture());
        SearchTask searchTask = searchTaskCaptor.getValue();
        assertEquals("stamps", searchTask.getTerm());
        assertEquals("testblog", searchTask.getWeblogHandle());
        assertEquals("collectibles", searchTask.getCategory());
        verify(mockGenerator).getSearchPager(any(), any(), eq(false));
    }

    @Test
    public void testListMapPopulatedWhenResultsReturned() throws IOException {
        pageRequest.setQuery("stamps");

        Instant now = Instant.now();
        WeblogEntry entry1 = createWeblogEntry("blogEntry1", now, PubStatus.PUBLISHED);
        WeblogEntry entry2 = createWeblogEntry("blogEntry2", now, PubStatus.PUBLISHED);
        IndexableField if1 = new StringField(FieldConstants.ID, entry1.getId(), Field.Store.YES);
        IndexableField if2 = new StringField(FieldConstants.ID, entry2.getId(), Field.Store.YES);
        Document doc1 = new Document();
        doc1.add(if1);
        Document doc2 = new Document();
        doc2.add(if2);
        ScoreDoc[] hits = new ScoreDoc[2];
        hits[0] = new ScoreDoc(111, 90);
        hits[1] = new ScoreDoc(222, 80);
        when(mockIndexSearcher.doc(hits[0].doc)).thenReturn(doc1);
        when(mockIndexSearcher.doc(hits[1].doc)).thenReturn(doc2);

        TopFieldDocs docs = new TopFieldDocs(2, hits, null, 100.0f);
        // mock executeIndexOperationNow to return the specified SearchTask object
        // see http://www.baeldung.com/mockito-void-methods
        doAnswer(invocation -> {
            SearchTask searchTask = invocation.getArgument(0);
            searchTask.setResults(docs);
            searchTask.setSearcher(mockIndexSearcher);
            return null; // void method, so return null
        }).when(mockLuceneIndexer).executeIndexOperationNow(any(SearchTask.class));

        searchResultsModel.getWeblogEntriesPager();
        verify(mockGenerator).getSearchPager(any(), dateEntryMapCaptor.capture(), eq(false));
        Map<LocalDate, List<WeblogEntry>> results = dateEntryMapCaptor.getValue();
        LocalDate expectedDate = now.atZone(ZoneId.systemDefault()).toLocalDate();
        assertEquals(1, results.size());
        assertEquals(2, results.get(expectedDate).size());
        assertEquals("blogEntry1Anchor", results.get(expectedDate).get(0).getAnchor());
        assertEquals("blogEntry2Anchor", results.get(expectedDate).get(1).getAnchor());
    }

    private WeblogEntry createWeblogEntry(String title, Instant pubTime, WeblogEntry.PubStatus status) {
        WeblogEntry entry = new WeblogEntry();
        entry.setWeblog(weblog);
        entry.setTitle(title);
        entry.setAnchor(title + "Anchor");
        entry.setStatus(status);
        entry.setPubTime(pubTime);
        when(mockWeblogEntryRepository.findByIdOrNull(entry.getId())).thenReturn(entry);
        return entry;
    }

    @Test
    public void testConvertHitsToEntries() throws IOException {
        // create three weblog entries
        // configure WEM to return three if requested
        // create a search task that will get two of them
        // create the Map
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
        WeblogEntry entry1 = createWeblogEntry("blogEntry1", oneDayAgo, PubStatus.PUBLISHED);
        // entry 2 should not appear in search results as it is not in published state
        WeblogEntry entry2 = createWeblogEntry("blogEntry2", oneDayAgo, PubStatus.DRAFT);
        // entry 3 should appear in a different map entry (list) as entry1 as it was published on a different day
        WeblogEntry entry3 = createWeblogEntry("blogEntry3", threeDaysAgo, PubStatus.PUBLISHED);
        // entry 4 should appear in the same map entry (list) as entry1 as it was published on the same day
        // should appear before entry1 in list due to secondary ordering by title.
        WeblogEntry entry4 = createWeblogEntry("ABlogEntry4", oneDayAgo, PubStatus.PUBLISHED);

        IndexableField if1 = new StringField(FieldConstants.ID, entry1.getId(), Field.Store.YES);
        IndexableField if2 = new StringField(FieldConstants.ID, entry2.getId(), Field.Store.YES);
        IndexableField if3 = new StringField(FieldConstants.ID, entry3.getId(), Field.Store.YES);
        IndexableField if4 = new StringField(FieldConstants.ID, entry4.getId(), Field.Store.YES);

        Document doc1 = new Document();
        doc1.add(if1);

        Document doc2 = new Document();
        doc2.add(if2);

        Document doc3 = new Document();
        doc3.add(if3);

        Document doc4 = new Document();
        doc4.add(if4);

        ScoreDoc[] hits = new ScoreDoc[4];
        hits[0] = new ScoreDoc(111, 80);
        hits[1] = new ScoreDoc(222, 70);
        hits[2] = new ScoreDoc(333, 60);
        hits[3] = new ScoreDoc(444, 50);

        when(mockIndexSearcher.doc(hits[0].doc)).thenReturn(doc1);
        when(mockIndexSearcher.doc(hits[1].doc)).thenReturn(doc2);
        when(mockIndexSearcher.doc(hits[2].doc)).thenReturn(doc3);
        when(mockIndexSearcher.doc(hits[3].doc)).thenReturn(doc4);

        Map<LocalDate, TreeSet<WeblogEntry>> testMap = searchResultsModel.convertHitsToEntries(hits, mockSearchTask);
        assertEquals(2, testMap.size());
        assertEquals(2, testMap.get(oneDayAgo.atZone(ZoneId.systemDefault()).toLocalDate()).size());
        Set<WeblogEntry> oneDayAgoSet = testMap.get(oneDayAgo.atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(2, oneDayAgoSet.size());
        assertEquals(entry4, ((TreeSet) oneDayAgoSet).first());
        assertTrue(oneDayAgoSet.contains(entry1));
        Set<WeblogEntry> threeDaysAgoSet = testMap.get(threeDaysAgo.atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(1, threeDaysAgoSet.size());
        assertTrue(threeDaysAgoSet.contains(entry3));
    }
}
