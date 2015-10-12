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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A worker that performs a given job continuously.
 */
public class ContinuousWorkerThread extends WorkerThread {
    private static Log mLogger = LogFactory.getLog(ContinuousWorkerThread.class);
    private static final int DEFAULT_SLEEP_IN_MS = 10000;
    long sleepTime = DEFAULT_SLEEP_IN_MS;

    public ContinuousWorkerThread(String id) {
        super(id);
    }

    public ContinuousWorkerThread(String id, long sleep) {
        super(id);
        this.sleepTime = sleep;
    }

    public ContinuousWorkerThread(String id, Job job) {
        super(id, job);
    }

    public ContinuousWorkerThread(String id, Job job, long sleep) {
        super(id, job);
        this.sleepTime = sleep;
    }

    /**
     * Thread execution.
     *
     * We run forever.  Each time a job completes we sleep for 
     * some amount of time before trying again.
     *
     * If we ever get interrupted then we quit.
     */
    public void run() {
        
        mLogger.info(this.id+" Started.");
        
        // run till interrupted
        while (!Thread.currentThread().isInterrupted()) {
            
            // execute our job
            super.run();
            
            // job is done, lets sleep it off for a bit
            try {
                mLogger.debug(this.id + " SLEEPING for " + this.sleepTime + " milliseconds ...");
                this.sleep(this.sleepTime);
            } catch (InterruptedException e) {
                mLogger.info(this.id + " INTERRUPT: " + e.getMessage());
                break;
            }
        }

        mLogger.info(this.id+" Done.");
    }

}
