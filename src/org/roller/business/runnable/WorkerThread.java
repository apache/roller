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
/*
 * WorkerThread.java
 *
 * Created on December 16, 2005, 6:12 PM
 */

package org.roller.business.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;


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
            
            // since this is a thread we have to make sure that we tidy up ourselves
            Roller roller = RollerFactory.getRoller();
            roller.release();
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
