/*
 * ContinuousWorkerThread.java
 *
 * Created on December 20, 2005, 1:57 PM
 */

package org.roller.business.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A worker that performs a given job continuously.
 *
 * @author Allen Gilliland
 */
public class ContinuousWorkerThread extends WorkerThread {
    
    private static Log mLogger = LogFactory.getLog(ContinuousWorkerThread.class);
    
    // default sleep time is 10 seconds
    long sleepTime = 10000;
    
    
    public ContinuousWorkerThread(String id) {
        super(id);
    }
    
    
    public ContinuousWorkerThread(String id, long sleep) {
        super(id);
        
        this.sleepTime = sleep;
    }
    
    
    public ContinuousWorkerThread(String id, Job job) {
        super(id, job);
    }
    
    
    public ContinuousWorkerThread(String id, Job job, long sleep) {
        super(id, job);
        
        this.sleepTime = sleep;
    }
    
    
    /**
     * Thread execution.
     *
     * We run forever.  Each time a job completes we sleep for 
     * some amount of time before trying again.
     *
     * If we ever get interrupted then we quit.
     */
    public void run() {
        
        mLogger.info(this.id+" Started.");
        
        // run forever
        while(true) {
            
            // execute our job
            super.run();
            
            // job is done, lets sleep it off for a bit
            try {
                mLogger.debug(this.id+" SLEEPING for "+this.sleepTime+" milliseconds ...");
                this.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                mLogger.info(this.id+" INTERRUPT: "+e.getMessage());
                break;
            }
        }
    }
    
}
