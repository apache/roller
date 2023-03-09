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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.SearchResultList;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;

/**
 * Lucene implementation of IndexManager. This is the central entry point into
 * the Lucene searching API.
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 * @author mraible (formatting and making indexDir configurable)
 */
@com.google.inject.Singleton
public class LuceneIndexManager implements IndexManager {

    private IndexReader reader;
    private final Weblogger roller;

    private final static Log logger = LogFactory.getFactory().getInstance(LuceneIndexManager.class);

    private boolean searchEnabled = true;

    private final String indexDir;

    private final File indexConsistencyMarker;

    private boolean inconsistentAtStartup = false;

    private final ReadWriteLock rwl = new ReentrantReadWriteLock();


    /**
     * Creates a new lucene index manager. This should only be created once.
     * Creating the index manager more than once will definitely result in
     * errors. The preferred way of getting an index is through the
     * RollerContext.
     * 
     * @param roller - the weblogger instance
     */
    @com.google.inject.Inject
    protected LuceneIndexManager(Weblogger roller) {
        this.roller = roller;

        // check config to see if the internal search is enabled
        String enabled = WebloggerConfig.getProperty("search.enabled");
        if ("false".equalsIgnoreCase(enabled)) {
            this.searchEnabled = false;
        }

        // we also need to know what our index directory is
        // Note: system property expansion is now handled by WebloggerConfig
        String searchIndexDir = WebloggerConfig.getProperty("search.index.dir");
        this.indexDir = searchIndexDir.replace('/', File.separatorChar);

        // a little debugging
        logger.info("search enabled: " + this.searchEnabled);
        logger.info("index dir: " + this.indexDir);

        String test = indexDir + File.separator + ".index-inconsistent";
        indexConsistencyMarker = new File(test);
    }

    /**
     * @inheritDoc
     */
    @Override
    public void initialize() throws InitializationException {

        // only initialize the index if search is enabled
        if (this.searchEnabled) {

            // delete index if inconsistency marker exists
            if (indexConsistencyMarker.exists()) {
                logger.debug("Index inconsistent: marker exists");
                inconsistentAtStartup = true;
                deleteIndex();
            } else {
                try {
                    File makeIndexDir = new File(indexDir);
                    if (!makeIndexDir.exists()) {
                        makeIndexDir.mkdirs();
                        inconsistentAtStartup = true;
                        logger.debug("Index inconsistent: new");
                    }
                    indexConsistencyMarker.createNewFile();
                } catch (IOException e) {
                    logger.error(e);
                }
            }

            if (indexExists()) {

                // test if the index is readable, if the version is outdated or it fails we rebuild.
                try {
                    synchronized(this) {
                        reader = DirectoryReader.open(getIndexDirectory());
                    }
                } catch (IOException | IllegalArgumentException ex) {  // IAE for incompatible codecs
                    logger.warn("Failed to open search index, scheduling rebuild.", ex);
                    inconsistentAtStartup = true;
                    deleteIndex();
                }
            } else {
                logger.debug("Creating index");
                inconsistentAtStartup = true;
                deleteIndex();
                createIndex(getIndexDirectory());
            }

            if (inconsistentAtStartup) {
                logger.info("Index was inconsistent. Rebuilding index in the background...");
                try {
                    rebuildWeblogIndex();
                } catch (WebloggerException ex) {
                    logger.error("ERROR: scheduling re-index operation", ex);
                }
            } else {
                logger.info("Index initialized and ready for use.");
            }
        }

    }

    @Override
    public void rebuildWeblogIndex() throws WebloggerException {
        scheduleIndexOperation(new RebuildWebsiteIndexOperation(roller, this, null));
    }

    @Override
    public void rebuildWeblogIndex(Weblog website) throws WebloggerException {
        scheduleIndexOperation(new RebuildWebsiteIndexOperation(roller, this, website));
    }

    @Override
    public void removeWeblogIndex(Weblog website) throws WebloggerException {
        scheduleIndexOperation(new RemoveWebsiteIndexOperation(roller, this, website));
    }

    @Override
    public void addEntryIndexOperation(WeblogEntry entry) throws WebloggerException {
        scheduleIndexOperation(new AddEntryOperation(roller, this, entry));
    }

    @Override
    public void addEntryReIndexOperation(WeblogEntry entry) throws WebloggerException {
        scheduleIndexOperation(new ReIndexEntryOperation(roller, this, entry));
    }

    @Override
    public void removeEntryIndexOperation(WeblogEntry entry) throws WebloggerException {
        executeIndexOperationNow(new RemoveEntryOperation(roller, this, entry));
    }

    @Override
    public SearchResultList search(
        String term,
        String weblogHandle,
        String category,
        String locale,
        int pageNum,
        int entryCount,
        URLStrategy urlStrategy) throws WebloggerException {

        SearchOperation search = new SearchOperation(this);
        search.setTerm(term);
        boolean weblogSpecific = !WebloggerRuntimeConfig.isSiteWideWeblog(weblogHandle);
        if (weblogSpecific) {
            search.setWeblogHandle(weblogHandle);
        }
        if (category != null) {
            search.setCategory(category);
        }
        if (locale != null) {
            search.setLocale(locale);
        }

        executeIndexOperationNow(search);
        if (search.getResultsCount() >= 0) {
            TopFieldDocs docs = search.getResults();
            ScoreDoc[] hitsArr = docs.scoreDocs;
            return convertHitsToEntryList(
                hitsArr,
                search,
                pageNum,
                entryCount,
                weblogHandle,
                weblogSpecific,
                urlStrategy);
        }
        throw new WebloggerException("Error executing search");
    }

