/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
/*
 * Generated file - Do not edit!
 */
package org.apache.roller.pojos;

import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Tag bean.
 * @author Elias Torres
 *
 * @ejb:bean name="WeblogEntryTagData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="roller_weblogentrytag"
 * @hibernate.cache usage="read-write"
 */
public class WeblogEntryTagData
    implements java.io.Serializable
{
    private static final long serialVersionUID = -2602052289337573384L;
    private java.lang.String id = UUIDGenerator.generateUUID();
    private WebsiteData website = null;
    private WeblogEntryData weblogEntry = null;
    private UserData user = null;
    private java.lang.String name = null;    
    private Timestamp time = null;

    public WeblogEntryTagData()
    {
    }

    public WeblogEntryTagData(java.lang.String id, 
                       WebsiteData website, 
                       WeblogEntryData weblogEntry, 
                       UserData user, java.lang.String name,
                       Timestamp time)
    {
        //this.id = id;
        this.website = website;
        this.weblogEntry = weblogEntry;
        this.user = user;
        this.name = name;
        this.time = time;
    }

    public WeblogEntryTagData(WeblogEntryTagData otherData)
    {
        setData(otherData);
    }

    //------------------------------------------------------- Simple properties

    /** 
     * Unique ID and primary key of this Referer.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.id column="id" generator-class="assigned"  
     */
    public java.lang.String getId()
    {
        return this.id;
    }

    public void setId(java.lang.String id)
    {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }

    /** 
     * ID of website that this tag refers to.
     *
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public org.apache.roller.pojos.WebsiteData getWeblog()
    {
        return this.website;
    }

    public void setWeblog(org.apache.roller.pojos.WebsiteData website)
    {
        this.website = website;
    }

    /**
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="entryid" cascade="none"
     */
    public org.apache.roller.pojos.WeblogEntryData getWeblogEntry()
    {
        return weblogEntry;
    }

    /**
     * @param data
     */
    public void setWeblogEntry(org.apache.roller.pojos.WeblogEntryData data)
    {
        weblogEntry = data;
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="userid" cascade="none"
     */
    public UserData getUser() {
        return this.user;
    }
    /** @ejb:persistent-field */
    public void setUser( UserData user ) {
        this.user = user;
    }    
    
    /**
     * Tag value
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return this.name;
    }
    /** @ejb:persistent-field */
    public void setName( String name ) {
        this.name = name;
    }   
    
    /**
    *
    * @roller.wrapPojoMethod type="simple"
    * @ejb:persistent-field
    * @hibernate.property column="time" non-null="true" unique="false"
    */
   public java.sql.Timestamp getTime()
   {
       return this.time;
   }

   /** @ejb:persistent-field */
   public void setTime(java.sql.Timestamp tagTime)
   {
       this.time = tagTime;
   }    

    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.time);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogEntryTagData != true) return false;
        WeblogEntryTagData o = (WeblogEntryTagData)other;
        return new EqualsBuilder()
            .append(getName(), o.getName()) 
            .append(getWeblogEntry(), o.getWeblogEntry()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getName())
            .append(getWeblogEntry())
            .toHashCode();
    }
 
    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogEntryTagData otherData)
    {
        WeblogEntryTagData data = (WeblogEntryTagData) otherData;
        this.id = data.getId();
        this.website = data.getWeblog();
        this.weblogEntry = data.getWeblogEntry();
        this.user = data.getUser();
        this.name = data.getName();
        this.time = data.getTime();
    }

}