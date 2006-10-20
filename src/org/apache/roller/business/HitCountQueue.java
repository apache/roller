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

package org.apache.roller.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.runnable.ContinuousWorkerThread;
import org.apache.roller.business.runnable.HitCountProcessingJob;
import org.apache.roller.business.runnable.WorkerThread;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.WebsiteData;


/**
 * Queue's up incoming hit counts so that they can be recorded to the db in
 * an asynchronous manner at give intervals.
 *
 * We also start up a single thread which runs continously to take the queued
 * hit counts and record them into the db.
 *
 * TODO: we may want to make this an interface that is pluggable if there is
 *   some indication that users may want to override this implementation.
 */
public class HitCountQueue {
    
    private static Log log = LogFactory.getLog(HitCountQueue.class);
    
    private static HitCountQueue instance = null;
    
    private int numWorkers = 1;
    private int sleepTime = 180000;
    private List workers = null;
    private Map queue = null;
    
    
    static {
        instance = new HitCountQueue();
    }
    
    
    // non-instantiable because we are a singleton
    private HitCountQueue() {
        
        String sleep = RollerConfig.getProperty("hitcount.queue.sleepTime", "180");
        
        try {
            // multiply by 1000 because we expect input in seconds
            this.sleepTime = Integer.parseInt(sleep) * 1000;
        } catch(NumberFormatException nfe) {
            log.warn("Invalid sleep time ["+sleep+"], using default");
        }
        
        // create the hits queue
        this.queue = Collections.synchronizedMap(new HashMap());
        
        // start up a worker to process the hits at intervals
        HitCountProcessingJob job = new HitCountProcessingJob();
        ContinuousWorkerThread worker = 
                new ContinuousWorkerThread("HitCountQueueProcessor", job, this.sleepTime);
        worker.start();
    }
    
    
    public static HitCountQueue getInstance() {
        return instance;
    }
    
    
    public void processHit(WebsiteData weblog, String url, String referrer) {
        
        // just update the count for the weblog
        Long count = (Long) this.queue.get(weblog.getHandle());
        if(count == null) {
            count = new Long(1);
        } else {
            count = new Long(count.longValue()+1);
        }
        this.queue.put(weblog.getHandle(), count);
    }
    
    
    public Map getHits() {
        return new HashMap(this.queue);
    }
    
    
    /**
     * Reset the queued hits.
     */
    public synchronized void resetHits() {
        this.queue = Collections.synchronizedMap(new HashMap());
    }
    
    
    /**
     * clean up.
     */
    public void shutdown() {
        
        if(this.workers != null && this.workers.size() > 0) {
            log.info("stopping all HitCountQueue worker threads");
            
            // kill all of our threads
            WorkerThread worker = null;
            Iterator it = this.workers.iterator();
            while(it.hasNext()) {
                worker = (WorkerThread) it.next();
                worker.interrupt();
            }
        }
        
    }
    
}
