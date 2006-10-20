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

package org.apache.roller.business.runnable;

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.HitCountQueue;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WebsiteData;


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
        
        UserManager umgr = null;
        WeblogManager wmgr = null;
        try {
            umgr = RollerFactory.getRoller().getUserManager();
            wmgr = RollerFactory.getRoller().getWeblogManager();
        } catch (RollerException ex) {
            // if we can't even get the manager instances then bail now
            log.error("Error getting managers", ex);
            return;
        }
        
        HitCountQueue counter = HitCountQueue.getInstance();
        HitCountQueue hitCounter = (HitCountQueue) counter;
        
        // first get the current set of hits
        Map currentHits = hitCounter.getHits();
        
        // now reset the queued hits
        hitCounter.resetHits();
        
        // iterate over the hits and store them in the db
        try {
            long startTime = System.currentTimeMillis();
            
            WebsiteData weblog = null;
            String key = null;
            Iterator it = currentHits.keySet().iterator();
            while(it.hasNext()) {
                key = (String) it.next();
                
                try {
                    weblog = umgr.getWebsiteByHandle(key);
                    wmgr.incrementHitCount(weblog, ((Long)currentHits.get(key)).intValue());
                } catch (RollerException ex) {
                    log.error(ex);
                }
            }
            
            // make sure and flush the results
            RollerFactory.getRoller().flush();
            
            long endTime = System.currentTimeMillis();
            
            log.debug("Completed: "+ (endTime-startTime)/1000 + " secs");
            
        } catch (RollerException ex) {
            log.error("Error persisting updated hit counts", ex);
        } finally {
            // release session
            RollerFactory.getRoller().release();
        }
    }
    
    
    public void input(Map input) {
        // no-op
    }
    
    public Map output() {
        return null;
    }
    
}
