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
package org.tightblog.business.search.tasks;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.search.FieldConstants;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.WeblogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Task for updating indexing information for a weblog entry.
 */
public class IndexEntryTask extends AbstractIndexTask {

    private static Logger log = LoggerFactory.getLogger(IndexEntryTask.class);
    private WeblogEntry weblogEntry;
    private WeblogEntryManager weblogEntryManager;
    private boolean deleteOnly;

    /**
     * Updates the indexing information for a weblog entry.
     * @param weblogEntry entry to index
     * @param deleteOnly If true just remove the weblog entry from the index.
     */
    public IndexEntryTask(WeblogEntryManager wem, IndexManager mgr,
                          WeblogEntry weblogEntry, boolean deleteOnly) {
        super(mgr);
        this.weblogEntryManager = wem;
        this.weblogEntry = weblogEntry;
        this.deleteOnly = deleteOnly;
    }

    public void doRun() {
        IndexWriter writer = beginWriting();
        try {
            if (writer != null) {
                // Delete Doc
                Term term = new Term(FieldConstants.ID, weblogEntry.getId());
                writer.deleteDocuments(term);

                if (!deleteOnly) {
                    // since this task is normally run on a separate thread we must treat
                    // the weblog object passed in as a detached JPA entity object with
                    // potentially obsolete data, so requery for the object now
                    this.weblogEntry = weblogEntryManager.getWeblogEntry(this.weblogEntry.getId(), false);
                    if (weblogEntry != null) {
                        writer.addDocument(getDocument(weblogEntry));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Problems adding/deleting doc to index", e);
        } finally {
            endWriting(writer);
        }
    }
}
