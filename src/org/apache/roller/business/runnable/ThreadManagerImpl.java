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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Manage Roller's thread use.
 */
public class ThreadManagerImpl implements ThreadManager {
    
    private static final Log log = LogFactory.getLog(ThreadManagerImpl.class);
    
    // background task scheduler
    private final ScheduledExecutorService serviceScheduler;
    
    
    public ThreadManagerImpl() {
        
        log.info("Intializing Thread Manager");
        
        serviceScheduler = Executors.newScheduledThreadPool(10);
    }
    
    
    public void executeInBackground(Runnable runnable)
            throws InterruptedException {
        ScheduledFuture scheduledTask = serviceScheduler.schedule(runnable, 0, TimeUnit.SECONDS);
    }
    
    
    public void executeInForeground(Runnable runnable)
            throws InterruptedException {
        ScheduledFuture scheduledTask = serviceScheduler.schedule(runnable, 0, TimeUnit.SECONDS);
        
        // if this task is really meant to be executed within this calling thread
        // then we can add a little code here to loop until it realizes the task is done
        // while(!scheduledTask.isDone())
    }
    
    
    public void scheduleFixedRateTimerTask(RollerTask task, Date startTime, long intervalMins) {
        
        if (intervalMins < MIN_RATE_INTERVAL_MINS) {
            throw new IllegalArgumentException("Interval (" + intervalMins +
                    ") shorter than minimum allowed (" + MIN_RATE_INTERVAL_MINS + ")");
        }
        
        ScheduledFuture scheduledTask = serviceScheduler.scheduleAtFixedRate(
                task, 
                startTime.getTime() - System.currentTimeMillis(), 
                intervalMins * 60 * 1000, 
                TimeUnit.MILLISECONDS);
        
        log.debug("Scheduled "+task.getClass().getName()+" at "+new Date(System.currentTimeMillis()+scheduledTask.getDelay(TimeUnit.MILLISECONDS)));
    }
    
    
    public void shutdown() {
        
        log.debug("starting shutdown sequence");
        
        // trigger an immediate shutdown of any backgrounded tasks
        serviceScheduler.shutdownNow();
    }
    
    
    public void release() {
    }
    
    
    public boolean registerLease(RollerTask task) {
        return true;
    }
    
    public boolean unregisterLease(RollerTask task) {
        return true;
    }
    
}
