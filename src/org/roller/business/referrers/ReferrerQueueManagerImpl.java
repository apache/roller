/*
 * ReferrerQueueManagerImpl.java
 *
 * Created on December 16, 2005, 5:06 PM
 */

package org.roller.business.referrers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.business.runnable.ContinuousWorkerThread;
import org.roller.business.runnable.WorkerThread;
import org.roller.config.RollerConfig;


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
 * run continously to process any referrers that have been queued.  Each worker
 * processes queued referrers until the queue is empty, then sleeps for a given
 * amount of time.  The number of workers used and their sleep time can be set
 * via properties of the static roller.properties file.
 *
 * @author Allen Gilliland
 */
public class ReferrerQueueManagerImpl implements ReferrerQueueManager {
    
    private static Log mLogger = LogFactory.getLog(ReferrerQueueManagerImpl.class);
    
    private static ReferrerQueueManager instance = null;
    
    private boolean asyncMode = false;
    private int numWorkers = 1;
    private int sleepTime = 10000;
    private List workers = null;
    private List referrerQueue = null;
    
    static {
        instance = new ReferrerQueueManagerImpl();
    }
    
    
    // private because we are a singleton
    private ReferrerQueueManagerImpl() {
        mLogger.info("Initializing Referrer Queue Manager");
        
        // lookup config options
        this.asyncMode = RollerConfig.getBooleanProperty("referrers.asyncProcessing.enabled");
        
        mLogger.info("Asynchronous referrer processing = "+this.asyncMode);
        
        if(this.asyncMode) {
            
            
            String num = RollerConfig.getProperty("referrers.queue.numWorkers");
            String sleep = RollerConfig.getProperty("referrers.queue.sleepTime");
            
            try {
                this.numWorkers = Integer.parseInt(num);
                
                if(numWorkers < 1)
                    this.numWorkers = 1;
                
            } catch(NumberFormatException nfe) {
                mLogger.warn("Invalid num workers ["+num+"], using default");
            }
            
            try {
                // multiply by 1000 because we expect input in seconds
                this.sleepTime = Integer.parseInt(sleep) * 1000;
            } catch(NumberFormatException nfe) {
                mLogger.warn("Invalid sleep time ["+sleep+"], using default");
            }
            
            // create the processing queue
            this.referrerQueue = Collections.synchronizedList(new ArrayList());
            
            // start up workers
            this.workers = new ArrayList();
            ContinuousWorkerThread worker = null;
            QueuedReferrerProcessingJob job = null;
            for(int i=0; i < this.numWorkers; i++) {
                job = new QueuedReferrerProcessingJob();
                worker = new ContinuousWorkerThread("ReferrerWorker"+i, job, this.sleepTime);
                workers.add(worker);
                worker.start();
            }
        }
    }
    
    
    /**
     * Get access to the singleton instance.
     */
    public static ReferrerQueueManager getInstance() {
        return instance;
    }
    
    
    /**
     * Process an incoming referrer.
     *
     * If we are doing asynchronous referrer processing then the referrer will
     * just go into the queue for later processing.  If not then we process it
     * now.
     */
    public void processReferrer(IncomingReferrer referrer) {
        
        if(this.asyncMode) {
            mLogger.debug("QUEUING: "+referrer.getRequestUrl());
            
            // add to queue
            this.enqueue(referrer);
        } else {
            // process now
            ReferrerProcessingJob job = new ReferrerProcessingJob();
            
            // setup input
            HashMap inputs = new HashMap();
            inputs.put("referrer", referrer);
            job.input(inputs);
            
            // execute
            job.execute();
        }
        
    }
    
    
    /**
     * Place a referrer in the queue.
     */
    public void enqueue(IncomingReferrer referrer) {
        this.referrerQueue.add(referrer);
        
        if(this.referrerQueue.size() > 250) {
            mLogger.warn("Referrer queue is rather full. queued="+this.referrerQueue.size());
        }
    }
    
    
    /**
     * Retrieve the next referrer in the queue.
     */
    public synchronized IncomingReferrer dequeue() {
        
        if(!this.referrerQueue.isEmpty()) {
            return (IncomingReferrer) this.referrerQueue.remove(0);
        }
        
        return null;
    }
    
    
    /**
     * clean up.
     */
    public void shutdown() {
        
        if(this.workers != null && this.workers.size() > 0) {
            mLogger.info("stopping all ReferrerQueue worker threads");
            
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
