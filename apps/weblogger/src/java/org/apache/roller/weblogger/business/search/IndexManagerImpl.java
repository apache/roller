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

package org.apache.roller.weblogger.business.search;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.*;
import org.apache.roller.weblogger.business.search.operations.AddEntryOperation;
import org.apache.roller.weblogger.business.search.operations.IndexOperation;
import org.apache.roller.weblogger.business.search.operations.ReIndexEntryOperation;
import org.apache.roller.weblogger.business.search.operations.RebuildWebsiteIndexOperation;
import org.apache.roller.weblogger.business.search.operations.RemoveEntryOperation;
import org.apache.roller.weblogger.business.search.operations.RemoveWebsiteIndexOperation;
import org.apache.roller.weblogger.business.search.operations.WriteToIndexOperation;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;

import EDU.oswego.cs.dl.util.concurrent.ReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.commons.lang.StringUtils;


/**
 * Lucene implementation of IndexManager. This is the central entry point 
 * into the Lucene searching API.
 * @author Mindaugas Idzelis (min@idzelis.com)
 * @author mraible (formatting and making indexDir configurable)
 */
@com.google.inject.Singleton
public class IndexManagerImpl implements IndexManager {
    //~ Static fields/initializers
    // =============================================
    
    private IndexReader reader;
    private final Weblogger roller;
    
    static Log mLogger = LogFactory.getFactory().getInstance(
            IndexManagerImpl.class);
    
    //~ Instance fields
    // ========================================================
    
    private boolean searchEnabled = true;
    
    File indexConsistencyMarker;
    
    private boolean useRAMIndex = false;
    
    private RAMDirectory fRAMindex;
    
    private String indexDir = null;
    
    private boolean inconsistentAtStartup = false;
    
    private ReadWriteLock rwl = new WriterPreferenceReadWriteLock();
    
    //~ Constructors
    // ===========================================================
    
    /**
     * Creates a new lucene index manager. This should only be created once.
     * Creating the index manager more than once will definately result in
     * errors. The preferred way of getting an index is through the
     * RollerContext.
     *
     * @param indexDir -
     *            the path to the index directory
     */
    @com.google.inject.Inject
    protected IndexManagerImpl(Weblogger roller) {
        this.roller = roller;

        // check config to see if the internal search is enabled
        String enabled = WebloggerConfig.getProperty("search.enabled");
        if("false".equalsIgnoreCase(enabled))
            this.searchEnabled = false;
        
        // we also need to know what our index directory is
        // Note: system property expansion is now handled by WebloggerConfig
        String searchIndexDir = WebloggerConfig.getProperty("search.index.dir");
        this.indexDir = searchIndexDir.replace('/', File.separatorChar);
        
        // a little debugging
        mLogger.info("search enabled: " + this.searchEnabled);
        mLogger.info("index dir: " + this.indexDir);
        
        String test = indexDir + File.separator + ".index-inconsistent";
        indexConsistencyMarker = new File(test);
    }
    
    
    /**
     * @inheritDoc
     */
    public void initialize() throws InitializationException {
        
        // only initialize the index if search is enabled
        if (this.searchEnabled) {
            
            // 1. If inconsistency marker exists.
            //     Delete index
            // 2. if we're using RAM index
            //     load ram index wrapper around index
            //
            if (indexConsistencyMarker.exists()) {
                getFSDirectory(true);
                inconsistentAtStartup = true;
            } else {
                try {
                    File makeIndexDir = new File(indexDir);
                    if (!makeIndexDir.exists()) {
                        makeIndexDir.mkdirs();
                        inconsistentAtStartup = true;
                    }
                    indexConsistencyMarker.createNewFile();
                } catch (IOException e) {
                    mLogger.error(e);
                }
            }
            
            if (indexExists()) {
                if (useRAMIndex) {
                    Directory filesystem = getFSDirectory(false);
                    
                    try {
                        fRAMindex = new RAMDirectory(filesystem);
                    } catch (IOException e) {
                        mLogger.error("Error creating in-memory index", e);
                    }
                }
            } else {
                if (useRAMIndex) {
                    fRAMindex = new RAMDirectory();
                    createIndex(fRAMindex);
                } else {
                    createIndex(getFSDirectory(true));
                }
            }
            
            if (isInconsistentAtStartup()) {
                mLogger.info(
                        "Index was inconsistent. Rebuilding index in the background...");
                try {
                    rebuildWebsiteIndex();
                } catch (WebloggerException e) {
                    mLogger.error("ERROR: scheduling re-index operation");
                }
            }
        }
        
    }
    
    
    //~ Methods
    // ================================================================
    
