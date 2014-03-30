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
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * This task is used to promote SCHEDULED weblog entries to the PUBLISHED
 * status when their publication time has been reached.
 */
public class ScheduledEntriesTask extends RollerTaskWithLeasing {
    private static Log log = LogFactory.getLog(ScheduledEntriesTask.class);
    
    public static String NAME = "ScheduledEntriesTask";

    
    // a unique id for this specific task instance
    // this is meant to be unique for each client in a clustered environment
    private String clientId = null;
    
    // a String description of when to start this task
    private String startTimeDesc = "immediate";
    
    // interval at which the task is run, default is once per minute
    private int interval = 1;
    
    // lease time given to task lock, default is 30 minutes
    private int leaseTime = RollerTaskWithLeasing.DEFAULT_LEASE_MINS;
    

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
        this.init(ScheduledEntriesTask.NAME);
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
    
    
    /**
     * Execute the task.
     */
    public void runTask() {
        
        log.debug("task started");
        
        try {
            WeblogEntryManager wMgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            IndexManager searchMgr = WebloggerFactory.getWeblogger().getIndexManager();
            
            Date now = new Date();
            
            log.debug("looking up scheduled entries older than "+now);
            
            // get all published entries older than current time
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setEndDate(now);
            wesc.setStatus(WeblogEntry.SCHEDULED);
            List<WeblogEntry> scheduledEntries = wMgr.getWeblogEntries(wesc);
            log.debug("promoting "+scheduledEntries.size()+" entries to PUBLISHED state");
            
            for (WeblogEntry entry : scheduledEntries) {
                entry.setStatus(WeblogEntry.PUBLISHED);
                wMgr.saveWeblogEntry(entry);
            }

            // commit the changes
            WebloggerFactory.getWeblogger().flush();
            
            // take a second pass to trigger reindexing and cache invalidations
            // this is because we need the updated entries flushed first
            for (WeblogEntry entry : scheduledEntries) {
                // trigger a cache invalidation
                CacheManager.invalidate(entry);
                // trigger search index on entry
                searchMgr.addEntryReIndexOperation(entry);
            }

        } catch (WebloggerException e) {
            log.error("Error getting scheduled entries", e);
        } catch(Exception e) {
            log.error("Unexpected exception running task", e);
        } finally {
            // always release
            WebloggerFactory.getWeblogger().release();
        }
        
        log.debug("task completed");
        
    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {
            ScheduledEntriesTask task = new ScheduledEntriesTask();
            task.init();
            task.run();
            System.exit(0);
        } catch (WebloggerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}
