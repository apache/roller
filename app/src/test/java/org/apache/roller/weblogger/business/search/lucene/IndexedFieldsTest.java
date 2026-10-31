/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.business.search.lucene;

import java.sql.Timestamp;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The index holds only what search needs, and an index built by an earlier
 * version with more fields is found so it can be rebuilt.
 */
class IndexedFieldsTest {

    @Test
    void entryDocumentHoldsNoAuthorOrCommenterDetails() {
        Document doc = new IndexOperation(null) {
            @Override
            protected void doRun() {
            }
        }.getDocument(entry());

        assertNotNull(doc.getField(FieldConstants.ID));
        assertNotNull(doc.getField(FieldConstants.C_CONTENT));
        for (String removed : FieldConstants.REMOVED_FIELDS) {
            assertNull(doc.getField(removed), removed);
        }
    }

    @Test
    void indexWithARemovedFieldIsFound() throws Exception {
        assertTrue(hasRemovedFields(FieldConstants.C_EMAIL));
        assertFalse(hasRemovedFields(FieldConstants.TITLE));
    }

    private static boolean hasRemovedFields(String field) throws Exception {
        try (Directory dir = new ByteBuffersDirectory()) {
            try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new StandardAnalyzer()))) {
                Document doc = new Document();
                doc.add(new StringField(field, "value", Field.Store.YES));
                writer.addDocument(doc);
            }
            try (DirectoryReader reader = DirectoryReader.open(dir)) {
                return LuceneIndexManager.hasRemovedFields(reader);
            }
        }
    }

    private static WeblogEntry entry() {
        WeblogEntryComment comment = mock(WeblogEntryComment.class);
        when(comment.getContent()).thenReturn("nice post");
        when(comment.getName()).thenReturn("Reader");
        when(comment.getEmail()).thenReturn("reader@example.com");

        Weblog weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("team");
        User author = mock(User.class);
        when(author.getUserName()).thenReturn("author");

        WeblogEntry entry = mock(WeblogEntry.class);
        when(entry.getId()).thenReturn("id1");
        when(entry.getWebsite()).thenReturn(weblog);
        when(entry.getCreator()).thenReturn(author);
        when(entry.getTitle()).thenReturn("Title");
        when(entry.getLocale()).thenReturn("en");
        when(entry.getText()).thenReturn("Text");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(entry.getUpdateTime()).thenReturn(now);
        when(entry.getPubTime()).thenReturn(now);
        when(entry.getComments()).thenReturn(List.of(comment));
        return entry;
    }
}
