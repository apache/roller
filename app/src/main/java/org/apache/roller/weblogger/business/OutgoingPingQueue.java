/*
   Copyright 2015 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.business.runnable.ContinuousWorkerThread;
import org.apache.roller.weblogger.business.runnable.PingProcessingJob;
import org.apache.roller.weblogger.business.runnable.WorkerThread;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.AutoPing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In memory storage of outgoing pings periodically sent out by
 * the PingProcessingJob.  Pings are sent to each ping server defined
 * for a blog whenever that blog has a new or updated blog entry.
 */
public class OutgoingPingQueue {

    private static Log log = LogFactory.getLog(OutgoingPingQueue.class);
    private static OutgoingPingQueue instance = null;
    private WorkerThread worker = null;
    private List<AutoPing> queue = null;

    static {
        instance = new OutgoingPingQueue();
    }

    // non-instantiable because we are a singleton
    private OutgoingPingQueue() {
        int sleepTime = 15;

        try {
            // convert input in seconds to ms
            Integer sleep = WebloggerConfig.getIntProperty("ping.queue.sleepTime.min", 15);
            sleepTime = sleep * RollerConstants.MIN_IN_MS;
        } catch (NumberFormatException nfe) {
            String sleepStr = WebloggerConfig.getProperty("ping.queue.sleepTime.min");
            log.warn("Invalid sleep time [" + sleepStr + "] (must be parseable as an integer), " +
                    "using default of " + sleepTime +" mins instead.");
        }

        // create the hits queue
        this.queue = Collections.synchronizedList(new ArrayList<AutoPing>());

        // start up a worker to process the hits at intervals
        PingProcessingJob job = new PingProcessingJob();
        worker = new ContinuousWorkerThread("PingProcessingJob", job, sleepTime);
        worker.start();
    }

    public static OutgoingPingQueue getInstance() {
        return instance;
    }

    public void addPing(AutoPing ping) {
        for (AutoPing pingTest : queue) {
            if (pingTest.equals(ping)) {
                log.debug("Already in ping queue, skipping: " + ping);
                return;
            }
        }

        this.queue.add(ping);
    }

    public List<AutoPing> getPings() {
        return new ArrayList<>(this.queue);
    }

    public synchronized void clearPings() {
        this.queue = Collections.synchronizedList(new ArrayList<AutoPing>());
    }

    /**
     * clean up.
     */
    public void shutdown() {
        if(this.worker != null) {
            log.info("stopping worker "+this.worker.getName());
            worker.interrupt();
        }
    }
}
