/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.pojos;

import java.io.Serializable;

/**
 * Ping Category Restriction.  An instance of this class relates an auto ping configuration {@link AutoPingData} to a
 * specific weblog category {@link WeblogCategoryData}.  When one or more instances of this class are present for a
 * given auto ping configuration, it means that pings should only go out for changes to the categories specified by those
 * instances.  If no instances of this class are present for a given auto ping configuration, it means that the ping
 * configuration is not restricted by category, so pings should go out for changes in any category.
 *
 * @author Anil Gangolli anil@busybuddha.org
 * @ejb:bean name="AutoPingData"
 * @hibernate.class lazy="false" table="pingcategory"
 */
public class PingCategoryRestrictionData extends PersistentObject implements Serializable
{
    protected String id;
    protected AutoPingData autoPing;
    protected WeblogCategoryData weblogCategory;

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
        id = other.id;
        autoPing = other.autoPing;
        weblogCategory = other.weblogCategory;
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

        if (id != null ? !id.equals(pingCategoryRestrictionData.id) : pingCategoryRestrictionData.id != null) return false;
        if (autoPing != null ? !autoPing.equals(pingCategoryRestrictionData.autoPing) : pingCategoryRestrictionData.autoPing != null) return false;
        if (weblogCategory != null ? !weblogCategory.equals(pingCategoryRestrictionData.weblogCategory) : pingCategoryRestrictionData.weblogCategory != null) return false;

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