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

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.pojos.TaskLock;


/**
 * Thread management for executing scheduled and asynchronous tasks.
 */
public interface ThreadManager {
    
    long MIN_RATE_INTERVAL_MINS = 1;
    
    
    /**
     * Initialize the thread management system.
     *
     * @throws InitializationException If there is a problem during initialization.
     */
    void initialize() throws InitializationException;
    
    
    /**
     * Execute runnable in background (asynchronously).
     * @param runnable
     * @throws java.lang.InterruptedException
     */
    void executeInBackground(Runnable runnable)
        throws InterruptedException;
    
    
    /**
     * Execute runnable in foreground (synchronously).
     */
    void executeInForeground(Runnable runnable)
        throws InterruptedException;
    
    
    /**
     * Lookup a TaskLock by name.
     * 
     * @param name The name of the task.
     * @return The TaskLock for the task, or null if not found.
     * @throws WebloggerException If there is an error looking up the TaskLock.
     */
    TaskLock getTaskLockByName(String name) throws WebloggerException;

    
    /**
     * Save a TaskLock.
     * 
     * @param tasklock The TaskLock to save.
     * @throws WebloggerException If there is an error saving the TaskLock.
     */
    void saveTaskLock(TaskLock tasklock) throws WebloggerException;


    /**
     * Try to register a lease for a given RollerTask.
     *
     * @param task The RollerTask to register the lease for.
     * @return boolean True if lease was registered, False otherwise.
     */
    boolean registerLease(RollerTask task);
    
    
    /**
     * Try to unregister the lease for a given RollerTask.
     *
     * @param task The RollerTask to unregister the lease for.
     * @return boolean True if lease was unregistered (or was not leased), False otherwise.
     */
    boolean unregisterLease(RollerTask task);
    
    
    /**
     * Shutdown.
     */
    void shutdown();
    
    
    /**
     * Release all resources associated with Roller session.
     */
    void release();
    
}
