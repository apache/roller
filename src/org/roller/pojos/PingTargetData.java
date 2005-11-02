/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed as part of the Roller Weblogger under the terms of the Roller Software License.
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
 */
public class PingTargetData extends PersistentObject implements Serializable
{
    protected String id;
    protected String name;
    protected String pingUrl;
    protected WebsiteData website;
    protected int conditionCode;
    protected Timestamp lastSuccess;

    public static final int CONDITION_OK = 0;           // last use (after possible retrials) was successful
    public static final int CONDITION_FAILING = 1;      // last use failed after retrials
    public static final int CONDITION_DISABLED = 2;     // disabled by failure policy after failures - editing resets

    static final long serialVersionUID = -6354583200913127874L;

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
        id = other.id;
        name = other.name;
        pingUrl = other.pingUrl;
        website = other.website;
        conditionCode = other.conditionCode;
        lastSuccess = other.lastSuccess;
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

        if (conditionCode != pingTargetData.conditionCode) return false;
        if (id != null ? !id.equals(pingTargetData.id) : pingTargetData.id != null) return false;
        if (lastSuccess != null ? !lastSuccess.equals(pingTargetData.lastSuccess) : pingTargetData.lastSuccess != null) return false;
        if (name != null ? !name.equals(pingTargetData.name) : pingTargetData.name != null) return false;
        if (pingUrl != null ? !pingUrl.equals(pingTargetData.pingUrl) : pingTargetData.pingUrl != null) return false;
        if (website != null ? !website.equals(pingTargetData.website) : pingTargetData.website != null) return false;

        return true;
    }

    /**
     * Determine if the current user has rights to save the current instance.
     *
     * @return true if the user has rights to save the current instance, false otherwise.
     * @throws RollerException
     */
    public boolean canSave() throws RollerException
    {
        Roller roller = RollerFactory.getRoller();
        UserData user = roller.getUser();
        // This is more verbose but easier to debug than returning the value of the boolean expression
        if (user.equals(UserData.SYSTEM_USER))
        {
            return true;
        }
        if (website == null && user.hasRole("admin"))
        {
            return true;
        }
        if (website != null && website.hasUserPermissions(
                user, (short)(PermissionsData.ADMIN | PermissionsData.AUTHOR))) 
        {
            return true;
        }
        return false;
    }

    /**
     * Remove the object.
     *
     * @throws RollerException
     * @see org.roller.pojos.PersistentObject#remove()
     */
    public void remove() throws RollerException
    {
        // First remove ping queue entries and auto ping configurations that use this target.
        PingQueueManager pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
        pingQueueMgr.removeQueueEntriesByPingTarget(this);
        AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
        List autopings = autoPingMgr.getAutoPingsByTarget(this);
        autoPingMgr.removeAutoPings(autopings);
        super.remove();
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
