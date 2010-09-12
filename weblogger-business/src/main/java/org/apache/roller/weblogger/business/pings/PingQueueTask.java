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

package org.apache.roller.weblogger.business.pings;

import java.util.Date;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.runnable.RollerTaskWithLeasing;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.runnable.ScheduledEntriesTask;


/**
 * Task for processing the ping queue at fixed intervals.   This is set up during context initialization by {@link
 * org.apache.roller.weblogger.ui.core.RollerContext}.  The queue processing interval is currently set from the configuration {@link
 * org.apache.roller.weblogger.config.PingConfig} at startup time only.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public class PingQueueTask extends RollerTaskWithLeasing {
    private static Log log = LogFactory.getLog(PingQueueTask.class);

    public static String NAME = "PingQueueTask";

    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = null;
    
    // a String description of when to start this task
    private String startTimeDesc = "immediate";
    
    // interval at which the task is run, default is 5 minutes
    private int interval = 5;
    
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
        this.init(PingQueueTask.NAME);
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
        
        // initialize queue processor
        PingQueueProcessor.init();
    }
    

    /**
     * Run the task once.
     */
    public void runTask() {
        
        try {
            log.debug("task started");
            
            PingQueueProcessor.getInstance().processQueue();
            WebloggerFactory.getWeblogger().flush();
            
            log.debug("task completed");
            
        } catch (WebloggerException e) {
            log.error("Error while processing ping queue", e);
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
            PingQueueTask task = new PingQueueTask();
            task.init();
            task.run();
            System.exit(0);
        } catch (WebloggerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}
