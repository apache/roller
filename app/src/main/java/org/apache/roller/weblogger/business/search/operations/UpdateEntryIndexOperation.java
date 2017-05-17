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
package org.apache.roller.weblogger.business.search.operations;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An operation that adds a new log entry into the index.
 *
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class UpdateEntryIndexOperation extends WriteToIndexOperation {

    private static Logger log = LoggerFactory.getLogger(UpdateEntryIndexOperation.class);
    private WeblogEntry weblogEntry;
    private WeblogEntryManager weblogEntryManager;
    private boolean deleteOnly;

    /**
     * Adds a web log entry into the index.
     */
    public UpdateEntryIndexOperation(WeblogEntryManager wem, IndexManager mgr,
                                     WeblogEntry weblogEntry, boolean deleteOnly) {
        super(mgr);
        this.weblogEntryManager = wem;
        this.weblogEntry = weblogEntry;
        this.deleteOnly = deleteOnly;
    }

    public void doRun() {
        // since this operation can be run on a separate thread we must treat
        // the weblog object passed in as a detached object which is prone to
        // lazy initialization problems, so requery for the object now
        this.weblogEntry = weblogEntryManager.getWeblogEntry(this.weblogEntry.getId(), false);

        IndexWriter writer = beginWriting();
        try {
            if (writer != null) {
                // Delete Doc
                Term term = new Term(FieldConstants.ID, weblogEntry.getId());
                writer.deleteDocuments(term);

                if (!deleteOnly) {
                    // Add Doc
                    writer.addDocument(getDocument(weblogEntry));
                }
            }
        } catch (IOException e) {
            log.error("Problems adding/deleting doc to index", e);
        } finally {
            endWriting();
        }
    }
}
