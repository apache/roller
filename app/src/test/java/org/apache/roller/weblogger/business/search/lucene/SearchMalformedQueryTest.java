/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.roller.weblogger.business.search.lucene;

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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A query Lucene cannot run finds nothing, the same as a query it cannot
 * parse, instead of failing the search.
 */
class SearchMalformedQueryTest {

    @Test
    void unfinishedRegularExpressionFindsNothing() throws Exception {
        assertFindsNothing("/[/");
    }

    @Test
    void negativePhraseSlopFindsNothing() throws Exception {
        assertFindsNothing("\"roller entry\"~-5");
    }

    @Test
    void unparseableQueryStillFindsNothing() throws Exception {
        assertFindsNothing("[a TO");
    }

    @Test
    void ordinaryQueryStillFindsTheEntry() throws Exception {
        try (Directory dir = index(); DirectoryReader reader = DirectoryReader.open(dir)) {
            SearchOperation search = search(reader, "roller");
            search.doRun();
            assertEquals(1, search.getResultsCount());
        }
    }

    private static void assertFindsNothing(String query) throws Exception {
        try (Directory dir = index(); DirectoryReader reader = DirectoryReader.open(dir)) {
            SearchOperation search = search(reader, query);
            assertDoesNotThrow(search::doRun, query);
            assertEquals(-1, search.getResultsCount(), query);
            assertNotNull(search.getParseError(), query);
        }
    }

    private static Directory index() throws Exception {
        Directory dir = new ByteBuffersDirectory();
        try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
            Document doc = new Document();
            doc.add(new StringField(FieldConstants.ID, "id1", Field.Store.YES));
            doc.add(new TextField(FieldConstants.CONTENT, "roller entry", Field.Store.NO));
            doc.add(new SortedDocValuesField(FieldConstants.PUBLISHED,
                    new BytesRef("2020-01-01 00:00:00.0")));
            writer.addDocument(doc);
        }
        return dir;
    }

    private static SearchOperation search(DirectoryReader reader, String query) {
        LuceneIndexManager manager = mock(LuceneIndexManager.class);
        when(manager.getSharedIndexReader()).thenReturn(reader);
        when(manager.getReadWriteLock()).thenReturn(new ReentrantReadWriteLock());
        SearchOperation search = new SearchOperation(manager);
        search.setTerm(query);
        return search;
    }
}
