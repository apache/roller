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
package org.apache.roller.weblogger.business.search.operations;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexUtil;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * An index operation that rebuilds a given users index (or all indexes).
 * @author Mindaugas Idzelis  (min@idzelis.com)
 */
public class RemoveWebsiteIndexOperation extends WriteToIndexOperation {
    
    //~ Static fields/initializers =============================================
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RemoveWebsiteIndexOperation.class);
    
    //~ Instance fields ========================================================
    
    private Weblog website;
    private Weblogger roller;
    
    //~ Constructors ===========================================================
    
    /**
     * Create a new operation that will recreate an index.
     * @param website The website to rebuild the index for, or null for all sites.
     */
    public RemoveWebsiteIndexOperation(Weblogger roller, IndexManagerImpl mgr, Weblog website) {
        super(mgr);
        this.roller = roller;
        this.website = website;
    }
    
    //~ Methods ================================================================
    
    public void doRun() {
        Date start = new Date();
        
        // since this operation can be run on a separate thread we must treat
        // the weblog object passed in as a detached object which is proned to
        // lazy initialization problems, so requery for the object now
        try {
            this.website = roller.getWeblogManager().getWeblog(this.website.getId());
        } catch (WebloggerException ex) {
            mLogger.error("Error getting website object", ex);
            return;
        }
        
        IndexReader reader = beginDeleting();
        try {
            if (reader != null) {
                String handle = null;
                if (website != null) {
                    handle = website.getHandle();
                }
                Term tHandle =
                        IndexUtil.getTerm(FieldConstants.WEBSITE_HANDLE, handle);
                
                if (tHandle != null) {
                    reader.deleteDocuments(tHandle);
                }
            }
        } catch (IOException e) {
            mLogger.info("Problems deleting doc from index", e);
        } finally {
            endDeleting();
        }
        
        Date end = new Date();
        double length = (end.getTime() - start.getTime()) / (double) 1000;
        
        if (website != null) {
            mLogger.info("Completed deleting indices for website '" +
                    website.getName() + "' in '" + length + "' seconds");
        }
    }
}
