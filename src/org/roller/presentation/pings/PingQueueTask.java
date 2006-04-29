/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            PingQueueProcessor.getInstance().processQueue();
            roller.flush();
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
