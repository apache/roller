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

package org.apache.roller.weblogger.business.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * A generic worker thread that knows how execute a Job.
 */
public class WorkerThread extends Thread {
    
    private static Log log = LogFactory.getLog(WorkerThread.class);
    
    String id = null;
    Job job = null;
    
    
    /**
     * A simple worker.
     */
    public WorkerThread(String id) {
        super(id);
        this.id = id;
    }
    
    
    /**
     * Start off with a job to do.
     */
    public WorkerThread(String id, Job job) {
        super(id);
        this.id = id;
        this.job = job;
    }
    
    
    /**
     * Thread execution.
     *
     * We just execute the job we were given if it's non-null.
     */
    @Override
    public void run() {
        
        // we only run once
        if (this.job != null) {
            // process job
            try {
                this.job.execute();
            } catch(Exception e) {
                // oops
                log.error("Error executing job. "+
                        "Worker = "+this.id+", "+
                        "Job = "+this.job.getClass().getName(), e);
            } finally {
                // since this is a thread we have to make sure that we tidy up ourselves
                Weblogger roller = WebloggerFactory.getWeblogger();
                roller.release();
            }
        }
        
    }
    
    
    /**
     * Set the job for this worker.
     */
    public void setJob(Job newJob) {
        log.debug("NEW JOB: "+newJob.getClass().getName());
        
        // set the job
        this.job = newJob;
    }
    
}
