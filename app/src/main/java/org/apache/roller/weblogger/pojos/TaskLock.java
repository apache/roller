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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import org.apache.roller.util.UUIDGenerator;


/**
 * Represents locking information about a specific RollerTask.
 */
public class TaskLock implements Serializable {
    
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private Date timeAquired = null;
    private int timeLeased = 0;
    private Date lastRun = null;
    private String clientId = null;
    
    
    public TaskLock() {}
    
    
    /**
     * Calculate the next allowed time this task is allowed to run allowed to run.  
     * i.e. lastRun + interval
     */
    public Date getNextAllowedRun(int interval) {
        
        Date previousRun = getLastRun();
        if(previousRun == null) {
            return new Date(0);
        }
        
        // calculate next run time
        Calendar cal = Calendar.getInstance();
        cal.setTime(previousRun);
        cal.add(Calendar.MINUTE, interval);
        
        return cal.getTime();
    }
    
    
    /**
     * Get the time the last/current lease for this lock expires.
     * 
     * expireTime = timeAcquired + (timeLeased * 60sec/min) - 1 sec
     * we remove 1 second to adjust for precision differences
     */
    public Date getLeaseExpiration() {
        
        Date leaseAcquisitionTime = new Date(0);
        if(getTimeAquired() != null) {
            leaseAcquisitionTime = getTimeAquired();
        }
        
        // calculate lease expiration time
        Calendar cal = Calendar.getInstance();
        cal.setTime(leaseAcquisitionTime);
        cal.add(Calendar.MINUTE, timeLeased);
        
        return cal.getTime();
    }

    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.timeAquired);
        buf.append(", ").append(this.timeLeased);
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        
        if(this == other) return true;
        if( !(other instanceof TaskLock) ) return false;
        
        // our natural key, or business key, is our name
        final TaskLock that = (TaskLock) other;
        return this.getName().equals(that.getName());
    }
    
    @Override
    public int hashCode() {
        // our natural key, or business key, is our name
        return this.getName().hashCode();
    }
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public Date getTimeAquired() {
        return timeAquired;
    }

    public void setTimeAquired(Date timeAquired) {
        this.timeAquired = timeAquired;
    }

    
    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }
    
    
    public int getTimeLeased() {
        return timeLeased;
    }

    public void setTimeLeased(int timeLeased) {
        this.timeLeased = timeLeased;
    }

    
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
}
