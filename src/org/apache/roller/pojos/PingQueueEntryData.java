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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Ping queue entry.  Each instance of this class represents an entry on the ping queue. The entry indicates when it was
 * added to the queue, which configuration to apply for the ping, and the number of ping attempts that have been made
 * for this entry so far.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @ejb:bean name="PingQueueEntryData"
 * @hibernate.class lazy="true" table="pingqueueentry"
 * @hibernate.cache usage="read-write"
 */
public class PingQueueEntryData implements Serializable {
    private String id = UUIDGenerator.generateUUID();
    private Timestamp entryTime = null;
    private PingTargetData pingTarget = null;
    private Weblog website = null;
    private int attempts = 0;

    public static final long serialVersionUID = -1468021030819538243L;

    /**
     * Default constructor.  Leaves all fields at Java-specified default values.
     */
    public PingQueueEntryData() {
    }

    /**
     * Construct with all members
     *
     * @param id         unique id of this entry
     * @param entryTime  timestamp of first entry onto queue
     * @param pingTarget target site to ping
     * @param website    website originating the ping
     * @param attempts   number of prior ping attempts
     */
    public PingQueueEntryData(String id, Timestamp entryTime, PingTargetData pingTarget, Weblog website, int attempts) {
        //this.id = id;
        this.entryTime = entryTime;
        this.pingTarget = pingTarget;
        this.website = website;
        this.attempts = attempts;
    }

    /**
     * Set bean properties based on other bean.
     */
    public void setData(PingQueueEntryData other) {
        id = other.getId();
        entryTime = other.getEntryTime();
        pingTarget = other.getPingTarget();
        website = other.getWebsite();
        attempts = other.getAttempts();
    }

    /**
     * Get the unique id (primary key) of this object.
     *
     * @return the unique id of this object.
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="assigned"  
     */
    public String getId() {
        return id;
    }

    /**
     * Set the unique id (primary key) of this object.
     *
     * @param id
     * @ejb:persistent-field
     */
    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }

    /**
     * Get the entry time.  Get the time this entry was first added to the queue.
     *
     * @return the time the entry was first added to the queue.
     * @ejb:persistent-field
     * @hibernate.property column="entrytime" non-null="true"
     */
    public Timestamp getEntryTime() {
        return entryTime;
    }

    /**
     * Set the entry time.
     *
     * @param entryTime the time the entry was first added to the queue.
     * @ejb:persistent-field
     */
    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Get the ping target.  Get the target to ping.
     *
     * @return the ping target to ping.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="pingtargetid" cascade="none" not-null="true"
     */
    public PingTargetData getPingTarget() {
        return pingTarget;
    }

    /**
     * Set the ping target.
     *
     * @param pingTarget target to ping.
     * @ejb:persistent-field
     */
    public void setPingTarget(PingTargetData pingTarget) {
        this.pingTarget = pingTarget;
    }

    /**
     * Get the website originating the ping.
     *
     * @return the website originating the ping.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public Weblog getWebsite() {
        return website;
    }

    /**
     * Set the website originating the ping.
     *
     * @param website the website originating the ping.
     * @ejb:persistent-field
     */
    public void setWebsite(Weblog website) {
        this.website = website;
    }

    /**
     * Get the number of ping attempts that have been made for this queue entry.
     *
     * @return the number of ping attempts that have been made for this queue entry.
     * @ejb:persistent-field
     * @hibernate.property column="attempts" non-null="true"
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * Set the number of failures that have occurred for this queue entry.
     *
     * @param attempts
     * @ejb:persistent-field
     */
    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * Increment the number of failures for this queue entry.
     *
     * @return the new value.
     */
    public int incrementAttempts() {
        return ++attempts;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.entryTime);
        buf.append(", ").append(this.attempts);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof PingQueueEntryData != true) return false;
        PingQueueEntryData o = (PingQueueEntryData)other;
        return new EqualsBuilder()
            .append(getEntryTime(), o.getEntryTime()) 
            .append(getWebsite(), o.getWebsite()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getEntryTime())
            .append(getWebsite())
            .toHashCode();
    }
}
