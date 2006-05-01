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
/*
 * Created on Mar 10, 2004
 */
package org.apache.roller.presentation;

import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ScheduledTask;
import org.apache.roller.util.Blacklist;

/**
 * Update MT Blacklist if needed.
 *
 * @author Allen Gilliland
 */
public class BlacklistUpdateTask extends TimerTask implements ScheduledTask {
    
    private static Log mLogger = LogFactory.getLog(BlacklistUpdateTask.class);
    
    
    /**
     * Task init.
     */
    public void init(Roller roller, String realPath) throws RollerException {
        mLogger.debug("initing");
    }
    
    
    /**
     * Excecute the task.
     */
    public void run() {
        
        mLogger.info("task started");

        Blacklist.checkForUpdate();
        
        mLogger.info("task completed");
    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {            
            // NOTE: if this task is run externally from the Roller webapp then
            // all it will really be doing is downloading the MT blacklist file
            BlacklistUpdateTask task = new BlacklistUpdateTask();
            task.init(null, null);
            task.run();
            System.exit(0);
        } catch (RollerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}
