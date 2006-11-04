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
/* Created on Aug 12, 2003 */
package org.apache.roller.business.search.operations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.search.IndexManagerImpl;

/**
 * An operation that writes to index.
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public abstract class WriteToIndexOperation extends IndexOperation {
    
    public WriteToIndexOperation(IndexManagerImpl mgr) {
        super(mgr);
    }
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(WriteToIndexOperation.class);
    
    public void run() {
        try {
            manager.getReadWriteLock().writeLock().acquire();
            mLogger.debug("Starting search index operation");
            doRun();
            mLogger.debug("Search index operation complete");
        } catch (InterruptedException e) {
            mLogger.error("Error acquiring write lock on index", e);
        } finally {
            manager.getReadWriteLock().writeLock().release();
        }
        manager.resetSharedReader();
    }
}
