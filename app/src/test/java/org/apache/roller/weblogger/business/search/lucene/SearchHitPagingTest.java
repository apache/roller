/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.business.search.lucene;

import java.sql.Timestamp;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.SearchResultList;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Search pages over published entries and loads only as many hits as the
 * requested page needs.
 */
class SearchHitPagingTest {

    private static final int HITS = 20;

    @Test
    void firstPageLoadsOnlyItsEntries() throws Exception {
        WeblogEntryManager entries = entries(-1);
        SearchResultList result = convert(entries, 0, 2);
        assertEquals(2, result.getResults().size());
        verify(entries, times(2)).getWeblogEntry(anyString());
    }

    @Test
    void draftHitDoesNotUseAPageSlot() throws Exception {
        WeblogEntryManager entries = entries(0);
        SearchResultList result = convert(entries, 0, 2);
        assertEquals(2, result.getResults().size());
        assertEquals("id1", result.getResults().get(0).getId());
        verify(entries, times(3)).getWeblogEntry(anyString());
    }

    @Test
    void pageBeyondTheHitsFallsBackToTheFirstPage() throws Exception {
        WeblogEntryManager entries = entries(-1);
        SearchResultList result = convert(entries, 50, 2);
        assertEquals(0, result.getOffset());
        assertEquals("id0", result.getResults().get(0).getId());
        verify(entries, times(HITS)).getWeblogEntry(anyString());
    }

    /** Entries id0..id19, published in the past; the one at draftIndex is a draft. */
    private static WeblogEntryManager entries(int draftIndex) throws Exception {
        WeblogEntryManager entries = mock(WeblogEntryManager.class);
        Timestamp past = new Timestamp(System.currentTimeMillis() - 60_000);
        for (int i = 0; i < HITS; i++) {
            WeblogEntry entry = mock(WeblogEntry.class);
            when(entry.getId()).thenReturn("id" + i);
            when(entry.isPublished()).thenReturn(i != draftIndex);
            when(entry.getPubTime()).thenReturn(past);
            when(entries.getWeblogEntry("id" + i)).thenReturn(entry);
        }
        return entries;
    }

    private static SearchResultList convert(WeblogEntryManager entries, int page, int size)
            throws Exception {
        IndexSearcher searcher = mock(IndexSearcher.class);
        ScoreDoc[] hits = new ScoreDoc[HITS];
        for (int i = 0; i < HITS; i++) {
            hits[i] = new ScoreDoc(i, 1f);
            Document doc = new Document();
            doc.add(new StringField(FieldConstants.ID, "id" + i, Field.Store.YES));
            doc.add(new StringField(FieldConstants.WEBSITE_HANDLE, "team", Field.Store.YES));
            when(searcher.doc(i)).thenReturn(doc);
        }
        SearchOperation search = mock(SearchOperation.class);
        when(search.getSearcher()).thenReturn(searcher);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getWeblogEntryManager()).thenReturn(entries);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            return LuceneIndexManager.convertHitsToEntryList(
                    hits, search, page, size, "team", true, mock(URLStrategy.class));
        }
    }
}
