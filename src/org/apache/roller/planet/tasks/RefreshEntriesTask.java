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

import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.runnable.RollerTaskWithLeasing;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.config.PlanetConfig;


/**
 * Run the Planet Roller refresh-entries method to fetch and parse newsfeeds.
 */
public class RefreshEntriesTask extends RollerTaskWithLeasing {
    
    private static Log log = LogFactory.getLog(RefreshEntriesTask.class);
    
    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = null;
    
    // a String description of when to start this task
    private String startTimeDesc = "startOfHour";
    
    // interval at which the task is run, default is 60 minutes
    private int interval = 60;
    
    // lease time given to task lock, default is 30 minutes
    private int leaseTime = 30;
    
    
    public String getName() {
        return "RefreshEntriesTask";
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public Date getStartTime(Date currentTime) {
        return getAdjustedTime(currentTime, startTimeDesc);
    }
    
    public int getInterval() {
        return this.interval;
    }
    
    public int getLeaseTime() {
        return this.leaseTime;
    }
    
    
    public void init() throws RollerException {
        
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
            PlanetFactory.getPlanet().getPlanetManager().refreshEntries(
                PlanetConfig.getProperty("planet.aggregator.cache.dir"));
            PlanetFactory.getPlanet().flush();
        } catch (RollerException e) {
            log.error("ERROR refreshing entries", e);
        } finally {
            PlanetFactory.getPlanet().release();
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
