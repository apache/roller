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
package org.tightblog.service.indexer;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.WeblogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.dao.WeblogEntryDao;

import java.io.IOException;

/**
 * Task for updating indexing information for a weblog entry.
 */
public class IndexEntryTask extends AbstractIndexTask {

    private static Logger log = LoggerFactory.getLogger(IndexEntryTask.class);
    private WeblogEntry weblogEntry;
    private WeblogEntryDao weblogEntryDao;
    private boolean deleteOnly;

    /**
     * Updates the indexing information for a weblog entry.
     * @param weblogEntry entry to index
     * @param deleteOnly If true just remove the weblog entry from the index.
     */
    public IndexEntryTask(WeblogEntryDao weblogEntryDao, LuceneIndexer indexer,
                          WeblogEntry weblogEntry, boolean deleteOnly) {
        super(indexer);
        this.weblogEntryDao = weblogEntryDao;
        this.weblogEntry = weblogEntry;
        this.deleteOnly = deleteOnly;
    }

    public void doRun() {
        try (IndexWriter writer = beginWriting()) {
            if (writer != null) {
                // Delete Doc
                Term term = new Term(FieldConstants.ID, weblogEntry.getId());
                writer.deleteDocuments(term);

                if (!deleteOnly) {
                    // since this task is normally run on a separate thread we must treat
                    // the weblog object passed in as a detached JPA entity object with
                    // potentially obsolete data, so requery for the object now
                    this.weblogEntry = weblogEntryDao.findByIdOrNull(this.weblogEntry.getId());
                    if (weblogEntry != null) {
                        writer.addDocument(getDocument(weblogEntry));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Problems adding/deleting doc to index", e);
        }
    }
}
