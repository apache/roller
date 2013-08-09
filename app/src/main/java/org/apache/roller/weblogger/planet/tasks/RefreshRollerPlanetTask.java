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

package org.apache.roller.weblogger.planet.tasks;

import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;
import org.apache.roller.planet.business.updater.FeedUpdater;
import org.apache.roller.planet.business.updater.SingleThreadedFeedUpdater;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.GuiceWebloggerProvider;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WebloggerProvider;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Updates Planet aggregator's database of feed entries.
 * <pre>
 * - Designed to be run outside of Roller via the TaskRunner class
 * - Calls Planet business layer to refresh entries
 * </pre>
 */
public class RefreshRollerPlanetTask extends RollerTaskWithLeasing {    
    private static Log log = LogFactory.getLog(RefreshRollerPlanetTask.class);
    
    public static String NAME = "RefreshRollerPlanetTask";
    
    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = "unspecifiedClientId";
    
    // a String description of when to start this task
    private String startTimeDesc = "immediate";
    
    // interval at which the task is run, default is 60 minutes
    private int interval = 60;
    
    // lease time given to task, default is 10 minutes
    private int leaseTime = 10;

    
    public String getClientId() {
        return clientId;
    }
    
    public Date getStartTime(Date currentTime) {
        return getAdjustedTime(currentTime, startTimeDesc);
    }
    
    public String getStartTimeDesc() {
        return startTimeDesc;
    }
    
    public int getInterval() {
        return this.interval;
    }
    
    public int getLeaseTime() {
        return this.leaseTime;
    }
    
    public void init() throws WebloggerException {
        this.init(RefreshRollerPlanetTask.NAME);
    }

    @Override
    public void init(String name) throws WebloggerException {
        super.init(name);

        // get relevant props
        Properties props = this.getTaskProperties();
        
        // extract clientId
        String client = props.getProperty("clientId");
        if(client != null) {
            this.clientId = client;
        }
        
        // extract start time
        String startTimeStr = props.getProperty("startTime");
        if(startTimeStr != null) {
            this.startTimeDesc = startTimeStr;
        }
        
        // extract interval
        String intervalStr = props.getProperty("interval");
        if(intervalStr != null) {
            try {
                this.interval = Integer.parseInt(intervalStr);
            } catch (NumberFormatException ex) {
                log.warn("Invalid interval: "+intervalStr);
            }
        }
        
        // extract lease time
        String leaseTimeStr = props.getProperty("leaseTime");
        if(leaseTimeStr != null) {
            try {
                this.leaseTime = Integer.parseInt(leaseTimeStr);
            } catch (NumberFormatException ex) {
                log.warn("Invalid leaseTime: "+leaseTimeStr);
            }
        }
    }
    
    
    public void runTask() {
        try {
            log.info("Refreshing Planet subscriptions");
            
            FeedUpdater updater = new SingleThreadedFeedUpdater();
            updater.updateSubscriptions();
            
        } catch (Exception e) {
            log.error("ERROR refreshing planet", e);
        } finally {
            // always release
            WebloggerFactory.getWeblogger().release();
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        
        // before we can do anything we need to bootstrap the planet backend
        WebloggerStartup.prepare();
        
        // we need to use our own planet provider for integration
        String guiceModule = WebloggerConfig.getProperty("planet.aggregator.guice.module");
        WebloggerProvider provider = new GuiceWebloggerProvider(guiceModule);
        WebloggerFactory.bootstrap(provider);
                        
        RefreshRollerPlanetTask task = new RefreshRollerPlanetTask();
        task.init();
        task.run();
    }
    
}
