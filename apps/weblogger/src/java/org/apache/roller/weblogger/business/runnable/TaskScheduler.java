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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.TaskLock;


/**
 * Manages scheduling of periodic tasks.
 * 
 * This scheduler is meant to be run on a single thread and once started it will
 * run continuously until the thread is interrupted.  The basic logic of the
 * scheduler is to accept some number of tasks to be run and once per minute
 * the scheduler will launch any tasks that need to be executed.  
 * 
 * Tasks are executed each on their own thread, so this scheduler does not run
 * serially like a TimerTask.  The threads used for running tasks are managed
 * by an instance of a ThreadPoolExecutor.
 */
public class TaskScheduler implements Runnable {
    
    private static Log log = LogFactory.getLog(TaskScheduler.class);
    
    private static final long ONE_MINUTE_MS = (60 * 1000);
    
    private final ExecutorService pool;
    private final List<RollerTask> tasks;
    
    
    public TaskScheduler(List<RollerTask> webloggerTasks) {
        
        // store list of tasks available to run
        tasks = webloggerTasks;
        
        // use an expanding thread executor pool
        pool = Executors.newCachedThreadPool();
    }
    
    
    public void run() {
        
        boolean firstRun = true;
        
        // run forever, or until we get interrupted
        while(true) {
            try {
                // get current time
                Date now = new Date();
                log.debug("Current time = "+now);
                
                // run tasks, skip run on first pass
                if(firstRun) {
                    // add a slight delay to scheduler start
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(now);
                    cal.add(Calendar.MINUTE, 1);
                    cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
                    cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
                    now = cal.getTime();
                    log.debug("Start time = "+now);
                    
                    firstRun = false;
                } else {
                    try {
                        runTasks(now);
                    } finally {
                        // always release session after each pass
                        WebloggerFactory.getWeblogger().release();
                    }
                }
                
                // wait 'til next minute
                // NOTE: we add 50ms of adjustment time to make sure we awaken
                //       during the next minute, and not before.  awakening at
                //       exactly the .000ms is not of any concern to us
                Date endOfMinute = DateUtil.getEndOfMinute(now);
                long sleepTime = (endOfMinute.getTime() + 50) - System.currentTimeMillis();
                if(sleepTime > 0) {
                    log.debug("sleeping - "+sleepTime);
                    Thread.sleep(sleepTime);
                } else {
                    // it's taken us more than 1 minute for the last loop
                    // so recalculate to sleep 'til the end of the current minute
                    endOfMinute = DateUtil.getEndOfMinute(new Date());
                    sleepTime = (endOfMinute.getTime() + 50) - System.currentTimeMillis();
                    log.debug("sleeping - "+sleepTime);
                    Thread.sleep(sleepTime);
                }
                
            } catch (InterruptedException ex) {
                // thread interrupted
                log.debug("Thread interrupted, scheduler is stopping");
                pool.shutdownNow();
                break;
            }
        }
        
    }
    
    
    /**
     * Run the necessary tasks given a specific currentTime to work from.
     */
    private void runTasks(Date currentTime) {
        
        log.debug("Started - "+currentTime);
        
        ThreadManager tmgr = WebloggerFactory.getWeblogger().getThreadManager();
        
        for( RollerTask task : tasks ) {
            try {
                // get tasklock for the task
                TaskLock tasklock = tmgr.getTaskLockByName(task.getName());
                
                // TODO: check if task is enabled, otherwise skip
                if(tasklock == null) {
                    log.debug("SKIPPING task : "+tasklock.getName());
                    continue;
                }
                
                // first, calculate the next allowed run time for the task
                // based on when the task was last run
                Date nextRunTime = tasklock.getNextAllowedRun(task.getInterval());
                log.debug(task.getName()+": next allowed run time = "+nextRunTime);
                
                // if we missed the last scheduled run time then see when the
                // most appropriate next run time should be and wait 'til then
                boolean needToWait = false;
                if(currentTime.getTime() > (nextRunTime.getTime() + ONE_MINUTE_MS)) {
                    
                    log.debug("MISSED last run, checking if waiting is necessary");
                    if("startOfDay".equals(task.getStartTimeDesc())) {
                        // for daily tasks we only run during the first 
                        // couple minutes of the day
                        Date startOfDay = DateUtil.getStartOfDay(currentTime);
                        if(currentTime.getTime() > startOfDay.getTime() + (2 * ONE_MINUTE_MS)) {
                            needToWait = true;
                            log.debug("WAITING for next reasonable run time");
                        }
                    } else if("startOfHour".equals(task.getStartTimeDesc())) {
                        // for hourly tasks we only run during the first
                        // couple minutes of the hour
                        Date startOfHour = DateUtil.getStartOfHour(currentTime);
                        if(currentTime.getTime() > startOfHour.getTime() + (2 * ONE_MINUTE_MS)) {
                            needToWait = true;
                            log.debug("WAITING for next reasonable run time");
                        }
                    } else {
                        // for immediate tasks we just go ahead and run
                    }
                }
                
                // if we are within 1 minute of run time then execute,
                // otherwise we do nothing
                long differential = currentTime.getTime() - nextRunTime.getTime();
                if (differential >= 0 && !needToWait) {
                    log.debug(task.getName()+": LAUNCHING task");
                    pool.submit(task);
                }
                
            } catch(Throwable t) {
                log.warn(task.getName()+": Unhandled exception caught", t);
            }
        }
        
        log.debug("Finished");
    }
    
}
