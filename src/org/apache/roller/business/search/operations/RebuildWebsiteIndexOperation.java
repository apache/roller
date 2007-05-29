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
import org.apache.roller.business.search.IndexManagerImpl;
import org.apache.roller.business.search.FieldConstants;
import org.apache.roller.business.search.IndexUtil;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogEntry;
import org.apache.roller.pojos.Weblog;

/**
 * An index operation that rebuilds a given users index (or all indexes).
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class RebuildWebsiteIndexOperation extends WriteToIndexOperation {
    
    //~ Static fields/initializers =============================================
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(RebuildWebsiteIndexOperation.class);
    
    //~ Instance fields ========================================================
    
    private Weblog website;
    
    //~ Constructors ===========================================================
    
    /**
     * Create a new operation that will recreate an index.
     *
     * @param website The website to rebuild the index for, or null for all users.
     */
    public RebuildWebsiteIndexOperation(IndexManagerImpl mgr, Weblog website) {
        super(mgr);
        this.website = website;
    }
    
    //~ Methods ================================================================
    
    public void doRun() {
        Date start = new Date();
        
        // since this operation can be run on a separate thread we must treat
        // the weblog object passed in as a detached object which is proned to
        // lazy initialization problems, so requery for the object now
        if(this.website != null) {
            try {
                UserManager uMgr = RollerFactory.getRoller().getUserManager();
                this.website = uMgr.getWebsite(this.website.getId());
            } catch (RollerException ex) {
                mLogger.error("Error getting website object", ex);
                return;
            }
        }
        
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
                        null,                      // endDate
                        null,                      // catName
                        null,WeblogEntry.PUBLISHED, // status
                        null,                      // text
                        null,                      // sortby (null means pubTime)
                        null, 
                        null,
                        0, -1);     // offset, length, locale
                for (Iterator wbItr = entries.iterator(); wbItr.hasNext();) {
                    WeblogEntry entry = (WeblogEntry) wbItr.next();
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
