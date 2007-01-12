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

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Manage Roller's thread use.
 *
 * TODO: when Roller starts requiring Java 5 then switch impl to make use of
 * the java.util.concurrent tools for this class.  in specific we should be
 * able to use the ScheduledExecutorService class for task scheduling.
 */
public class ThreadManagerImpl implements ThreadManager {
    
    private static Log log = LogFactory.getLog(ThreadManagerImpl.class);
    
    private PooledExecutor backgroundExecutor = null;
    private DirectExecutor nodelayExecutor = null;
    private Timer scheduler = null;
    
    
    public ThreadManagerImpl() {
        
        log.info("Intializing Thread Manager");
        
        backgroundExecutor = new PooledExecutor(new BoundedBuffer(10), 25);
        backgroundExecutor.setMinimumPoolSize(4);
        backgroundExecutor.setKeepAliveTime(1000 * 60 * 5);
        backgroundExecutor.waitWhenBlocked();
        backgroundExecutor.createThreads(9);
        
        backgroundExecutor.setThreadFactory(new ThreadFactory() {
            public Thread newThread(Runnable command) {
                Thread t = new Thread(command);
                t.setDaemon(false);
                t.setName("Background Execution Threads");
                t.setPriority(Thread.NORM_PRIORITY);
                
                return t;
            }
        });
        
        nodelayExecutor = new DirectExecutor();
        scheduler = new Timer(true);
    }
    
    
    public void executeInBackground(Runnable runnable)
            throws InterruptedException {
        backgroundExecutor.execute(runnable);
    }
    
    
    public void executeInForeground(Runnable runnable)
            throws InterruptedException {
        nodelayExecutor.execute(runnable);
    }
    
    
    public void scheduleFixedRateTimerTask(RollerTask task, Date startTime, long intervalMins) {
        
        if (intervalMins < MIN_RATE_INTERVAL_MINS) {
            throw new IllegalArgumentException("Interval (" + intervalMins +
                    ") shorter than minimum allowed (" + MIN_RATE_INTERVAL_MINS + ")");
        }
        
        //scheduler.scheduleAtFixedRate(task, startTime, intervalMins * 60 * 1000);
        scheduler.scheduleAtFixedRate(new TaskExecutor(task), startTime, intervalMins * 60 * 1000);
    }
    
    
    public void shutdown() {
        
        log.debug("starting shutdown sequence");
        
        // trigger an immediate shutdown of any backgrounded tasks
        backgroundExecutor.shutdownNow();
        
        // TODO: it appears that this doesn't affect tasks which may be running
        //   when this is called and that may not be what we want.  It would be
        //   nice if shutdown() meant shutdown immediately.
        scheduler.cancel();
    }
    
    
    public void release() {
    }
    
    
    public boolean registerLease(RollerTask task) {
        return true;
    }
    
    public boolean unregisterLease(RollerTask task) {
        return true;
    }
    
    
    private class TaskExecutor extends TimerTask {
        
        private RollerTask task = null;
        
        public TaskExecutor(RollerTask task) {
            this.task = task;
        }
        
        public void run() {
            try {
                log.debug("Executing task"+task.getName());
                executeInBackground(task);
            } catch (InterruptedException ex) {
                log.info("Interrupted - "+task.getName());
            }
        }
    }
    
}
