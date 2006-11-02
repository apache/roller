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
 * @author Elias Torres
 *
 * @ejb:bean name="WeblogEntryTagAggregateData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="roller_weblogentrytagagg"
 * @hibernate.cache usage="read-write"
 */
public class WeblogEntryTagAggregateData extends PersistentObject
    implements java.io.Serializable
{
    private static final long serialVersionUID = -4343500268898106982L;
    private java.lang.String id = null;
    private java.lang.String name = null;    
    private WebsiteData website = null;
    private Timestamp lastUsed = null;
    private int total = 0;

    public WeblogEntryTagAggregateData()
    {
    }

    public WeblogEntryTagAggregateData(java.lang.String id,
                       WebsiteData website,
                       java.lang.String name, int total)
    {
        this.id = id;
        this.website = website;
        this.name = name;
        this.total = total;
    }

    public WeblogEntryTagAggregateData(WeblogEntryTagAggregateData otherData)
    {
        setData(otherData);
    }

    //------------------------------------------------------- Simple properties

    /** 
     * Unique ID and primary key of this Referer.
     *
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
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="false"
     */
    public WebsiteData getWeblog() {
        return this.website;
    }
    
    /** @ejb:persistent-field */
    public void setWeblog(WebsiteData website) {
        this.website = website;
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
    * @hibernate.property column="total" non-null="true" unique="false"
    */
   public int getTotal()
   {
       return this.total;
   }
   
   /**
   *
   * @roller.wrapPojoMethod type="simple"
   * @ejb:persistent-field
   * @hibernate.property column="lastused" non-null="true" unique="false"
   */   
   public Timestamp getLastUsed() {
       return this.lastUsed;
   }
   
   /** @ejb:persistent-field */
   public void setLastUsed(Timestamp lastUsed) {
       this.lastUsed = lastUsed;
   }   

   /** @ejb:persistent-field */
   public void setTotal(int total)
   {
       this.total = total;
   }    

   public String toString() {
     StringBuffer str = new StringBuffer("{");
     
     str.append("id=" + id + " " +
             "website=" + website +
             "name=" + name + " " +
             "total=" + total + " " +
             "lastUsed=" + lastUsed);
     str.append('}');
     
     return (str.toString());
 }
 
   public boolean equals(Object pOther) {
       if (pOther instanceof WeblogEntryTagAggregateData) {
           WeblogEntryTagAggregateData lTest = (WeblogEntryTagAggregateData) pOther;
           boolean lEquals = true;
           
           lEquals = PojoUtil.equals(lEquals, this.id, lTest.getId());
           lEquals = PojoUtil.equals(lEquals, this.website, lTest.getWeblog());
           lEquals = PojoUtil.equals(lEquals, this.name, lTest.getName());
           lEquals = this.total == lTest.getTotal();
           return lEquals;
       } else {
           return false;
       }
   }
 
   public int hashCode() {
       int result = 17;
       result = PojoUtil.addHashCode(result, this.id);
       result = PojoUtil.addHashCode(result, this.website);
       result = PojoUtil.addHashCode(result, this.name);
       result = PojoUtil.addHashCode(result, new Integer(this.total));
       
       return result;
   }
 
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData)
    {
        WeblogEntryTagAggregateData data = (WeblogEntryTagAggregateData) otherData;
        this.id = data.getId();
        this.website = data.getWeblog();
        this.name = data.getName();
        this.total = data.getTotal();
        this.lastUsed = data.getLastUsed();
    }

}