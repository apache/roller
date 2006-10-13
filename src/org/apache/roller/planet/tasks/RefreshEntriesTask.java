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

package org.apache.roller.planet.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.runnable.RollerTask;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;


/**
 * Run the Planet Roller refresh-entries method to fetch and parse newsfeeds.
 */
public class RefreshEntriesTask extends RollerTask {
    
    private static Log log = LogFactory.getLog(RefreshEntriesTask.class);
    
    
    public String getName() {
        return "RefreshEntriesTask";
    }
    
    public int getLeaseTime() {
        return 10;
    }
    
    public int getInterval() {
        return 60;
    }
    
    public void init() throws RollerException {
        // no-op
    }
    
    
    public void runTask() {
        try {
            Roller roller = RollerFactory.getRoller();
            roller.getPlanetManager().refreshEntries();
            roller.flush();
        } catch (RollerException e) {
            log.error("ERROR refreshing entries", e);
        } finally {
            RollerFactory.getRoller().release();
        }
    }
    
    
    /** 
     * Task may be run from the command line 
     */
    public static void main(String[] args) {
        try {
            RefreshEntriesTask task = new RefreshEntriesTask();
            task.init();
            task.run();
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }
    
}
