/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */


package org.apache.roller.business.pings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.PingConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.PingQueueManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PingQueueEntryData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;

import java.util.Iterator;
import java.util.List;

/**
 * Ping Queue Processor.  Singleton encapsulating logic for processing the weblog update ping queue.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public class PingQueueProcessor {
    private static final Log logger = LogFactory.getLog(PingQueueProcessor.class);

    private static PingQueueProcessor theInstance;


    private PingQueueManager pingQueueMgr;

    public static PingQueueProcessor getInstance() {
        return theInstance;
    }

    private PingQueueProcessor() throws RollerException {
        pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
    }

    /**
     * Initialize the singleton.  This is called during <code>RollerContext</code> initialization.
     *
     * @throws RollerException
     */
    public static synchronized void init() throws RollerException {
        if (theInstance != null) {
            logger.warn("Ignoring duplicate initialization of PingQueueProcessor!");
            return;
        }
        theInstance = new PingQueueProcessor();
        if (logger.isDebugEnabled()) logger.debug("Ping queue processor initialized.");
    }

    /**
     * Process the ping queue.  Performs one pass through the ping queue, processing every entry once.  On ping failure
     * an entry is requeued for processing on subsequent passes until the configured maximum number of attempts is
     * reached.
     */
    public synchronized void processQueue() {
        if (PingConfig.getSuspendPingProcessing()) {
            logger.info("Ping processing has been suspended.  Skipping current round of ping queue processing.");
            return;
        }

        String absoluteContextUrl = RollerRuntimeConfig.getAbsoluteContextURL();
        if (absoluteContextUrl == null) {
            logger.warn("WARNING: Skipping current ping queue processing round because we cannot yet determine the site's absolute context url.");
            return;
        }

        // TODO: Group by ping target and ping all sites for that target?
        // We're currently not taking advantage of grouping by ping target site and then sending
        // all of the pings for that target at once.  If it becomes an efficiency issue, we should do
        // that.

        try {
            if (logger.isDebugEnabled()) logger.debug("Started processing ping queue.");
            // Get all of the entries
            List entries = pingQueueMgr.getAllQueueEntries();

            // Process each entry
            for (Iterator i = entries.iterator(); i.hasNext();) {
                PingQueueEntryData pingQueueEntry = (PingQueueEntryData) i.next();
                processQueueEntry(pingQueueEntry);
            }
            if (logger.isDebugEnabled()) logger.debug("Finished processing ping queue.");
        } catch (Exception ex) {
            logger.error("Unexpected exception processing ping queue!  Aborting this pass of ping queue processing.", ex);
        }
    }

    /**
     * Process an individual ping queue entry.
     *
     * @param pingQueueEntry     the ping queue entry
     * @throws RollerException only if there are problems processing the queue.  Exceptions from sending pings are
     *                         handled, not thrown.
     */
    private void processQueueEntry(PingQueueEntryData pingQueueEntry) throws RollerException {
        if (logger.isDebugEnabled()) logger.debug("Processing ping queue entry: " + pingQueueEntry);

        PingTargetData pingTarget = pingQueueEntry.getPingTarget();
        WebsiteData website = pingQueueEntry.getWebsite();
        boolean pingSucceeded = false;
        if (PingConfig.getLogPingsOnly()) {
            // Just log the ping and pretend it succeeded.
            logger.info("Logging simulated ping for ping queue entry " + pingQueueEntry);
            pingSucceeded = true;
        } else {
            // Actually process the ping
            try {
                // Send the ping
                WeblogUpdatePinger.sendPing(pingTarget, website);
                // Consider successful ping transmission if we didn't get an exception.  We don't care here
                // about the result of the ping if it was transmitted.
                pingSucceeded = true;
            } catch (Exception ex) {
                // Handle the ping error, either removing or requeuing the ping queue entry.
                handlePingError(pingQueueEntry, ex);
            }
        }
        // We do this outside of the previous try-catch because we don't want an exception here to be considered a ping error.
        if (pingSucceeded) {
            if (logger.isDebugEnabled()) logger.debug("Processed ping: " + pingQueueEntry);
            pingQueueMgr.removeQueueEntry(pingQueueEntry);
        }
    }

    /**
     * Handle any ping error.
     *
     * @param pingQueueEntry the ping queue entry
     * @param ex             the exception that occurred on the ping attempt
     * @throws RollerException
     */
    private void handlePingError(PingQueueEntryData pingQueueEntry, Exception ex) throws RollerException {
        if ((pingQueueEntry.incrementAttempts() < PingConfig.getMaxPingAttempts()) && WeblogUpdatePinger.shouldRetry(ex))
        {
            // We have attempts remaining, and it looks like we should retry,
            // so requeue the entry for processing on subsequent rounds
            logger.warn("Error on ping attempt (" + pingQueueEntry.getAttempts() + ") for " + pingQueueEntry + ": [" + ex.getMessage() + "]. Will re-queue for later attempts.");
            if (logger.isDebugEnabled()) logger.debug("Error on last ping attempt was: ", ex);
            pingQueueMgr.saveQueueEntry(pingQueueEntry);
        } else {
            // Remove the entry
            logger.warn("Error on ping attempt (" + pingQueueEntry.getAttempts() + ") for " + pingQueueEntry + ": [" + ex.getMessage() + "].  Entry will be REMOVED from ping queue.");
            if (logger.isDebugEnabled()) logger.debug("Error on last ping attempt was: ", ex);
            pingQueueMgr.removeQueueEntry(pingQueueEntry);
            // TODO: mark ping target invalid?
        }
    }


}
