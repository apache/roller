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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.business.runnable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.business.InitializationException;


/**
 * Manage Roller's thread use.
 */
public class ThreadManagerImpl implements ThreadManager {
    
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
    
}
