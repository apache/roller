/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.runnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.HitCountQueue;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * A job which gathers the currently queued hits from the HitCountQueue and
 * stores them in the database.
 */
public class HitCountProcessingJob implements Job {
    
    private static Log log = LogFactory.getLog(HitCountProcessingJob.class);
    
    
    public HitCountProcessingJob() {}
    
    
    /**
     * Execute the job.
     *
     * We want to extract the currently queued hits from the HitCounter and
     * then propogate them to the db for persistent storage.
     */
    public void execute() {
        
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
        WeblogEntryManager emgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        HitCountQueue hitCounter = HitCountQueue.getInstance();
        
        // first get the current set of hits
        List currentHits = hitCounter.getHits();
        
        // now reset the queued hits
        hitCounter.resetHits();
        
        // tally the counts, grouped by weblog handle
        Map hitsTally = new HashMap();
        String weblogHandle = null;
        for(int i=0; i < currentHits.size(); i++) {
            weblogHandle = (String) currentHits.get(i);
            
            Long count = (Long) hitsTally.get(weblogHandle);
            if(count == null) {
                count = new Long(1);
            } else {
                count = new Long(count.longValue()+1);
            }
            hitsTally.put(weblogHandle, count);
        }
        
        // iterate over the tallied hits and store them in the db
        try {
            long startTime = System.currentTimeMillis();
            
            Weblog weblog = null;
            String key = null;
            Iterator it = hitsTally.keySet().iterator();
            while(it.hasNext()) {
                key = (String) it.next();
                
                try {
                    weblog = wmgr.getWeblogByHandle(key);
                    emgr.incrementHitCount(weblog, ((Long)hitsTally.get(key)).intValue());
                } catch (WebloggerException ex) {
                    log.error(ex);
                }
            }
            
            // flush the results to the db
            WebloggerFactory.getWeblogger().flush();
            
            long endTime = System.currentTimeMillis();
            
            log.debug("Completed: "+ (endTime-startTime)/1000 + " secs");
            
        } catch (WebloggerException ex) {
            log.error("Error persisting updated hit counts", ex);
        } finally {
            // release session
            WebloggerFactory.getWeblogger().release();
        }
    }
    
    
    public void input(Map input) {
        // no-op
    }
    
    public Map output() {
        return null;
    }
    
}
