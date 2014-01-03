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

package org.apache.roller.weblogger.business;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.runnable.ContinuousWorkerThread;
import org.apache.roller.weblogger.business.runnable.HitCountProcessingJob;
import org.apache.roller.weblogger.business.runnable.WorkerThread;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Queue's up incoming hit counts so that they can be recorded to the db in
 * an asynchronous manner at give intervals.
 *
 * We also start up a single thread which runs continously to take the queued
 * hit counts, tally them, and record them into the db.
 *
 * TODO: we may want to make this an interface that is pluggable if there is
 *   some indication that users want to override this implementation.
 */
public class HitCountQueue {
    
    private static Log log = LogFactory.getLog(HitCountQueue.class);
    
    private static HitCountQueue instance = null;
    
    private int numWorkers = 1;
    private int sleepTime = 180000;
    private WorkerThread worker = null;
    private List<String> queue = null;
    
    
    static {
        instance = new HitCountQueue();
    }
    
    
    // non-instantiable because we are a singleton
    private HitCountQueue() {
        
        String sleep = WebloggerConfig.getProperty("hitcount.queue.sleepTime", "180");
        
        try {
            // multiply by 1000 because we expect input in seconds
            this.sleepTime = Integer.parseInt(sleep) * 1000;
        } catch(NumberFormatException nfe) {
            log.warn("Invalid sleep time ["+sleep+"], using default");
        }
        
        // create the hits queue
        this.queue = Collections.synchronizedList(new ArrayList<String>());
        
        // start up a worker to process the hits at intervals
        HitCountProcessingJob job = new HitCountProcessingJob();
        worker = new ContinuousWorkerThread("HitCountQueueProcessor", job, this.sleepTime);
        worker.start();
    }
    
    
    public static HitCountQueue getInstance() {
        return instance;
    }
    
    
    public void processHit(Weblog weblog, String url, String referrer) {
        
        // if the weblog isn't null then just drop its handle in the queue
        // each entry in the queue is a weblog handle and indicates a single hit
        if(weblog != null) {
            this.queue.add(weblog.getHandle());
        }
    }
    
    
    public List<String> getHits() {
        return new ArrayList<String>(this.queue);
    }
    
    
    /**
     * Reset the queued hits.
     */
    public synchronized void resetHits() {
        this.queue = Collections.synchronizedList(new ArrayList<String>());
    }
    
    
    /**
     * clean up.
     */
    public void shutdown() {
        
        if(this.worker != null) {
            log.info("stopping worker "+this.worker.getName());
            worker.interrupt();
        }
        
    }
    
}
