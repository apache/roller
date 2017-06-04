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
package org.tightblog.business.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.tightblog.business.search.tasks.AbstractTask;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Interface to a blog search facility.
 */
public interface IndexManager {

    /**
     * Update all weblog indexes
     */
    void rebuildWeblogIndex();

    /**
     * Update a single weblog index
     * @param weblog Weblog to update.
     * @param remove If true, remove the weblog from the index.  If false, adds/updates weblog.
     */
    void updateIndex(Weblog weblog, boolean remove);

    /**
     * Update a single weblog entry
     * @param entry Weblog entry to update.
     * @param remove If true, remove the weblog entry from the index.  If false, adds/updates weblog entry.
     */
    void updateIndex(WeblogEntry entry, boolean remove);

    /**
     * Execute task immediately
     */
    void executeIndexOperationNow(final AbstractTask op);

    /**
     * Retrieve common ReadWriteLock for indexing and searching
     */
    ReadWriteLock getReadWriteLock();

    /**
     * Retrieve Lucene Directory reader to perform searches
     */
    IndexReader getDirectoryReader();

    /**
     * Return directory used by Lucene index
     */
    Directory getIndexDirectory();

    /**
     * Are comments to be indexed and used for search results?
     */
    boolean isIndexComments();

    /**
     * Initialize the search system.
     */
    void initialize();

}
