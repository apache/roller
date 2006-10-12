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
import java.util.Date;


/**
 * Represents locking information about a specific RollerTask.
 *
 * @ejb:bean name="CommentData"
 * @hibernate.class lazy="false" table="roller_tasklock"
 * @hibernate.cache usage="read-write"
 */
public class TaskLockData extends PersistentObject implements Serializable {
    
    private String id = null;
    private String name = null;
    private boolean locked = false;
    private Date timeAquired = null;
    private int timeLeased = 0;
    private Date lastRun = null;
    
    
    public TaskLockData() {}
    
    
    public void setData(PersistentObject otherData) {
        TaskLockData other = (TaskLockData) otherData;
        this.id = other.getId();
        this.name = other.getName();
        this.locked = other.isLocked();
        this.timeAquired = other.getTimeAquired();
        this.timeLeased = other.getTimeLeased();
        this.lastRun = other.getLastRun();
    }
    
    public boolean equals(Object other) {
        
        if(this == other) return true;
        if( !(other instanceof TaskLockData) ) return false;
        
        // our natural key, or business key, is our name
        final TaskLockData that = (TaskLockData) other;
        return this.name.equals(that.getName());
    }
    
    public int hashCode() {
        // our natrual key, or business key, is our name
        return this.name.hashCode();
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        return locked;
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
    
}
