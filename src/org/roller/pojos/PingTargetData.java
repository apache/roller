/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roller.pojos;

import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.AutoPingManager;
import org.roller.model.PingQueueManager;

import java.io.Serializable;
import java.util.List;
import java.sql.Timestamp;

/**
 * Ping target.   Each instance represents a possible target of a weblog update ping that we send.  Ping targets are
 * either common (defined centrally by an administrator and used by any website), or custom (defined by the user of a
 * specific website) for update pings issued for that website.
 *
 * @author Anil Gangolli anil@busybuddha.org
 * @ejb:bean name="PingTargetData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="pingtarget"
 * @hibernate.cache usage="read-write"
 */
public class PingTargetData extends PersistentObject implements Serializable
{
    private String id = null;
    private String name = null;
    private String pingUrl = null;
    private WebsiteData website = null;
    private int conditionCode = -1;
    private Timestamp lastSuccess = null;

    public static final int CONDITION_OK = 0;           // last use (after possible retrials) was successful
    public static final int CONDITION_FAILING = 1;      // last use failed after retrials
    public static final int CONDITION_DISABLED = 2;     // disabled by failure policy after failures - editing resets

    public static final long serialVersionUID = -6354583200913127874L;

    /**
     * Default empty constructor.
     */
    public PingTargetData()
    {
    }

    /**
     * Constructor.
     *
     * @param id      the id (primary key) of this target
     * @param name    the descriptive name of this target
     * @param pingUrl the URL to which to send the ping
     * @param website the website (on this server) for which this is a custom ping target (may be null)
     */
    public PingTargetData(String id, String name, String pingUrl, WebsiteData website)
    {
        this.id = id;
        this.name = name;
        this.pingUrl = pingUrl;
        this.website = website;
        this.conditionCode = CONDITION_OK;
        this.lastSuccess = null;
    }

    /**
     * Setter needed by RollerImpl.storePersistentObject()
     */
    public void setData(PersistentObject vo)
    {
        PingTargetData other = (PingTargetData) vo;
        
        id = other.getId();
        name = other.getName();
        pingUrl = other.getPingUrl();
        website = other.getWebsite();
        conditionCode = other.getConditionCode();
        lastSuccess = other.getLastSuccess();
    }


    /**
     * Get the unique id of this ping target.
     *
     * @return the unique id of this ping target.
     * @struts.validator type="required" msgkey="errors.required"
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId()
    {
        return this.id;
    }

    /**
     * Set the unique id of this ping target
     *
     * @param id
     * @ejb:persistent-field
     */
    public void setId(java.lang.String id)
    {
        this.id = id;
    }

    /**
     * get the name of this ping target.  This is a name assigned by the administrator or a user (for custom) targets.
     * It is deescriptive and is not necessarily unique.
     *
     * @return the name of this ping target
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true"
     */
    public java.lang.String getName()
    {
        return this.name;
    }

    /**
     * Set the name of this ping target.
     *
     * @param name the name of this ping target
     * @ejb:persistent-field
     */
    public void setName(java.lang.String name)
    {
        this.name = name;
    }

    /**
     * Get the URL to ping.
     *
     * @return the URL to ping.
     * @ejb:persistent-field
     * @hibernate.property column="pingurl" non-null="true"
     */
    public String getPingUrl()
    {
        return pingUrl;
    }

    /**
     * Set the URL to ping.
     *
     * @param pingUrl
     * @ejb:persistent-field
     */
    public void setPingUrl(String pingUrl)
    {
        this.pingUrl = pingUrl;
    }

    /**
     * Get the website (on this server) for which this ping target is a custom target.  This may be null, indicating
     * that it is a common ping target, not a custom one.
     *
     * @return the website for which this ping target is a custom target, or null if this ping target is not a custom
     *         target.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="false"
     */
    public WebsiteData getWebsite()
    {
        return website;
    }

    /**
     * Set the website (on this server) for which this ping target is a custom target.
     *
     * @param website the website for which this ping target is a custom target, or null if this ping target is not a
     *                custom target
     * @ejb:persistent-field
     */
    public void setWebsite(WebsiteData website)
    {
        this.website = website;
    }

    /**
     * Get the condition code value.  This code, in combination with the last success timestamp, provides a status
     * indicator on the ping target based on its  usage by the ping queue processor. It can be used to implement a
     * failure-based disabling policy.
     *
     * @return one of the condition codes {@link #CONDITION_OK}, {@link #CONDITION_FAILING}, {@link
     *         #CONDITION_DISABLED}.
     * @ejb:persistent-field
     * @hibernate.property column="conditioncode" not-null="true"
     */
    public int getConditionCode()
    {
        return conditionCode;
    }

    /**
     * Set the condition code value.
     *
     * @param conditionCode the condition code value to set
     * @ejb:persistent-field
     */
    public void setConditionCode(int conditionCode)
    {
        this.conditionCode = conditionCode;
    }

    /**
     * Get the timestamp of the last successful ping (UTC/GMT).
     *
     * @return the timestamp of the last successful ping; <code>null</code> if the target has not yet been used.
     * @ejb:persistent-field
     * @hibernate.property column="lastsuccess" not-null="false"
     */
    public Timestamp getLastSuccess()
    {
        return lastSuccess;
    }

    /**
     * Set the timestamp of the last successful ping.
     *
     * @param lastSuccess the timestamp of the last successful ping.
     * @ejb:persistent-field
     */
    public void setLastSuccess(Timestamp lastSuccess)
    {
        this.lastSuccess = lastSuccess;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * @see java.lang.Object#equals(Object o)
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PingTargetData)) return false;

        final PingTargetData pingTargetData = (PingTargetData) o;

        if (conditionCode != pingTargetData.getConditionCode()) return false;
        if (id != null ? !id.equals(pingTargetData.getId()) : pingTargetData.getId() != null) return false;
        if (lastSuccess != null ? !lastSuccess.equals(pingTargetData.getLastSuccess()) : pingTargetData.getLastSuccess() != null) return false;
        if (name != null ? !name.equals(pingTargetData.getName()) : pingTargetData.getName() != null) return false;
        if (pingUrl != null ? !pingUrl.equals(pingTargetData.getPingUrl()) : pingTargetData.getPingUrl() != null) return false;
        if (website != null ? !website.equals(pingTargetData.getWebsite()) : pingTargetData.getWebsite() != null) return false;

        return true;
    }


    /**
     * Generate a string form of the object appropriate for logging or debugging.
     *
     * @return a string form of the object appropriate for logging or debugging.
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "PingTargetData{" +
            "id='" + id + "'" +
            ", name='" + name + "'" +
            ", pingUrl='" + pingUrl + "'" +
            ", website= " + (website == null ? "null" : "{id='" + website.getId() + "'} ") +
            ", conditionCode=" + conditionCode +
            ", lastSuccess=" + lastSuccess +
            "}";
    }
}
