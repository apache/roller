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
package org.apache.roller.ui.core.tasks;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ScheduledTask;
import org.apache.roller.pojos.UserData;

/**
 * Run the Planet Roller refresh-entries method to fetch and parse newsfeeds.
 * @author Dave Johnson
 */
public class RefreshEntriesTask extends TimerTask implements ScheduledTask {
    private static Log logger =
            LogFactory.getFactory().getInstance(RefreshEntriesTask.class);
    private Roller roller = null;
    
    /** Task may be run from the command line */
    public static void main(String[] args) {
        try {
            RollerFactory.setRoller(
                    "org.apache.roller.business.hibernate.HibernateRollerImpl");
            RefreshEntriesTask task = new RefreshEntriesTask();
            task.init(RollerFactory.getRoller(), "dummy");
            task.run();
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }
    }
    public void init(Roller roller, String realPath) throws RollerException {
        this.roller = (Roller)roller;
    }
    public void run() {
        try {
            roller.getPlanetManager().refreshEntries();
            roller.flush();
            roller.release();
        } catch (RollerException e) {
            logger.error("ERROR refreshing entries", e);
        }
    }
}

