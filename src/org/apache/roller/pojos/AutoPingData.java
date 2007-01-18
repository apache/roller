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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Automatic ping configuration.  An instance of this class relates a website and ping target; it indicates that the specified
 * ping target should be pinged when the corresponding website is changed.  Pinging can be restricted to changes to
 * specific categories on the website by instances of the {@link PingCategoryRestrictionData} object.  In the absence of
 * any category restrictions, the ping target is pinged whenever the corresponding website changes.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @ejb:bean name="AutoPingData"
 * @hibernate.class lazy="true" table="autoping"
 * @hibernate.cache usage="read-write"
 */
public class AutoPingData implements Serializable {
    private String id = null;
    private PingTargetData pingTarget = null;
    private WebsiteData website = null;

    public static final long serialVersionUID = -9105985454111986435L;

    /**
     * Default constructor.  Leaves all fields null.  Required for bean compliance.
     */
    public AutoPingData() {
    }

    /**
     * Constructor.
     *
     * @param id         unique id (primary key) for this instance
     * @param pingtarget ping target that should be pinged
     * @param website    website to which this configuration applies
     */
    public AutoPingData(String id, PingTargetData pingtarget, WebsiteData website) {
        this.id = id;
        this.website = website;
        this.pingTarget = pingtarget;
    }

    /**
     * Set bean properties based on other bean.
     */
    public void setData(AutoPingData other) {
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
    public String getId() {
        return id;
    }

    /**
     * Set the unique id (primary key) of this object
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
     * Get the website.  Get the website whose changes should result in a ping to the ping target specified by this
     * object.
     *
     * @return the website.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="false"
     */
    public WebsiteData getWebsite() {
        return website;
    }

    /**
     * Set the website.  Set the website whose changes should result in a ping to the ping target specified by this
     * object.
     *
     * @param website the website.
     * @ejb:persistent-field
     */
    public void setWebsite(WebsiteData website) {
        this.website = website;
    }

    /**
     * Get the ping target.  Get the target to be pinged when the corresponding website changes.
     *
     * @return the target to be pinged.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="pingtargetid" cascade="none" not-null="false"
     */
    public PingTargetData getPingTarget() {
        return pingTarget;
    }

    /**
     * Set the ping target.  Set the target to be pinged when the corresponding website changes.
     *
     * @param pingtarget the target to be pinged.
     * @ejb:persistent-field
     */
    public void setPingTarget(PingTargetData pingtarget) {
        this.pingTarget = pingtarget;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof AutoPingData != true) return false;
        AutoPingData o = (AutoPingData)other;
        return new EqualsBuilder()
            .append(getId(), o.getId())
            .append(getPingTarget(), o.getPingTarget()) 
            .append(getWebsite(), o.getWebsite()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder().append(getId()).toHashCode();
    }
}
