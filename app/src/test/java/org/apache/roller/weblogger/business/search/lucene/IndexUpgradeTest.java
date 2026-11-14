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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexFormatTooOldException;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Starting on a search index that an earlier Roller release left behind.
 * Each fixture holds two entries, written with the Lucene version and the
 * index fields of the release it is named after.
 */
class IndexUpgradeTest {

    @TempDir
    Path indexDir;

    @Test
    void indexFromRoller60IsReplacedAndRebuilt() throws Exception {
        // Roller 6.0 shipped Lucene 8.3, which this Lucene cannot read
        unzip("roller-6.0-lucene-8.3.0.zip");
        try (Directory dir = FSDirectory.open(indexDir)) {
            assertThrows(IndexFormatTooOldException.class, () -> DirectoryReader.open(dir));
        }

        ThreadManager threads = mock(ThreadManager.class);
        WeblogEntryManager entries = mock(WeblogEntryManager.class);
        LuceneIndexManager manager = manager(threads, entries);
        try {
            manager.initialize();

            assertTrue(manager.isInconsistentAtStartup());
            ArgumentCaptor<Runnable> rebuild = ArgumentCaptor.forClass(Runnable.class);
            verify(threads).executeInBackground(rebuild.capture());
            assertInstanceOf(RebuildWebsiteIndexOperation.class, rebuild.getValue());
            try (Directory dir = FSDirectory.open(indexDir)) {
                assertEquals(Version.LATEST.major,
                        SegmentInfos.readLatestCommit(dir).getIndexCreatedVersionMajor());
            }
            // until the rebuild fills it, searches find nothing rather than fail
            assertEquals(0, search(manager));

            List<WeblogEntry> published = List.of(
                    entry("entry-1", "Roller rebuilds its search index after an upgrade."),
                    entry("entry-2", "Another Roller entry about search."));
            when(entries.getWeblogEntries(any())).thenReturn(published);
            rebuild.getValue().run();
            assertEquals(2, search(manager));
        } finally {
            manager.shutdown();
        }
    }

    @Test
    void indexFromRoller617IsKept() throws Exception {
        // Roller 6.1 shipped Lucene 9, which this Lucene reads through
        // lucene-backward-codecs, and 6.1.7 indexes the fields this one does
        unzip("roller-6.1.7-lucene-9.12.3.zip");
        try (Directory dir = FSDirectory.open(indexDir)) {
            assertEquals(9, SegmentInfos.readLatestCommit(dir).getIndexCreatedVersionMajor());
        }

        ThreadManager threads = mock(ThreadManager.class);
        LuceneIndexManager manager = manager(threads, mock(WeblogEntryManager.class));
        try {
            manager.initialize();

            assertFalse(manager.isInconsistentAtStartup());
            verify(threads, never()).executeInBackground(any());
            assertEquals(2, search(manager));

            // new entries go into the same index
            new WriteToIndexOperation(manager) {
                @Override
                protected void doRun() {
                    Document doc = new Document();
                    doc.add(new StringField(FieldConstants.ID, "new-entry", Field.Store.YES));
                    doc.add(new StringField(FieldConstants.WEBSITE_HANDLE, "upgrade", Field.Store.YES));
                    doc.add(new TextField(FieldConstants.CONTENT, "Written by Roller 7", Field.Store.NO));
                    try {
                        beginWriting().addDocument(doc);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    } finally {
                        endWriting();
                    }
                }
            }.run();
            assertEquals(3, search(manager));
        } finally {
            manager.shutdown();
        }
    }

    private void unzip(String fixture) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/search-index/" + fixture);
             ZipInputStream zip = new ZipInputStream(in)) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                Files.copy(zip, indexDir.resolve(Path.of(entry.getName()).getFileName()));
            }
        }
    }

    private LuceneIndexManager manager(ThreadManager threads, WeblogEntryManager entries) {
        Weblogger roller = mock(Weblogger.class);
        when(roller.getThreadManager()).thenReturn(threads);
        when(roller.getWeblogEntryManager()).thenReturn(entries);
        WebloggerConfig.init();
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getProperty("search.index.dir"))
                    .thenReturn(indexDir.toString());
            return new LuceneIndexManager(roller);
        }
    }

    private static WeblogEntry entry(String id, String text) {
        Weblog weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("upgrade");
        Timestamp time = new Timestamp(System.currentTimeMillis());
        WeblogEntry entry = mock(WeblogEntry.class);
        when(entry.getId()).thenReturn(id);
        when(entry.getWebsite()).thenReturn(weblog);
        when(entry.getTitle()).thenReturn(id);
        when(entry.getLocale()).thenReturn("en_US");
        when(entry.getText()).thenReturn(text);
        when(entry.getUpdateTime()).thenReturn(time);
        when(entry.getPubTime()).thenReturn(time);
        return entry;
    }

    /** Number of entries matching "roller", or -1 if the search failed. */
    private static int search(LuceneIndexManager manager) {
        SearchOperation search = new SearchOperation(manager);
        search.setTerm("roller");
        search.run();
        return search.getResultsCount();
    }
}
