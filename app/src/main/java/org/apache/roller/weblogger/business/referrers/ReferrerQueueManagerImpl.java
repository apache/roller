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

package org.apache.roller.weblogger.business.referrers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.runnable.ContinuousWorkerThread;
import org.apache.roller.weblogger.business.runnable.WorkerThread;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * The base implementation of the ReferrerQueueManager.
 * 
 * This class is implemented using the singleton pattern to ensure that only
 * one instance exists at any given time.
 *
 * This implementation can be configured to handle referrers in 2 ways ...
 *  1. synchronously.  referrers are processed immediately.
 *  2. asynchronously.  referrers are queued for later processing.
 *
 * Users can control the referrer queue mode via properties in the static
 * roller.properties configuration file.
 *
 * In asynchronous processing mode we start some number of worker threads which
 * run continuously to process any referrers that have been queued.  Each worker
 * processes queued referrers until the queue is empty, then sleeps for a given
 * amount of time.  The number of workers used and their sleep time can be set
 * via properties of the static roller.properties file.
 *
 * @author Allen Gilliland
 */
@com.google.inject.Singleton
public class ReferrerQueueManagerImpl implements ReferrerQueueManager {
    
    private static Log mLogger = LogFactory.getLog(ReferrerQueueManagerImpl.class);
    
    private final Weblogger roller;
    
    private boolean asyncMode = false;
    private List<WorkerThread> workers = null;
    private List<IncomingReferrer> referrerQueue = null;
    private int referrerCount = 0;
    private int maxAsyncQueueSize = 0;

    // private because we are a singleton
    @com.google.inject.Inject
    protected ReferrerQueueManagerImpl(Weblogger roller) {
        mLogger.info("Instantiating Referrer Queue Manager");
        this.roller = roller;
        int sleepTime = 10000;
        int numWorkers = 1;

        // lookup config options
        this.asyncMode = WebloggerConfig.getBooleanProperty("referrers.asyncProcessing.enabled");
        mLogger.info("Asynchronous referrer processing = "+this.asyncMode);

        if(this.asyncMode) {
            String num = WebloggerConfig.getProperty("referrers.queue.numWorkers");
            String sleep = WebloggerConfig.getProperty("referrers.queue.sleepTime");
            
            try {
                numWorkers = Integer.parseInt(num);
                
                if (numWorkers < 1) {
                    numWorkers = 1;
                }
                
            } catch(NumberFormatException nfe) {
                mLogger.warn("Invalid num workers ["+num+"], using default");
            }
            
            try {
                // multiply by 1000 because we expect input in seconds
                sleepTime = Integer.parseInt(sleep) * RollerConstants.SEC_IN_MS;
            } catch(NumberFormatException nfe) {
                mLogger.warn("Invalid sleep time ["+sleep+"], using default");
            }
            
            // create the processing queue
            this.referrerQueue = Collections.synchronizedList(new ArrayList<IncomingReferrer>());
            
            // start up workers
            this.workers = new ArrayList<WorkerThread>();
            ContinuousWorkerThread worker;
            QueuedReferrerProcessingJob job;
            for (int i = 0; i < numWorkers; i++) {
                job = new QueuedReferrerProcessingJob();
                worker = new ContinuousWorkerThread("ReferrerWorker" + i, job, sleepTime);
                workers.add(worker);
                worker.start();
            }
        }
    }
    
    
    /**
     * Process an incoming referrer.
     *
     * If we are doing asynchronous referrer processing then the referrer will
     * just go into the queue for later processing.  If not then we process it
     * now.
     */
    public void processReferrer(IncomingReferrer referrer) {
        referrerCount++;

        if(this.asyncMode) {
            mLogger.debug("QUEUING: "+referrer.getRequestUrl());
            
            // add to queue
            this.enqueue(referrer);
        } else {
            // process now
            ReferrerProcessingJob job = new ReferrerProcessingJob();
            
            // setup input
            HashMap<String, Object> inputs = new HashMap<String, Object>();
            inputs.put("referrer", referrer);
            job.input(inputs);
            
            // execute
            job.execute();
            
            try {
                // flush changes
                roller.flush();
            } catch (WebloggerException ex) {
                mLogger.error("ERROR commiting referrer", ex);
            }
        }
        
    }
    
    
    /**
     * Place a referrer in the queue.
     */
    public void enqueue(IncomingReferrer referrer) {
        this.referrerQueue.add(referrer);

        int currentSize = this.referrerQueue.size();
        if (currentSize > maxAsyncQueueSize) {
            maxAsyncQueueSize = currentSize;
        }
        if (currentSize > 250) {
            mLogger.warn("Referrer queue is rather full. queued="+this.referrerQueue.size());
        }
    }
    
    
    /**
     * Retrieve the next referrer in the queue.
     */
    public synchronized IncomingReferrer dequeue() {
        
        if(!this.referrerQueue.isEmpty()) {
            return this.referrerQueue.remove(0);
        }
        
        return null;
    }
    
    
    /**
     * clean up.
     */
    public void shutdown() {
        
        if(this.workers != null && this.workers.size() > 0) {
            mLogger.info("stopping all ReferrerQueue worker threads");
            mLogger.info("Total referrers received: " + referrerCount);
            if (this.asyncMode) {
                mLogger.info("Max Async Referrer queue size: " + maxAsyncQueueSize);
            }
            // kill all of our threads
            for (WorkerThread worker : workers) {
                worker.interrupt();
            }
        }
        
    }
    
}
