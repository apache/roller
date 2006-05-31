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
 */
/* Created on Jul 16, 2003 */
package org.apache.roller.business.search.operations;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.roller.RollerException;
import org.apache.roller.business.IndexManagerImpl;
import org.apache.roller.business.search.FieldConstants;
import org.apache.roller.business.search.IndexUtil;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/**
 * An index operation that rebuilds a given users index (or all indexes).
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class RebuildWebsiteIndexOperation extends WriteToIndexOperation {
    
    //~ Static fields/initializers =============================================
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(RebuildWebsiteIndexOperation.class);
    
    //~ Instance fields ========================================================
    
    private WebsiteData website;
    
    //~ Constructors ===========================================================
    
    /**
     * Create a new operation that will recreate an index.
     *
     * @param website The website to rebuild the index for, or null for all users.
     */
    public RebuildWebsiteIndexOperation(IndexManagerImpl mgr, WebsiteData website) {
        super(mgr);
        this.website = website;
    }
    
    //~ Methods ================================================================
    
    public void doRun() {
        Date start = new Date();
        
        IndexReader reader = beginDeleting();
        
        try {
            if (reader != null) {
                Term tWebsite = null;
                if (website != null) {
                    tWebsite = IndexUtil.getTerm(
                            FieldConstants.WEBSITE_HANDLE, website.getHandle());
                }
                if (tWebsite != null) {
                    reader.delete(tWebsite);
                } else {
                    Term all =
                            IndexUtil.getTerm(FieldConstants.CONSTANT,
                            FieldConstants.CONSTANT_V);
                    reader.delete(all);
                }
            }
        } catch (IOException e) {
            mLogger.info("Problems deleting doc from index", e);
        } finally {
            endDeleting();
        }
        
        IndexWriter writer = beginWriting();
        
        Roller roller = RollerFactory.getRoller();
        try {
            if (writer != null) {
                WeblogManager weblogManager = roller.getWeblogManager();
                
                List entries = weblogManager .getWeblogEntries(
                        website,                   // website            
                        null,
                        null,                      // startDate
                        new Date(),                // endDate (don't index 'future' entries)
                        null,                      // catName
                        WeblogEntryData.PUBLISHED, // status
                        null,                      // sortby (null means pubTime)
                        0, Integer.MAX_VALUE);     // offset, length
                
                for (Iterator wbItr = entries.iterator(); wbItr.hasNext();) {
                    WeblogEntryData entry = (WeblogEntryData) wbItr.next();
                    writer.addDocument(getDocument(entry));
                    mLogger.debug(
                            MessageFormat.format("Indexed entry {0}: {1}",
                            new Object[] {entry.getPubTime(), entry.getAnchor()}));
                }
                // release the database connection
                roller.release();
            }
        } catch (Exception e) {
            mLogger.error("ERROR adding doc to index", e);
        } finally {
            endWriting();
            if (roller != null) roller.release();
        }
        
        Date end = new Date();
        double length = (end.getTime() - start.getTime()) / (double) 1000;
        
        if (website == null) {
            mLogger.info(
                    "Completed rebuilding index for all users in '" + length + "' secs");
        } else {
            mLogger.info("Completed rebuilding index for website handle: '" +
                    website.getHandle() + "' in '" + length + "' seconds");
        }
    }
}
