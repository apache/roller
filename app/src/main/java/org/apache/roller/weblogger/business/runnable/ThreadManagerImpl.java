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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.TaskLock;


/**
 * Manage Roller's thread use.
 */
@com.google.inject.Singleton
public abstract class ThreadManagerImpl implements ThreadManager {
    
    private static final Log LOG = LogFactory.getLog(ThreadManagerImpl.class);
    
    // our own scheduler thread
    private Thread schedulerThread = null;
    
    // a simple thread executor
    private final ExecutorService serviceScheduler;
    
    
    public ThreadManagerImpl() {
        
        LOG.info("Instantiating Thread Manager");
        
        serviceScheduler = Executors.newCachedThreadPool();
    }
    
    
    public void initialize() throws InitializationException {
                    
        // initialize tasks, making sure that each task has a tasklock record in the db
        List<RollerTask> webloggerTasks = new ArrayList<RollerTask>();
        String tasksStr = WebloggerConfig.getProperty("tasks.enabled");
        String[] tasks = StringUtils.stripAll(StringUtils.split(tasksStr, ","));
        for ( String taskName : tasks ) {
            
            String taskClassName = WebloggerConfig.getProperty("tasks."+taskName+".class");
            if(taskClassName != null) {
                LOG.info("Initializing task: " + taskName);
                
                try {
                    Class taskClass = Class.forName(taskClassName);
                    RollerTask task = (RollerTask) taskClass.newInstance();
                    task.init(taskName);
                    
                    // make sure there is a tasklock record in the db
                    TaskLock taskLock = getTaskLockByName(task.getName());
                    if (taskLock == null) {
                        LOG.debug("Task record does not exist, inserting empty record to start with");

                        // insert an empty record
                        taskLock = new TaskLock();
                        taskLock.setName(task.getName());
                        taskLock.setLastRun(new Date(0));
                        taskLock.setTimeAcquired(new Date(0));
                        taskLock.setTimeLeased(0);

                        // save it
                        this.saveTaskLock(taskLock);
                    }
                    
                    // add it to the list of configured tasks
                    webloggerTasks.add(task);
                    
                } catch (ClassCastException ex) {
                    LOG.warn("Task does not extend RollerTask class", ex);
                } catch (WebloggerException ex) {
                    LOG.error("Error scheduling task", ex);
                } catch (Exception ex) {
                    LOG.error("Error instantiating task", ex);
                }
            }
        }
        
        // create scheduler
        TaskScheduler scheduler = new TaskScheduler(webloggerTasks);
        
        // start scheduler thread, but only if it's not already running
        if (schedulerThread == null) {
            LOG.debug("Starting scheduler thread");
            schedulerThread = new Thread(scheduler, "Roller Weblogger Task Scheduler");
            // set thread priority between MAX and NORM so we get slightly preferential treatment
            schedulerThread.setPriority((Thread.MAX_PRIORITY + Thread.NORM_PRIORITY)/2);
            schedulerThread.start();
        }
    }
    
    
    public void executeInBackground(Runnable runnable)
            throws InterruptedException {
        serviceScheduler.submit(runnable);
    }
    
    
    public void executeInForeground(Runnable runnable)
            throws InterruptedException {
        Future task = serviceScheduler.submit(runnable);
        
        // since this task is really meant to be executed within this calling 
        // thread, here we can add a little code here to loop until it realizes 
        // the task is done
        while(!task.isDone()) {
            Thread.sleep(RollerConstants.HALF_SEC_IN_MS);
        }
    }
    
    
    public void shutdown() {
        
        LOG.debug("starting shutdown sequence");
        
        // trigger an immediate shutdown of any backgrounded tasks
        serviceScheduler.shutdownNow();
        
        // only stop if we are already running
        if(schedulerThread != null) {
            LOG.debug("Stopping scheduler");
            schedulerThread.interrupt();
        }
    }
    
    
    public void release() {
        // no-op
    }
    
    
    /**
     * Default implementation of lease registration, always returns true.
     * 
     * Subclasses should override this method if they plan to run in an
     * environment that supports clustered deployments.
     */
    public boolean registerLease(RollerTask task) {
        return true;
    }
    
    
    /**
     * Default implementation of lease unregistration, always returns true.
     * 
     * Subclasses should override this method if they plan to run in an
     * environment that supports clustered deployments.
     */
    public boolean unregisterLease(RollerTask task) {
        return true;
    }
    
}
