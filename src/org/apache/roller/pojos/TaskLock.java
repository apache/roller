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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import org.apache.roller.util.UUIDGenerator;

/**
 * Represents locking information about a specific RollerTask.
 * 
 * @ejb:bean name="TaskLock"
 * @hibernate.cache usage="read-write"
 * @hibernate.class lazy="true" table="roller_tasklock"
 */
public class TaskLock implements Serializable {
    
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private boolean locked = false;
    private Date timeAquired = null;
    private int timeLeased = 0;
    private Date lastRun = null;
    private String clientId = null;
    
    
    public TaskLock() {}
    
    
    /**
     * Calculate the next allowed time the task managed by this lock would
     * be allowed to run.  i.e. lastRun + interval
     */
    public Date getNextRun(int interval) {
        
        Date lastRun = this.getLastRun();
        if(lastRun == null) {
            return null;
        }
        
        // calculate next run time
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastRun);
        cal.add(Calendar.MINUTE, interval);
        
        return cal.getTime();
    }
    
    
    /**
     * Get the time the lease for this lock will expire, or null if this task
     * lock is not currently locked.
     */
    public Date getLeaseExpires() {
        
        if(!locked || timeAquired == null) {
            return null;
        }
        
        // calculate lease expiration time
        Calendar cal = Calendar.getInstance();
        cal.setTime(timeAquired);
        cal.add(Calendar.MINUTE, timeLeased);
        
        return cal.getTime();
    }
    
    
    public void setData(TaskLock other) {
        this.id = other.getId();
        this.name = other.getName();
        this.locked = other.isLocked();
        this.timeAquired = other.getTimeAquired();
        this.timeLeased = other.getTimeLeased();
        this.lastRun = other.getLastRun();
    }

    //------------------------------------------------------- Good citizenship

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

    public boolean equals(Object other) {
        
        if(this == other) return true;
        if( !(other instanceof TaskLock) ) return false;
        
        // our natural key, or business key, is our name
        final TaskLock that = (TaskLock) other;
        return this.getName().equals(that.getName());
    }
    
    public int hashCode() {
        // our natrual key, or business key, is our name
        return this.getName().hashCode();
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="assigned"  
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="true"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="timeacquired" non-null="false" unique="false"
     */
    public Date getTimeAquired() {
        return timeAquired;
    }

    public void setTimeAquired(Date timeAquired) {
        this.timeAquired = timeAquired;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="lastrun" non-null="false" unique="false"
     */
    public Date getLastRun() {
        return lastRun;
    }

    public void setLastRun(Date lastRun) {
        this.lastRun = lastRun;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="islocked" non-null="false" unique="false"
     */
    public boolean isLocked() {
        
        // this method requires a little extra logic because we return false
        // even if a task is locked when it's lease has expired
        if(!locked) {
            return false;
        }
        
        Date now = new Date();
        Date leaseExpiration = this.getLeaseExpires();
        
        return now.before(leaseExpiration);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="timeleased" non-null="false" unique="false"
     */
    public int getTimeLeased() {
        return timeLeased;
    }

    public void setTimeLeased(int timeLeased) {
        this.timeLeased = timeLeased;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="client" non-null="false" unique="false"
     */
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
}
