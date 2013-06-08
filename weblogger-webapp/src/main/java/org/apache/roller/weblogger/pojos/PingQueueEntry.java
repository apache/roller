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

package org.apache.roller.weblogger.pojos;

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
 */
public class PingQueueEntry implements Serializable {
    
    private String id = UUIDGenerator.generateUUID();
    private Timestamp entryTime = null;
    private PingTarget pingTarget = null;
    private Weblog website = null;
    private int attempts = 0;

    public static final long serialVersionUID = -1468021030819538243L;

    
    /**
     * Default constructor.  Leaves all fields at Java-specified default values.
     */
    public PingQueueEntry() {
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
    public PingQueueEntry(String id, Timestamp entryTime, PingTarget pingTarget, Weblog website, int attempts) {
        //this.id = id;
        this.entryTime = entryTime;
        this.pingTarget = pingTarget;
        this.website = website;
        this.attempts = attempts;
    }

    /**
     * Get the unique id (primary key) of this object.
     *
     * @return the unique id of this object.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the unique id (primary key) of this object.
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the entry time.  Get the time this entry was first added to the queue.
     *
     * @return the time the entry was first added to the queue.
     */
    public Timestamp getEntryTime() {
        return entryTime;
    }

    /**
     * Set the entry time.
     *
     * @param entryTime the time the entry was first added to the queue.
     */
    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    /**
     * Get the ping target.  Get the target to ping.
     *
     * @return the ping target to ping.
     */
    public PingTarget getPingTarget() {
        return pingTarget;
    }

    /**
     * Set the ping target.
     *
     * @param pingTarget target to ping.
     */
    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }

    /**
     * Get the website originating the ping.
     *
     * @return the website originating the ping.
     */
    public Weblog getWebsite() {
        return website;
    }

    /**
     * Set the website originating the ping.
     *
     * @param website the website originating the ping.
     */
    public void setWebsite(Weblog website) {
        this.website = website;
    }

    /**
     * Get the number of ping attempts that have been made for this queue entry.
     *
     * @return the number of ping attempts that have been made for this queue entry.
     */
    public int getAttempts() {
        return attempts;
    }

    /**
     * Set the number of failures that have occurred for this queue entry.
     *
     * @param attempts
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
        if (other instanceof PingQueueEntry != true) return false;
        PingQueueEntry o = (PingQueueEntry)other;
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
