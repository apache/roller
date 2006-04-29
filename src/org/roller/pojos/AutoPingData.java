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

import java.io.Serializable;

/**
 * Automatic ping configuration.  An instance of this class relates a website and ping target; it indicates that the specified
 * ping target should be pinged when the corresponding website is changed.  Pinging can be restricted to changes to
 * specific categories on the website by instances of the {@link PingCategoryRestrictionData} object.  In the absence of
 * any category restrictions, the ping target is pinged whenever the corresponding website changes.
 *
 * @author Anil Gangolli anil@busybuddha.org
 * @ejb:bean name="AutoPingData"
 * @hibernate.class lazy="false" table="autoping"
 * @hibernate.cache usage="read-write"
 */
public class AutoPingData extends PersistentObject implements Serializable
{
    private String id = null;
    private PingTargetData pingTarget = null;
    private WebsiteData website = null;

    public static final long serialVersionUID = -9105985454111986435L;
    
    /**
     * Default constructor.  Leaves all fields null.  Required for bean compliance.
     */
    public AutoPingData()
    {
    }

    /**
     * Constructor.
     *
     * @param id         unique id (primary key) for this instance
     * @param pingtarget ping target that should be pinged
     * @param website    website to which this configuration applies
     */
    public AutoPingData(String id, PingTargetData pingtarget, WebsiteData website)
    {
        this.id = id;
        this.website = website;
        this.pingTarget = pingtarget;
    }

    /**
     * Setter needed by RollerImpl.storePersistentObject()
     */
    public void setData(PersistentObject vo)
    {
        AutoPingData other = (AutoPingData)vo;
        
        id = other.getId();
        website = other.getWebsite();
        pingTarget = other.getPingTarget();
    }

    /**
     * Get the unique id (primary key) of this object.
     *
     * @return the unique id of this object. -- struts.validator type="required" msgkey="errors.required"
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId()
    {
        return id;
    }

    /**
     * Set the unique id (primary key) of this object
     *
     * @param id
     * @ejb:persistent-field
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * Get the website.  Get the website whose changes should result in a ping to the ping target specified by this
     * object.
     *
     * @return the website.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="false"
     */
    public WebsiteData getWebsite()
    {
        return website;
    }

    /**
     * Set the website.  Set the website whose changes should result in a ping to the ping target specified by this
     * object.
     *
     * @param website the website.
     * @ejb:persistent-field
     */
    public void setWebsite(WebsiteData website)
    {
        this.website = website;
    }

    /**
     * Get the ping target.  Get the target to be pinged when the corresponding website changes.
     *
     * @return the target to be pinged.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="pingtargetid" cascade="none" not-null="false"
     */
    public PingTargetData getPingTarget()
    {
        return pingTarget;
    }

    /**
     * Set the ping target.  Set the target to be pinged when the corresponding website changes.
     *
     * @param pingtarget the target to be pinged.
     * @ejb:persistent-field
     */
    public void setPingTarget(PingTargetData pingtarget)
    {
        this.pingTarget = pingtarget;
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof AutoPingData)) return false;

        final AutoPingData autoPingData = (AutoPingData)o;

        if (id != null ? !id.equals(autoPingData.getId()) : autoPingData.getId() != null) return false;
        if (pingTarget != null ? !pingTarget.equals(autoPingData.getPingTarget()) : autoPingData.getPingTarget() != null) return false;
        if (website != null ? !website.equals(autoPingData.getWebsite()) : autoPingData.getWebsite() != null) return false;

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
        return "AutoPingData{" +
            "id='" + id + "'" +
            ", pingTarget=" + pingTarget +
            ", website= " + (website == null ? "null" : "{id='" + website.getId() +"'} ") +
            "}";
    }
}
