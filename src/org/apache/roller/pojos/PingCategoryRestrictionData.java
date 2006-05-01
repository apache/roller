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

/**
 * Ping Category Restriction.  An instance of this class relates an auto ping configuration {@link AutoPingData} to a
 * specific weblog category {@link WeblogCategoryData}.  When one or more instances of this class are present for a
 * given auto ping configuration, it means that pings should only go out for changes to the categories specified by those
 * instances.  If no instances of this class are present for a given auto ping configuration, it means that the ping
 * configuration is not restricted by category, so pings should go out for changes in any category.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @ejb:bean name="AutoPingData"
 * @hibernate.class lazy="false" table="pingcategory"
 * @hibernate.cache usage="read-write"
 */
public class PingCategoryRestrictionData extends PersistentObject implements Serializable
{
    private String id;
    private AutoPingData autoPing;
    private WeblogCategoryData weblogCategory;

    static final long serialVersionUID = 2261280579491859418L;

    /**
     * Default constructor.  Leaves all fields null.  Required for bean compliance.
     */
    public PingCategoryRestrictionData()
    {
    }

    /**
     * Constructor
     *
     * @param id             unique id of this object
     * @param autoPing       auto ping configuration being restricted
     * @param weblogCategory weblog category to which this auto ping configuration is restricted
     */
    public PingCategoryRestrictionData(String id, AutoPingData autoPing, WeblogCategoryData weblogCategory)
    {
        this.id = id;
        this.autoPing = autoPing;
        this.weblogCategory = weblogCategory;
    }

    /**
     * Setter needed by RollerImpl.storePersistentObject()
     */
    public void setData(PersistentObject vo)
    {
        PingCategoryRestrictionData other = (PingCategoryRestrictionData)vo;
        id = other.getId();
        autoPing = other.getAutoping();
        weblogCategory = other.getWeblogCategory();
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
     * Get the auto ping configuration to which this category restriction applies.
     *
     * @return the auto ping configuration to which this category restriction applies.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="autopingid" cascade="none" not-null="true"
     */
    public AutoPingData getAutoping()
    {
        return autoPing;
    }

    /**
     * Set the auto ping configuration to which this category restriction applies.
     *
     * @param autoPing the auto ping configuration to which this category restriction applies.
     * @ejb:persistent-field
     */
    public void setAutoping(AutoPingData autoPing)
    {
        this.autoPing = autoPing;
    }

    /**
     * Get the weblog category.  Get the weblog category to which pings should be restricted.
     *
     * @return the weblog category to which pings should be restricted.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="weblogcategoryid" cascade="none" not-null="true"
     */
    public WeblogCategoryData getWeblogCategory()
    {
        return weblogCategory;
    }

    /**
     * Set the ping target.  Set the target to be pinged when the corresponding website changes.
     *
     * @param weblogCategory the weblog category to which pings should be restricted.
     * @ejb:persistent-field
     */
    public void setWeblogCategory(WeblogCategoryData weblogCategory)
    {
        this.weblogCategory = weblogCategory;
    }

    /**
     * @see Object#equals(Object)
     */
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof PingCategoryRestrictionData)) return false;

        final PingCategoryRestrictionData pingCategoryRestrictionData = (PingCategoryRestrictionData)o;

        if (id != null ? !id.equals(pingCategoryRestrictionData.getId()) : pingCategoryRestrictionData.getId() != null) return false;
        if (autoPing != null ? !autoPing.equals(pingCategoryRestrictionData.getAutoping()) : pingCategoryRestrictionData.getAutoping() != null) return false;
        if (weblogCategory != null ? !weblogCategory.equals(pingCategoryRestrictionData.getWeblogCategory()) : pingCategoryRestrictionData.getWeblogCategory() != null) return false;

        return true;
    }

    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }
}