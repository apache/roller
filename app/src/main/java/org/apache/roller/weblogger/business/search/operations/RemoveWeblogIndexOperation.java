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

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;

/**
 * An index operation that rebuilds a given users index (or all indexes).
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class RemoveWeblogIndexOperation extends WriteToIndexOperation {

    // ~ Static fields/initializers
    // =============================================

    private static Log mLogger = LogFactory.getFactory().getInstance(
            RemoveWeblogIndexOperation.class);

    // ~ Instance fields
    // ========================================================

    private String weblogHandle;

    // ~ Constructors
    // ===========================================================

    /**
     * Create a operation that remove the index data for a weblog.
     * 
     * @param weblogHandle The weblog whose index data is to be removed.
     */
    public RemoveWeblogIndexOperation(IndexManagerImpl mgr, String weblogHandle) {
        super(mgr);
        this.weblogHandle = weblogHandle;
    }

    // ~ Methods
    // ================================================================

    public void doRun() {
        Date start = new Date();

        IndexWriter writer = beginWriting();
        try {
            if (writer != null) {
                Term tHandle = IndexOperation.getTerm(FieldConstants.WEBSITE_HANDLE,
                        weblogHandle);

                if (tHandle != null) {
                    writer.deleteDocuments(tHandle);
                }
            }
        } catch (IOException e) {
            mLogger.info("Problems deleting doc from index", e);
        } finally {
            endWriting();
        }

        Date end = new Date();
        double length = (end.getTime() - start.getTime()) / (double) DateUtils.MILLIS_PER_SECOND;

        mLogger.info("Completed deleting indices for weblog with handle '"
                + weblogHandle + "' in '" + length + "' seconds");
    }
}