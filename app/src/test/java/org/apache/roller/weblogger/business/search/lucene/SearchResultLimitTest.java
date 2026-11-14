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
 */
package org.apache.roller.weblogger.business.search.lucene;

import java.sql.Timestamp;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.SearchResultList;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * A search run against a real index counts every match, however many there
 * are, and pages through them as far as its limit.
 */
class SearchResultLimitTest {

    /** More than the 1,000 matches up to which Lucene promises an exact count by default. */
    private static final int MATCHES = 1200;

    private static final int PAGE_SIZE = 10;

    @Test
    void countsEveryMatchPastFiveHundredAndPastOneThousand() throws Exception {
        try (Directory dir = index(MATCHES); DirectoryReader reader = DirectoryReader.open(dir)) {
            SearchOperation search = search(reader);
            search.run();
            assertEquals(MATCHES, search.getResultsCount());
            assertEquals(TotalHits.Relation.EQUAL_TO, search.getResults().totalHits.relation);
            assertEquals(MATCHES, search.getResults().scoreDocs.length);
        }
    }

    @Test
    void pagePastFiveHundredIsReturned() throws Exception {
        try (Directory dir = index(MATCHES); DirectoryReader reader = DirectoryReader.open(dir)) {
            SearchOperation search = search(reader);
            search.run();

            // entries 601 to 610, newest first
            SearchResultList result = convert(search, 60);
            assertEquals(600, result.getOffset());
            assertEquals(PAGE_SIZE, result.getResults().size());
            assertEquals(id(MATCHES - 601), result.getResults().get(0).getId());
            assertEquals(MATCHES, result.getTotalHits());
            assertTrue(result.hasMore());

            // the last page, past the first thousand
            result = convert(search, MATCHES / PAGE_SIZE - 1);
            assertEquals(MATCHES - PAGE_SIZE, result.getOffset());
            assertEquals(id(0), result.getResults().get(PAGE_SIZE - 1).getId());
            assertEquals(MATCHES, result.getTotalHits());
            assertFalse(result.hasMore());
        }
    }

    @Test
    void pagingStopsAtTheLimitButTheTotalDoesNot() throws Exception {
        int max = 25;
        try (Directory dir = index(MATCHES); DirectoryReader reader = DirectoryReader.open(dir)) {
            SearchOperation search = search(reader);
            search.setMaxResults(max);
            search.run();
            // an exact count, not the lower bound Lucene gives by default
            // when there are more than 1,000 matches and fewer are kept
            assertEquals(MATCHES, search.getResultsCount());
            assertEquals(TotalHits.Relation.EQUAL_TO, search.getResults().totalHits.relation);
            assertEquals(max, search.getResults().scoreDocs.length);

            // the page before the limit has a next page
            SearchResultList result = convert(search, 1);
            assertEquals(PAGE_SIZE, result.getResults().size());
            assertTrue(result.hasMore());
            assertEquals(MATCHES, result.getTotalHits());

            // the page the limit cuts short is the last one: it holds the
            // newest entries up to the limit and offers no next page
            result = convert(search, 2);
            assertEquals(20, result.getOffset());
            assertEquals(5, result.getResults().size());
            assertEquals(id(MATCHES - max), result.getResults().get(4).getId());
            assertFalse(result.hasMore());
            assertEquals(MATCHES, result.getTotalHits());

            // a page past the limit falls back to the first page, as any page
            // past the end does
            result = convert(search, 3);
            assertEquals(0, result.getOffset());
            assertEquals(id(MATCHES - 1), result.getResults().get(0).getId());
            assertEquals(MATCHES, result.getTotalHits());
        }
    }

    @Test
    void limitOnAPageBoundaryLeavesNoNextPage() throws Exception {
        try (Directory dir = index(MATCHES); DirectoryReader reader = DirectoryReader.open(dir)) {
            SearchOperation search = search(reader);
            search.setMaxResults(2 * PAGE_SIZE);
            search.run();

            SearchResultList result = convert(search, 1);
            assertEquals(PAGE_SIZE, result.getResults().size());
            assertFalse(result.hasMore());
            assertEquals(MATCHES, result.getTotalHits());
        }
    }

    private static String id(int i) {
        return "id" + i;
    }

    /** Entries id0, id1 and so on, each published a minute after the one before. */
    private static Directory index(int count) throws Exception {
        Directory dir = new ByteBuffersDirectory();
        long start = Timestamp.valueOf("2020-01-01 00:00:00").getTime();
        try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            for (int i = 0; i < count; i++) {
                Document doc = new Document();
                doc.add(new StringField(FieldConstants.ID, id(i), Field.Store.YES));
                doc.add(new StringField(FieldConstants.WEBSITE_HANDLE, "team", Field.Store.YES));
                doc.add(new TextField(FieldConstants.CONTENT, "roller entry " + i, Field.Store.NO));
                doc.add(new SortedDocValuesField(FieldConstants.PUBLISHED,
                        new BytesRef(new Timestamp(start + i * 60_000L).toString())));
                writer.addDocument(doc);
            }
        }
        return dir;
    }

    private static SearchOperation search(DirectoryReader reader) {
        LuceneIndexManager manager = mock(LuceneIndexManager.class);
        when(manager.getSharedIndexReader()).thenReturn(reader);
        when(manager.getReadWriteLock()).thenReturn(new ReentrantReadWriteLock());
        SearchOperation search = new SearchOperation(manager);
        search.setTerm("roller");
        search.setWeblogHandle("team");
        return search;
    }

    /** Converts one page of the search's hits; every hit's entry is published. */
    private static SearchResultList convert(SearchOperation search, int page) throws Exception {
        Timestamp past = new Timestamp(System.currentTimeMillis() - 60_000);
        WeblogEntryManager entries = mock(WeblogEntryManager.class);
        when(entries.getWeblogEntry(anyString())).thenAnswer(call -> {
            WeblogEntry entry = mock(WeblogEntry.class);
            when(entry.getId()).thenReturn(call.getArgument(0));
            when(entry.isPublished()).thenReturn(true);
            when(entry.getPubTime()).thenReturn(past);
            return entry;
        });
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getWeblogEntryManager()).thenReturn(entries);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            return LuceneIndexManager.convertHitsToEntryList(
                    search.getResults().scoreDocs, search, page, PAGE_SIZE, "team", true,
                    mock(URLStrategy.class));
        }
    }
}
