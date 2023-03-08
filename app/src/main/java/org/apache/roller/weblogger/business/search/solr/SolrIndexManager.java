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

/* Created on March 8, 2023 */

package org.apache.roller.weblogger.business.search.solr;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;

public class SolrIndexManager implements IndexManager {
    @Override
    public void initialize() throws InitializationException {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void release() {

    }

    @Override
    public boolean isInconsistentAtStartup() {
        return false;
    }

    @Override
    public void addEntryIndexOperation(WeblogEntry entry) throws WebloggerException {

    }

    @Override
    public void addEntryReIndexOperation(WeblogEntry entry) throws WebloggerException {

    }

    @Override
    public void rebuildWeblogIndex(Weblog weblog) throws WebloggerException {

    }

    @Override
    public void rebuildWeblogIndex() throws WebloggerException {

    }

    @Override
    public void removeWeblogIndex(Weblog weblog) throws WebloggerException {

    }

    @Override
    public void removeEntryIndexOperation(WeblogEntry entry) throws WebloggerException {

    }

    @Override
    public SearchResult search(String term, String weblogHandle, String category, String locale) throws WebloggerException {
        return null;
    }
}
