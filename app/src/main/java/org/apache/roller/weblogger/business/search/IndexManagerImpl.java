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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.search;

import java.io.File;
import java.io.IOException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.operations.AddEntryOperation;
import org.apache.roller.weblogger.business.search.operations.IndexOperation;
import org.apache.roller.weblogger.business.search.operations.ReIndexEntryOperation;
import org.apache.roller.weblogger.business.search.operations.RebuildWeblogIndexOperation;
import org.apache.roller.weblogger.business.search.operations.RemoveEntryOperation;
import org.apache.roller.weblogger.business.search.operations.RemoveWeblogIndexOperation;
import org.apache.roller.weblogger.business.search.operations.WriteToIndexOperation;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.config.WebloggerConfig;

import javax.annotation.PreDestroy;

/**
 * Lucene implementation of IndexManager. This is the central entry point into
 * the Lucene searching API.
 */
public class IndexManagerImpl implements IndexManager {
    // ~ Static fields/initializers
    // =============================================

    private IndexReader reader;
    private WeblogEntryManager weblogEntryManager;
    private final ExecutorService serviceScheduler;

    private final int MAX_TOKEN_COUNT = 100;

    static Log log = LogFactory.getFactory().getInstance(IndexManagerImpl.class);

    // ~ Instance fields
    // ========================================================

    private boolean searchEnabled = true;

    private boolean indexComments = true;

    File indexConsistencyMarker;

    private boolean useRAMIndex = false;

    private RAMDirectory fRAMindex;

    private String indexDir = null;

    private boolean inconsistentAtStartup = false;

    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    // ~ Constructors
    // ===========================================================

    /**
     * Creates a new lucene index manager. This should only be created once.
     * Creating the index manager more than once will definately result in
     * errors. The preferred way of getting an index is through the
     * RollerContext.
     */
    protected IndexManagerImpl() {
        serviceScheduler = Executors.newCachedThreadPool();

        // we also need to know what our index directory is
        // Note: system property expansion is now handled by WebloggerConfig
        String searchIndexDir = WebloggerConfig.getProperty("search.index.dir");
        this.indexDir = searchIndexDir.replace('/', File.separatorChar);

        // a little debugging
        log.info("search enabled: " + this.searchEnabled);
        log.info("index dir: " + this.indexDir);

        String test = indexDir + File.separator + ".index-inconsistent";
        indexConsistencyMarker = new File(test);
    }

    @Override
    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    @Override
    public boolean isIndexComments() {
        return indexComments;
    }

    public void setIndexComments(boolean indexComments) {
        this.indexComments = indexComments;
    }

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Override
    public void initialize() throws WebloggerException {

        // only initialize the index if search is enabled
        if (this.searchEnabled) {

            // 1. If inconsistency marker exists.
            // Delete index
            // 2. if we're using RAM index
            // load ram index wrapper around index
            //
            if (indexConsistencyMarker.exists()) {
                getFSDirectory(true);
                inconsistentAtStartup = true;
                log.debug("Index inconsistent: marker exists");
            } else {
                try {
                    File makeIndexDir = new File(indexDir);
                    if (!makeIndexDir.exists()) {
                        makeIndexDir.mkdirs();
                        inconsistentAtStartup = true;
                        log.debug("Index inconsistent: new");
                    }
                    indexConsistencyMarker.createNewFile();
                } catch (IOException e) {
                    log.error(e);
                }
            }

            if (indexExists()) {
                if (useRAMIndex) {
                    FSDirectory filesystem = getFSDirectory(false);
                    try {
                        fRAMindex = new RAMDirectory(filesystem, IOContext.DEFAULT);
                    } catch (IOException e) {
                        log.error("Error creating in-memory index", e);
                    }
                }
            } else {
                log.debug("Creating index");
                inconsistentAtStartup = true;
                if (useRAMIndex) {
                    fRAMindex = new RAMDirectory();
                    createIndex(fRAMindex);
                } else {
                    createIndex(getFSDirectory(true));
                }
            }

            if (isInconsistentAtStartup()) {
                log.info("Index was inconsistent. Rebuilding index in the background...");
                try {
                    rebuildWeblogIndex();
                } catch (WebloggerException e) {
                    log.error("ERROR: scheduling re-index operation");
                }
            } else {
                log.info("Index initialized and ready for use.");
            }
        }

    }

    // ~ Methods
    // ================================================================

    public void rebuildWeblogIndex() throws WebloggerException {
        scheduleIndexOperation(new RebuildWeblogIndexOperation(this, weblogEntryManager,
                null));
    }

    public void rebuildWeblogIndex(Weblog weblog) throws WebloggerException {
        scheduleIndexOperation(new RebuildWeblogIndexOperation(this, weblogEntryManager,
                weblog));
    }

    public void removeWeblogIndexOperation(Weblog weblog) throws WebloggerException {
        scheduleIndexOperation(new RemoveWeblogIndexOperation(this, weblog.getHandle()));
    }

