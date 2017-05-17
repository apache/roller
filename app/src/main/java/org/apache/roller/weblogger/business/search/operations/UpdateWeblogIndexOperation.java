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
package org.apache.roller.weblogger.business.search.operations;

import org.apache.commons.lang.time.DateUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

/**
 * An index operation that rebuilds a given weblog index (or all indexes).
 */
public class UpdateWeblogIndexOperation extends WriteToIndexOperation {

    private static Logger log = LoggerFactory.getLogger(UpdateWeblogIndexOperation.class);
    private Weblog website;
    private WeblogEntryManager weblogEntryManager;
    private boolean deleteOnly;

    // ~ Constructors
    // ===========================================================

    /**
     * Create a new operation that will recreate an index.
     *
     * @param website The website to rebuild the index for, or null for all users.
     */
    public UpdateWeblogIndexOperation(IndexManagerImpl mgr, WeblogEntryManager wem,
                                      Weblog website, boolean deleteOnly) {
        super(mgr);
        this.weblogEntryManager = wem;
        this.website = website;
        this.deleteOnly = deleteOnly;
    }

    // ~ Methods
    // ================================================================

    public void doRun() {
        Instant start = Instant.now();

        if (this.website != null) {
            log.debug("Reindexining weblog {}", website.getHandle());
        } else {
            log.debug("Reindexining entire site");
        }

        IndexWriter writer = beginWriting();

        try {
            if (writer != null) {

                // Delete Doc
                if (website != null) {
                    Term tWebsite = IndexOperation.getTerm(FieldConstants.WEBSITE_HANDLE, website.getHandle());

                    if (tWebsite != null) {
                        writer.deleteDocuments(tWebsite);
                    }
                } else {
                    Term all = IndexOperation.getTerm(FieldConstants.CONSTANT, FieldConstants.CONSTANT_V);
                    writer.deleteDocuments(all);
                }

                if (!deleteOnly) {
                    // Re-Add Doc
                    WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                    wesc.setWeblog(website);
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
        } finally {
            endWriting();
        }

        Instant end = Instant.now();
        double length = (end.toEpochMilli() - start.toEpochMilli()) / (double) DateUtils.MILLIS_PER_SECOND;

        if (website == null) {
            log.info("Completed updating index for all users in {} secs", length);
        } else {
            log.info("Completed updating index for weblog: '{}' in {} seconds", website.getHandle(), length);
        }
    }
}