    public ReadWriteLock getReadWriteLock() {
        return rwl;
    }

    @Override
    public boolean isInconsistentAtStartup() {
        return inconsistentAtStartup;
    }

    /**
     * This is the analyzer that will be used to tokenize comment text.
     * 
     * @return Analyzer to be used in manipulating the database.
     */
    public static final Analyzer getAnalyzer() {
        return instantiateAnalyzer();
    }

    private static Analyzer instantiateAnalyzer() {
        final String className = WebloggerConfig.getProperty("lucene.analyzer.class");
        try {
            final Class<?> clazz = Class.forName(className);
            return (Analyzer) ConstructorUtils.invokeConstructor(clazz, null);
        } catch (final ClassNotFoundException e) {
            logger.error("failed to lookup analyzer class: " + className, e);
            return instantiateDefaultAnalyzer();
        } catch (final NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("failed to instantiate analyzer: " + className, e);
            return instantiateDefaultAnalyzer();
        }
    }

    private static Analyzer instantiateDefaultAnalyzer() {
        return new StandardAnalyzer();
    }

    private void scheduleIndexOperation(final IndexOperation op) {
        try {
            // only if search is enabled
            if (this.searchEnabled) {
                logger.debug("Starting scheduled index operation: "
                        + op.getClass().getName());
                roller.getThreadManager().executeInBackground(op);
            }
        } catch (InterruptedException e) {
            logger.error("Error executing operation", e);
        }
    }

    /**
     * @param op
     */
    private void executeIndexOperationNow(final IndexOperation op) {
        try {
            // only if search is enabled
            if (this.searchEnabled) {
                logger.debug("Executing index operation now: " + op.getClass().getName());
                roller.getThreadManager().executeInForeground(op);
            }
        } catch (InterruptedException e) {
            logger.error("Error executing operation", e);
        }
    }

    public synchronized void resetSharedReader() {
        reader = null;
    }

    public synchronized IndexReader getSharedIndexReader() {
        if (reader == null) {
            try {
                reader = DirectoryReader.open(getIndexDirectory());
            } catch (IOException ex) {
                logger.error("Error opening DirectoryReader", ex);
                throw new RuntimeException(ex);
            }
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

        try {
            return FSDirectory.open(Path.of(indexDir));
        } catch (IOException e) {
            logger.error("Problem accessing index directory", e);
        }
        return null;
    }

    private boolean indexExists() {
        try {
            return DirectoryReader.indexExists(getIndexDirectory());
        } catch (IOException e) {
            logger.error("Problem accessing index directory", e);
        }
        return false;
    }

    
    private void deleteIndex() {
        
        try(FSDirectory directory = FSDirectory.open(Path.of(indexDir))) {
            
            String[] files = directory.listAll();
            for (String file : files) {
                Files.delete(Path.of(indexDir, file));
            }
        } catch (IOException ex) {
             logger.error("Problem accessing index directory", ex);
        }

    }

    private void createIndex(Directory dir) {
        IndexWriter writer = null;

        try {

            IndexWriterConfig config = new IndexWriterConfig(
                    new LimitTokenCountAnalyzer(
                            LuceneIndexManager.getAnalyzer(), 128));

            writer = new IndexWriter(dir, config);

        } catch (IOException e) {
            logger.error("Error creating index", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    logger.warn("Unable to close IndexWriter.", ex);
                }
            }
        }
    }

    @Override
    public void release() {
        // no-op
    }

    @Override
    public void shutdown() {
        
        indexConsistencyMarker.delete();

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.error("Unable to close reader.", ex);
            }
        }
    }

    /**
     * Convert hits to entries.
     *
     * @param hits
     *            the hits
     * @param search
     *            the search
     * @throws WebloggerException
     *             the weblogger exception
     */
    static SearchResultList convertHitsToEntryList(
        ScoreDoc[] hits,
        SearchOperation search,
        int pageNum,
        int entryCount,
        String weblogHandle,
        boolean websiteSpecificSearch,
        URLStrategy urlStrategy)
        throws WebloggerException {

        List<WeblogEntryWrapper> results = new ArrayList<>();

        // determine offset
        int offset = pageNum * entryCount;
        if (offset >= hits.length) {
            offset = 0;
        }

        // determine limit
        int limit = entryCount;
        if (offset + limit > hits.length) {
            limit = hits.length - offset;
        }

        try {
            Set<String> categories = new TreeSet<>();
            TreeSet<String> categorySet = new TreeSet<>();
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();

            WeblogEntry entry;
            Document doc;
            String handle;
            Timestamp now = new Timestamp(new Date().getTime());
            for (int i = offset; i < offset + limit; i++) {
                doc = search.getSearcher().doc(hits[i].doc);
                handle = doc.getField(FieldConstants.WEBSITE_HANDLE).stringValue();
                entry = weblogMgr.getWeblogEntry(doc.getField(FieldConstants.ID).stringValue());

                if (!(websiteSpecificSearch && handle.equals(weblogHandle))
                    && doc.getField(FieldConstants.CATEGORY) != null) {
                    categorySet.add(doc.getField(FieldConstants.CATEGORY).stringValue());
                }

                // maybe null if search result returned inactive user
                // or entry's user is not the requested user.
                // but don't return future posts
                if (entry != null && entry.getPubTime().before(now)) {
                    results.add(WeblogEntryWrapper.wrap(entry, urlStrategy));
                }
            }

            if (!categorySet.isEmpty()) {
                categories = categorySet;
            }

            return new SearchResultList(results, categories, limit, offset);

        } catch (IOException e) {
            throw new WebloggerException(e);
        }
    }
}
