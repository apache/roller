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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Roller;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * An operation that removes the weblog from the index.
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class RemoveEntryOperation extends WriteToIndexOperation {
    
    //~ Static fields/initializers =============================================
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RemoveEntryOperation.class);
    
    //~ Instance fields ========================================================
    
    private WeblogEntry data;
    private Roller roller;
    
    //~ Constructors ===========================================================
    
    public RemoveEntryOperation(Roller roller, IndexManagerImpl mgr,WeblogEntry data) {
        super(mgr);
        this.roller = roller;
        this.data = data;
    }
    
    //~ Methods ================================================================
    
    public void doRun() {
        
        // since this operation can be run on a separate thread we must treat
        // the weblog object passed in as a detached object which is proned to
        // lazy initialization problems, so requery for the object now
        try {
            WeblogManager wMgr = roller.getWeblogManager();
            this.data = wMgr.getWeblogEntry(this.data.getId());
        } catch (WebloggerException ex) {
            mLogger.error("Error getting weblogentry object", ex);
            return;
        }
        
        IndexReader reader = beginDeleting();
        try {
            if (reader != null) {
                Term term = new Term(FieldConstants.ID, data.getId());
                reader.delete(term);
            }
        } catch (IOException e) {
            mLogger.error("Error deleting doc from index", e);
        } finally {
            endDeleting();
        }
    }
    
    
}
