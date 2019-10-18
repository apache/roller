/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.tightblog.service.indexer.AbstractTask;
import org.tightblog.service.indexer.IndexEntryTask;
import org.tightblog.service.indexer.IndexWeblogTask;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.dao.WeblogEntryDao;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lucene indexer for blog articles, to supply blog search functionality.  Can be disabled with the
 * searchEnabled property if no search functionality desired or if blogs instead rely on 3rd party
 * search tools for this functionality.
 */
@Component
public class LuceneIndexer {

    private static Logger log = LoggerFactory.getLogger(LuceneIndexer.class);

    private DirectoryReader reader;
    private WeblogEntryManager weblogEntryManager;
    private WeblogEntryDao weblogEntryDao;

    @Value("${search.analyzer.class:org.apache.lucene.analysis.standard.StandardAnalyzer}")
    private String luceneAnalyzerName;

    @Value("${search.analyzer.maxTokenCount:1000}")
    private int maxTokenCount;

    private ExecutorService serviceScheduler;
    private boolean searchEnabled;
    private boolean indexComments;
    private File indexConsistencyMarker;
    private String indexDir;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * Creates a new Lucene index manager. Just one manager should be created per instance of Tightblog.
     */
    @Autowired
    public LuceneIndexer(
            @Lazy WeblogEntryManager weblogEntryManager, @Lazy WeblogEntryDao weblogEntryDao,
            @Value("${search.include.comments:true}") boolean indexComments,
            @Value("${search.enabled:false}") boolean searchEnabled,
            @Value("${search.index.dir:#{null}}") String indexDir) {

        this.weblogEntryManager = weblogEntryManager;
        this.weblogEntryDao = weblogEntryDao;
        this.indexComments = indexComments;
        this.searchEnabled = searchEnabled;
        this.indexDir = indexDir;

        if (!searchEnabled) {
            indexComments = false;
        }

        log.info("Lucene search enabled: {} {}", searchEnabled,
                searchEnabled ? "(If not using internal search capability, can increase performance by disabling in" +
                        " TightBlog properties file)" : "(Can be activated in TightBlog properties file)");

        if (searchEnabled) {
            log.info("Include comment text as part of blog search? {}", indexComments);

            serviceScheduler = Executors.newCachedThreadPool();

            if (indexDir == null) {
                throw new IllegalStateException("Check tightblog properties file -- If search.enabled = true, " +
                        "search.index.dir must also be provided.");
            }

            String test = indexDir + File.separator + ".index-inconsistent";
            indexConsistencyMarker = new File(test);
            log.info("search index dir: {}", indexDir);
        }
    }

    /**
     * Are comments to be indexed and used for search results?
     */
    public boolean isIndexComments() {
        return indexComments;
    }

    /**
     * Initialize the Lucene indexer.
     */
    public void initialize() {

        // only initialize the index if search is enabled
        if (searchEnabled) {
            boolean indexNeedsCreating = false;

            try {
                // existing indexConsistencyMarker means this.shutdown() wasn't called at last app shutdown
                if (indexConsistencyMarker.exists()) {
                    log.info("Index was not closed properly with last shutdown; will be rebuilt");
                    indexNeedsCreating = true;
                } else {
                    // see if index directory exists, if not create
                    File testIndexDir = new File(indexDir);
                    if (!testIndexDir.exists()) {
                        if (testIndexDir.mkdirs()) {
                            indexNeedsCreating = true;
                            log.info("Index folder path {} created", testIndexDir.getAbsolutePath());
                        } else {
                            throw new IOException("Folder path " + testIndexDir.getAbsolutePath() + " could not be " +
                                    "created (file permission rights?)");
                        }
                    } else {
                        // OK, index directory exists, see if Lucene index exists within it
                        if (!DirectoryReader.indexExists(getIndexDirectory())) {
                            log.info("Lucene index not detected, will create");
                            indexNeedsCreating = true;
                        } else {
                            log.info("Lucene search index already available and ready for use.");
                        }
                    }
                }

                if (indexNeedsCreating) {
                    log.info("Generating Lucene index in the background...");
                    // deletes index consistency marker if it exists
                    createIndex(getFSDirectory(true));

                    // create index consistency marker for next app shutdown
                    if (!indexConsistencyMarker.createNewFile()) {
                        throw new IOException("Could not create index consistency marker " +
                                indexConsistencyMarker.getAbsolutePath() + " (file permission rights?)");
                    }
                    rebuildWeblogIndex();
                }

                reader = DirectoryReader.open(getIndexDirectory());

            } catch (IOException e) {
                log.error("Could not create index, searching will be deactivated.", e);
                searchEnabled = false;
            }
        }

    }

