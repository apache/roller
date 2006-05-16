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
 * Created on Aug 16, 2003
 */
package org.apache.roller.ui.core.tasks;

import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RefererManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ScheduledTask;


/**
 * Reset referer counts.
 *
 * @author Allen Gilliland
 */
public class TurnoverReferersTask extends TimerTask implements ScheduledTask {
    
    private static Log mLogger = LogFactory.getLog(TurnoverReferersTask.class);
    
    
    /**
     * Task init.
     */
    public void init(Roller roller, String realPath) throws RollerException {
        mLogger.debug("initing");
    }
    
    
    /**
     * Execute the task.
     */
    public void run() {
        
        mLogger.info("task started");
        
        try {
            Roller roller = RollerFactory.getRoller();
            roller.getRefererManager().clearReferrers();
            roller.flush();
            roller.release();
            mLogger.info("task completed");   
            
        } catch (RollerException e) {
            mLogger.error("Error while checking for referer turnover", e);
        } catch (Exception ee) {
            mLogger.error("unexpected exception", ee);
        }


    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {            
            TurnoverReferersTask task = new TurnoverReferersTask();
            task.init(null, null);
            task.run();
            System.exit(0);
        } catch (RollerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}