    public void rebuildWebsiteIndex() throws WebloggerException {
        scheduleIndexOperation(
                new RebuildWebsiteIndexOperation(roller, this, null));
    }
    
    public void rebuildWebsiteIndex(Weblog website) throws WebloggerException {
        scheduleIndexOperation(
                new RebuildWebsiteIndexOperation(roller, this, website));
    }
    
    public void removeWebsiteIndex(Weblog website) throws WebloggerException {
        scheduleIndexOperation(
                new RemoveWebsiteIndexOperation(roller, this, website));
    }
    
    public void addEntryIndexOperation(WeblogEntry entry) throws WebloggerException {
        AddEntryOperation addEntry = new AddEntryOperation(roller, this, entry);
        scheduleIndexOperation(addEntry);
    }
    
    public void addEntryReIndexOperation(WeblogEntry entry) throws WebloggerException {
        ReIndexEntryOperation reindex = new ReIndexEntryOperation(roller, this, entry);
        scheduleIndexOperation(reindex);
    }
    
    public void removeEntryIndexOperation(WeblogEntry entry) throws WebloggerException {
        RemoveEntryOperation removeOp = new RemoveEntryOperation(roller, this, entry);
        executeIndexOperationNow(removeOp);
    }
    
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
        try {
            // only if search is enabled
            if(this.searchEnabled) {
                mLogger.debug("Starting scheduled index operation: "+op.getClass().getName());
                roller.getThreadManager().executeInBackground(op);
            }
        } catch (InterruptedException e) {
            mLogger.error("Error executing operation", e);
        }
    }
    
    /**
     * @param search
     */
    public void executeIndexOperationNow(final IndexOperation op) {
        try {
            // only if search is enabled
            if(this.searchEnabled) {
                mLogger.debug("Executing index operation now: "+op.getClass().getName());
                roller.getThreadManager().executeInForeground(op);
            }
        } catch (InterruptedException e) {
            mLogger.error("Error executing operation", e);
        }
    }
    
    public synchronized void resetSharedReader() {
        reader = null;
    }
    public synchronized IndexReader getSharedIndexReader() {
        if (reader == null) {
            try {
                reader = IndexReader.open(getIndexDirectory());
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
    public Directory getIndexDirectory() {
        if (useRAMIndex) {
            return fRAMindex;
        } else {
            return getFSDirectory(false);
        }
    }
    
    private boolean indexExists() {
        return IndexReader.indexExists(indexDir);
    }
    
    Directory getFSDirectory(boolean delete) {
        Directory directory = null;
        
        try {
            directory = FSDirectory.getDirectory(indexDir, delete);
        } catch (IOException e) {
            mLogger.error("Problem accessing index directory", e);
        }
        
        return directory;
    }
    
    private void createIndex(Directory dir) {
        IndexWriter writer = null;
        
        try {
            writer = new IndexWriter(dir, IndexManagerImpl.getAnalyzer(), true);
        } catch (IOException e) {
            mLogger.error("Error creating index", e);
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
                    writer = new IndexWriter(fsdir, IndexManagerImpl
                            .getAnalyzer(), true);
                    
                    writer.addIndexes(new Directory[] { dir });
                    indexConsistencyMarker.delete();
                } catch (IOException e) {
                    mLogger.error("Problem saving index to disk", e);
                    
                    // Delete the directory, since there was a problem saving
                    // the RAM contents
                    getFSDirectory(true);
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e1) {
                        mLogger.warn("Unable to close IndexWriter.");
                    }
                }
                
            }
        };
    }
    
    public void release() {
        // no-op
    }
    
    public void shutdown() {
        if (useRAMIndex) {
            scheduleIndexOperation(getSaveIndexOperation());
        } else {
            indexConsistencyMarker.delete();
        }
        
        try {
            if (reader != null)
                reader.close();
        } catch (IOException e) {
            // won't happen, since it was
        }
    }

}
