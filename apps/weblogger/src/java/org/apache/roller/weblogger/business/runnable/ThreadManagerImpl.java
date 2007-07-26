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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Manage Roller's thread use.
 */
@com.google.inject.Singleton
public abstract class ThreadManagerImpl implements ThreadManager {
    
    private static final Log log = LogFactory.getLog(ThreadManagerImpl.class);
    
    // our own scheduler thread
    private Thread schedulerThread = null;
    
    // a simple thread executor
    private final ExecutorService serviceScheduler;
    
    
    public ThreadManagerImpl() {
        
        log.info("Instantiating Thread Manager");
        
        serviceScheduler = Executors.newCachedThreadPool();
    }
    
    
    public void initialize() throws InitializationException {
        
        // create scheduler
        TaskScheduler scheduler = new TaskScheduler();
                    
        // okay, first we look for what tasks have been enabled
        String tasksStr = WebloggerConfig.getProperty("tasks.enabled");
        String[] tasks = StringUtils.stripAll(StringUtils.split(tasksStr, ","));
        for (int i=0; i < tasks.length; i++) {
            
            String taskClassName = WebloggerConfig.getProperty("tasks."+tasks[i]+".class");
            if(taskClassName != null) {
                log.info("Initializing task: "+tasks[i]);
                
                try {
                    Class taskClass = Class.forName(taskClassName);
                    RollerTask task = (RollerTask) taskClass.newInstance();
                    task.init();
                    
                    // schedule it
                    scheduler.scheduleTask(task);
                    
                } catch (ClassCastException ex) {
                    log.warn("Task does not extend RollerTask class", ex);
                } catch (WebloggerException ex) {
                    log.error("Error scheduling task", ex);
                } catch (Exception ex) {
                    log.error("Error instantiating task", ex);
                }
            }
        }
        
        // only start if we aren't already running
        if (schedulerThread == null && scheduler != null) {
            log.debug("Starting scheduler thread");
            schedulerThread = new Thread(scheduler, "Roller Weblogger Task Scheduler");
            schedulerThread.start();
        }
    }
    
    
    public void executeInBackground(Runnable runnable)
            throws InterruptedException {
        Future task = serviceScheduler.submit(runnable);
    }
    
    
    public void executeInForeground(Runnable runnable)
            throws InterruptedException {
        Future task = serviceScheduler.submit(runnable);
        
        // if this task is really meant to be executed within this calling thread
        // then we can add a little code here to loop until it realizes the task is done
        // while(!scheduledTask.isDone())
    }
    
    
    public void shutdown() {
        
        log.debug("starting shutdown sequence");
        
        // trigger an immediate shutdown of any backgrounded tasks
        serviceScheduler.shutdownNow();
        
        // only stop if we are already running
        if(schedulerThread != null) {
            log.debug("Stopping scheduler");
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
