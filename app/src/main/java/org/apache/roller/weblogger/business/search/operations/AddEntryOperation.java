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

import org.apache.lucene.index.IndexWriter;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An operation that adds a new log entry into the index.
 */
public class AddEntryOperation extends WriteToIndexOperation {
    
    //~ Static fields/initializers =============================================

    private static Logger log = LoggerFactory.getLogger(AddEntryOperation.class);
    
    //~ Instance fields ========================================================
    
    private WeblogEntry data;
    private WeblogEntryManager weblogEntryManager;
    
    //~ Constructors ===========================================================
    
    /**
     * Adds a web log entry into the index.
     */
    public AddEntryOperation(WeblogEntryManager wem, IndexManager mgr, WeblogEntry data) {
        super(mgr);
        this.weblogEntryManager = wem;
        this.data = data;
    }
    
    //~ Methods ================================================================
    
    public void doRun() {
        IndexWriter writer = beginWriting();
        
        // since this operation can be run on a separate thread we must treat
        // the weblog object passed in as a detached object which is prone to
        // lazy initialization problems, so requery for the object now
        this.data = weblogEntryManager.getWeblogEntry(this.data.getId());

        try {
            if (writer != null) {
                writer.addDocument(getDocument(data));
            }
        } catch (IOException e) {
            log.error("Problems adding doc to index", e);
        } finally {
            endWriting();
        }
    }   
}
