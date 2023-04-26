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
package org.apache.roller.weblogger.business.search;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

/**
 * Interface to Roller's full-text search facility.
 * @author Dave Johnson
 */
public interface IndexManager {

    /**
     * Initialize the search system.
     * @throws InitializationException If there is a problem during initialization.
     */
    void initialize() throws InitializationException;

    /** Shutdown to be called on application shutdown */
    void shutdown();

    /**
     * Release all resources associated with Roller session.
     */
    void release();

    /** Does index need to be rebuilt */
    boolean isInconsistentAtStartup();

    /** Add entry to index, returns immediately and operates in background */
    void addEntryIndexOperation(WeblogEntry entry) throws WebloggerException;
    
    /** Re-index entry, returns immediately and operates in background */
    void addEntryReIndexOperation(WeblogEntry entry) throws WebloggerException;

    void rebuildWeblogIndex(Weblog weblog) throws WebloggerException;

    void rebuildWeblogIndex() throws WebloggerException;

    /** Remove weblog from index, returns immediately and operates in background */
    void removeWeblogIndex(Weblog weblog) throws WebloggerException;

    /** Remove entry from index, returns immediately and operates in background */
    void removeEntryIndexOperation(WeblogEntry entry) throws WebloggerException;

    SearchResultList search(
        String term,
        String weblogHandle,
        String category,
        String locale,
        int pageNum,
        int entryCount,
        URLStrategy urlStrategy
    ) throws WebloggerException;
}


