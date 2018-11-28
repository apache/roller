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

package org.apache.roller.weblogger.business.runnable;

import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Reset referer counts.
 */
public class TurnoverReferersTask extends RollerTaskWithLeasing {
    private static Log log = LogFactory.getLog(TurnoverReferersTask.class);

    public static String NAME = "TurnoverReferersTask";

    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = null;
    
    // a String description of when to start this task
    private String startTimeDesc = "startOfDay";
    
    // interval at which the task is run, default is 1 day
    private int interval = 1440;
    
    // lease time given to task lock, default is 30 minutes
    private int leaseTime = 30;

    
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
        this.init(TurnoverReferersTask.NAME);
    }

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
    
    
    /**
     * Execute the task.
     */
    public void runTask() {
        
        try {
            log.info("task started");
            
            Weblogger roller = WebloggerFactory.getWeblogger();
            roller.getRefererManager().clearReferrers();
            roller.flush();
            
            log.info("task completed");
            
        } catch (WebloggerException e) {
            log.error("Error while checking for referer turnover", e);
        } catch (Exception ee) {
            log.error("unexpected exception", ee);
        } finally {
            // always release
            WebloggerFactory.getWeblogger().release();
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
        } catch (WebloggerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}
