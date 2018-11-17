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
package org.tightblog.service.indexer;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntry.PubStatus;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

/**
 * Task for updating indexing information for weblogs.
 */
public class IndexWeblogTask extends AbstractIndexTask {

    private static Logger log = LoggerFactory.getLogger(IndexWeblogTask.class);
    private Weblog weblog;
    private WeblogEntryManager weblogEntryManager;
    private boolean deleteOnly;

    /**
     * Create a new task to update an index for a weblog
     * @param weblog The weblog to rebuild the index for, or null for all weblogs.
     * @param deleteOnly Remove the weblog(s) from the index.
     */
    public IndexWeblogTask(LuceneIndexer mgr, WeblogEntryManager wem,
                           Weblog weblog, boolean deleteOnly) {
        super(mgr);
        this.weblogEntryManager = wem;
        this.weblog = weblog;
        this.deleteOnly = deleteOnly;
    }

    public void doRun() {
        if (weblog == null && deleteOnly) {
            log.error("Weblog must be provided for delete task, skipping indexing");
            return;
        }

        Instant start = Instant.now();

        if (weblog == null) {
            log.info("Starting reindex of all weblogs...");
        }

        try (IndexWriter writer = beginWriting()) {
            if (writer != null) {

                // Delete all entries from given weblog(s)
                if (weblog != null) {
                    Term tWebsite = getTerm(FieldConstants.WEBLOG_HANDLE, weblog.getHandle());

                    if (tWebsite != null) {
                        writer.deleteDocuments(tWebsite);
                    }
                } else {
                    Term all = getTerm(FieldConstants.CONSTANT, FieldConstants.CONSTANT_V);
                    writer.deleteDocuments(all);
                }

                if (!deleteOnly) {
                    // Add entries from weblog(s)
                    WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                    wesc.setWeblog(weblog);
                    wesc.setStatus(PubStatus.PUBLISHED);
                    List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);

                    log.debug("Entries to index: {}", entries.size());

                    for (WeblogEntry entry : entries) {
                        writer.addDocument(getDocument(entry));
                        log.debug("Indexed entry {0}: {1}", entry.getPubTime(), entry.getAnchor());
                    }
                }
            }
        } catch (Exception e) {
            log.error("ERROR adding/deleting doc to index", e);
        }

        Instant end = Instant.now();
        double length = (end.toEpochMilli() - start.toEpochMilli()) / (double) DateUtils.MILLIS_PER_SECOND;

        if (weblog == null) {
            log.info("Indexed all weblogs in {} secs", length);
        } else {
            log.info("Indexed weblog '{}' in {} secs", weblog.getHandle(), length);
        }
    }
}
