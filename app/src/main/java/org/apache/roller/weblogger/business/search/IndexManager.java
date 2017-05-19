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
package org.apache.roller.weblogger.business.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.roller.weblogger.business.search.operations.IndexOperation;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Interface to a blog search facility.
 */
public interface IndexManager {
    ReadWriteLock getReadWriteLock();

    /**
     * Remove weblog from index, returns immediately and operates in background
     */
    void removeWeblogIndexOperation(Weblog weblog);

    /**
     * Remove entry from index, returns immediately and operates in background
     */
    void removeEntryIndexOperation(WeblogEntry entry);

    /**
     * Reindex entry, returns immediately and operates in background
     */
    void addEntryReIndexOperation(WeblogEntry entry);

    /**
     * Execute operation immediately
     */
    void executeIndexOperationNow(final IndexOperation op);

    IndexReader getSharedIndexReader();

    /**
     * Return directory used by Lucene index
     */
    Directory getIndexDirectory();

    boolean isSearchEnabled();

    boolean isIndexComments();

    /**
     * Initialize the search system.
     */
    void initialize();

    /**
     * Shutdown to be called on application shutdown
     */
    void shutdown();

    void rebuildWeblogIndex(Weblog weblog);

    void rebuildWeblogIndex();

}
