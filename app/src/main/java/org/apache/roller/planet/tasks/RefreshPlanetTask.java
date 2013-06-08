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
import org.apache.roller.planet.business.updater.FeedUpdater;
import org.apache.roller.planet.business.updater.SingleThreadedFeedUpdater;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;


/**
 * Updates Planet aggregator's database of feed entries.
 * <pre>
 * - Designed to be run outside of Roller via the TaskRunner class
 * - Calls Planet business layer to refresh entries
 * </pre>
 */
public class RefreshPlanetTask extends PlanetTask {
    
    private static Log log = LogFactory.getLog(RefreshPlanetTask.class);
    
    
    public void run() {
        try {            
            // Update all feeds in planet
            log.info("Refreshing Planet entries");
            FeedUpdater updater = new SingleThreadedFeedUpdater();
            updater.updateSubscriptions();
            WebloggerFactory.getWeblogger().release();
            
        } catch (Exception e) {
            log.error("ERROR refreshing planet", e);
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        
        RefreshPlanetTask task = new RefreshPlanetTask();
        task.initialize();
        
        // need to prepare and bootstrap Planet as well
        WebloggerStartup.prepare();
        WebloggerFactory.bootstrap();
        
        task.run();
    }
    
}
