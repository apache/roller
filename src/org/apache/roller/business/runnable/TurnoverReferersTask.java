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

package org.apache.roller.business.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;


/**
 * Reset referer counts.
 */
public class TurnoverReferersTask extends RollerTask {
    
    Log log = LogFactory.getLog(TurnoverReferersTask.class);
    
    
    public String getName() {
        return "TurnoverReferersTask";
    }
    
    public int getLeaseTime() {
        return 5;
    }
    
    public int getInterval() {
        return 60;
    }
    
    
    /**
     * Task init.
     */
    public void init() throws RollerException {
        log.debug("initing");
    }
    
    
    /**
     * Execute the task.
     */
    public void runTask() {
        
        try {
            log.info("task started");
            
            Roller roller = RollerFactory.getRoller();
            roller.getRefererManager().clearReferrers();
            roller.flush();
            
            log.info("task completed");
            
        } catch (RollerException e) {
            log.error("Error while checking for referer turnover", e);
        } catch (Exception ee) {
            log.error("unexpected exception", ee);
        } finally {
            // always release
            RollerFactory.getRoller().release();
        }
        
        
    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {
            TurnoverReferersTask task = new TurnoverReferersTask();
            task.init();
            task.run();
            System.exit(0);
        } catch (RollerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}