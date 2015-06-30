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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * Ping queue entry.  Each instance of this class represents an entry on the ping queue. The entry indicates when it was
 * added to the queue, which configuration to apply for the ping, and the number of ping attempts that have been made
 * for this entry so far.
 */
@Entity
@Table(name="pingqueueentry")
@NamedQueries({
    @NamedQuery(name="PingQueueEntry.getAllOrderByEntryTime",
            query="SELECT p FROM PingQueueEntry p ORDER BY p.entryTime"),

    @NamedQuery(name="PingQueueEntry.getByPingTarget&Weblog",
            query="SELECT p FROM PingQueueEntry p WHERE p.pingTarget = ?1 AND p.weblog = ?2"),

    @NamedQuery(name="PingQueueEntry.getByWeblog",
            query="SELECT p FROM PingQueueEntry p WHERE p.weblog = ?1"),

    @NamedQuery(name="PingQueueEntry.removeByPingTarget",
            query="DELETE FROM PingQueueEntry p WHERE p.pingTarget = ?1")
})
public class PingQueueEntry implements Serializable {

    public static final long serialVersionUID = -1468021030819538243L;

    // Unique ID of object
    private String id = UUIDGenerator.generateUUID();

    // Timestamp of first entry onto queue
    private Timestamp entryTime = null;

    // Target site to ping
    private PingTarget pingTarget = null;

    // weblog causing the ping
    private Weblog weblog = null;

    // number of prior ping attempts
    private int attempts = 0;

    public PingQueueEntry() {
    }

    public PingQueueEntry(Timestamp entryTime, PingTarget pingTarget, Weblog weblog, int attempts) {
        this.entryTime = entryTime;
        this.pingTarget = pingTarget;
        this.weblog = weblog;
        this.attempts = attempts;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional=false)
    public Timestamp getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Timestamp entryTime) {
        this.entryTime = entryTime;
    }

    @ManyToOne
    @JoinColumn(name="pingtargetid",nullable=false)
    public PingTarget getPingTarget() {
        return pingTarget;
    }

    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Basic(optional=false)
    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    /**
     * Increment the number of failures for this queue entry.
     *
     * @return the new value.
     */
    public int incrementAttempts() {
        int newAttempts = getAttempts() + 1;
        setAttempts(newAttempts);
        return newAttempts;
    }

    public String toString() {
        return "{" + getId() + ", " + getEntryTime() + ", " + getAttempts() + "}";
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PingQueueEntry)) {
            return false;
        }
        PingQueueEntry o = (PingQueueEntry)other;
        return new EqualsBuilder()
            .append(getEntryTime(), o.getEntryTime()) 
            .append(getWeblog(), o.getWeblog())
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getEntryTime())
            .append(getWeblog())
            .toHashCode();
    }
    
}
