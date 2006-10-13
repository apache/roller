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
import org.apache.roller.business.runnable.RollerTask;
import org.apache.roller.config.PingConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;


/**
 * Task for processing the ping queue at fixed intervals.   This is set up during context initialization by {@link
 * RollerContext}.  The queue processing interval is currently set from the configuration {@link
 * org.apache.roller.config.PingConfig} at startup time only.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public class PingQueueTask extends RollerTask {
    
    private static final Log logger = LogFactory.getLog(PingQueueTask.class);
    

    public String getName() {
        return "PingQueueTask";
    }
    
    public int getLeaseTime() {
        return 3;
    }
    
    public int getInterval() {
        return PingConfig.getQueueProcessingIntervalMins();
    }
    
    /**
     * Initialize the task.
     */
    public void init() throws RollerException {
        PingQueueProcessor.init();
    }
    

    /**
     * Run the task once.
     */
    public void runTask() {
        // Call the ping queue processor to process the queue
        Roller roller = null;
        try {
            roller = RollerFactory.getRoller();
            PingQueueProcessor.getInstance().processQueue();
            roller.flush();
        } catch (RollerException e) {
            // This is probably duplicate logging. May want to eliminate it, but should be rare.
            logger.error("Error while processing ping queue", e);
        } finally {
            if (roller != null) roller.release();
        }
    }
    
}
