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
package org.tightblog.rendering.controller;

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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.tightblog.TestUtils;
import org.tightblog.config.WebConfig;
import org.tightblog.dao.WeblogEntryDao;
import org.tightblog.domain.SharedTheme;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.generators.WeblogEntryListGenerator;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.SearchResultsModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.rendering.model.URLModel;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.requests.WeblogSearchRequest;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.dao.WeblogDao;
import org.tightblog.service.indexer.FieldConstants;
import org.tightblog.service.indexer.SearchTask;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SearchControllerTest {

    private SearchController controller;
    private Weblog weblog;
    private WeblogDao mockWD;
    private WeblogTheme mockWeblogTheme;
    private ThemeManager mockThemeManager;
    private ThymeleafRenderer mockRenderer;
    private SharedTheme sharedTheme;
    private ApplicationContext mockApplicationContext;
    private Principal mockPrincipal;
    private LuceneIndexer mockLuceneIndexer;
    private WeblogEntryListGenerator mockWELG;
    private SearchTask mockSearchTask;
    private IndexSearcher mockIndexSearcher;
    private WeblogEntryDao mockWeblogEntryDao;
    private WeblogTemplate searchResultsTemplate;

    @Captor
    ArgumentCaptor<Map<LocalDate, List<WeblogEntry>>> entriesByDateMapCaptor;

    @Captor
    ArgumentCaptor<Map<String, Object>> stringObjectMapCaptor;

    @Before
    public void initializeMocks() throws IOException {
        mockPrincipal = mock(Principal.class);

        mockWD = mock(WeblogDao.class);
        weblog = new Weblog();
        weblog.setHandle("myblog");
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(weblog);

        mockRenderer = mock(ThymeleafRenderer.class);
        when(mockRenderer.render(any(), any()))
                .thenReturn(new CachedContent(Template.Role.WEBLOG));

        sharedTheme = new SharedTheme();
        sharedTheme.setSiteWide(false);
        mockWeblogTheme = mock(WeblogTheme.class);
        mockThemeManager = mock(ThemeManager.class);
        when(mockThemeManager.getWeblogTheme(any())).thenReturn(mockWeblogTheme);
        when(mockThemeManager.getSharedTheme(any())).thenReturn(sharedTheme);

        Function<WeblogPageRequest, SiteModel> siteModelFactory = new WebConfig().siteModelFactory();

        mockApplicationContext = mock(ApplicationContext.class);
        // return empty model map in getModelMap()
        when(mockApplicationContext.getBean(anyString(), eq(Set.class))).thenReturn(new HashSet());

        mockIndexSearcher = mock(IndexSearcher.class);
        mockSearchTask = mock(SearchTask.class);
        when(mockSearchTask.getSearcher()).thenReturn(mockIndexSearcher);

        mockLuceneIndexer = mock(LuceneIndexer.class);
        mockWELG = mock(WeblogEntryListGenerator.class);
        mockWeblogEntryDao = mock(WeblogEntryDao.class);

        SearchResultsModel mockSearchResultsModel = mock(SearchResultsModel.class);
        when(mockSearchResultsModel.getWeblogEntryListGenerator()).thenReturn(mockWELG);
        when(mockSearchResultsModel.getLuceneIndexer()).thenReturn(mockLuceneIndexer);
        when(mockSearchResultsModel.getWeblogEntryDao()).thenReturn(mockWeblogEntryDao);
        when(mockSearchResultsModel.getThemeManager()).thenReturn(mockThemeManager);

        controller = new SearchController(mockWD, mockRenderer, mockThemeManager, mockSearchResultsModel,
                siteModelFactory);
        controller.setApplicationContext(mockApplicationContext);

        searchResultsTemplate = new WeblogTemplate();
        searchResultsTemplate.setRole(Template.Role.SEARCH_RESULTS);
        when(mockWeblogTheme.getTemplateByRole(Template.Role.SEARCH_RESULTS)).thenReturn(searchResultsTemplate);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testWeblogEntryListGeneratorCalledWithCorrectParameters() throws IOException {
        controller.getSearchResults("myblog", "stamps", "collectibles",
                4, mockPrincipal);

        WeblogSearchRequest wsr = TestUtils.extractWeblogSearchRequestFromMockRenderer(mockRenderer);

        wsr.getWeblogEntriesPager();
        ArgumentCaptor<SearchTask> searchTaskCaptor = ArgumentCaptor.forClass(SearchTask.class);
        verify(mockLuceneIndexer).executeIndexOperationNow(searchTaskCaptor.capture());
        SearchTask searchTask = searchTaskCaptor.getValue();
        assertEquals("stamps", searchTask.getTerm());
        assertEquals(TestUtils.BLOG_HANDLE, searchTask.getWeblogHandle());
        assertEquals("collectibles", searchTask.getCategory());
        verify(mockWELG).getSearchPager(eq(weblog), eq("stamps"), eq("collectibles"),
                eq(4), eq(Collections.emptyMap()), eq(false));

        // no search phrase case
        wsr.setSearchPhrase(null);
        wsr.getWeblogEntriesPager();
        verify(mockWELG).getSearchPager(eq(weblog), isNull(), eq("collectibles"),
                eq(4), eq(Collections.emptyMap()), eq(false));
    }

    @Test
    public void test404OnMissingWeblog() throws IOException {
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
        ResponseEntity<Resource> result = controller.getSearchResults("myblog", "foo", null,
                0, mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testCorrectTemplateChosen() throws IOException {
        WeblogTemplate weblogTemplate = new WeblogTemplate();
        weblogTemplate.setRole(Template.Role.WEBLOG);

        when(mockWeblogTheme.getTemplateByRole(Template.Role.SEARCH_RESULTS)).thenReturn(null);

        ResponseEntity<Resource> result = controller.getSearchResults("myblog", null, "foo", 0,
                mockPrincipal);

        // verify weblog retrieved, NOT FOUND returned due to no matching template
        verify(mockThemeManager).getWeblogTheme(weblog);
        verify(mockWeblogTheme).getTemplateByRole(Template.Role.WEBLOG);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());

        // weblogTheme should be chosen because no template of type SEARCH_RESULTS
        when(mockWeblogTheme.getTemplateByRole(Template.Role.WEBLOG)).thenReturn(weblogTemplate);

        Mockito.clearInvocations(mockThemeManager, mockWeblogTheme);
        result = controller.getSearchResults("myblog", null, "foo", 0,
                mockPrincipal);
        verify(mockWeblogTheme).getTemplateByRole(Template.Role.WEBLOG);
        assertEquals(MediaType.TEXT_HTML, result.getHeaders().getContentType());

        when(mockWeblogTheme.getTemplateByRole(Template.Role.SEARCH_RESULTS)).thenReturn(searchResultsTemplate);

        // test proper page models provided to renderer
        URLModel mockURLModel = mock(URLModel.class);
        when(mockURLModel.getModelName()).thenReturn("model");
        Set<Model> pageModelSet = new HashSet<>();
        pageModelSet.add(mockURLModel);
        when(mockApplicationContext.getBean(eq("searchModelSet"), eq(Set.class))).thenReturn(pageModelSet);

        Mockito.clearInvocations(mockThemeManager, mockWeblogTheme, mockRenderer);
        result = controller.getSearchResults("myblog", null, "foo", 0,
                mockPrincipal);
        // search results template should now be retrieved, backup weblog template call not occurring
        verify(mockWeblogTheme, never()).getTemplateByRole(Template.Role.WEBLOG);

        assertEquals(0, result.getHeaders().getContentLength());

        // set up captors on thymeleafRenderer.render()
        verify(mockRenderer).render(any(), stringObjectMapCaptor.capture());
        Map<String, Object> results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertFalse(results.containsKey("site"));

        // try a site-wide theme
        sharedTheme.setSiteWide(true);
        Mockito.clearInvocations(mockRenderer);
        result = controller.getSearchResults("myblog", null, "foo", 0,
                mockPrincipal);
        verify(mockRenderer).render(any(), stringObjectMapCaptor.capture());
        results = stringObjectMapCaptor.getValue();
        assertTrue(results.containsKey("model"));
        assertTrue(results.containsKey("site"));

        // test 404 if exception during rendering
        doThrow(new IllegalArgumentException("Expected exception during testing")).when(mockRenderer).render(any(), any());
        result = controller.getSearchResults("myblog", null, "foo", 0,
                mockPrincipal);
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void testConvertHitsToEntries() throws IOException {
        controller.getSearchResults("myblog", "collectibles",
                "stamps", 4, mockPrincipal);

        WeblogSearchRequest wsr = TestUtils.extractWeblogSearchRequestFromMockRenderer(mockRenderer);

        // create three weblog entries
        // configure WEM to return three if requested
        // create a search task that will get two of them
        // create the Map
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(1, ChronoUnit.DAYS);
        Instant threeDaysAgo = now.minus(3, ChronoUnit.DAYS);
        WeblogEntry entry1 = createWeblogEntry("blogEntry1", oneDayAgo, WeblogEntry.PubStatus.PUBLISHED);
        // entry 2 should not appear in search results as it is not in published state
        WeblogEntry entry2 = createWeblogEntry("blogEntry2", oneDayAgo, WeblogEntry.PubStatus.DRAFT);
        // entry 3 should appear in a different map entry (list) as entry1 as it was published on a different day
        WeblogEntry entry3 = createWeblogEntry("blogEntry3", threeDaysAgo, WeblogEntry.PubStatus.PUBLISHED);
        // entry 4 should appear in the same map entry (list) as entry1 as it was published on the same day
        // should appear before entry1 in list due to secondary ordering by title.
        WeblogEntry entry4 = createWeblogEntry("ABlogEntry4", oneDayAgo, WeblogEntry.PubStatus.PUBLISHED);

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

        Map<LocalDate, TreeSet<WeblogEntry>> testMap = wsr.convertHitsToEntries(hits, mockSearchTask);
        assertEquals(2, testMap.size());
        Set<WeblogEntry> oneDayAgoSet = testMap.get(oneDayAgo.atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(2, oneDayAgoSet.size());
        assertEquals(entry4, ((TreeSet) oneDayAgoSet).first());
        assertTrue(oneDayAgoSet.contains(entry1));
        Set<WeblogEntry> threeDaysAgoSet = testMap.get(threeDaysAgo.atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(1, threeDaysAgoSet.size());
        assertTrue(threeDaysAgoSet.contains(entry3));

        // test recovers from an IOException by skipping problematic doc
        when(mockIndexSearcher.doc(hits[0].doc)).thenThrow(new IOException());
        testMap = wsr.convertHitsToEntries(hits, mockSearchTask);
        assertEquals(2, testMap.size());
        oneDayAgoSet = testMap.get(oneDayAgo.atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(1, oneDayAgoSet.size());
        assertEquals(entry4, ((TreeSet) oneDayAgoSet).first());
        assertFalse(oneDayAgoSet.contains(entry1));
        threeDaysAgoSet = testMap.get(threeDaysAgo.atZone(ZoneId.systemDefault()).toLocalDate());
        assertEquals(1, threeDaysAgoSet.size());
        assertTrue(threeDaysAgoSet.contains(entry3));
    }

    @Test
    public void testEntriesByDateMapPopulatedWhenResultsReturned() throws IOException {
        controller.getSearchResults("myblog", "stamps",
                "collectibles", 4, mockPrincipal);

        WeblogSearchRequest wsr = TestUtils.extractWeblogSearchRequestFromMockRenderer(mockRenderer);

        Instant now = Instant.now();
        WeblogEntry entry1 = createWeblogEntry("blogEntry1", now, WeblogEntry.PubStatus.PUBLISHED);
        WeblogEntry entry2 = createWeblogEntry("blogEntry2", now, WeblogEntry.PubStatus.PUBLISHED);
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

        wsr.getWeblogEntriesPager();
        verify(mockWELG).getSearchPager(any(), any(), any(), anyInt(), entriesByDateMapCaptor.capture(), eq(false));
        Map<LocalDate, List<WeblogEntry>> results = entriesByDateMapCaptor.getValue();
        LocalDate expectedDate = now.atZone(ZoneId.systemDefault()).toLocalDate();
        assertEquals(1, results.size());
        assertEquals(2, results.get(expectedDate).size());
        assertEquals("blogEntry1Anchor", results.get(expectedDate).get(0).getAnchor());
        assertEquals("blogEntry2Anchor", results.get(expectedDate).get(1).getAnchor());
        assertEquals(2, wsr.getResultCount());
        assertEquals(0, wsr.getOffset());
        assertEquals(2, wsr.getLimit());
        assertTrue(wsr.isSearchResults());
        assertTrue(wsr.isNoIndex());
        assertTrue(wsr.toString().contains("category=collectibles searchPhrase=stamps"));
    }

    private WeblogEntry createWeblogEntry(String title, Instant pubTime, WeblogEntry.PubStatus status) {
        WeblogEntry entry = new WeblogEntry();
        entry.setWeblog(weblog);
        entry.setTitle(title);
        entry.setAnchor(title + "Anchor");
        entry.setStatus(status);
        entry.setPubTime(pubTime);
        when(mockWeblogEntryDao.findByIdOrNull(entry.getId())).thenReturn(entry);
        return entry;
    }
}
