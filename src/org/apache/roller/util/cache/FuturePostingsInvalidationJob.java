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

package org.apache.roller.util.cache;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.runnable.Job;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.pojos.WeblogEntryData;


/**
 * Trigger publication for entries scheduled for future release.
 *
 * This job is meant to be run at timed intervals, such as once per minute.
 *
 * This job queries for all entries with the WeblogEntryData.SCHEDULED status
 * which are older than the current time and promotes their status to PUBLISHED.
 */
public class FuturePostingsInvalidationJob implements Job {
    
    private static Log log = LogFactory.getLog(FuturePostingsInvalidationJob.class);
    
    
    public void execute() {
        
        log.debug("starting");
        
        try {
            WeblogManager wMgr = RollerFactory.getRoller().getWeblogManager();
            IndexManager searchMgr = RollerFactory.getRoller().getIndexManager();
            
            Date now = new Date();
            
            log.debug("looking up scheduled entries older than "+now);
            
            // get all published entries older than current time
            List scheduledEntries = wMgr.getWeblogEntries(null, null, null, now, null, 
                    null, null, WeblogEntryData.SCHEDULED, null, null, 0, -1);
            
            WeblogEntryData entry = null;
            Iterator it = scheduledEntries.iterator();
            while(it.hasNext()) {
                entry = (WeblogEntryData) it.next();
                
                // update status to PUBLISHED and save
                entry.setStatus(WeblogEntryData.PUBLISHED);
                wMgr.saveWeblogEntry(entry);
            }
            
            // commit the changes
            RollerFactory.getRoller().flush();
            
            // take a second pass to trigger reindexing and cache invalidations
            // this is because we need the updated entries flushed first
            it = scheduledEntries.iterator();
            while(it.hasNext()) {
                entry = (WeblogEntryData) it.next();
                
                // trigger a cache invalidation
                CacheManager.invalidate(entry);
                
                // trigger search index on entry
                searchMgr.addEntryReIndexOperation(entry);
            }

        } catch(Exception e) {
            log.error(e);
        }
        
        log.debug("finished");
    }
    
    
    public Map output() {
       return null; 
    }
    
    
    public void input(Map input) {
        // no-op
    }
    
}
