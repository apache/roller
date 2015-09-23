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

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;

/**
 * An index operation that rebuilds a given users index (or all indexes).
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class RebuildWeblogIndexOperation extends WriteToIndexOperation {

    // ~ Static fields/initializers
    // =============================================

    private static Log mLogger = LogFactory.getFactory().getInstance(
            RebuildWeblogIndexOperation.class);

    // ~ Instance fields
    // ========================================================

    private Weblog website;
    private WeblogEntryManager weblogEntryManager;

    // ~ Constructors
    // ===========================================================

    /**
     * Create a new operation that will recreate an index.
     * 
     * @param website
     *            The website to rebuild the index for, or null for all users.
     */
    public RebuildWeblogIndexOperation(IndexManagerImpl mgr, WeblogEntryManager wem,
                                       Weblog website) {
        super(mgr);
        this.weblogEntryManager = wem;
        this.website = website;
    }

    // ~ Methods
    // ================================================================

    public void doRun() {
        Date start = new Date();

        if (this.website != null) {
            mLogger.debug("Reindexining weblog " + website.getHandle());
        } else {
            mLogger.debug("Reindexining entire site");
        }

        IndexWriter writer = beginWriting();

        try {
            if (writer != null) {

                // Delete Doc
                Term tWebsite = null;
                if (website != null) {
                    tWebsite = IndexOperation.getTerm(FieldConstants.WEBSITE_HANDLE,
                            website.getHandle());
                }
                if (tWebsite != null) {
                    writer.deleteDocuments(tWebsite);
                } else {
                    Term all = IndexOperation.getTerm(FieldConstants.CONSTANT,
                            FieldConstants.CONSTANT_V);
                    writer.deleteDocuments(all);
                }

                // Add Doc
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(website);
                wesc.setStatus(PubStatus.PUBLISHED);
                List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);

                mLogger.debug("Entries to index: " + entries.size());

                for (WeblogEntry entry : entries) {
                    writer.addDocument(getDocument(entry));
                    mLogger.debug(MessageFormat.format(
                            "Indexed entry {0}: {1}",
                            entry.getPubTime(), entry.getAnchor()));
                }
            }
        } catch (Exception e) {
            mLogger.error("ERROR adding/deleting doc to index", e);
        } finally {
            endWriting();
        }

        Date end = new Date();
        double length = (end.getTime() - start.getTime()) / (double) DateUtils.MILLIS_PER_SECOND;

        if (website == null) {
            mLogger.info("Completed rebuilding index for all users in '"
                    + length + "' secs");
        } else {
            mLogger.info("Completed rebuilding index for website handle: '"
                    + website.getHandle() + "' in '" + length + "' seconds");
        }
    }
}