    /**
     * Update all weblog indexes
     */
    public void rebuildWeblogIndex() {
        scheduleIndexOperation(new IndexWeblogTask(this, weblogEntryManager, null, false));
    }

    /**
     * Update a single weblog index
     * @param weblog Weblog to update.
     * @param remove If true, remove the weblog from the index.  If false, adds/updates weblog.
     */
    public void updateIndex(Weblog weblog, boolean remove) {
        scheduleIndexOperation(new IndexWeblogTask(this, weblogEntryManager, weblog, remove));
    }

    /**
     * Update a single weblog entry
     * @param entry Weblog entry to update.
     * @param remove If true, remove the weblog entry from the index.  If false, adds/updates weblog entry.
     */
    public void updateIndex(WeblogEntry entry, boolean remove) {
        scheduleIndexOperation(new IndexEntryTask(weblogEntryDao, this, entry, remove));
    }

    /**
     * Retrieve common ReadWriteLock for indexing and searching
     */
    public ReadWriteLock getReadWriteLock() {
        return rwl;
    }

    /**
     * This is the analyzer that will be used to tokenize comment text.
     *
     * @return Analyzer to be used in manipulating the database.
     */
    public Analyzer getAnalyzer() {
        try {
            return (Analyzer) Class.forName(luceneAnalyzerName).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("Cannot instantiate class {}", luceneAnalyzerName, e);
        }
        return null;
    }

    /**
     * Get maximum number of tokens to parse out of anything being indexed
     */
    public int getMaxTokenCount() {
        return maxTokenCount;
    }

    /**
     * Execute task immediately
     */
    public void executeIndexOperationNow(final AbstractTask op) {
        if (this.searchEnabled) {
            log.debug("Executing {}", op.getClass().getName());
            op.run();
        }
    }

    /**
     * Retrieve Lucene Directory reader to perform searches
     */
    public synchronized IndexReader getDirectoryReader() {
        try {
            DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
            if (newReader != null) {
                closeReader();
                reader = newReader;
            }
        } catch (IOException ignored) {
        }
        return reader;
    }

    /**
     * Get the directory that is used by the lucene index. This method will
     * return null if there is no index at the directory location.
     *
     * @return Directory The directory containing the index, or null if error.
     */
    public Directory getIndexDirectory() {
        return getFSDirectory(false);
    }

    private void scheduleIndexOperation(final AbstractTask op) {
        if (this.searchEnabled) {
            log.debug("Starting scheduled {}", op.getClass().getName());
            serviceScheduler.submit(op);
        }
    }

    private synchronized void closeReader() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException ignored) {
        }
    }

    private Directory getFSDirectory(boolean delete) {
        FSDirectory directory = null;

        try {
            directory = FSDirectory.open(new File(indexDir).toPath());
            if (delete) {
                // clear old files
                String[] files = directory.listAll();
                for (String fileName : files) {
                    File file = new File(indexDir, fileName);
                    if (!file.delete()) {
                        throw new IOException("couldn't delete " + fileName);
                    }
                }
            }
        } catch (IOException e) {
            log.error("Problem accessing index directory", e);
        }

        return directory;

    }

    private void createIndex(Directory dir) {
        int maxTokens = 100;

        try (Analyzer analyzer = getAnalyzer()) {
            if (analyzer != null) {
                IndexWriterConfig config = new IndexWriterConfig(new LimitTokenCountAnalyzer(analyzer, maxTokens));

                // constructor alone makes directory available for indexing
                //CHECKSTYLE.OFF: EmptyBlock
                try (IndexWriter ignored = new IndexWriter(dir, config)) {
                //CHECKSTYLE.ON: EmptyBlock
                } catch (IOException e) {
                    log.error("Error creating index", e);
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (searchEnabled) {
            // trigger an immediate shutdown of any backgrounded tasks
            serviceScheduler.shutdownNow();
            try {
                serviceScheduler.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.debug("Exception: ", e);
            }

            if (!indexConsistencyMarker.delete()) {
                log.warn("Expected index consistency marker {} not present or otherwise could not be deleted",
                        indexConsistencyMarker.getAbsolutePath());
            }
            closeReader();
        }
    }
}
