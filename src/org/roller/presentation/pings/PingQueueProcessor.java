/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.presentation.pings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.PingConfig;
import org.roller.model.PingQueueManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.PingQueueEntryData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;

import java.util.Iterator;
import java.util.List;

/**
 * Ping Queue Processor.  Singleton encapsulating logic for processing the weblog update ping queue.
 */
public class PingQueueProcessor
{
    private static final Log logger = LogFactory.getLog(PingQueueProcessor.class);

    private static PingQueueProcessor theInstance;


    private RollerContext rollerContext;
    private PingQueueManager pingQueueMgr;

    public static PingQueueProcessor getInstance()
    {
        return theInstance;
    }

    private PingQueueProcessor(RollerContext rc) throws RollerException
    {
        rollerContext = rc;
        pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
    }

    /**
     * Initialize the singleton.  This is called during <code>RollerContext</code> initialization.
     *
     * @param rc the Roller context
     * @throws RollerException
     */
    public static synchronized void init(RollerContext rc) throws RollerException
    {
        if (theInstance != null)
        {
            logger.warn("Ignoring duplicate initialization of PingQueueProcessor!");
            return;
        }
        theInstance = new PingQueueProcessor(rc);
        if (logger.isDebugEnabled()) logger.debug("Ping queue processor initialized.");
    }

    /**
     * Process the ping queue.  Performs one pass through the ping queue, processing every entry once.  On ping failure
     * an entry is requeued for processing on subsequent passes until the configured maximum number of attempts is
     * reached.
     */
    public synchronized void processQueue()
    {
        if (PingConfig.getSuspendPingProcessing()) {
            logger.info("Ping processing has been suspended.  Skipping current round of ping queue processing.");
            return;
        }

        String absoluteContextUrl = rollerContext.getAbsoluteContextUrl();
        if (absoluteContextUrl == null)
        {
            logger.warn("WARNING: Skipping current ping queue processing round because we cannot yet determine the site's absolute context url.");
            return;
        }

        // TODO: Group by ping target and ping all sites for that target?
        // We're currently not taking advantage of grouping by ping target site and then sending
        // all of the pings for that target at once.  If it becomes an efficiency issue, we should do
        // that.

        try
        {
            if (logger.isDebugEnabled()) logger.debug("Started processing ping queue.");
            // Get all of the entries
            List entries = pingQueueMgr.getAllQueueEntries();

            // Process each entry
            for (Iterator i = entries.iterator(); i.hasNext();)
            {
                PingQueueEntryData pingQueueEntry = (PingQueueEntryData) i.next();
                processQueueEntry(absoluteContextUrl, pingQueueEntry);
            }
            if (logger.isDebugEnabled()) logger.debug("Finished processing ping queue.");
        }
        catch (Exception ex)
        {
            logger.error("Unexpected exception processing ping queue!  Aborting this pass of ping queue processing.", ex);
        }
    }

    /**
     * Process an individual ping queue entry.
     *
     * @param absoluteContextUrl absolute context URL of the Roller site
     * @param pingQueueEntry     the ping queue entry
     * @throws RollerException only if there are problems processing the queue.  Exceptions from sending pings are
     *                         handled, not thrown.
     */
    private void processQueueEntry(String absoluteContextUrl, PingQueueEntryData pingQueueEntry)
        throws RollerException
    {
        if (logger.isDebugEnabled()) logger.debug("Processing ping queue entry: " + pingQueueEntry);

        PingTargetData pingTarget = pingQueueEntry.getPingTarget();
        WebsiteData website = pingQueueEntry.getWebsite();
        boolean pingSucceeded = false;
        if (PingConfig.getLogPingsOnly())
        {
            // Just log the ping and pretend it succeeded.
            logger.info("Logging simulated ping for ping queue entry " + pingQueueEntry);
            pingSucceeded = true;
        }
        else
        {
            // Actually process the ping
            try
            {
                // Send the ping
                WeblogUpdatePinger.sendPing(absoluteContextUrl, pingTarget, website);
                // Consider successful ping transmission if we didn't get an exception.  We don't care here
                // about the result of the ping if it was transmitted.
                pingSucceeded = true;
            }
            catch (Exception ex)
            {
                // Handle the ping error, either removing or requeuing the ping queue entry.
                handlePingError(pingQueueEntry, ex);
            }
        }
        // We do this outside of the previous try-catch because we don't want an exception here to be considered a ping error.
        if (pingSucceeded)
        {
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
    private void handlePingError(PingQueueEntryData pingQueueEntry, Exception ex)
        throws RollerException
    {
        if ((pingQueueEntry.incrementAttempts() < PingConfig.getMaxPingAttempts()) &&
            WeblogUpdatePinger.shouldRetry(ex))
        {
            // We have attempts remaining, and it looks like we should retry,
            // so requeue the entry for processing on subsequent rounds
            logger.warn("Error on ping attempt (" + pingQueueEntry.getAttempts() + ") for " + pingQueueEntry +
                ": [" + ex.getMessage() + "]. Will re-queue for later attempts.");
            if (logger.isDebugEnabled()) logger.debug("Error on last ping attempt was: ", ex);
            pingQueueMgr.saveQueueEntry(pingQueueEntry);
        }
        else
        {
            // Remove the entry
            logger.warn("Error on ping attempt (" + pingQueueEntry.getAttempts() + ") for " + pingQueueEntry +
                ": [" + ex.getMessage() + "].  Entry will be REMOVED from ping queue.");
            if (logger.isDebugEnabled()) logger.debug("Error on last ping attempt was: ", ex);
            pingQueueMgr.removeQueueEntry(pingQueueEntry);
            // TODO: mark ping target invalid?
        }
    }


}
