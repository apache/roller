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

import org.apache.roller.util.PojoUtil;


/**
 * Tag bean.
 * @author Elias Torres
 *
 * @ejb:bean name="WeblogEntryTagData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="weblogentrytag"
 * @hibernate.cache usage="read-write"
 */
public class WeblogEntryTagData extends PersistentObject
    implements java.io.Serializable
{
    private static final long serialVersionUID = -2602052289337573384L;
    private java.lang.String id = null;
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
        this.id = id;
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
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId()
    {
        return this.id;
    }

    public void setId(java.lang.String id)
    {
        this.id = id;
    }

    /** 
     * ID of website that this tag refers to.
     *
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public org.apache.roller.pojos.WebsiteData getWebsite()
    {
        return this.website;
    }

    public void setWebsite(org.apache.roller.pojos.WebsiteData website)
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

   public String toString() {
     StringBuffer str = new StringBuffer("{");
     
     str.append("id=" + id + " " +
             "website=" + website + " " +
             "weblogEntry=" + weblogEntry + " " +
             "user=" + user + " " +
             "name=" + name + " " +
             "tagTime=" + time);
     str.append('}');
     
     return (str.toString());
 }
 
   public boolean equals(Object pOther) {
       if (pOther instanceof WeblogEntryTagData) {
           WeblogEntryTagData lTest = (WeblogEntryTagData) pOther;
           boolean lEquals = true;
           
           lEquals = PojoUtil.equals(lEquals, this.id, lTest.getId());
           lEquals = PojoUtil.equals(lEquals, this.website, lTest.getWebsite());
           lEquals = PojoUtil.equals(lEquals, this.weblogEntry, lTest.getWeblogEntry());
           lEquals = PojoUtil.equals(lEquals, this.user, lTest.getUser());
           lEquals = PojoUtil.equals(lEquals, this.name, lTest.getName());
           lEquals = PojoUtil.equals(lEquals, this.time, lTest.getTime());
           return lEquals;
       } else {
           return false;
       }
   }
 
   public int hashCode() {
       
       int result = 17;
       result = PojoUtil.addHashCode(result, this.id);
       result = PojoUtil.addHashCode(result, this.website != null ? this.website.getId() : null);
       result = PojoUtil.addHashCode(result, this.weblogEntry != null ? this.weblogEntry.getId() : null);
       result = PojoUtil.addHashCode(result, this.user != null ? this.user.getId() : null);
       result = PojoUtil.addHashCode(result, this.name);
       result = PojoUtil.addHashCode(result, this.time);

       return result;
   }
 
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData)
    {
        WeblogEntryTagData data = (WeblogEntryTagData) otherData;
        this.id = data.getId();
        this.website = data.getWebsite();
        this.weblogEntry = data.getWeblogEntry();
        this.user = data.getUser();
        this.name = data.getName();
        this.time = data.getTime();
    }

}