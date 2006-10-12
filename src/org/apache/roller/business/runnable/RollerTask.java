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

package org.apache.roller.business.runnable;

import java.util.Date;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ThreadManager;


/**
 * An abstract class representing a scheduled task in Roller.
 *
 * This class extends the java.util.TimerTask class an builds in some Roller
 * specifics, such as handling of locks for synchronization in clustered
 * environments.
 */
public abstract class RollerTask extends TimerTask {
    
    // this is meant to be overridden by subclasses
    Log log = LogFactory.getLog(RollerTask.class);
    
    
    /**
     * Initialization.  Run once before the task is started.
     */
    public void init() throws RollerException {
        // no-op by default
    }
    
    
    /**
     * Get the unique name for this task.
     */
    public abstract String getName();
    
    
    /**
     * Get the time, in seconds, this task wants to be leased for.
     *
     * example: 300 means the task is allowed 5 minutes to run.
     */
    public abstract int getLeaseTime();
    
    
    /**
     * How often should the task run, in seconds.
     *
     * example: 3600 means this task runs once every hour.
     */
    public abstract int getInterval();
    
    
    /**
     * Run the task.
     */
    public abstract void runTask() throws RollerException;
    
    
    /**
     * The run() method as called by our thread manager.
     *
     * This method is purposely defined as "final" so that any tasks that are
     * defined may not override it and remove any of its functionality.  It is
     * setup to provide some basic functionality to the running of all tasks,
     * such as lock acquisition and releasing.
     *
     * Roller tasks should put their logic in the runTask() method.
     */
    public final void run() {
        
        ThreadManager mgr = null;
        try {
            mgr = RollerFactory.getRoller().getThreadManager();
        } catch (Exception ex) {
            log.fatal("Unable to obtain TaskLockManager", ex);
            return;
        }
        
        boolean lockAcquired = false;
        try {
            // is task already locked
            if(!mgr.isLocked(this)) {
                // have we waited enough time since the last run?
                Date nextRun = mgr.getNextRun(this);
                Date now = new Date();
                if(nextRun == null || now.after(nextRun)) {
                    
                    log.debug("Attempting to acquire lock");
                    
                    // acquire lock
                    lockAcquired = mgr.acquireLock(this);
                    
                    if(lockAcquired) {
                        log.debug("Lock acquired, about to begin real work");
                    } else {
                        log.debug("Lock not acquired, assuming race condition");
                        return;
                    }
                } else {
                    log.debug("Interval time hasn't elapsed since last run, nothing to do");
                }
            } else {
                log.info("Task already locked, nothing to do");
            }

            // now if we have a lock then run the task
            if(lockAcquired) {
                this.runTask();
            }
            
        } catch (Exception ex) {
            log.error("Unexpected exception running task", ex);
        } finally {
            log.debug("in the finally block");
            if(lockAcquired) {
                log.debug("Attempting to release lock");
                
                // release lock
                boolean lockReleased = mgr.releaseLock(this);
                
                if(lockReleased) {
                    log.debug("Lock released, time to sleep");
                } else {
                    log.error("Lock NOT released, something went wrong");
                }
            }
        }
        
    }

}
