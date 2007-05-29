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


/**
 * Thread management for executing scheduled and asynchronous tasks.
 */
public interface ThreadManager {
    
    public static final long MIN_RATE_INTERVAL_MINS = 1;
    
    
    /**
     * Schedule execution of all configured tasks.
     */
    public void startTasks();
    
    
    /**
     * Execute runnable in background (asynchronously).
     * @param runnable
     * @throws java.lang.InterruptedException
     */
    public void executeInBackground(Runnable runnable)
        throws InterruptedException;
    
    
    /**
     * Execute runnable in foreground (synchronously).
     */
    public void executeInForeground(Runnable runnable)
        throws InterruptedException;
    
    
    /**
     * Schedule task to run at fixed rate.
     *
     * @param task The RollerTask to schedule.
     * @param startTime The Date at which to start the task.
     * @param long The interval (in minutes) at which the task should run.
     */
    public void scheduleFixedRateTimerTask(RollerTask task, Date startTime, long intervalMins);
    
    
    /**
     * Try to register a lease for a given RollerTask.
     *
     * @param task The RollerTask to register the lease for.
     * @return boolean True if lease was registered, False otherwise.
     */
    public boolean registerLease(RollerTask task);
    
    
    /**
     * Try to unregister the lease for a given RollerTask.
     *
     * @param task The RollerTask to unregister the lease for.
     * @return boolean True if lease was unregistered (or was not leased), False otherwise.
     */
    public boolean unregisterLease(RollerTask task);
    
    
    /**
     * Shutdown.
     */
    public void shutdown();
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
