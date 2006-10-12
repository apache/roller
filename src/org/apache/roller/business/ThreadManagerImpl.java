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

package org.apache.roller.business;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;
import EDU.oswego.cs.dl.util.concurrent.DirectExecutor;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.roller.model.ThreadManager;
import org.apache.roller.util.DateUtil;


/**
 * Manage Roller's background thread use. Currently, Roller starts background
 * threads for two purposes: 1) the nightly purge of referer counts and 2)
 * following linkbacks (only occurs if linkbacks are enabled).
 */
public class ThreadManagerImpl implements ThreadManager {
    
    private PooledExecutor backgroundExecutor;
    private DirectExecutor nodelayExecutor;
    private Timer scheduler;
    
    
    public ThreadManagerImpl() {
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
    
    
    public void scheduleDailyTimerTask(TimerTask task) {
        scheduler.scheduleAtFixedRate(task,
                DateUtil.getEndOfDay(new Date()), DateUtil.millisInDay);
    }
    
    
    public void scheduleHourlyTimerTask(TimerTask task) {
        scheduler.scheduleAtFixedRate(task, new Date(), 60*60*1000);
    }
    
    
    public void scheduleFixedRateTimerTask(TimerTask task, long delayMins, long periodMins) {
        if (periodMins < MIN_RATE_INTERVAL_MINS) {
            throw new IllegalArgumentException("Period (" + periodMins +
                    ") shorter than minimum allowed (" + MIN_RATE_INTERVAL_MINS + ")");
        }
        scheduler.scheduleAtFixedRate(task, delayMins * 60 * 1000, periodMins * 60 * 1000);
    }
    
    
    public void shutdown() {
        backgroundExecutor.shutdownAfterProcessingCurrentlyQueuedTasks();
        scheduler.cancel();
    }
    
    
    public void release() {
    }
    
}
