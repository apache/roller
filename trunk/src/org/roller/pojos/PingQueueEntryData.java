/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.pojos;

import java.sql.Timestamp;
import java.io.Serializable;

/**
 * Ping queue entry.  Each instance of this class represents an entry on the ping queue. The entry indicates when it was
 * added to the queue, which configuration to apply for the ping, and the number of ping attempts that have been made
 * for this entry so far.
 *
 * @author Anil Gangolli anil@busybuddha.org
 * @ejb:bean name="PingQueueEntryData"
 * @hibernate.class lazy="false" table="pingqueueentry"
 */
public class PingQueueEntryData extends PersistentObject implements Serializable
{
    private String id = null;
    private Timestamp entryTime = null;
    private PingTargetData pingTarget = null;
    private WebsiteData website = null;
    private int attempts = 0;

    public static final long serialVersionUID = -1468021030819538243L;

    /**
     * Default constructor.  Leaves all fields at Java-specified default values.
     */
    public PingQueueEntryData()
    {
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
    public PingQueueEntryData(String id, Timestamp entryTime, PingTargetData pingTarget, WebsiteData website, int attempts)
    {
        this.id = id;
        this.entryTime = entryTime;
        this.pingTarget = pingTarget;
        this.website = website;
        this.attempts = attempts;
    }

    /**
     * @see PersistentObject#setData(PersistentObject)
     */
    public void setData(PersistentObject vo)
    {
        PingQueueEntryData other = (PingQueueEntryData) vo;
        
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
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId()
    {
        return id;
    }

    /**
     * Set the unique id (primary key) of this object.
     *
     * @param id
     * @ejb:persistent-field
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Get the entry time.  Get the time this entry was first added to the queue.
     *
     * @return the time the entry was first added to the queue.
     * @ejb:persistent-field
     * @hibernate.property column="entrytime" non-null="true"
     */
    public Timestamp getEntryTime()
    {
        return entryTime;
    }

    /**
     * Set the entry time.
     *
     * @param entryTime the time the entry was first added to the queue.
     * @ejb:persistent-field
     */
    public void setEntryTime(Timestamp entryTime)
    {
        this.entryTime = entryTime;
    }

    /**
     * Get the ping target.  Get the target to ping.
     *
     * @return the ping target to ping.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="pingtargetid" cascade="none" not-null="true"
     */
    public PingTargetData getPingTarget()
    {
        return pingTarget;
    }

    /**
     * Set the ping target.
     *
     * @param pingTarget target to ping.
     * @ejb:persistent-field
     */
    public void setPingTarget(PingTargetData pingTarget)
    {
        this.pingTarget = pingTarget;
    }

    /**
     * Get the website originating the ping.
     *
     * @return the website originating the ping.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite()
    {
        return website;
    }

    /**
     * Set the website originating the ping.
     *
     * @param website the website originating the ping.
     * @ejb:persistent-field
     */
    public void setWebsite(WebsiteData website)
    {
        this.website = website;
    }

    /**
     * Get the number of ping attempts that have been made for this queue entry.
     *
     * @return the number of ping attempts that have been made for this queue entry.
     * @ejb:persistent-field
     * @hibernate.property column="attempts" non-null="true"
     */
    public int getAttempts()
    {
        return attempts;
    }

    /**
     * Set the number of failures that have occurred for this queue entry.
     *
     * @param attempts
     * @ejb:persistent-field
     */
    public void setAttempts(int attempts)
    {
        this.attempts = attempts;
    }

    /**
     * Increment the number of failures for this queue entry.
     *
     * @return the new value.
     */
    public int incrementAttempts()
    {
        return ++attempts;
    }

    /**
     * @see Object#equals(Object o)
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PingQueueEntryData)) return false;

        final PingQueueEntryData pingQueueEntryData = (PingQueueEntryData) o;

        if (attempts != pingQueueEntryData.getAttempts()) return false;
        if (entryTime != null ? !entryTime.equals(pingQueueEntryData.getEntryTime()) : pingQueueEntryData.getEntryTime() != null) return false;
        if (id != null ? !id.equals(pingQueueEntryData.getId()) : pingQueueEntryData.getId() != null) return false;
        if (pingTarget != null ? !pingTarget.equals(pingQueueEntryData.getPingTarget()) : pingQueueEntryData.getPingTarget() != null) return false;
        if (website != null ? !website.equals(pingQueueEntryData.getWebsite()) : pingQueueEntryData.getWebsite() != null) return false;

        return true;
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }

    /**
     * Generate a string form of the object appropriate for logging or debugging.
     * @return a string form of the object appropriate for logging or debugging.
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "PingQueueEntryData{" +
            "id='" + id + "'" +
            ", entryTime=" + entryTime +
            ", pingTarget=" + pingTarget +
            ", website= " + (website == null ? "null" : "{id='" + website.getId() + "'} ") +
            ", attempts=" + attempts +
            "}";
    }
}
