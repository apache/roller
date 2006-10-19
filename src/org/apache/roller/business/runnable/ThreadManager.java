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


/**
 * Thread management for executing scheduled and asynchronous tasks.
 */
public interface ThreadManager {
    
    public static final long MIN_RATE_INTERVAL_MINS = 1;
    
    
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
     * Try to aquire a lock for a given RollerTask.
     *
     * @param task The RollerTask to aquire the lock for.
     * @return boolean True if lock was acquired, False otherwise.
     */
    public boolean acquireLock(RollerTask task);
    
    
    /**
     * Try to release the lock for a given RollerTask.
     *
     * @param task The RollerTask to release the lock for.
     * @return boolean True if lock was released (or was not locked), False otherwise.
     */
    public boolean releaseLock(RollerTask task);
    
    
    /**
     * Is a task currently locked?
     * 
     * @param task The RollerTask to check the lock state for.
     * @return boolean True if task is locked, False otherwise.
     */
    public boolean isLocked(RollerTask task);
    
    
    /**
     * What was the last time a task was run?
     * 
     * @param task The RollerTask to check the last run time for.
     * @return Date The last time the task was run, or null if task has never been run.
     */
    public Date getLastRun(RollerTask task);
    
    
    /**
     * What is the next time a task is allowed to run?
     * 
     * @param task The RollerTask to calculate the next run time for.
     * @return Date The next time the task is allowed to run, or null if task has never been run.
     */
    public Date getNextRun(RollerTask task);
    
    
    /**
     * Shutdown.
     */
    public void shutdown();
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
