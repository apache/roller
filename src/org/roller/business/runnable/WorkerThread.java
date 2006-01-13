/*
 * WorkerThread.java
 *
 * Created on December 16, 2005, 6:12 PM
 */

package org.roller.business.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A generic worker thread that knows how execute a Job.
 *
 * @author Allen Gilliland
 */
public class WorkerThread extends Thread {
    
    private static Log mLogger = LogFactory.getLog(WorkerThread.class);
    
    String id = null;
    Job job = null;
    
    
    /**
     * A simple worker.
     */
    public WorkerThread(String id) {
        this.id = id;
    }
    
    
    /**
     * Start off with a job to do.
     */
    public WorkerThread(String id, Job job) {
        this.id = id;
        this.job = job;
    }
    
    
    /**
     * Thread execution.
     *
     * We just execute the job we were given if it's non-null.
     */
    public void run() {
        
        // we only run once
        if (this.job != null) {
            // process job
            try {
                this.job.execute();
            } catch(Throwable t) {
                // oops
                mLogger.error("Error executing job. "+
                        "Worker = "+this.id+", "+
                        "Job = "+this.job.getClass().getName(), t);
            }
        }
        
    }
    
    
    /**
     * Set the job for this worker.
     */
    public void setJob(Job newJob) {
        mLogger.debug("NEW JOB: "+newJob.getClass().getName());
        
        // set the job
        this.job = newJob;
    }
    
}
