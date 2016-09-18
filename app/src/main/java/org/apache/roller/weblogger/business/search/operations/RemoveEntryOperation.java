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
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * An operation that removes the weblog from the index.
 */
public class RemoveEntryOperation extends WriteToIndexOperation {

    // ~ Static fields/initializers
    // =============================================

    private static Logger log = LoggerFactory.getLogger(RemoveEntryOperation.class);

    // ~ Instance fields
    // ========================================================

    private String weblogEntryId;

    // ~ Constructors
    // ===========================================================

    public RemoveEntryOperation(IndexManagerImpl mgr, String weblogEntryId) {
        super(mgr);
        this.weblogEntryId = weblogEntryId;
    }

    // ~ Methods
    // ================================================================

    public void doRun() {
        IndexWriter writer = beginWriting();
        try {
            if (writer != null) {
                Term term = new Term(FieldConstants.ID, weblogEntryId);
                writer.deleteDocuments(term);
            }
        } catch (IOException e) {
            log.error("Error deleting doc from index", e);
        } finally {
            endWriting();
        }
    }

}
