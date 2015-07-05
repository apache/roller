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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import org.apache.roller.util.UUIDGenerator;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Represents locking information about a specific RollerTask.
 */
@Entity
@Table(name="roller_tasklock")
@NamedQueries({
        @NamedQuery(name="TaskLock.getByName",
                query="SELECT t FROM TaskLock t WHERE t.name = ?1"),
        @NamedQuery(name="TaskLock.updateClient&Timeacquired&Timeleased&LastRunByName&Timeacquired",
                query="UPDATE TaskLock t SET t.clientId=?1, t.timeAcquired=CURRENT_TIMESTAMP, t.timeLeased= ?2, t.lastRun= ?3 WHERE t.name=?4 AND t.timeAcquired=?5 AND ?6 < CURRENT_TIMESTAMP"),
        @NamedQuery(name="TaskLock.updateTimeLeasedByName&Client",
                query="UPDATE TaskLock t SET t.timeLeased=?1 WHERE t.name=?2 AND t.clientId=?3")
})
public class TaskLock implements Serializable {
    
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private Date timeAcquired = null;
    private int timeLeased = 0;
    private Date lastRun = null;
    private String clientId = null;

    public TaskLock() {}

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimeAcquired() {
        return timeAcquired;
    }

    public void setTimeAcquired(Date timeAcquired) {
        this.timeAcquired = timeAcquired;
    }

    @Temporal(TemporalType.TIMESTAMP)
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

    @Column(name="client")
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }


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
    @Transient
    public Date getLeaseExpiration() {

        Date leaseAcquisitionTime = new Date(0);
        if(getTimeAcquired() != null) {
            leaseAcquisitionTime = getTimeAcquired();
        }

        // calculate lease expiration time
        Calendar cal = Calendar.getInstance();
        cal.setTime(leaseAcquisitionTime);
        cal.add(Calendar.MINUTE, getTimeLeased());

        return cal.getTime();
    }

    //------------------------------------------------------- Good citizenship

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getName());
        buf.append(", ").append(getTimeAcquired());
        buf.append(", ").append(getTimeLeased());
        buf.append("}");
        return buf.toString();
    }

    @Override
    public boolean equals(Object other) {
        
        if(this == other) {
            return true;
        }
        if( !(other instanceof TaskLock) ) {
            return false;
        }
        // our natural key, or business key, is our name
        final TaskLock that = (TaskLock) other;
        return this.getName().equals(that.getName());
    }
    
    @Override
    public int hashCode() {
        // our natural key, or business key, is our name
        return this.getName().hashCode();
    }
}