    public void addEntryIndexOperation(WeblogEntry entry)
            throws WebloggerException {
        AddEntryOperation addEntry = new AddEntryOperation(weblogEntryManager, this, entry);
        scheduleIndexOperation(addEntry);
    }

    public void addEntryReIndexOperation(WeblogEntry entry)
            throws WebloggerException {
        ReIndexEntryOperation reindex = new ReIndexEntryOperation(weblogEntryManager, this,
                entry);
        scheduleIndexOperation(reindex);
    }

    public void removeEntryIndexOperation(WeblogEntry entry)
            throws WebloggerException {
        RemoveEntryOperation removeOp = new RemoveEntryOperation(this, entry.getId());
        executeIndexOperationNow(removeOp);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return rwl;
    }

    public boolean isInconsistentAtStartup() {
        return inconsistentAtStartup;
    }

    /**
     * This is the analyzer that will be used to tokenize comment text.
     * 
     * @return Analyzer to be used in manipulating the database.
     */
    public static final Analyzer getAnalyzer() {
        return new StandardAnalyzer();
    }

    private void scheduleIndexOperation(final IndexOperation op) {
        if (this.searchEnabled) {
            log.debug("Starting scheduled index operation: " + op.getClass().getName());
            serviceScheduler.submit(op);
        }
    }

    /**
     * @param op
     */
    public void executeIndexOperationNow(final IndexOperation op) {
        if (this.searchEnabled) {
            log.debug("Executing index operation now: " + op.getClass().getName());
            op.run();
        }
    }

    @Override
    public synchronized void resetSharedReader() {
        reader = null;
    }

    @Override
    public synchronized IndexReader getSharedIndexReader() {
        if (reader == null) {
            try {
                reader = DirectoryReader.open(getIndexDirectory());
            } catch (IOException e) {
            }
        }
        return reader;
    }

    /**
     * Get the directory that is used by the lucene index. This method will
     * return null if there is no index at the directory location. If we are
     * using a RAM index, the directory will be a ram directory.
     * 
     * @return Directory The directory containing the index, or null if error.
     */
    @Override
    public Directory getIndexDirectory() {
        if (useRAMIndex) {
            return fRAMindex;
        } else {
            return getFSDirectory(false);
        }
    }

    private boolean indexExists() {
        try {
            return DirectoryReader.indexExists(getIndexDirectory());
        } catch (IOException e) {
            log.error("Problem accessing index directory", e);
        }
        return false;
    }

    private FSDirectory getFSDirectory(boolean delete) {

        FSDirectory directory = null;

        try {

            directory = FSDirectory.open(new File(indexDir).toPath());

            if (delete && directory != null) {
                // clear old files
                String[] files = directory.listAll();
                for (int i = 0; i < files.length; i++) {
                    File file = new File(indexDir, files[i]);
                    if (!file.delete()) {
                        throw new IOException("couldn't delete " + files[i]);
                    }
                }
            }

        } catch (IOException e) {
            log.error("Problem accessing index directory", e);
        }

        return directory;

    }

    private void createIndex(Directory dir) {
        IndexWriter writer = null;

        try {

            IndexWriterConfig config = new IndexWriterConfig(
                    new LimitTokenCountAnalyzer(
                            IndexManagerImpl.getAnalyzer(), MAX_TOKEN_COUNT));

            writer = new IndexWriter(dir, config);

        } catch (IOException e) {
            log.error("Error creating index", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private IndexOperation getSaveIndexOperation() {
        return new WriteToIndexOperation(this) {
            public void doRun() {
                Directory dir = getIndexDirectory();
                Directory fsdir = getFSDirectory(true);
                IndexWriter writer = null;
                try {
                    IndexWriterConfig config = new IndexWriterConfig(
                            new LimitTokenCountAnalyzer(IndexManagerImpl.getAnalyzer(), MAX_TOKEN_COUNT));
                    writer = new IndexWriter(fsdir, config);
                    writer.addIndexes(new Directory[] { dir });
                    writer.commit();
                    indexConsistencyMarker.delete();
                } catch (IOException e) {
                    log.error("Problem saving index to disk", e);
                    // Delete the directory, since there was a problem saving the RAM contents
                    getFSDirectory(true);
                } finally {
                    try {
                        if (writer != null) {
                            writer.close();
                        }
                    } catch (IOException e1) {
                        log.warn("Unable to close IndexWriter.");
                    }
                }
            }
        };
    }

    @Override
    @PreDestroy
    public void shutdown() {
        // trigger an immediate shutdown of any backgrounded tasks
        serviceScheduler.shutdownNow();
        try {
            serviceScheduler.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.debug(e.getMessage(), e);
        }

        if (!useRAMIndex) {
            indexConsistencyMarker.delete();
        }

        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
        }
    }

}
