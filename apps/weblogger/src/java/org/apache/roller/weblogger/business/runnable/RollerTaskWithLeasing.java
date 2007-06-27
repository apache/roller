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
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * An abstract class representing a scheduled task in Roller that will always
 * attempt to acquire a lease before doing its work.
 */
public abstract class RollerTaskWithLeasing extends RollerTask {
    
    private static Log log = LogFactory.getLog(RollerTaskWithLeasing.class);
    
    
    /**
     * Run the task.
     */
    public abstract void runTask() throws WebloggerException;
    
    
    /**
     * The run() method as called by our thread manager.
     *
     * This method is purposely defined as "final" so that any tasks that are
     * defined may not override it and remove any of its functionality.  It is
     * setup to provide some basic functionality to the running of all tasks,
     * such as lease acquisition and releasing.
     *
     * Roller tasks should put their logic in the runTask() method.
     */
    public final void run() {
        
        ThreadManager mgr = WebloggerFactory.getWeblogger().getThreadManager();
        
        boolean lockAcquired = false;
        try {
            log.debug("Attempting to acquire lock");
            
            lockAcquired = mgr.registerLease(this);
            
            // now if we have a lock then run the task
            if(lockAcquired) {
                log.debug("Lock acquired, running task");
                this.runTask();
            } else {
                log.debug("Lock NOT acquired, cannot continue");
                return;
            }
            
        } catch (Exception ex) {
            log.error("Unexpected exception running task", ex);
        } finally {
            
            if(lockAcquired) {
                
                log.debug("Attempting to release lock");
                
                boolean lockReleased = mgr.unregisterLease(this);
                
                if(lockReleased) {
                    log.debug("Lock released, time to sleep");
                } else {
                    log.debug("Lock NOT released, some kind of problem");
                }
            }
            
            // always release Roller session
            WebloggerFactory.getWeblogger().release();
        }
        
    }
    
}
