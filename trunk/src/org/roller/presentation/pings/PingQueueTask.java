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
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.presentation.RollerContext;

import java.util.TimerTask;

/**
 * Task for processing the ping queue at fixed intervals.   This is set up during context initialization by {@link
 * RollerContext}.  The queue processing interval is currently set from the configuration {@link
 * org.roller.config.PingConfig} at startup time only.
 */
public class PingQueueTask extends TimerTask
{
    private static final Log logger = LogFactory.getLog(PingQueueTask.class);

    //  The periodic interval (in minutes) at which we are configured to run
    long intervalMins;

    /**
     * Initialize the task.
     *
     * @param rc the Roller context.
     * @throws RollerException
     */
    public void init(RollerContext rc, long intervalMins) throws RollerException
    {
        PingQueueProcessor.init(rc);
        this.intervalMins = intervalMins;
    }

    /**
     * Get the task's configured interval (in minutes).
     *
     * @return the tasks configured interval (in minutes).
     */
    public long getIntervalMins()
    {
        return intervalMins;
    }

    /**
     * Run the task once.
     */
    public void run()
    {
        // Call the ping queue processor to process the queue
        Roller roller = null;
        try
        {
            roller = RollerFactory.getRoller();
            roller.begin();
            PingQueueProcessor.getInstance().processQueue();
            roller.commit();
        }
        catch (RollerException e)
        {
            // This is probably duplicate logging. May want to eliminate it, but should be rare.
            logger.error("Error while processing ping queuer", e);
        }
        finally
        {
            if (roller != null) roller.release();
        }
    }
}
